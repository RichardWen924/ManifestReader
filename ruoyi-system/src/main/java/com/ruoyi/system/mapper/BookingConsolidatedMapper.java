package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BookingConsolidated;

/**
 * 订舱与集装箱合并信息Mapper接口
 * 
 * @author ruoyi
 * @date 2026-01-27
 */
public interface BookingConsolidatedMapper 
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
     * 删除订舱与集装箱合并信息
     * 
     * @param bookingNo 订舱与集装箱合并信息主键
     * @return 结果
     */
    public int deleteBookingConsolidatedByBookingNo(String bookingNo);

    /**
     * 批量删除订舱与集装箱合并信息
     * 
     * @param bookingNos 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBookingConsolidatedByBookingNos(String[] bookingNos);
}
