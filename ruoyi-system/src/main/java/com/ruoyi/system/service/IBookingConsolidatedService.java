package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.BookingConsolidated;

/**
 * 订舱与集装箱合并信息Service接口
 * 
 * @author ruoyi
 * @date 2026-01-27
 */
public interface IBookingConsolidatedService 
{
    /**
     * 查询订舱与集装箱合并信息
     * 
     * @param bookingNo 订舱与集装箱合并信息主键
     * @return 订舱与集装箱合并信息
     */
    public BookingConsolidated selectBookingConsolidatedByBookingNo(String bookingNo);

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
     * @param bookingNos 需要删除的订舱与集装箱合并信息主键集合
     * @return 结果
     */
    public int deleteBookingConsolidatedByBookingNos(String bookingNos);

    /**
     * 删除订舱与集装箱合并信息信息
     * 
     * @param bookingNo 订舱与集装箱合并信息主键
     * @return 结果
     */
    public int deleteBookingConsolidatedByBookingNo(String bookingNo);


    /**
     * 实现与工作流进行联动
     * @param filePath 文件路径
     * @return 进过工作流处理好的数据，将数据返回并加入数据库
     */
    BookingConsolidated getMessageFromFlow(String filePath);
}
