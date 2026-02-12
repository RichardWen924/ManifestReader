# Shipping DocFlow (智能提单系统)

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.5.15-green.svg) ![Vue](https://img.shields.io/badge/Vue.js-3.0.4-4FC08D.svg) ![Vite](https://img.shields.io/badge/Vite-7.3.1-646CFF.svg) ![Dify](https://img.shields.io/badge/AI-Dify-blue.svg) ![License](https://img.shields.io/badge/License-MIT-yellow.svg)

基于 RuoYi-Vue 4.8.2 二次开发的各种国际物流单据智能解析与生成系统。采用前后端分离架构，集成 Dify AI 工作流，提供智能解析、模版管理、实时预览与一键导出功能。

## 目录 (Table of Contents)

- [1. 系统框架与架构](#1-系统框架与架构)
- [2. 技术栈清单](#2-技术栈清单)
- [3. 核心功能模块](#3-核心功能模块)
- [4. 部署指南](#4-部署指南)
- [5. 端口映射与访问地址](#5-端口映射与访问地址)

## 1. 系统框架与架构

本项目采用标准的 **前后端分离** 架构设计：

*   **后端 (`ruoyi-admin`)**: 基于 Spring Boot 2.5.15 的 RuoYi 框架，负责业务逻辑、数据持久化、权限控制及 Dify API 的对接。
*   **前端 (`ruoyi-client`)**: 基于 Vue 3 + Vite 的全新客户端，专注于提供极速、丝滑的用户体验，采用 Vanilla CSS 进行深度定制设计。
*   **AI 层**: 集成 Dify 平台，通过 API 实现文档的智能解析与结构化数据提取。

## 2. 技术栈清单

### 后端 (Backend)
*   **开发语言**: Java 1.8
*   **核心框架**: Spring Boot 2.5.15
*   **构建工具**: Maven 3.x
*   **数据库**: MySQL 5.7+ / 8.0
*   **缓存**: Redis (推荐 5.0+)
*   **核心依赖**:
    *   `mybatis-plus` / `pagehelper`: 数据分页
    *   `druid`: 数据库连接池
    *   `fastjson`: JSON 处理
    *   `poi`: Excel/Word 处理
    *   `shiro`: 安全控制

### 前端 (Frontend - Client)
*   **核心框架**: Vue 3.0.4 (Composition API)
*   **构建工具**: Vite 7.3.1
*   **路由管理**: Vue Router 4.x
*   **国际化**: Vue I18n 9.x
*   **HTTP 客户端**: Axios
*   **以 UI 库**: 主要是原生 CSS (Vanilla CSS) + FontAwesome 图标，配合 SweetAlert2 弹窗。

### AI 集成 (AI Integration)
*   **Dify API**: 用于调用编排好的 LLM 工作流，解析 PDF/Image 内容。
*   **PDF/Word 处理**: `docx-preview` (前端预览), Apache POI (后端生成)。

## 3. 核心功能模块

1.  **智能看板 (Dashboard)**
    *   支持拖拽上传 PDF/图片文件。
    *   集成 Dify 工作流进行实时智能解析。
    *   **分栏编辑预览**: 左侧表单编辑解析结果，右侧实时预览生成的 PDF 单据。
2.  **模版实验室 (Template Lab)**
    *   可视化配置 Word/PDF 模版。
    *   定义数据字段与占位符映射。
3.  **单据历史 (History)**
    *   查看历史解析记录。
    *   支持分页查询与再次导出。
4.  **会员系统 (Membership)**
    *   VIP 状态管理与额度控制。
    *   公司简称与用户画像管理。
5.  **后台管理 (Admin)**
    *   继承 RuoYi 强大的权限管理系统。
    *   用户管理、模版审核、系统监控。

## 4. 部署指南

### 环境要求
*   **JDK**: 1.8+
*   **MySQL**: 5.7 或 8.0
*   **Redis**: 3.2+
*   **Node.js**: 16.0+ (建议 18.x 以适配 Vite 7)

### 关键配置修改

**1. 后端配置 (`ruoyi-admin/src/main/resources/application.yml`)**
```yaml
server:
  port: 81 # 后端服务端口

spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/ry-vue?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
      username: root
      password: password # 修改为你的数据库密码
  redis:
    host: localhost
    port: 6379
```

**2. 前端代理配置 (`ruoyi-client/vite.config.js`)**
```javascript
export default defineConfig({
  server: {
    port: 8082, // 前端开发服务器端口
    proxy: {
      '/client-api': {
        target: 'http://localhost:81', // 指向后端地址
        changeOrigin: true
      },
      // ...其他代理配置
    }
  }
})
```

### 本地运行步骤

**后端启动**:
1.  导入项目到 IntelliJ IDEA。
2.  更新 Maven 依赖。
3.  运行 `RuoYiApplication.java`。

**前端启动 (`ruoyi-client`)**:
1.  进入前端目录: `cd ruoyi-client`
2.  安装依赖: `npm install`
3.  启动开发服务: `npm run dev`

### 生产打包

**后端**:
```bash
mvn clean package
# 生成 jar 包在 ruoyi-admin/target/ruoyi-admin.jar
```

**前端**:
```bash
cd ruoyi-client
npm run build
# 生成静态文件在 ruoyi-client/dist 目录
```

## 5. 端口映射与访问地址

| 服务 | 默认端口 | 说明 |
| :--- | :--- | :--- |
| **前端 (Client)** | `8082` | 访问地址: `http://localhost:8082` |
| **后端 (Admin API)** | `81` | 核心 API 服务 |
| **Swagger 文档** | `81` | 访问地址: `http://localhost:81/swagger-ui.html` |
| **Dify 代理** | - | 通过后端 `/client-api/analyze` 转发 |

## 6. 开源贡献 (Contributing)

欢迎提交 Issue 和 Pull Request 来改进本项目。

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request

## 7. 版权说明 (License)

本项目采用 MIT 许可证，详情请参阅 [LICENSE](LICENSE) 文件。
