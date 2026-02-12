package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.SysCompanyUserMapper;
import com.ruoyi.system.domain.SysCompanyUser;
import com.ruoyi.system.service.ISysCompanyUserService;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.system.mapper.BillOfLadingMapper;
import com.ruoyi.system.domain.BillOfLading;

/**
 * 公司用户Service业务层处理
 * 
 * @author Richard
 * @date 2026-02-05
 */
@Service
public class SysCompanyUserServiceImpl implements ISysCompanyUserService {
    @Autowired
    private SysCompanyUserMapper sysCompanyUserMapper;

    @Autowired
    private BillOfLadingMapper billOfLadingMapper;

    /**
     * 查询公司用户
     * 
     * @param userId 公司用户主键
     * @return 公司用户
     */
    @Override
    public SysCompanyUser selectSysCompanyUserByUserId(Long userId) {
        SysCompanyUser user = sysCompanyUserMapper.selectSysCompanyUserByUserId(userId);
        if (user != null) {
            calculateDataCount(user);
        }
        return user;
    }

    /**
     * 查询公司用户列表
     * 
     * @param sysCompanyUser 公司用户
     * @return 公司用户
     */
    @Override
    public List<SysCompanyUser> selectSysCompanyUserList(SysCompanyUser sysCompanyUser) {
        List<SysCompanyUser> list = sysCompanyUserMapper.selectSysCompanyUserList(sysCompanyUser);
        for (SysCompanyUser user : list) {
            calculateDataCount(user);
        }
        return list;
    }

    /**
     * 手动计算并设置业务数据量
     */
    private void calculateDataCount(SysCompanyUser user) {
        if (user != null && user.getCompanyCode() != null) {
            BillOfLading query = new BillOfLading();
            query.setCreateBy(user.getCompanyCode());
            List<BillOfLading> records = billOfLadingMapper.selectBillOfLadingList(query);
            System.out.println("DEBUG: Quota count for user " + user.getCompanyCode() + " is "
                    + (records != null ? records.size() : 0));
            user.setDataCount((long) (records != null ? records.size() : 0));
        }
    }

    /**
     * 新增公司用户
     * 
     * @param sysCompanyUser 公司用户
     * @return 结果
     */
    @Override
    public int insertSysCompanyUser(SysCompanyUser sysCompanyUser) {
        sysCompanyUser.setCreateTime(DateUtils.getNowDate());
        return sysCompanyUserMapper.insertSysCompanyUser(sysCompanyUser);
    }

    /**
     * 修改公司用户
     * 
     * @param sysCompanyUser 公司用户
     * @return 结果
     */
    @Override
    public int updateSysCompanyUser(SysCompanyUser sysCompanyUser) {
        sysCompanyUser.setUpdateTime(DateUtils.getNowDate());
        return sysCompanyUserMapper.updateSysCompanyUser(sysCompanyUser);
    }

    /**
     * 批量删除公司用户
     * 
     * @param userIds 需要删除的公司用户主键
     * @return 结果
     */
    @Override
    public int deleteSysCompanyUserByUserIds(String userIds) {
        return sysCompanyUserMapper.deleteSysCompanyUserByUserIds(Convert.toStrArray(userIds));
    }

    /**
     * 删除公司用户信息
     * 
     * @param userId 公司用户主键
     * @return 结果
     */
    @Override
    public int deleteSysCompanyUserByUserId(Long userId) {
        return sysCompanyUserMapper.deleteSysCompanyUserByUserId(userId);
    }

    /**
     * 获取指定前缀的最大公司编号
     */
    @Override
    public String getLatestCompanyCode(String prefix) {
        return sysCompanyUserMapper.getLatestCompanyCode(prefix);
    }

    /**
     * 修改公司用户内容状态
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int changeStatus(SysCompanyUser user) {
        return sysCompanyUserMapper.updateSysCompanyUser(user);
    }

    /**
     * 修改会员状态
     * 
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int changeVipStatus(SysCompanyUser user) {
        return sysCompanyUserMapper.updateSysCompanyUser(user);
    }
}
