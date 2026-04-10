package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysCompanyUser;

/**
 * 公司用户Service接口
 * 
 * @author Richard
 * @date 2026-02-05
 */
public interface ISysCompanyUserService {
    /**
     * 查询公司用户
     * 
     * @param userId 公司用户主键
     * @return 公司用户
     */
    public SysCompanyUser selectSysCompanyUserByUserId(Long userId);

    /**
     * 查询公司用户列表
     * 
     * @param sysCompanyUser 公司用户
     * @return 公司用户集合
     */
    public List<SysCompanyUser> selectSysCompanyUserList(SysCompanyUser sysCompanyUser);

    /**
     * 新增公司用户
     * 
     * @param sysCompanyUser 公司用户
     * @return 结果
     */
    public int insertSysCompanyUser(SysCompanyUser sysCompanyUser);

    /**
     * 修改公司用户
     * 
     * @param sysCompanyUser 公司用户
     * @return 结果
     */
    public int updateSysCompanyUser(SysCompanyUser sysCompanyUser);

    /**
     * 批量删除公司用户
     * 
     * @param userIds 需要删除的公司用户主键集合
     * @return 结果
     */
    public int deleteSysCompanyUserByUserIds(String userIds);

    /**
     * 删除公司用户信息
     * 
     * @param userId 公司用户主键
     * @return 结果
     */
    public int deleteSysCompanyUserByUserId(Long userId);

    /**
     * 获取指定前缀的最大公司编号
     */
    public String getLatestCompanyCode(String prefix);

    /**
     * 修改公司用户内容状态
     * 
     * @param user 用户信息
     * @return 结果
     */
    public int changeStatus(SysCompanyUser user);

    /**
     * 修改会员状态
     * 
     * @param user 用户信息
     * @return 结果
     */
    public int changeVipStatus(SysCompanyUser user);
}
