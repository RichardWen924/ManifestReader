package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.Containers;

/**
 * 集装箱信息Service接口
 * 
 * @author ruoyi
 * @date 2026-01-27
 */
public interface IContainersService 
{
    /**
     * 查询集装箱信息
     * 
     * @param id 集装箱信息主键
     * @return 集装箱信息
     */
    public Containers selectContainersById(Long id);

    /**
     * 查询集装箱信息列表
     * 
     * @param containers 集装箱信息
     * @return 集装箱信息集合
     */
    public List<Containers> selectContainersList(Containers containers);

    /**
     * 新增集装箱信息
     * 
     * @param containers 集装箱信息
     * @return 结果
     */
    public int insertContainers(Containers containers);

    /**
     * 修改集装箱信息
     * 
     * @param containers 集装箱信息
     * @return 结果
     */
    public int updateContainers(Containers containers);

    /**
     * 批量删除集装箱信息
     * 
     * @param ids 需要删除的集装箱信息主键集合
     * @return 结果
     */
    public int deleteContainersByIds(String ids);

    /**
     * 删除集装箱信息信息
     * 
     * @param id 集装箱信息主键
     * @return 结果
     */
    public int deleteContainersById(Long id);
}
