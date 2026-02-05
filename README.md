# 🚢 提单导出系统 (Shipping Document System)

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.5+-green.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue.js-3.x-brightgreen.svg)](https://vuejs.org/)
[![AI-Powered](https://img.shields.io/badge/AI-Extraction-blueviolet.svg)](https://github.com/langchain/langchain)

## 📖 项目简介

**提单导出系统** 是一款结合 AI 智能识别技术的现代化航运单据管理平台。它旨在通过自动化的数据提取和智能化的导出流程，大幅提升航运从业人员处理提单（B/L）及其关联单据的效率。

无论是 PDF 文件、图片还是 Word 文档，系统都能快速精准地提取关键业务信息（如提单号、订舱号、发货人、收货人、集装箱明细等），并一键导出标准化的 PDF 格式文件。

---

## ✨ 核心特性

- 🤖 **AI 智能识别**：基于深度学习的数据提取引擎，支持多种格式单据解析。
- 📊 **合并信息管理**：完美整合订舱与集装箱信息，支持高效的字段级编辑与校验。
- 👁️ **实时预览编辑**：分析结果立等可见，支持实时修正与数据补全。
- 📋 **历史记录溯源**：完整的操作记录管理，支持基于提单号和订舱号的快速检索。
- 📁 **标准化导出**：一键生成符合行业标准的 PDF 提单文件。
- 🛡️ **权限与可见性**：严格的数据权限控制，确保用户仅能访问和管理自己的业务数据。
- 🎨 **现代化 UI**：极简深色系设计语言，提供极佳的可视化操作体验。

---

## 🛠️ 技术栈

### 后端 (Management API)
- **框架**: [Spring Boot](https://spring.io/projects/spring-boot) & [RuoYi-Fast](https://github.com/yangzongzhuan/RuoYi)
- **权限控制**: Apache Shiro
- **持久层**: MyBatis
- **数据库**: MySQL / PostgreSQL
- **AI 集成**: Dify 控制器交互层

### 前端 (Client Portal)
- **框架**: [Vue.js 3](https://vuejs.org/)
- **工程化工具**: Vite
- **状态管理**: Ref / Reactive (Composition API)
- **路由**: Vue Router
- **交互反馈**: Font Awesome & CSS3 Transitions

---

## 🚀 快速开始

### 环境依赖
- Java 8+
- Node.js 14+ / NPM 7+
- MySQL 5.7+ / 8.0

### 后端部署
1. 克隆代码至本地。
2. 导入 `ruoyi-admin/src/main/resources/sql` 下的数据库脚本。
3. 修改 `application-druid.yml` 中的数据库连接信息。
4. 运行 `com.ruoyi.RuoYiApplication`。

### 前端部署
```bash
cd ruoyi-client
npm install
npm run dev
```

---

## 🤝 参与贡献

我们欢迎任何形式的贡献 (Issue, PR, Feedback)！

---

## 📄 开源协议

本项目基于 [MIT License](LICENSE) 协议。

---

© 2026 RichardWen
