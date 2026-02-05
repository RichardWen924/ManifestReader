package com.ruoyi.system.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.system.domain.BookingConsolidated;

/**
 * 订舱与集装箱合并信息Service接口
 * 
 * @author ruoyi
 * @date 2026-01-27
 */
public interface IBookingConsolidatedService {
    /**
     * 查询订舱与集装箱合并信息
     * 
     * @param bookingNo 订舱与集装箱合并信息主键
     * @return 订舱与集装箱合并信息
     */
    public BookingConsolidated selectBookingConsolidatedByBookingNo(String bookingNo);

    /**
     * 查询订舱与集装箱合并信息
     * 
     * @param id 订舱与集装箱合并信息ID
     * @return 订舱与集装箱合并信息
     */
    public BookingConsolidated selectBookingConsolidatedById(Long id);

    /**
     * 查询订舱与集装箱合并信息列表
     * 
     * @param bookingConsolidated 订舱与集装箱合并信息
     * @return 订舱与集装箱合并信息集合
     */
    public List<BookingConsolidated> selectBookingConsolidatedList(BookingConsolidated bookingConsolidated);

    /**
     * 新增订舱与集装箱合并信息
     * 
     * @param bookingConsolidated 订舱与集装箱合并信息
     * @return 结果
     */
    public int insertBookingConsolidated(BookingConsolidated bookingConsolidated);

    /**
     * 修改订舱与集装箱合并信息
     * 
     * @param bookingConsolidated 订舱与集装箱合并信息
     * @return 结果
     */
    public int updateBookingConsolidated(BookingConsolidated bookingConsolidated);

    /**
     * 批量删除订舱与集装箱合并信息
     * 
     * @param ids 需要删除的订舱与集装箱合并信息主键集合
     * @return 结果
     */
    public int deleteBookingConsolidatedByBookingNos(String ids);

    /**
     * 删除订舱与集装箱合并信息信息
     * 
     * @param id 订舱与集装箱合并信息主键
     * @return 结果
     */
    public int deleteBookingConsolidatedByBookingNo(Long id);

    /**
     * 删除订舱与集装箱合并信息信息
     * 
     * @param id 订舱与集装箱合并信息ID
     * @return 结果
     */
    public int deleteBookingConsolidatedById(Long id);

    /**
     * 直接保存模式：接受编辑后的数据直接保存（不调用Dify）
     * 
     * @param filePath   文件路径
     * @param editedData 编辑后的数据
     * @return 保存结果
     */
    BookingConsolidated directProcessAndSave(String filePath, Map<String, Object> editedData);

    /**
     * AI智能提取 - 分析文件 (返回 DTO 供前端确认)
     * 
     * @param filePath 文件路径
     * @return 包含数据和坐标的 DTO
     */
    com.ruoyi.system.domain.BookingConsolidatedDto analyzeFile(String filePath);

    /**
     * AI智能提取 - 生成最终PDF并保存
     * 
     * @param dto 用户确认后的数据
     * @return 保存后的实体
     */
    BookingConsolidated generateAndSavePdf(com.ruoyi.system.domain.BookingConsolidatedDto dto);

    /**
     * 仅生成PDF（不保存到数据库）
     * 
     * @param dto 用户确认后的数据
     * @return PDF文件路径
     */
    String generatePdfOnly(com.ruoyi.system.domain.BookingConsolidatedDto dto);

    /**
     * 只保存到数据库（不生成PDF）
     * 
     * @param dto 用户确认后的数据
     * @return 保存结果
     */
    BookingConsolidated saveToDbOnly(com.ruoyi.system.domain.BookingConsolidatedDto dto);

    /**
     * 校验单号是否唯一
     * 
     * @param docNo 单号
     * @param id    排除的ID（可选）
     * @return 结果 true 唯一 false 不唯一
     */
    public boolean checkDocNoUnique(String docNo, Long id);
}
