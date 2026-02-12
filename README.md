# Shipping DocFlow - 提单导出与智能管理系统

本项目是一款基于 **RuoYi-Vue** 架构深度定制的智能化航运单据处理平台。系统通过集成 **Dify AI** 工作流，实现了从原始单据（PDF/图片）到结构化数据的手动校验、模版填充及最终单据导出的全流程自动化。

---

## 1. 系统框架与架构

本项目采用业界成熟的 **前后端分离** 架构设计：

*   **后端 (Backend)**: 基于 **SpringBoot 2.5.x** + **Shiro** 构建的微服务化单体架构。通过模块化设计（`ruoyi-admin`, `ruoyi-system`, `ruoyi-common` 等）确保高内聚低耦合。
*   **前端 (Frontend)**: 采用现代化的 **Vue 3** + **Vite 7** 方案。不同于传统的 RuoYi 前端，本项目独立开发了用户端界面 (`ruoyi-client`)，提供极致的响应速度与交互体验。
*   **AI 层 (AI Integration)**: 作为系统的“大脑”，通过调用 **Dify API** 完成非结构化文档的数据提取与智能语义理解。

---

## 2. 技术栈清单

### 后端 (Java Stack)
-   **核心框架**: Spring Boot 2.5.15, Spring Framework 5.3.39
-   **安全框架**: Apache Shiro 1.13.0
-   **持久层**: MyBatis 3.5.x, Druid 1.2.27 (数据库连接池)
-   **数据库**: MySQL 5.7+ (引擎: InnoDB, 字符集: UTF8MB4)
-   **缓存**: Redis 6.0+ (用于 Session 共享与热点数据缓存)
-   **工具类**: Fastjson 1.2.83, Apache POI 4.1.2 (单据处理)

### 前端 (Frontend Stack)
-   **核心框架**: Vue 3.0.4
-   **构建工具**: Vite 7.3.1
-   **路由与状态**: Vue Router 4.6.4, Vue-i18n 9.14.5 (多语言支持)
-   **UI 组件**: 基于 FontAwesome 7.1.0 的定制化深色模式界面，配合 SweetAlert2 实现精致反馈。
-   **预览技术**: docx-preview (实现 Office 文档的实时前端渲染)。

### AI & 基础设施
-   **AI 后台**: Dify API (工作流调度)
-   **容器化**: 支持 Docker 部署
-   **API 文档**: Swagger 3.0.0

---

## 3. 核心功能模块

1.  **PDF/图片智能解析**: 利用 Dify AI 自动提取提单 (B/L) 中的关键字段（如 Shipper, Consignee, Voyage No 等）。
2.  **实时单据预览**: 支持在数据修改过程中实时生成并预览 PDF/Docx 效果，实现“所见即所得”。
3.  **多模版管理系统**: 允许用户上传 Docx 模版并配置占位符映射，适配全球不同航运公司的单据格式。
4.  **智能看板 (Dashboard)**: 集中展示上传状态、解析进度以及历史记录，支持批量处理与一键导出。
5.  **会员与配额系统**: 内置 VIP 等级控制，通过 Redis 原子计数器实现对不同精度解析任务的频次控制。

---

## 4. 部署指南

### 环境要求
-   **JDK**: 1.8 (生产环境建议 OpenJDK 8u251+)
-   **MySQL**: 5.7 或 8.0 (需导入 `sql/` 目录下的初始化脚本)
-   **Redis**: 3.2+ (必须启动，用于存储登录态与缓存)
-   **Node.js**: 18.x 或 20.x (Vite 7 对旧版本 Node 兼容性较差)
-   **Maven**: 3.6.0+

### 关键配置文件修改
1.  **数据库连接**: 修改 `ruoyi-admin/src/main/resources/application-druid.yml` 中的 `url`, `username`, `password`。
2.  **Redis 配置**: 修改 `ruoyi-admin/src/main/resources/application.yml` 中的 `spring.redis` 部分。
3.  **文件外挂路径**: 修改 `application.yml` 中的 `ruoyi.profile`，该路径用于存储上传的原始单据与生成的模版文件。

### 本地运行与生产打包
-   **后端启动**:
    ```bash
    mvn clean install
    # 运行 ruoyi-admin 模块下的 RuoYiApplication.java
    ```
-   **前端启动**:
    ```bash
    cd ruoyi-client
    npm install
    npm run dev
    ```
-   **生产打包**:
    ```bash
    # 后端打包
    mvn clean package -Dmaven.test.skip=true
    # 前端打包
    npm run build
    ```

---

## 5. 端口映射与访问地址

| 组件 | 默认端口 | 访问地址 |
| :--- | :--- | :--- |
| **前端开发环境** | `5173` | `http://localhost:5173` |
| **后端 API 服务** | `81` | `http://localhost:81` |
| **Swagger 文档** | `81` | `http://localhost:81/swagger-ui/index.html` |
| **Druid 监控** | `81` | `http://localhost:81/druid` (账号: ruoyi) |

---
> [!IMPORTANT]
> 部署前请务必确认 Dify 侧的工作流 API Key 已正确配置在后端环境变量或配置项中。
