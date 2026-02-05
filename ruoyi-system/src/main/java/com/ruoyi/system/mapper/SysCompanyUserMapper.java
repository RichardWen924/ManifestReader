package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysCompanyUser;

/**
 * 公司用户Mapper接口
 * 
 * @author ruoyi
 * @date 2026-02-05
 */
public interface SysCompanyUserMapper 
{
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
     * 删除公司用户
     * 
     * @param userId 公司用户主键
     * @return 结果
     */
    public int deleteSysCompanyUserByUserId(Long userId);

    /**
     * 批量删除公司用户
     * 
     * @param userIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysCompanyUserByUserIds(String[] userIds);
}
