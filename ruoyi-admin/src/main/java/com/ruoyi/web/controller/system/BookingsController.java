package com.ruoyi.web.controller.system;

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
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.Bookings;
import com.ruoyi.system.service.IBookingsService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 订舱信息Controller
 * 
 * @author ruoyi
 * @date 2026-01-27
 */
@Controller
@RequestMapping("/booking/bookings")
public class BookingsController extends BaseController
{
    private String prefix = "booking/bookings";

    @Autowired
    private IBookingsService bookingsService;

    @RequiresPermissions("booking:bookings:view")
    @GetMapping()
    public String bookings()
    {
        return prefix + "/bookings";
    }

    /**
     * 查询订舱信息列表
     */
    @RequiresPermissions("booking:bookings:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Bookings bookings)
    {
        startPage();
        List<Bookings> list = bookingsService.selectBookingsList(bookings);
        return getDataTable(list);
    }

    /**
     * 导出订舱信息列表
     */
    @RequiresPermissions("booking:bookings:export")
    @Log(title = "订舱信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(Bookings bookings)
    {
        List<Bookings> list = bookingsService.selectBookingsList(bookings);
        ExcelUtil<Bookings> util = new ExcelUtil<Bookings>(Bookings.class);
        return util.exportExcel(list, "订舱信息数据");
    }

    /**
     * 新增订舱信息
     */
    @RequiresPermissions("booking:bookings:add")
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存订舱信息
     */
    @RequiresPermissions("booking:bookings:add")
    @Log(title = "订舱信息", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(Bookings bookings)
    {
        return toAjax(bookingsService.insertBookings(bookings));
    }

    /**
     * 修改订舱信息
     */
    @RequiresPermissions("booking:bookings:edit")
    @GetMapping("/edit/{bookingNo}")
    public String edit(@PathVariable("bookingNo") String bookingNo, ModelMap mmap)
    {
        Bookings bookings = bookingsService.selectBookingsByBookingNo(bookingNo);
        mmap.put("bookings", bookings);
        return prefix + "/edit";
    }

    /**
     * 修改保存订舱信息
     */
    @RequiresPermissions("booking:bookings:edit")
    @Log(title = "订舱信息", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(Bookings bookings)
    {
        return toAjax(bookingsService.updateBookings(bookings));
    }

    /**
     * 删除订舱信息
     */
    @RequiresPermissions("booking:bookings:remove")
    @Log(title = "订舱信息", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(bookingsService.deleteBookingsByBookingNos(ids));
    }
}
