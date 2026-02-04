package com.ruoyi.web.controller.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.BookingConsolidated;
import com.ruoyi.system.service.IBookingConsolidatedService;
// import com.ruoyi.system.domain.BillOfLadingDto;

/**
 * 客户端 API 接口
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/client-api")
public class ClientApiController extends BaseController {

    @Autowired
    private IBookingConsolidatedService bookingConsolidatedService;

    @Autowired
    private javax.sql.DataSource dataSource;

    /**
     * 初始化数据库（添加缺失的字段）
     */
    @GetMapping("/init-db")
    public AjaxResult initDb() {
        try (java.sql.Connection conn = dataSource.getConnection();
                java.sql.Statement stmt = conn.createStatement()) {
            try {
                stmt.execute(
                        "ALTER TABLE bill_of_lading_v5 ADD COLUMN create_by VARCHAR(64) DEFAULT NULL COMMENT '创建者'");
                return AjaxResult.success("字段 create_by 添加成功");
            } catch (java.sql.SQLException e) {
                if (e.getMessage().contains("Duplicate column name")) {
                    return AjaxResult.success("字段 create_by 已存在");
                }
                throw e;
            }
        } catch (Exception e) {
            return AjaxResult.error("数据库初始化失败: " + e.getMessage());
        }
    }

    private static final String CLIENT_SESSION_KEY = "CLIENT_USER_ID";

    /**
     * 客户登录 (简化版)
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody Map<String, String> loginData, HttpSession session) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        // 简化验证逻辑：允许任何非空用户名，密码固定为 12345 (或者由您指定)
        if ("admin".equals(username) || (username != null && !username.isEmpty() && "12345".equals(password))) {
            session.setAttribute(CLIENT_SESSION_KEY, username);
            return AjaxResult.success("登录成功", username);
        }
        return AjaxResult.error("用户名或密码错误");
    }

    /**
     * 检查登录状态
     */
    @GetMapping("/check-auth")
    public AjaxResult checkAuth(HttpSession session) {
        Object user = session.getAttribute(CLIENT_SESSION_KEY);
        if (user != null) {
            return AjaxResult.success("已登录", user);
        }
        return AjaxResult.error("未登录");
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public AjaxResult logout(HttpSession session) {
        session.removeAttribute(CLIENT_SESSION_KEY);
        return AjaxResult.success("已退出");
    }

    /**
     * 查询我的报单列表
     */
    @GetMapping("/list")
    public TableDataInfo list(BookingConsolidated query, HttpSession session) {
        String clientId = (String) session.getAttribute(CLIENT_SESSION_KEY);
        if (clientId == null) {
            return new TableDataInfo();
        }
        startPage();
        query.setCreateBy(clientId);
        List<BookingConsolidated> list = bookingConsolidatedService.selectBookingConsolidatedList(query);
        return getDataTable(list);
    }

    /**
     * AI智能提取 - 分析文件
     */
    @PostMapping("/analyze")
    public AjaxResult analyze(@RequestParam("filePath") String filePath, HttpSession session) {
        if (session.getAttribute(CLIENT_SESSION_KEY) == null) {
            return AjaxResult.error("未登录或登录超时");
        }
        return AjaxResult.success(bookingConsolidatedService.analyzeFile(filePath));
    }

    /**
     * 保存到数据库
     */
    @PostMapping("/save")
    public AjaxResult save(@RequestBody Map<String, Object> requestData, HttpSession session) {
        String clientId = (String) session.getAttribute(CLIENT_SESSION_KEY);
        if (clientId == null) {
            return AjaxResult.error("未登录");
        }

        String filePath = (String) requestData.get("filePath");
        String uuid = (String) requestData.get("uuid");
        @SuppressWarnings("unchecked")
        Map<String, Object> editedData = (Map<String, Object>) requestData.get("editedData");

        try {
            // 构造 DTO 并确保包含 createBy
            com.ruoyi.system.domain.BookingConsolidatedDto dto = new com.ruoyi.system.domain.BookingConsolidatedDto();
            dto.setUuid(uuid);
            editedData.put("createBy", clientId);
            editedData.put("originalFilePath", filePath);
            dto.setBusinessData(editedData);

            BookingConsolidated result = bookingConsolidatedService.saveToDbOnly(dto);
            return AjaxResult.success(result);
        } catch (Exception e) {
            return AjaxResult.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 修改报单数据
     */
    @PostMapping("/update")
    public AjaxResult update(@RequestBody BookingConsolidated data, HttpSession session) {
        String clientId = (String) session.getAttribute(CLIENT_SESSION_KEY);
        if (clientId == null) {
            return AjaxResult.error("未登录");
        }

        // 权限检查：只能修改自己的数据
        BookingConsolidated original = bookingConsolidatedService
                .selectBookingConsolidatedByBookingNo(data.getBookingNo());
        if (original == null || !clientId.equals(original.getCreateBy())) {
            return AjaxResult.error("无权修改此数据");
        }

        return toAjax(bookingConsolidatedService.updateBookingConsolidated(data));
    }

    /**
     * 仅导出 PDF
     */
    @PostMapping("/export-pdf")
    public void exportPdf(@RequestBody com.ruoyi.system.domain.BookingConsolidatedDto dto,
            HttpServletResponse response, HttpSession session) throws IOException {
        if (session.getAttribute(CLIENT_SESSION_KEY) == null) {
            response.sendError(401, "Unauthorized");
            return;
        }

        try {
            // 调用Service生成PDF（返回临时文件路径）
            String tempFilePath = bookingConsolidatedService.generatePdfOnly(dto);

            // 读取PDF文件
            java.io.File pdfFile = new java.io.File(tempFilePath);
            if (!pdfFile.exists()) {
                throw new RuntimeException("PDF文件不存在");
            }

            // 设置响应头
            response.setContentType("application/pdf");
            response.setCharacterEncoding("UTF-8");

            // 生成文件名
            String fileName = "BL_" + System.currentTimeMillis() + ".pdf";
            @SuppressWarnings("unchecked")
            Map<String, Object> businessData = (Map<String, Object>) dto.getBusinessData();
            if (businessData != null && businessData.containsKey("bookingNo")) {
                fileName = "BL_" + businessData.get("bookingNo") + ".pdf";
            }

            // 设置下载文件名
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));

            // 写入PDF到响应流
            try (java.io.FileInputStream fis = new java.io.FileInputStream(pdfFile);
                    java.io.OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }

            // 删除临时文件
            pdfFile.delete();

        } catch (Exception e) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"msg\":\"" + e.getMessage() + "\",\"code\":500}");
        }
    }
}
