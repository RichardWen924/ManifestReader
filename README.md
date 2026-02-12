# 🚢 Shipping DocFlow - 智能航运单据处理系统

[![Java Version](https://img.shields.io/badge/Java-8-blue.svg?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-2.5.15-brightgreen.svg?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Vue Version](https://img.shields.io/badge/Vue-3.0-4FC08D.svg?style=flat-square&logo=vuedotjs)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](LICENSE)

**Shipping DocFlow** 是一款专为航运物流行业设计的智能化单据处理平台。基于 **RuoYi-Vue** 核心架构，深度集成 **Dify AI** 工作流，解决传统提单处理过程中“格式杂、录入慢、校对难”的痛点。

---

## ✨ 核心特性

-   🔍 **智能 OCR 解析**: 自动识别 PDF/图片提单，提取货主、收货人、船号、航次等 30+ 核心字段。
-   👁️ **实时同步预览**: 修改表单数据时，右侧实时生成 PDF/Docx 预览，所见即所得。
-   📝 **动态模版管理**: 支持多种贸易格式模版 (DOCX)，支持灵活配置占位符映射。
-   🌐 **全应用国际化**: 深度适配中英双语环境，满足全球化贸易协作需求。
-   🛡️ **会员与配额管理**: 内置颗粒度权限控制，支持按月/按次精确计量 AI 解析配额。
-   🧹 **自动化卫生维护**: 定时清理服务器冗余临时文件，保护商业隐私。

---

## 🏗️ 系统架构

系统遵循经典的前后端分离架构，并在此基础上进行了针对性的性能优化：

-   **后端**: SpringBoot 2.5.x + Apache Shiro，采用模块化设计，具备极佳的可扩展性。
-   **前端**: Vue 3.0 + Vite 7.x 组合，提供极致的开发热更新速度与轻量化生产包。
-   **AI 层**: 集成 Dify API 编排解析逻辑，支持高度自定义的语义化数据提取方案。

---

## 🛠️ 技术栈录

| 维度 | 核心技术 |
| :--- | :--- |
| **基础框架** | Java 8, Spring Boot 2.5.15, Spring Framework 5.3.39 |
| **安全/持久化** | Apache Shiro 1.13.0, MyBatis 3.5.x, Druid 1.2.27 |
| **前端技术** | Vue 3.0.4, Vue Router 4.x, Vite 7.3, i18n |
| **存储/性能** | MySQL 5.7+, Redis 6.0+, Fastjson |
| **文档处理** | docx-preview, Apache POI |

---

## 🚀 快速开始

### 1. 环境准备
-   **JDK**: 1.8+
-   **MySQL**: 5.7+ (需导入 `sql/` 脚本)
-   **Redis**: 必须安装并运行
-   **Node.js**: 18.x / 20.x

### 2. 后端配置与启动
修改 `ruoyi-admin/src/main/resources/application-druid.yml` 中的数据库配置，以及 `application.yml` 中的文件上传路径 `profile`。
```bash
mvn clean install
# 运行 RuoYiApplication.java
```

### 3. 前端配置与启动
```bash
cd ruoyi-client
npm install
npm run dev
```

---

## 📂 项目结构

```text
├── bin/                    # 可执行脚本
├── ruoyi-admin/            # 后端核心入口与 API 控制器
├── ruoyi-client/           # Vue 3 智能化客户端前端
├── ruoyi-common/           # 公共工具类与核心包
├── ruoyi-system/           # 系统业务逻辑层
├── sql/                    # 数据库初始化脚本
└── README.md               # 本文档
```

---

## 📊 服务访问

-   **前端门户**: `http://localhost:5173`
-   **后端接口**: `http://localhost:81`
-   **接口文档**: `http://localhost:81/swagger-ui/index.html`
-   **监控中心**: `http://localhost:81/druid` (`ruoyi`/`123456`)

---

> [!CAUTION]
> 生产环境下请务必修改默认登录密码与 Redis 访问权限。

---
@Richard
