package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.Bookings;

/**
 * 订舱信息Mapper接口
 * 
 * @author ruoyi
 * @date 2026-01-27
 */
public interface BookingsMapper 
{
    /**
     * 查询订舱信息
     * 
     * @param bookingNo 订舱信息主键
     * @return 订舱信息
     */
    public Bookings selectBookingsByBookingNo(String bookingNo);

    /**
     * 查询订舱信息列表
     * 
     * @param bookings 订舱信息
     * @return 订舱信息集合
     */
    public List<Bookings> selectBookingsList(Bookings bookings);

    /**
     * 新增订舱信息
     * 
     * @param bookings 订舱信息
     * @return 结果
     */
    public int insertBookings(Bookings bookings);

    /**
     * 修改订舱信息
     * 
     * @param bookings 订舱信息
     * @return 结果
     */
    public int updateBookings(Bookings bookings);

    /**
     * 删除订舱信息
     * 
     * @param bookingNo 订舱信息主键
     * @return 结果
     */
    public int deleteBookingsByBookingNo(String bookingNo);

    /**
     * 批量删除订舱信息
     * 
     * @param bookingNos 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBookingsByBookingNos(String[] bookingNos);
}
