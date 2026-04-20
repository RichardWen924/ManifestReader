package com.ruoyi.manifest.controller.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.BookingConsolidated;
import com.ruoyi.system.domain.SysCompanyUser;
import com.ruoyi.system.service.IBookingConsolidatedService;
import com.ruoyi.system.service.ISysCompanyUserService;
import com.ruoyi.system.mapper.BillOfLadingMapper;
import com.ruoyi.common.utils.StringUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
// import com.ruoyi.system.domain.BillOfLadingDto;

/**
 * 客户端 API 接口
 * 
 * @author Richard
 */
@RestController
@RequestMapping("/client-api")
public class ClientApiController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ClientApiController.class);

    @Autowired
    private IBookingConsolidatedService bookingConsolidatedService;

    @Autowired
    private ISysCompanyUserService sysCompanyUserService;

    @Autowired
    private BillOfLadingMapper billOfLadingMapper;

    @Autowired
    private com.ruoyi.system.service.ISysUserService sysUserService;

    @Autowired
    private com.ruoyi.framework.shiro.service.SysPasswordService passwordService;

    private static final String CLIENT_SESSION_KEY = "CLIENT_USER_ID";

    /**
     * 客户注册
     */
    @PostMapping("/register")
    public AjaxResult register(@RequestBody SysCompanyUser companyUser) {
        if (StringUtils.isEmpty(companyUser.getCompanyName()) || StringUtils.isEmpty(companyUser.getCompanyAbbr())
                || StringUtils.isEmpty(companyUser.getPassword())) {
            return AjaxResult.error("公司名称、航司缩写及密码不能为空");
        }

        // 1. 验证航司缩写是否为4位字母
        String abbr = companyUser.getCompanyAbbr().toUpperCase();
        if (abbr.length() != 4 || !abbr.matches("[A-Z]{4}")) {
            return AjaxResult.error("航司缩写必须为4位大写字母");
        }

        // 验证缩写唯一性
        SysCompanyUser abbrQuery = new SysCompanyUser();
        abbrQuery.setCompanyAbbr(abbr);
        if (!sysCompanyUserService.selectSysCompanyUserList(abbrQuery).isEmpty()) {
            return AjaxResult.error("该航司缩写已存在");
        }
        companyUser.setCompanyAbbr(abbr);

        // 2. 生成8位公司编号 YYMMDDNN
        String datePrefix = new SimpleDateFormat("yyMMdd").format(new Date());
        String latestCode = sysCompanyUserService.getLatestCompanyCode(datePrefix);

        String newCode;
        if (StringUtils.isEmpty(latestCode)) {
            newCode = datePrefix + "01";
        } else {
            int seq = Integer.parseInt(latestCode.substring(6)) + 1;
            if (seq > 99) {
                return AjaxResult.error("今日注册公司数量已达上限");
            }
            newCode = datePrefix + String.format("%02d", seq);
        }
        companyUser.setCompanyCode(newCode);
        companyUser.setStatus("0"); // 正常

        int result = sysCompanyUserService.insertSysCompanyUser(companyUser);
        if (result > 0) {
            return AjaxResult.success("注册成功，您的公司编号为：" + newCode, newCode);
        }
        return AjaxResult.error("注册失败");
    }

    /**
     * 客户登录
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody Map<String, String> loginData, HttpSession session) {
        String username = loginData.get("username"); // 这里的 username 对应公司名、公司编号或航司缩写
        String password = loginData.get("password");

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            return AjaxResult.error("用户名或密码不能为空");
        }

        List<SysCompanyUser> list = new java.util.ArrayList<>();

        // 1. 先按公司编号查
        SysCompanyUser queryCode = new SysCompanyUser();
        queryCode.setCompanyCode(username);
        list.addAll(sysCompanyUserService.selectSysCompanyUserList(queryCode));

        // 2. 如果没找到，按航司缩写查
        if (list.isEmpty()) {
            SysCompanyUser queryAbbr = new SysCompanyUser();
            queryAbbr.setCompanyAbbr(username.toUpperCase());
            list.addAll(sysCompanyUserService.selectSysCompanyUserList(queryAbbr));
        }

        // 3. 如果还没找到，按公司名查
        if (list.isEmpty()) {
            SysCompanyUser queryName = new SysCompanyUser();
            queryName.setCompanyName(username);
            list.addAll(sysCompanyUserService.selectSysCompanyUserList(queryName));
        }

        // 4. 遍历所有匹配的用户，检查密码
        List<SysCompanyUser> validUsers = new java.util.ArrayList<>();
        if (!list.isEmpty()) {
            for (SysCompanyUser user : list) {
                if (password.equals(user.getPassword())) {
                    validUsers.add(user);
                }
            }
        }

        // 5. 处理登录结果
        if (validUsers.size() == 1) {
            SysCompanyUser user = validUsers.get(0);
            if ("1".equals(user.getStatus())) {
                return AjaxResult.error("帐号已停用");
            }
            session.setAttribute(CLIENT_SESSION_KEY, user.getCompanyCode());
            return AjaxResult.success("登录成功", user.getCompanyCode());
        } else if (validUsers.size() > 1) {
            return AjaxResult.error("存在重复同名帐号，请使用公司编号或航司缩写登录");
        }

        // 检查是否为后台管理员 (sys_user 表)
        if (StringUtils.isNotEmpty(username)) {
            com.ruoyi.common.core.domain.entity.SysUser sysUser = sysUserService.selectUserByLoginName(username);
            if (sysUser != null && passwordService.matches(sysUser, password)) {
                // 验证通过，标记为需要跳转到管理端
                return AjaxResult.success("REDIRECT_TO_ADMIN", "admin");
            }
        }

        return AjaxResult.error("用户名或密码错误");
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current-user")
    public AjaxResult getCurrentUser(HttpSession session) {
        String companyCode = (String) session.getAttribute(CLIENT_SESSION_KEY);
        if (StringUtils.isEmpty(companyCode)) {
            return AjaxResult.error("未登录");
        }

        SysCompanyUser user;
        if ("admin".equals(companyCode)) {
            user = new SysCompanyUser();
            user.setCompanyCode("admin");
            user.setCompanyName("Administrator");
            user.setCompanyAbbr("ADMN");
            user.setVipStatus("1");
        } else {
            SysCompanyUser query = new SysCompanyUser();
            query.setCompanyCode(companyCode);
            List<SysCompanyUser> list = sysCompanyUserService.selectSysCompanyUserList(query);
            if (list.isEmpty()) {
                return AjaxResult.error("用户不存在");
            }
            user = list.get(0);
        }
        return AjaxResult.success(user);
    }

    /**
     * 修改个人信息
     */
    @PostMapping("/update-profile")
    public AjaxResult updateProfile(@RequestBody Map<String, Object> params, HttpSession session) {
        String companyCode = (String) session.getAttribute(CLIENT_SESSION_KEY);
        if (StringUtils.isEmpty(companyCode) || "admin".equals(companyCode)) {
            return AjaxResult.error("该账号无法修改个人信息");
        }

        SysCompanyUser query = new SysCompanyUser();
        query.setCompanyCode(companyCode);
        List<SysCompanyUser> list = sysCompanyUserService.selectSysCompanyUserList(query);
        if (list.isEmpty()) {
            return AjaxResult.error("用户不存在");
        }

        SysCompanyUser dbUser = list.get(0);

        String companyName = (String) params.get("companyName");
        String companyAbbr = (String) params.get("companyAbbr");
        String oldPassword = (String) params.get("oldPassword");
        String newPassword = (String) params.get("password");

        if (StringUtils.isNotEmpty(companyName)) {
            dbUser.setCompanyName(companyName);
        }

        if (StringUtils.isNotEmpty(companyAbbr)) {
            String abbr = companyAbbr.toUpperCase();
            if (abbr.length() != 4 || !abbr.matches("[A-Z]{4}")) {
                return AjaxResult.error("航司缩写必须为4位大写字母");
            }
            // 验证缩写唯一性（排除自己）
            if (!abbr.equals(dbUser.getCompanyAbbr())) {
                SysCompanyUser abbrQuery = new SysCompanyUser();
                abbrQuery.setCompanyAbbr(abbr);
                if (!sysCompanyUserService.selectSysCompanyUserList(abbrQuery).isEmpty()) {
                    return AjaxResult.error("该航司缩写已存在");
                }

                // 航司缩写变动，同步更新业务单号的前缀
                log.info("用户 {} 更改航司缩写: {} -> {}", dbUser.getCompanyCode(), dbUser.getCompanyAbbr(), abbr);
                billOfLadingMapper.updateDocNoPrefix(abbr, dbUser.getCompanyCode());
                log.info("同步更新业务单号前缀完成");
            }
            dbUser.setCompanyAbbr(abbr);
        }

        if (StringUtils.isNotEmpty(newPassword)) {
            if (StringUtils.isEmpty(oldPassword)) {
                return AjaxResult.error("请输入原密码");
            }
            if (!dbUser.getPassword().equals(oldPassword)) {
                return AjaxResult.error("原密码不正确");
            }
            dbUser.setPassword(newPassword);
        }

        dbUser.setUpdateTime(new Date());

        int result = sysCompanyUserService.updateSysCompanyUser(dbUser);
        return result > 0 ? AjaxResult.success("修改成功") : AjaxResult.error("修改失败");
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
        String clientId = (String) session.getAttribute(CLIENT_SESSION_KEY);
        if (clientId == null) {
            return AjaxResult.error("未登录或登录超时");
        }

        // 会员功能：非会员限额4个文件
        SysCompanyUser query = new SysCompanyUser();
        query.setCompanyCode(clientId);
        List<SysCompanyUser> users = sysCompanyUserService.selectSysCompanyUserList(query);
        if (!users.isEmpty()) {
            SysCompanyUser user = users.get(0);
            if (!"1".equals(user.getVipStatus())) {
                BookingConsolidated countQuery = new BookingConsolidated();
                countQuery.setCreateBy(clientId);
                List<BookingConsolidated> list = bookingConsolidatedService.selectBookingConsolidatedList(countQuery);
                if (list.size() >= 4) {
                    return AjaxResult.error("非会员额度已用完（限4个文件），请升级账户以解锁无限额度。");
                }
            }
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
     * 删除报单数据
     */
    @PostMapping("/remove/{id}")
    public AjaxResult remove(@PathVariable("id") Long id, HttpSession session) {
        String clientId = (String) session.getAttribute(CLIENT_SESSION_KEY);
        if (clientId == null) {
            return AjaxResult.error("未登录");
        }

        // 权限检查：只能删除自己的数据
        BookingConsolidated original = bookingConsolidatedService.selectBookingConsolidatedById(id);
        if (original == null || !clientId.equals(original.getCreateBy())) {
            return AjaxResult.error("无权删除此数据");
        }

        return toAjax(bookingConsolidatedService.deleteBookingConsolidatedById(id));
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

    /**
     * 调试：检查所有提单数据的作者，排查配额错误
     */
    @GetMapping("/debug-quota")
    public AjaxResult debugQuota() {
        // 获取所有记录
        List<com.ruoyi.system.domain.BillOfLading> all = billOfLadingMapper
                .selectBillOfLadingList(new com.ruoyi.system.domain.BillOfLading());
        return AjaxResult.success(all);
    }
}
