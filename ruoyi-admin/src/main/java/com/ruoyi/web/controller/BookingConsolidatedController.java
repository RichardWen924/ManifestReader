package com.ruoyi.web.controller;

import java.io.IOException;
import java.util.List;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.file.FileUploadUtils;
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

/**
 * 订舱与集装箱合并信息Controller
 * //TODO   提单号、单位
 *
 * @author ruoyi
 * @date 2026-01-27
 */
@Controller
@RequestMapping("/system/consolidated")
public class BookingConsolidatedController extends BaseController
{
    private String prefix = "system/consolidated";

    @Autowired
    private IBookingConsolidatedService bookingConsolidatedService;

    @RequiresPermissions("system:consolidated:view")
    @GetMapping()
    public String consolidated()
    {
        return prefix + "/consolidated";
    }

    /**
     * 查询订舱与集装箱合并信息列表
     */
    @RequiresPermissions("system:consolidated:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(BookingConsolidated bookingConsolidated)
    {
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
    public AjaxResult export(BookingConsolidated bookingConsolidated)
    {
        List<BookingConsolidated> list = bookingConsolidatedService.selectBookingConsolidatedList(bookingConsolidated);
        ExcelUtil<BookingConsolidated> util = new ExcelUtil<BookingConsolidated>(BookingConsolidated.class);
        return util.exportExcel(list, "订舱与集装箱合并信息数据");
    }

    /**
     * 新增订舱与集装箱合并信息
     */
    @RequiresPermissions("system:consolidated:add")
    @GetMapping("/add")
    public String add()
    {



        return prefix + "/add";
    }

    /**
     * 新增保存订舱与集装箱合并信息
     */
    @RequiresPermissions("system:consolidated:add")
    @Log(title = "订舱与集装箱合并信息", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(BookingConsolidated bookingConsolidated) throws IOException {

        // 1. 获取文件路径
        String filePath = bookingConsolidated.getFilePath();

        // 2. 如果filePath为空，且不是文件上传模式，直接保存表单数据
        if (filePath == null || filePath.isEmpty()) {
            return toAjax(bookingConsolidatedService.insertBookingConsolidated(bookingConsolidated));
        }

        // 3. 处理文件逻辑（如果需要从文件解析数据覆盖表单数据）
        // 注意：getMessageFromFlow可能需要绝对路径，前端传来的可能是URL（如 /profile/upload/...）
        // 需要将URL转换为本地绝对路径
        // 假设 filePath 已经是相对路径或者可以被 getMessageFromFlow 处理
        // 如果 getMessageFromFlow 返回一个新的 BookingConsolidated 对象，我们可能需要合并数据
        BookingConsolidated parsedData = bookingConsolidatedService.getMessageFromFlow(filePath);
        
        if (parsedData != null) {
            // getMessageFromFlow 已经完成了入库操作，直接返回成功
            return success();
        }

        // 4. 如果没有解析出数据，保存原表单对象
        return toAjax(bookingConsolidatedService.insertBookingConsolidated(bookingConsolidated));
    }

    /**
     * 修改订舱与集装箱合并信息
     */
    @RequiresPermissions("system:consolidated:edit")
    @GetMapping("/edit/{bookingNo}")
    public String edit(@PathVariable("bookingNo") String bookingNo, ModelMap mmap)
    {
        BookingConsolidated bookingConsolidated = bookingConsolidatedService.selectBookingConsolidatedByBookingNo(bookingNo);
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
    public AjaxResult editSave(BookingConsolidated bookingConsolidated)
    {
        return toAjax(bookingConsolidatedService.updateBookingConsolidated(bookingConsolidated));
    }

    /**
     * 删除订舱与集装箱合并信息
     */
    @RequiresPermissions("system:consolidated:remove")
    @Log(title = "订舱与集装箱合并信息", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(bookingConsolidatedService.deleteBookingConsolidatedByBookingNos(ids));
    }
}
