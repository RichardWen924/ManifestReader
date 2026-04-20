# ManifestReader - 智能提单管理系统

基于 **Spring Boot 2.7 + Spring Cloud 2021** 构建的纯净微服务架构，完全去 RuoYi 化。

## 项目结构

```
manifest-reader/
├── manifest-reader-common/     # 公共模块（R、BaseEntity、异常、JwtUtil）
├── manifest-reader-model/      # 数据模型（Entity、DTO、VO）
├── manifest-reader-gateway/    # 网关（JWT鉴权、路由、跨域）  端口: 9000
├── manifest-reader-auth/       # 认证服务（登录、Token签发）  端口: 9001
├── manifest-reader-service/    # 业务服务（提单管理、AI解析） 端口: 9002
└── resources/
    ├── sql/init_schema.sql     # 数据库初始化脚本
    └── templates/              # Word 模版文件
```

## 启动顺序

1. 启动 **Nacos**（默认 `localhost:8848`）
2. 启动 **MySQL** + **Redis**
3. 执行 `resources/sql/init_schema.sql` 初始化数据库
4. 依次启动：`manifest-reader-auth` → `manifest-reader-service` → `manifest-reader-gateway`

## 核心接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 登录 | POST | `/auth/login` | 返回 JWT Token |
| 提单列表 | GET | `/api/bill/list` | 当前公司提单 |
| AI 解析 | POST | `/api/bill/analyze?filePath=...` | 上传文件触发解析 |
| 确认入库 | POST | `/api/bill/confirm` | 用户确认后持久化 |
| 导出 PDF | GET | `/api/bill/export/{id}` | 高保真 PDF 下载 |
| 接口文档 | GET | `/doc.html` | Knife4j 可视化文档 |
| 新增提单 | POST | `/api/bill` | 手动录入 |
| 更新提单 | PUT | `/api/bill` | 修改 |
| 删除提单 | DELETE | `/api/bill/{id}` | 删除 |

## 技术特色

1. **高保真 PDF 导出**：采用 `Word(poi-tl) -> PDF(LibreOffice) -> Fix(iText7)` 三级流水线，确保打印效果。
2. **混合脚本引擎**：集成 Python 脚本（mammoth, python-docx）用于处理 Docx 与 HTML 的双向转换及复杂格式替换。
3. **中心化配置**：全面接入 **Nacos Config**，支持数据库、Redis 等配置的热更新。
3. **可视化文档**：通过 **Knife4j** 统一管理各微服务接口。
4. **智能业务规则**：解析过程中自动应用提单校验规则（如：提单号订舱号同步、运费条款自动补全）。

## 技术栈

- **Spring Boot 2.7.18** + **Spring Cloud 2021.0.8**
- **Spring Cloud Alibaba** (Nacos 服务注册/发现/配置)
- **MyBatis-Plus 3.5.x**（垂直分表 + 级联查询）
- **Spring Cloud Gateway**（全局 JWT 鉴权过滤器）
- **Knife4j 3.0.3** (Swagger 可视化接口文档)
- **iText7** PDF 编辑 + **poi-tl** Word 渲染 + **JODConverter** (LibreOffice)
- **Hutool** + **Lombok** 工具链
- **Dify** AI Workflow 集成（文件解析）
