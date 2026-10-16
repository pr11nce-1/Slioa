package com.slioa.campus.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampusService {
    private final JdbcTemplate jdbc;

    public CampusService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional
    public void initializeDemoData() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM campus_device", Integer.class);
        if (count != null && count > 0) return;

        Object[][] devices = {
            {"ENV-B1-001", "一号楼环境传感器", "环境监测", "一号楼 · 1F 大厅", "ONLINE", "温度、湿度与空气质量"},
            {"ENV-B2-003", "二号楼环境传感器", "环境监测", "二号楼 · 3F 实验室", "ONLINE", "温湿度与空气质量"},
            {"ACC-GATE-01", "东门门禁控制器", "门禁设备", "园区东门", "ONLINE", "门禁状态与远程开门"},
            {"ACC-GATE-02", "西门门禁控制器", "门禁设备", "园区西门", "OFFLINE", "门禁状态与远程开门"},
            {"MTR-W-008", "生活用水总表", "水表", "能源站 · 给水区", "ONLINE", "用水量与瞬时流量"},
            {"MTR-E-002", "一号楼智能电表", "电表", "一号楼 · 配电间", "ONLINE", "能耗与电压监测"},
            {"ENV-B3-006", "三号楼环境传感器", "环境监测", "三号楼 · 2F 走廊", "WARNING", "温湿度监测"},
            {"ACC-LAB-01", "实验室门禁", "门禁设备", "创新实验室", "ONLINE", "门禁状态监控"}
        };
        for (Object[] d : devices) {
            jdbc.update("INSERT INTO campus_device(device_code,name,category,location,status,description,last_seen) VALUES(?,?,?,?,?,?,?)",
                d[0], d[1], d[2], d[3], d[4], d[5], Timestamp.valueOf(LocalDateTime.now().minusMinutes((long)(Math.random()*18))));
        }

        List<Map<String, Object>> saved = jdbc.queryForList("SELECT id, category, device_code FROM campus_device ORDER BY id");
        for (Map<String, Object> d : saved) {
            long id = ((Number)d.get("id")).longValue();
            String category = String.valueOf(d.get("category"));
            if ("环境监测".equals(category)) {
                insertTelemetry(id, "temperature", 22 + Math.random()*7, "°C");
                insertTelemetry(id, "humidity", 38 + Math.random()*28, "%");
                insertTelemetry(id, "pm25", 8 + Math.random()*34, "μg/m³");
            } else if ("水表".equals(category)) {
                insertTelemetry(id, "flow", 1.2 + Math.random()*2.5, "m³/h");
                insertTelemetry(id, "usage_today", 18 + Math.random()*24, "m³");
            } else if ("电表".equals(category)) {
                insertTelemetry(id, "power", 24 + Math.random()*25, "kW");
                insertTelemetry(id, "usage_today", 320 + Math.random()*140, "kWh");
            } else {
                insertTelemetry(id, "access_events", 5 + Math.random()*28, "次/小时");
            }
        }
        Long warningId = jdbc.queryForObject("SELECT id FROM campus_device WHERE device_code='ENV-B3-006'", Long.class);
        Long offlineId = jdbc.queryForObject("SELECT id FROM campus_device WHERE device_code='ACC-GATE-02'", Long.class);
        jdbc.update("INSERT INTO campus_alarm(device_id,level,title,detail,status,created_at) VALUES(?,?,?,?,?,?)",
            warningId, "WARNING", "三号楼温度接近阈值", "当前温度 29.1°C，已接近设定上限 30°C。", "OPEN", Timestamp.valueOf(LocalDateTime.now().minusMinutes(8)));
        jdbc.update("INSERT INTO campus_alarm(device_id,level,title,detail,status,created_at) VALUES(?,?,?,?,?,?)",
            offlineId, "CRITICAL", "西门门禁失去连接", "设备超过 5 分钟未上报心跳，请检查供电与网络。", "OPEN", Timestamp.valueOf(LocalDateTime.now().minusMinutes(21)));
        jdbc.update("INSERT INTO campus_alarm(device_id,level,title,detail,status,created_at) VALUES(?,?,?,?,?,?)",
            warningId, "INFO", "环境传感器已完成自检", "最近一次设备自检通过，运行状态正常。", "ACKNOWLEDGED", Timestamp.valueOf(LocalDateTime.now().minusHours(2)));
        jdbc.update("INSERT INTO operation_log(actor,action,detail) VALUES(?,?,?)", "system", "系统初始化", "已写入演示设备与遥测样例数据。");
    }

    private void insertTelemetry(long deviceId, String key, double value, String unit) {
        jdbc.update("INSERT INTO telemetry_record(device_id,metric_key,metric_value,unit,recorded_at) VALUES(?,?,?,?,?)",
            deviceId, key, Math.round(value * 10.0) / 10.0, unit, Timestamp.valueOf(LocalDateTime.now().minusMinutes((long)(Math.random()*50))));
    }

    public Map<String, Object> dashboard() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalDevices", scalar("SELECT COUNT(*) FROM campus_device"));
        result.put("onlineDevices", scalar("SELECT COUNT(*) FROM campus_device WHERE status='ONLINE'"));
        result.put("offlineDevices", scalar("SELECT COUNT(*) FROM campus_device WHERE status='OFFLINE'"));
        result.put("warningDevices", scalar("SELECT COUNT(*) FROM campus_device WHERE status='WARNING'"));
        result.put("openAlarms", scalar("SELECT COUNT(*) FROM campus_alarm WHERE status='OPEN'"));
        result.put("todayOperations", scalar("SELECT COUNT(*) FROM operation_log WHERE created_at >= CURRENT_DATE"));
        result.put("devices", devices(6));
        result.put("alarms", alarms(4));
        result.put("series", telemetrySeries());
        result.put("generatedAt", LocalDateTime.now().toString());
        return result;
    }

    public List<Map<String, Object>> devices(int limit) {
        return jdbc.queryForList("""
            SELECT d.id, d.device_code AS "deviceCode", d.name, d.category, d.location, d.status,
                   d.last_seen AS "lastSeen", COALESCE(t.metric_value, 0) AS "latestValue",
                   COALESCE(t.metric_key, '') AS "latestMetric", COALESCE(t.unit, '') AS unit
            FROM campus_device d
            LEFT JOIN LATERAL (
              SELECT metric_value, metric_key, unit FROM telemetry_record tr
              WHERE tr.device_id=d.id ORDER BY recorded_at DESC LIMIT 1
            ) t ON TRUE
            ORDER BY CASE d.status WHEN 'WARNING' THEN 0 WHEN 'OFFLINE' THEN 1 ELSE 2 END, d.id
            LIMIT ?
            """, limit);
    }

    public List<Map<String, Object>> alarms(int limit) {
        return jdbc.queryForList("""
            SELECT a.id, a.level, a.title, a.detail, a.status, a.created_at AS "createdAt",
                   COALESCE(d.name, '未关联设备') AS "deviceName", COALESCE(d.device_code, '') AS "deviceCode"
            FROM campus_alarm a LEFT JOIN campus_device d ON d.id=a.device_id
            ORDER BY CASE a.status WHEN 'OPEN' THEN 0 ELSE 1 END, a.created_at DESC LIMIT ?
            """, limit);
    }

    public List<Map<String, Object>> telemetrySeries() {
        return jdbc.queryForList("""
            SELECT TO_CHAR(date_trunc('hour', recorded_at), 'HH24:00') AS label,
                   ROUND(AVG(metric_value)::numeric, 1) AS value
            FROM telemetry_record
            WHERE metric_key='temperature' AND recorded_at >= CURRENT_TIMESTAMP - INTERVAL '24 hours'
            GROUP BY date_trunc('hour', recorded_at) ORDER BY date_trunc('hour', recorded_at)
            """);
    }

    public Map<String, Object> createCommand(long deviceId, String command, String actor) {
        Map<String, Object> device = jdbc.queryForMap("SELECT id, device_code, name FROM campus_device WHERE id=?", deviceId);
        String commandKey = command == null ? "STATUS_CHECK" : command.trim().toUpperCase();
        if (!List.of("STATUS_CHECK", "RESTART", "OPEN", "CLOSE", "CALIBRATE").contains(commandKey)) {
            throw new IllegalArgumentException("不支持的控制命令：" + commandKey);
        }
        jdbc.update("INSERT INTO control_command(device_id,command_key,command_payload,command_status) VALUES(?,?,?,?)",
            deviceId, commandKey, "{\"source\":\"web-console\",\"mode\":\"demo\"}", "SIMULATED");
        jdbc.update("INSERT INTO operation_log(actor,action,detail) VALUES(?,?,?)",
            actor == null || actor.isBlank() ? "admin" : actor, "发送设备命令", device.get("device_code") + " · " + commandKey + "（演示指令，未连接真实执行器）");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("deviceId", deviceId);
        result.put("deviceName", device.get("name"));
        result.put("command", commandKey);
        result.put("status", "SIMULATED");
        result.put("message", "演示指令已记录；当前版本不会直接驱动真实硬件。");
        return result;
    }

    public Map<String, Object> acknowledgeAlarm(long id, String actor) {
        int changed = jdbc.update("UPDATE campus_alarm SET status='ACKNOWLEDGED', acknowledged_at=CURRENT_TIMESTAMP WHERE id=? AND status='OPEN'", id);
        if (changed == 0) throw new IllegalArgumentException("告警不存在或已处理");
        jdbc.update("INSERT INTO operation_log(actor,action,detail) VALUES(?,?,?)", actor == null ? "admin" : actor, "确认告警", "告警编号 #" + id);
        return Map.of("id", id, "status", "ACKNOWLEDGED");
    }

    private long scalar(String sql) { Long v = jdbc.queryForObject(sql, Long.class); return v == null ? 0 : v; }
}