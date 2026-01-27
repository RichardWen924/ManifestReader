package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.BookingsMapper;
import com.ruoyi.system.domain.Bookings;
import com.ruoyi.system.service.IBookingsService;
import com.ruoyi.common.core.text.Convert;

/**
 * 订舱信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-01-27
 */
@Service
public class BookingsServiceImpl implements IBookingsService 
{
    @Autowired
    private BookingsMapper bookingsMapper;

    /**
     * 查询订舱信息
     * 
     * @param bookingNo 订舱信息主键
     * @return 订舱信息
     */
    @Override
    public Bookings selectBookingsByBookingNo(String bookingNo)
    {
        return bookingsMapper.selectBookingsByBookingNo(bookingNo);
    }

    /**
     * 查询订舱信息列表
     * 
     * @param bookings 订舱信息
     * @return 订舱信息
     */
    @Override
    public List<Bookings> selectBookingsList(Bookings bookings)
    {
        return bookingsMapper.selectBookingsList(bookings);
    }

    /**
     * 新增订舱信息
     * 
     * @param bookings 订舱信息
     * @return 结果
     */
    @Override
    public int insertBookings(Bookings bookings)
    {
        return bookingsMapper.insertBookings(bookings);
    }

    /**
     * 修改订舱信息
     * 
     * @param bookings 订舱信息
     * @return 结果
     */
    @Override
    public int updateBookings(Bookings bookings)
    {
        return bookingsMapper.updateBookings(bookings);
    }

    /**
     * 批量删除订舱信息
     * 
     * @param bookingNos 需要删除的订舱信息主键
     * @return 结果
     */
    @Override
    public int deleteBookingsByBookingNos(String bookingNos)
    {
        return bookingsMapper.deleteBookingsByBookingNos(Convert.toStrArray(bookingNos));
    }

    /**
     * 删除订舱信息信息
     * 
     * @param bookingNo 订舱信息主键
     * @return 结果
     */
    @Override
    public int deleteBookingsByBookingNo(String bookingNo)
    {
        return bookingsMapper.deleteBookingsByBookingNo(bookingNo);
    }
}
