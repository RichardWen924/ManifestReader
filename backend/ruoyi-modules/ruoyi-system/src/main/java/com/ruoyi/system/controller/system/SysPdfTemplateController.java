package com.ruoyi.system.controller.system;

import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.system.domain.SysPdfTemplate;
import com.ruoyi.system.service.ISysPdfTemplateService;
import com.ruoyi.system.service.ITemplateLabService;

/**
 * PDF模版配置Controller
 * 
 * @author Richard
 * @date 2026-02-11
 */
@Controller
@RequestMapping("/system/pdfTemplate")
public class SysPdfTemplateController extends BaseController {
    private String prefix = "system/pdfTemplate";

    @Autowired
    private ISysPdfTemplateService sysPdfTemplateService;

    @Autowired
    private ITemplateLabService templateLabService;

    @RequiresPermissions("system:pdfTemplate:view")
    @GetMapping()
    public String pdfTemplate() {
        return prefix + "/pdfTemplate";
    }

    /**
     * 查询PDF模版列表
     */
    @RequiresPermissions("system:pdfTemplate:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(SysPdfTemplate sysPdfTemplate) {
        startPage();
        List<SysPdfTemplate> list = sysPdfTemplateService.selectSysPdfTemplateList(sysPdfTemplate);
        return getDataTable(list);
    }

    /**
     * 新增PDF模版
     */
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 新增保存PDF模版
     */
    @RequiresPermissions("system:pdfTemplate:add")
    @Log(title = "PDF模版", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(SysPdfTemplate sysPdfTemplate, MultipartFile file) {
        try {
            if (file != null && !file.isEmpty()) {
                // 使用 TemplateLabService 的保存逻辑，它会处理文件存储和解析
                String path = templateLabService.saveTemplate(file, null, sysPdfTemplate.getTemplateName());
                sysPdfTemplate.setTemplateFilePath(path);
                // 简单的解析一下字段，虽然这里可能没有 mappings 数据
                // sysPdfTemplate.setFieldConfig(...)
            }
            sysPdfTemplate.setCreateBy(getLoginName());
            return toAjax(sysPdfTemplateService.insertSysPdfTemplate(sysPdfTemplate));
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 修改PDF模版
     */
    @RequiresPermissions("system:pdfTemplate:edit")
    @GetMapping("/edit/{templateId}")
    public String edit(@PathVariable("templateId") Long templateId, ModelMap mmap) {
        SysPdfTemplate sysPdfTemplate = sysPdfTemplateService.selectSysPdfTemplateById(templateId);
        mmap.put("sysPdfTemplate", sysPdfTemplate);
        return prefix + "/edit";
    }

    /**
     * 修改保存PDF模版
     */
    @RequiresPermissions("system:pdfTemplate:edit")
    @Log(title = "PDF模版", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(SysPdfTemplate sysPdfTemplate, MultipartFile file) {
        try {
            if (file != null && !file.isEmpty()) {
                String path = templateLabService.saveTemplate(file, null, sysPdfTemplate.getTemplateName());
                sysPdfTemplate.setTemplateFilePath(path);
            }
            return toAjax(sysPdfTemplateService.updateSysPdfTemplate(sysPdfTemplate));
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 删除PDF模版
     */
    @RequiresPermissions("system:pdfTemplate:remove")
    @Log(title = "PDF模版", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(sysPdfTemplateService.deleteSysPdfTemplateByIds(Convert.toLongArray(ids)));
    }
}
