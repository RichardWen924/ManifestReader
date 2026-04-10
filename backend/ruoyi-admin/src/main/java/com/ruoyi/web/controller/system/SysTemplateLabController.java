package com.ruoyi.web.controller.system;

import com.ruoyi.common.core.page.TableDataInfo;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.http.HttpServletResponse;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson.JSON;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.SysPdfTemplate;
import com.ruoyi.system.domain.SysTemplateMapping;
import com.ruoyi.system.service.ISysPdfTemplateService;
import com.ruoyi.system.service.ITemplateLabService;

/**
 * 智能模版实验室Controller
 */
@RestController
@RequestMapping("/client-api/template-lab")
public class SysTemplateLabController extends BaseController {

    @Autowired
    private ITemplateLabService templateLabService;

    @Autowired
    private ISysPdfTemplateService pdfTemplateService;

    @Autowired
    private com.ruoyi.system.service.ISysCompanyUserService sysCompanyUserService;

    /**
     * 第一步：上传并解析文档
     */
    @PostMapping("/analyze")
    public AjaxResult analyze(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return AjaxResult.error("文件不能为空");
        }
        List<SysTemplateMapping> mappings = templateLabService.analyzeDocument(file);
        return AjaxResult.success(mappings);
    }

    /**
     * 第三步：同步预览
     * 接收文件和映射数据，返回预览字节流
     */
    @PostMapping("/preview")
    public void preview(@RequestParam("file") MultipartFile file,
            @RequestParam("mappings") String mappingsJson,
            HttpServletResponse response) throws IOException {
        List<SysTemplateMapping> mappings = null;
        try {
            if (StringUtils.isNotEmpty(mappingsJson)) {
                mappings = JSON.parseArray(mappingsJson, SysTemplateMapping.class);
            }
        } catch (Exception e) {
        }
        if (mappings == null) {
            mappings = new ArrayList<>();
        }
        byte[] data = templateLabService.previewTemplate(file, mappings);

        response.reset();
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment; filename=\"preview.docx\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");
        response.getOutputStream().write(data);
    }

    /**
     * 第四步：保存模版
     */
    @PostMapping("/save")
    public AjaxResult save(@RequestParam("file") MultipartFile file,
            @RequestParam("mappings") String mappingsJson,
            @RequestParam("templateName") String templateName) {
        List<SysTemplateMapping> mappings = null;
        try {
            if (StringUtils.isNotEmpty(mappingsJson)) {
                mappings = JSON.parseArray(mappingsJson, SysTemplateMapping.class);
            }
        } catch (Exception e) {
        }
        if (mappings == null) {
            mappings = new ArrayList<>();
        }
        String loginName = getCurrentLoginName();
        if (loginName == null) {
            return AjaxResult.error("未获取到用户信息，请重新登录");
        }

        // 会员限额检查：非会员限额2个模版
        com.ruoyi.system.domain.SysCompanyUser userQuery = new com.ruoyi.system.domain.SysCompanyUser();
        userQuery.setCompanyCode(loginName);
        List<com.ruoyi.system.domain.SysCompanyUser> users = sysCompanyUserService.selectSysCompanyUserList(userQuery);
        if (!users.isEmpty()) {
            com.ruoyi.system.domain.SysCompanyUser user = users.get(0);
            if (!"1".equals(user.getVipStatus())) {
                SysPdfTemplate countQuery = new SysPdfTemplate();
                countQuery.setCreateBy(loginName);
                List<SysPdfTemplate> existing = pdfTemplateService.selectSysPdfTemplateList(countQuery);
                if (existing.size() >= 2) {
                    return AjaxResult.error("非会员模版数量已达上限（限2个），请升级账户以解锁无限模版！");
                }
            }
        }

        String path = templateLabService.saveTemplate(file, mappings, templateName);

        // 同时保存到 sys_pdf_template 表中，供管理页面使用
        SysPdfTemplate template = new SysPdfTemplate();
        template.setTemplateName(templateName);
        template.setTemplateCode("LAB_" + System.currentTimeMillis());
        template.setTemplateFilePath(path);
        template.setFieldConfig(mappingsJson);
        template.setCreateBy(loginName); // 关联用户
        pdfTemplateService.insertSysPdfTemplate(template);

        return AjaxResult.success("保存成功", path);
    }

    /**
     * 查询模版列表 (仅限当前用户)
     */
    /**
     * 查询模版列表 (仅限当前用户)
     */
    @GetMapping("/list")
    public TableDataInfo list(SysPdfTemplate sysPdfTemplate) {
        String loginName = getCurrentLoginName();
        if (loginName == null) {
            return new TableDataInfo();
        }
        startPage();
        sysPdfTemplate.setCreateBy(loginName);
        List<SysPdfTemplate> list = pdfTemplateService.selectSysPdfTemplateList(sysPdfTemplate);
        return getDataTable(list);
    }

    /**
     * 获取模版详细信息
     */
    @GetMapping(value = "/{templateId}")
    public AjaxResult getInfo(@PathVariable("templateId") Long templateId) {
        return AjaxResult.success(pdfTemplateService.selectSysPdfTemplateById(templateId));
    }

    /**
     * 修改模版
     */
    @PutMapping
    public AjaxResult edit(@RequestBody SysPdfTemplate sysPdfTemplate) {
        return toAjax(pdfTemplateService.updateSysPdfTemplate(sysPdfTemplate));
    }

    /**
     * 使用模版导出docx：将业务数据填充到模版的占位符中
     */
    @PostMapping("/export")
    public void exportDoc(@RequestBody java.util.Map<String, Object> params,
            HttpServletResponse response) throws IOException {
        Long templateId = Long.valueOf(params.get("templateId").toString());
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> businessData = (java.util.Map<String, Object>) params.get("businessData");

        byte[] data = templateLabService.exportWithTemplate(templateId, businessData);

        response.reset();
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment; filename=\"export.docx\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");
        response.getOutputStream().write(data);
    }

    /**
     * 删除模版
     */
    @DeleteMapping("/{templateIds}")
    public AjaxResult remove(@PathVariable Long[] templateIds) {
        return toAjax(pdfTemplateService.deleteSysPdfTemplateByIds(templateIds));
    }

    /**
     * 获取当前登录用户名 (兼容后台管理系统与客户端 API 系统)
     */
    private String getCurrentLoginName() {
        try {
            // 1. 尝试从 Shiro 获取 (后台管理用户)
            com.ruoyi.common.core.domain.entity.SysUser user = getSysUser();
            if (user != null) {
                return user.getLoginName();
            }
        } catch (Exception e) {
            // 忽略异常，继续尝试其他方式
        }

        // 2. 尝试从 Session 获取 (客户端用户，对应 ClientApiController 中的 CLIENT_USER_ID)
        Object clientUserId = getSession().getAttribute("CLIENT_USER_ID");
        return clientUserId != null ? clientUserId.toString() : null;
    }

    /**
     * 获取 HTML 内容 (用于在线编辑)
     */
    @PostMapping("/get-html")
    public AjaxResult getHtml(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return AjaxResult.error("文件不能为空");
        }
        try {
            String html = templateLabService.getTemplateHtml(file);
            return AjaxResult.success("获取成功", html);
        } catch (Exception e) {
            return AjaxResult.error("获取HTML失败: " + e.getMessage());
        }
    }

    /**
     * 将编辑后的 HTML 转换为 Docx 并下载
     */
    @PostMapping("/convert-to-docx")
    public void convertToDocx(@RequestBody java.util.Map<String, String> params, HttpServletResponse response)
            throws IOException {
        String html = params.get("html");
        if (StringUtils.isEmpty(html)) {
            response.sendError(500, "HTML内容不能为空");
            return;
        }

        java.io.File docxFile = templateLabService.convertHtmlToDocx(html);

        response.reset();
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment; filename=\"edited_template.docx\"");
        response.addHeader("Content-Length", "" + docxFile.length());
        response.setContentType("application/octet-stream; charset=UTF-8");

        try (java.io.FileInputStream fis = new java.io.FileInputStream(docxFile);
                java.io.OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } finally {
            docxFile.delete(); // 发送完成后删除临时文件
        }
    }

    /**
     * 获取已有模版的 HTML 内容 (用于在线编辑)
     */
    @GetMapping("/get-template-html/{templateId}")
    public AjaxResult getTemplateHtmlById(@PathVariable("templateId") Long templateId) {
        try {
            String html = templateLabService.getTemplateHtmlById(templateId);
            return AjaxResult.success("获取成功", html);
        } catch (Exception e) {
            return AjaxResult.error("获取HTML失败: " + e.getMessage());
        }
    }

    /**
     * 保存编辑后的模版 HTML 内容
     */
    @PostMapping("/save-template-html")
    public AjaxResult saveTemplateHtmlById(@RequestBody java.util.Map<String, Object> params) {
        try {
            Long templateId = Long.valueOf(params.get("templateId").toString());
            String html = params.get("html").toString();
            templateLabService.saveTemplateHtmlById(templateId, html);
            return AjaxResult.success("保存成功");
        } catch (Exception e) {
            return AjaxResult.error("保存失败: " + e.getMessage());
        }
    }
}
