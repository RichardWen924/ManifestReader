package com.ruoyi.web.controller.system;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson.JSON;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.SysTemplateMapping;
import com.ruoyi.system.service.ITemplateLabService;

/**
 * 智能模版实验室Controller
 */
@RestController
@RequestMapping("/system/template-lab")
public class SysTemplateLabController extends BaseController {

    @Autowired
    private ITemplateLabService templateLabService;

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
        List<SysTemplateMapping> mappings = JSON.parseArray(mappingsJson, SysTemplateMapping.class);
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
        List<SysTemplateMapping> mappings = JSON.parseArray(mappingsJson, SysTemplateMapping.class);
        String path = templateLabService.saveTemplate(file, mappings, templateName);
        return AjaxResult.success("保存成功", path);
    }
}
