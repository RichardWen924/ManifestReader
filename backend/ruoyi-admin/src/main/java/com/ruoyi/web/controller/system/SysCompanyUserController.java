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
import com.ruoyi.system.domain.SysCompanyUser;
import com.ruoyi.system.service.ISysCompanyUserService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 公司用户Controller
 * 
 * @author Richard
 * @date 2026-02-05
 */
@Controller
@RequestMapping("/system/user")
public class SysCompanyUserController extends BaseController {
    private String prefix = "system/user";

    @Autowired
    private ISysCompanyUserService sysCompanyUserService;

    @RequiresPermissions("system:user:view")
    @GetMapping()
    public String user() {
        return prefix + "/user";
    }

    /**
     * 查询公司用户列表
     */
    @RequiresPermissions("system:user:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(SysCompanyUser sysCompanyUser) {
        startPage();
        List<SysCompanyUser> list = sysCompanyUserService.selectSysCompanyUserList(sysCompanyUser);
        return getDataTable(list);
    }

    /**
     * 导出公司用户列表
     */
    @RequiresPermissions("system:user:export")
    @Log(title = "公司用户", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(SysCompanyUser sysCompanyUser) {
        List<SysCompanyUser> list = sysCompanyUserService.selectSysCompanyUserList(sysCompanyUser);
        ExcelUtil<SysCompanyUser> util = new ExcelUtil<SysCompanyUser>(SysCompanyUser.class);
        return util.exportExcel(list, "公司用户数据");
    }

    /**
     * 新增公司用户
     */
    @RequiresPermissions("system:user:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 新增保存公司用户
     */
    @RequiresPermissions("system:user:add")
    @Log(title = "公司用户", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(SysCompanyUser sysCompanyUser) {
        return toAjax(sysCompanyUserService.insertSysCompanyUser(sysCompanyUser));
    }

    /**
     * 修改公司用户
     */
    @RequiresPermissions("system:user:edit")
    @GetMapping("/edit/{userId}")
    public String edit(@PathVariable("userId") Long userId, ModelMap mmap) {
        SysCompanyUser sysCompanyUser = sysCompanyUserService.selectSysCompanyUserByUserId(userId);
        mmap.put("sysCompanyUser", sysCompanyUser);
        return prefix + "/edit";
    }

    /**
     * 修改保存公司用户
     */
    @RequiresPermissions("system:user:edit")
    @Log(title = "公司用户", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(SysCompanyUser sysCompanyUser) {
        return toAjax(sysCompanyUserService.updateSysCompanyUser(sysCompanyUser));
    }

    /**
     * 删除公司用户
     */
    @RequiresPermissions("system:user:remove")
    @Log(title = "公司用户", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(sysCompanyUserService.deleteSysCompanyUserByUserIds(ids));
    }

    /**
     * 公司用户状态修改
     */
    @Log(title = "公司用户", businessType = BusinessType.UPDATE)
    @RequiresPermissions("system:user:edit")
    @PostMapping("/changeStatus")
    @ResponseBody
    public AjaxResult changeStatus(SysCompanyUser user) {
        return toAjax(sysCompanyUserService.changeStatus(user));
    }

    /**
     * 会员状态修改
     */
    @Log(title = "公司用户", businessType = BusinessType.UPDATE)
    @RequiresPermissions("system:user:edit")
    @PostMapping("/changeVipStatus")
    @ResponseBody
    public AjaxResult changeVipStatus(SysCompanyUser user) {
        return toAjax(sysCompanyUserService.changeVipStatus(user));
    }
}
