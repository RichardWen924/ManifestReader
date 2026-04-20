package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.BillOfLading;
import com.ruoyi.system.domain.BillOfLadingDto;

/**
 * 提单信息Service接口
 * 
 * @author Richard
 * @date 2026-01-29
 */
public interface IBillOfLadingService {
    /**
     * 查询提单信息
     * 
     * @param id 提单信息主键
     * @return 提单信息
     */
    public BillOfLading selectBillOfLadingById(Long id);

    /**
     * 查询提单信息列表
     * 
     * @param billOfLading 提单信息
     * @return 提单信息集合
     */
    public List<BillOfLading> selectBillOfLadingList(BillOfLading billOfLading);

    /**
     * 新增提单信息
     * 
     * @param billOfLading 提单信息
     * @return 结果
     */
    public int insertBillOfLading(BillOfLading billOfLading);

    /**
     * 修改提单信息
     * 
     * @param billOfLading 提单信息
     * @return 结果
     */
    public int updateBillOfLading(BillOfLading billOfLading);

    /**
     * 批量删除提单信息
     * 
     * @param ids 需要删除的提单信息主键集合
     * @return 结果
     */
    public int deleteBillOfLadingByIds(String ids);

    /**
     * 删除提单信息信息
     * 
     * @param id 提单信息主键
     * @return 结果
     */
    public int deleteBillOfLadingById(Long id);

    /**
     * AI智能分析 - 分析文件 (返回 DTO 供前端确认)
     * 
     * @param filePath 文件路径
     * @return 包含数据和坐标的 DTO
     */
    BillOfLadingDto analyzeFile(String filePath);

    /**
     * AI智能提取 - 生成最终PDF并保存
     * 
     * @param dto 用户确认后的数据
     * @return 保存后的实体
     */
    BillOfLading generateAndSavePdf(BillOfLadingDto dto);

    /**
     * 仅生成PDF（不保存到数据库）
     * 
     * @param dto 用户确认后的数据
     * @return PDF文件字节数组
     */
    byte[] generatePdfOnly(BillOfLadingDto dto);
}
