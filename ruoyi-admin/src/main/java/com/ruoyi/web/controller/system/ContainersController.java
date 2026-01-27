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
import com.ruoyi.system.domain.Containers;
import com.ruoyi.system.service.IContainersService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 集装箱信息Controller
 * 
 * @author ruoyi
 * @date 2026-01-27
 */
@Controller
@RequestMapping("/container/containers")
public class ContainersController extends BaseController
{
    private String prefix = "container/containers";

    @Autowired
    private IContainersService containersService;

    @RequiresPermissions("container:containers:view")
    @GetMapping()
    public String containers()
    {
        return prefix + "/containers";
    }

    /**
     * 查询集装箱信息列表
     */
    @RequiresPermissions("container:containers:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(Containers containers)
    {
        startPage();
        List<Containers> list = containersService.selectContainersList(containers);
        return getDataTable(list);
    }

    /**
     * 导出集装箱信息列表
     */
    @RequiresPermissions("container:containers:export")
    @Log(title = "集装箱信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(Containers containers)
    {
        List<Containers> list = containersService.selectContainersList(containers);
        ExcelUtil<Containers> util = new ExcelUtil<Containers>(Containers.class);
        return util.exportExcel(list, "集装箱信息数据");
    }

    /**
     * 新增集装箱信息
     */
    @RequiresPermissions("container:containers:add")
    @GetMapping("/add")
    public String add()
    {
        return prefix + "/add";
    }

    /**
     * 新增保存集装箱信息
     */
    @RequiresPermissions("container:containers:add")
    @Log(title = "集装箱信息", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave(Containers containers)
    {
        return toAjax(containersService.insertContainers(containers));
    }

    /**
     * 修改集装箱信息
     */
    @RequiresPermissions("container:containers:edit")
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {
        Containers containers = containersService.selectContainersById(id);
        mmap.put("containers", containers);
        return prefix + "/edit";
    }

    /**
     * 修改保存集装箱信息
     */
    @RequiresPermissions("container:containers:edit")
    @Log(title = "集装箱信息", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(Containers containers)
    {
        return toAjax(containersService.updateContainers(containers));
    }

    /**
     * 删除集装箱信息
     */
    @RequiresPermissions("container:containers:remove")
    @Log(title = "集装箱信息", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {
        return toAjax(containersService.deleteContainersByIds(ids));
    }
}
