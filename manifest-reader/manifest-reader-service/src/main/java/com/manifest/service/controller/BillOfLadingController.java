package com.manifest.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.manifest.common.response.R;
import com.manifest.model.dto.BillOfLadingDto;
import com.manifest.model.entity.BillOfLading;
import com.manifest.service.service.BillOfLadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bill")
@RequiredArgsConstructor
public class BillOfLadingController {

    private final BillOfLadingService billOfLadingService;
    private final com.manifest.service.service.BillOfLadingExportService billOfLadingExportService;

    /**
     * 分页查询（MP Page，仅主表字段，轻量列表）
     * GET /bill/page?pageNum=1&pageSize=10
     */
    @GetMapping("/page")
    public R<Page<BillOfLading>> page(
            @RequestHeader("company-code") String companyCode,
            @RequestParam(defaultValue = "1")  int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return R.ok(billOfLadingService.pageByCompany(companyCode, pageNum, pageSize));
    }

    /**
     * 单条详情（级联 JOIN 三张子表）
     * GET /bill/{id}
     */
    @GetMapping("/{id}")
    public R<BillOfLading> getById(@PathVariable Long id) {
        return R.ok(billOfLadingService.getWithDetails(id));
    }

    /**
     * 第一步：触发 AI 解析，返回解析结果供前端预览
     * POST /bill/analyze?filePath=xxx
     */
    @PostMapping("/analyze")
    public R<BillOfLadingDto> analyze(@RequestParam String filePath) {
        return R.ok(billOfLadingService.analyzeFile(filePath));
    }

    /**
     * 第二步：用户确认 AI 结果后入库
     * POST /bill/confirm
     */
    @PostMapping("/confirm")
    public R<BillOfLading> confirm(@RequestBody BillOfLadingDto dto) {
        return R.ok(billOfLadingService.confirmAndSave(dto));
    }

    /**
     * 导出 PDF 文件
     * GET /bill/export/{id}
     */
    @GetMapping("/export/{id}")
    public void export(@PathVariable Long id, javax.servlet.http.HttpServletResponse response) throws Exception {
        BillOfLading bl = billOfLadingService.getWithDetails(id);
        if (bl == null) return;
        
        // 模版路径（从 classpath 获取）
        String tmpl = "src/main/resources/templates/bill_of_lading.docx";
        
        byte[] pdf = billOfLadingExportService.exportPdf(bl, tmpl, null);
        
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + bl.getBlNo() + ".pdf");
        response.getOutputStream().write(pdf);
    }

    /**
     * 手动新增提单
     * POST /bill
     */
    @PostMapping
    public R<Void> save(@RequestBody BillOfLading bl) {
        billOfLadingService.saveBillOfLading(bl);
        return R.ok();
    }

    /**
     * 更新提单（MP updateById，只更新非 null 字段）
     * PUT /bill
     */
    @PutMapping
    public R<Void> update(@RequestBody BillOfLading bl) {
        billOfLadingService.updateBillOfLading(bl);
        return R.ok();
    }

    /**
     * 删除提单（级联删除三张子表）
     * DELETE /bill/{id}
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        billOfLadingService.removeBillOfLading(id);
        return R.ok();
    }
}
