package com.slioa.campus.api;

import com.slioa.campus.service.CampusService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class CampusController {
    private final CampusService service;
    public CampusController(CampusService service) { this.service = service; }

    @GetMapping("/health")
    public Map<String, Object> health() { return Map.of("status", "ok", "database", "openGauss-compatible PostgreSQL", "service", "slioa-smart-campus"); }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() { return service.dashboard(); }

    @GetMapping("/devices")
    public List<Map<String, Object>> devices(@RequestParam(defaultValue = "100") int limit) {
        return service.devices(Math.max(1, Math.min(limit, 300)));
    }

    @PostMapping("/devices/{id}/commands")
    public Map<String, Object> command(@PathVariable long id, @RequestBody CommandRequest request) {
        try { return service.createCommand(id, request.command(), request.actor()); }
        catch (IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage()); }
        catch (org.springframework.dao.EmptyResultDataAccessException ex) { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "设备不存在"); }
    }

    @GetMapping("/alarms")
    public List<Map<String, Object>> alarms(@RequestParam(defaultValue = "100") int limit) {
        return service.alarms(Math.max(1, Math.min(limit, 300)));
    }

    @PostMapping("/alarms/{id}/acknowledge")
    public Map<String, Object> acknowledge(@PathVariable long id, @RequestBody(required = false) ActorRequest request) {
        try { return service.acknowledgeAlarm(id, request == null ? "admin" : request.actor()); }
        catch (IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage()); }
    }

    @GetMapping("/telemetry/series")
    public List<Map<String, Object>> series() { return service.telemetrySeries(); }

    public record CommandRequest(String command, String actor) {}
    public record ActorRequest(String actor) {}
}