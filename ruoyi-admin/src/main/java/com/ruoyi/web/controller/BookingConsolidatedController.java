package com.ruoyi.web.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.file.FileUploadUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.BookingConsolidated;
import com.ruoyi.system.service.IBookingConsolidatedService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.system.domain.BookingConsolidatedDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 订舱与集装箱合并信息Controller
 * //TODO 提单号、单位
 *
 * @author ruoyi
 * @date 2026-01-27
 */
@Controller
@RequestMapping("/system/consolidated")
public class BookingConsolidatedController extends BaseController {
    private String prefix = "system/consolidated";

    @Autowired
    private IBookingConsolidatedService bookingConsolidatedService;

    @RequiresPermissions("system:consolidated:view")
    @GetMapping()
    public String consolidated() {
        return prefix + "/consolidated";
    }

    /**
     * 查询订舱与集装箱合并信息列表
     */
    @RequiresPermissions("system:consolidated:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(BookingConsolidated bookingConsolidated) {
        startPage();
        List<BookingConsolidated> list = bookingConsolidatedService.selectBookingConsolidatedList(bookingConsolidated);
        return getDataTable(list);
    }

    /**
     * 导出订舱与集装箱合并信息列表
     */
    @RequiresPermissions("system:consolidated:export")
    @Log(title = "订舱与集装箱合并信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(BookingConsolidated bookingConsolidated) {
        List<BookingConsolidated> list = bookingConsolidatedService.selectBookingConsolidatedList(bookingConsolidated);
        ExcelUtil<BookingConsolidated> util = new ExcelUtil<BookingConsolidated>(BookingConsolidated.class);
        return util.exportExcel(list, "订舱与集装箱合并信息数据");
    }

    /**
     * 新增订舱与集装箱合并信息
     */
    @RequiresPermissions("system:consolidated:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    @RequiresPermissions("system:consolidated:add")
    @Log(title = "订舱与集装箱合并信息", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(@RequestBody Map<String, Object> requestData) throws IOException {
        String filePath = (String) requestData.get("filePath");
        @SuppressWarnings("unchecked")
        Map<String, Object> editedData = (Map<String, Object>) requestData.get("editedData");

        // 如果有文件路径和编辑数据，直接保存（不调用Dify）
        if (filePath != null && !filePath.isEmpty() && editedData != null) {
            try {
                bookingConsolidatedService.directProcessAndSave(filePath, editedData);
                return success();
            } catch (Exception e) {
                return error("保存失败: " + e.getMessage());
            }
        }

        // 如果没有提供编辑数据，返回错误
        return error("缺少必要参数：filePath 或 editedData");
    }

    /**
     * AI智能提取 - 分析文件
     */
    @RequiresPermissions("system:consolidated:add")
    @PostMapping("/analyze")
    @ResponseBody
    public AjaxResult analyze(String filePath) {
        try {
            return AjaxResult.success(bookingConsolidatedService.analyzeFile(filePath));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * AI智能提取 - 生成最终PDF并保存
     */
    @RequiresPermissions("system:consolidated:add")
    @Log(title = "订舱与集装箱合并信息", businessType = BusinessType.INSERT)
    @PostMapping("/generate-pdf")
    @ResponseBody
    public AjaxResult generatePdf(@RequestBody BookingConsolidatedDto dto) {
        try {
            BookingConsolidated result = bookingConsolidatedService.generateAndSavePdf(dto);
            return AjaxResult.success(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 仅导出PDF（不保存到数据库）
     */
    @RequiresPermissions("system:consolidated:add")
    @Log(title = "订舱与集装箱合并信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export-pdf-only")
    @ResponseBody
    public AjaxResult exportPdfOnly(@RequestBody BookingConsolidatedDto dto) {
        try {
            String filePath = bookingConsolidatedService.generatePdfOnly(dto);
            return AjaxResult.success(filePath);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * AI智能提取 - 只保存到数据库（不生成PDF）
     */
    @RequiresPermissions("system:consolidated:add")
    @Log(title = "订舱与集装箱合并信息", businessType = BusinessType.INSERT)
    @PostMapping("/save-to-db")
    @ResponseBody
    public AjaxResult saveToDb(@RequestBody BookingConsolidatedDto dto) {
        try {
            BookingConsolidated result = bookingConsolidatedService.saveToDbOnly(dto);
            return AjaxResult.success(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    /**
     * 下载生成的PDF文件
     */
    @RequiresPermissions("system:consolidated:add")
    @GetMapping("/download")
    public void downloadPdf(String filePath, javax.servlet.http.HttpServletResponse response) {
        try {
            if (StringUtils.isEmpty(filePath)) {
                return;
            }

            java.io.File file = new java.io.File(filePath);
            if (!file.exists()) {
                return;
            }

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" +
                    java.net.URLEncoder.encode(file.getName(), "UTF-8"));

            try (java.io.FileInputStream fis = new java.io.FileInputStream(file);
                    javax.servlet.ServletOutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }
        } catch (Exception e) {
            // 记录错误日志
        }
    }

    /**
     * 修改订舱与集装箱合并信息
     */
    @RequiresPermissions("system:consolidated:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap) {
        // 通过id查询记录
        BookingConsolidated bookingConsolidated = bookingConsolidatedService
                .selectBookingConsolidatedList(new BookingConsolidated() {
                    {
                        setId(id);
                    }
                }).stream().findFirst().orElse(null);
        mmap.put("bookingConsolidated", bookingConsolidated);
        return prefix + "/edit";
    }

    /**
     * 修改保存订舱与集装箱合并信息
     */
    @RequiresPermissions("system:consolidated:edit")
    @Log(title = "订舱与集装箱合并信息", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(BookingConsolidated bookingConsolidated) {
        return toAjax(bookingConsolidatedService.updateBookingConsolidated(bookingConsolidated));
    }

    /**
     * 删除订舱与集装箱合并信息
     */
    @RequiresPermissions("system:consolidated:remove")
    @Log(title = "订舱与集装箱合并信息", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(bookingConsolidatedService.deleteBookingConsolidatedByBookingNos(ids));
    }
}
