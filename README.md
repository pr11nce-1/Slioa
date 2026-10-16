# Slioa 智慧园区设备协同监控与管理平台

课程项目源码仓库。当前主开发版本为 **V2：Java + openGauss**；V1 Python/SQLite 原型归档保留。

## V2（推荐）
- **后端：** Java 21 + Spring Boot 3.4 + Spring JDBC
- **数据库：** openGauss，使用 PostgreSQL JDBC 驱动
- **前端：** 原生 HTML / CSS / JavaScript
- **启动：** 进入 `java-opengauss/`，复制 `.env.example` 为 `.env`，运行 `docker compose up --build -d`
- **Web：** http://localhost:8080
- **健康检查：** http://localhost:8080/api/health

V2 目录说明与启动指南：[`java-opengauss/README.md`](./java-opengauss/README.md)

### 核心能力
园区概览、设备目录与筛选、环境温度趋势、告警确认、遥测记录查询、操作日志、演示控制命令记录，以及 openGauss 持久化数据表和首次启动演示数据。

### 重要说明
演示数据仅用于课堂展示。设备控制 API 只记录 `SIMULATED` 指令，不会控制真实门禁或执行器。该版本不是生产系统，不包含生产级登录、完整权限体系或公网安全加固。完整 Java 编译和 openGauss 联调需在安装 JDK 21、Maven 或 Docker 的环境中验收。

## V1（历史原型）
第一代 Python / FastAPI / SQLite 原型保留在 [`Slioa_SmartCampus_IoT_V1.zip`](./Slioa_SmartCampus_IoT_V1.zip)，不再作为当前主技术栈。

## 许可与使用
此仓库用于课程设计、教学演示与继续开发。公开部署前，请替换示例数据库密码并完成身份认证、授权、密钥与网络安全配置。
