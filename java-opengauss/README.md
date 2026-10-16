# Slioa 智慧园区协同监控平台 · Java + openGauss

> V2.0 · 软件工程课程项目原型

将旧版 Python/SQLite 原型重构为 **Java 21 + Spring Boot + openGauss + 原生 HTML/CSS/JavaScript**。前端采用苹果产品官网式的视觉取向：充足留白、浅灰层次、轻边框、圆角面板、细腻的蓝色点缀与克制的信息密度，不依赖 React/Vue 或远程字体。

## 功能范围
- 园区仪表盘：设备总量、在线率、待处理告警、操作记录和温度趋势
- 设备中心：设备编号、类别、安装位置、状态与最近读数，支持搜索和状态筛选
- 告警中心：告警等级、关联设备、描述与处理状态，可确认事件
- 数据分析与系统状态页面
- REST API：仪表盘、设备列表、遥测趋势、告警列表、演示控制指令与告警确认
- openGauss 持久化：设备、遥测、告警、控制命令与操作日志
- 首次启动自动建表并写入演示数据

## 技术架构
| 层 | 技术 |
|---|---|
| 前端 | HTML5、CSS3、原生 JavaScript |
| 后端 | Java 21、Spring Boot 3.4、Spring Web、Spring JDBC |
| 数据库 | openGauss 5.x，使用 PostgreSQL JDBC 驱动 |
| 部署 | Docker Compose、Maven 多阶段构建 |

## 启动（Windows PowerShell）
先确保 Docker Desktop 可运行且能访问 Docker Hub。

```powershell
cd E:\Slioa-Java-OpenGauss
Copy-Item .env.example .env
docker compose up --build -d
docker compose ps
docker compose logs -f app
```

浏览器打开 `http://localhost:8080`。健康检查：`http://localhost:8080/api/health`。

## 本地通过 IDEA 运行
前置条件：JDK 21、Maven 3.9+、可访问的 openGauss。设置环境变量：

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/postgres"
$env:SPRING_DATASOURCE_USERNAME="gaussdb"
$env:SPRING_DATASOURCE_PASSWORD="你的数据库密码"
mvn spring-boot:run
```

## API
- `GET /api/health`
- `GET /api/dashboard`
- `GET /api/devices?limit=100`
- `POST /api/devices/{id}/commands`
- `GET /api/alarms?limit=100`
- `POST /api/alarms/{id}/acknowledge`
- `GET /api/telemetry/series`

## 安全与验证边界
演示数据不是实测数据。控制接口只记录 `SIMULATED` 指令，不会驱动真实门禁或执行器。当前是本地课程演示原型，不包含生产级登录和完整 RBAC，请勿直接暴露到公网。需要在具备 JDK/Maven/Docker 与 openGauss 的环境中完成完整编译和联调验收。

## 目录
```text
java-opengauss/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── .env.example
└── src/main/
    ├── java/com/slioa/campus/
    └── resources/{application.yml,schema.sql,static/}
```

