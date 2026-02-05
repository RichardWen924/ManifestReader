package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.SysCompanyUserMapper;
import com.ruoyi.system.domain.SysCompanyUser;
import com.ruoyi.system.service.ISysCompanyUserService;
import com.ruoyi.common.core.text.Convert;

/**
 * 公司用户Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-02-05
 */
@Service
public class SysCompanyUserServiceImpl implements ISysCompanyUserService {
    @Autowired
    private SysCompanyUserMapper sysCompanyUserMapper;

    /**
     * 查询公司用户
     * 
     * @param userId 公司用户主键
     * @return 公司用户
     */
    @Override
    public SysCompanyUser selectSysCompanyUserByUserId(Long userId) {
        return sysCompanyUserMapper.selectSysCompanyUserByUserId(userId);
    }

    /**
     * 查询公司用户列表
     * 
     * @param sysCompanyUser 公司用户
     * @return 公司用户
     */
    @Override
    public List<SysCompanyUser> selectSysCompanyUserList(SysCompanyUser sysCompanyUser) {
        return sysCompanyUserMapper.selectSysCompanyUserList(sysCompanyUser);
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
}
