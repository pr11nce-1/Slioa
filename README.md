# 基于鸿蒙的智慧园区设备协同监控与管理平台（Slioa）

> 第一代可运行原型｜软件工程课程项目

本项目面向智慧园区环境传感器、门禁、水电表等设备的统一接入、实时监控、阈值告警、远程控制、设备台账和数据分析。系统以 MQTT 作为可选设备消息通道，以统一后端为业务中心，并提供 Web 管理端与 OpenHarmony / HarmonyOS 移动端起步工程。

## 获取第一代完整源码

**[下载完整源码与文档包（ZIP）](./Slioa_SmartCampus_IoT_V1.zip)**

源码包内含后端、Web 管理控制台、MQTT 桥接、ArkTS 移动端起步工程、Story 清单、API 与架构说明、测试计划、Docker Compose 配置及 Windows 推送脚本。

## 功能范围

- 设备台账、设备状态与模拟遥测数据
- HTTP 数据接入、数据校验与历史数据查询
- 温湿度阈值告警、告警确认与恢复
- 远程控制命令、命令编号、模拟执行与 MQTT 回执流程
- 管理员 / 运维人员角色、接口授权与操作审计
- 维护记录、CSV 导出和基础数据统计
- Web 管理控制台与鸿蒙 ArkTS 接入起步工程

## 技术栈

- 后端：Python 3.11、FastAPI、SQLAlchemy
- 默认开发数据库：SQLite
- 前端：原生 HTML、CSS、JavaScript
- 设备消息：MQTT（通过可选桥接模块接入）
- 部署：Docker Compose
- 鸿蒙移动端：ArkTS 起步工程（需在目标 DevEco Studio / SDK 环境中完成编译验证）

## 快速启动

### Windows / macOS / Linux：本地运行

```bash
cd smart-campus-iot
python -m venv .venv
```

Windows PowerShell：

```powershell
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app.main:app --reload
```

macOS / Linux：

```bash
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload
```

浏览器打开 `http://127.0.0.1:8000`，API 文档位于 `http://127.0.0.1:8000/docs`。

### Docker Compose

```bash
docker compose up --build
```

启动参数、演示账号、环境变量和重置方式请以源码包中的 README 为准。

## 文档索引

- `docs/STORIES.md`：完整用户故事、优先级及验收条件（完整版本包含于源码 ZIP）
- `docs/API.md`：API 与 MQTT 约定
- `docs/ARCHITECTURE.md`：系统架构与数据流
- `docs/TEST_PLAN.md`：核心功能和异常场景的测试计划
- `CHANGELOG.md`：版本变更记录
- `openharmony-app/README.md`：鸿蒙移动端起步工程说明

## 当前版本边界

这是第一代课程项目原型。默认演示流程可以使用模拟设备；模拟控制成功不等于真实硬件已经执行。真实硬件接入需要按开发板、传感器和执行器规格完成驱动与协议适配。鸿蒙工程仍需在目标开发环境中编译验证。性能数据、硬件兼容性和生产安全性需以实际部署测试为准。

## 测试

源码包包含自动化测试和测试计划。可在项目目录中运行：

```bash
pytest -q
```

## 许可与使用

本仓库用于课程项目开发与教学演示。请在公开部署前检查默认账号配置、环境变量、密钥管理、设备控制安全策略和依赖版本。