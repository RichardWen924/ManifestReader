package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.ContainersMapper;
import com.ruoyi.system.domain.Containers;
import com.ruoyi.system.service.IContainersService;
import com.ruoyi.common.core.text.Convert;

/**
 * 集装箱信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-01-27
 */
@Service
public class ContainersServiceImpl implements IContainersService 
{
    @Autowired
    private ContainersMapper containersMapper;

    /**
     * 查询集装箱信息
     * 
     * @param id 集装箱信息主键
     * @return 集装箱信息
     */
    @Override
    public Containers selectContainersById(Long id)
    {
        return containersMapper.selectContainersById(id);
    }

    /**
     * 查询集装箱信息列表
     * 
     * @param containers 集装箱信息
     * @return 集装箱信息
     */
    @Override
    public List<Containers> selectContainersList(Containers containers)
    {
        return containersMapper.selectContainersList(containers);
    }

    /**
     * 新增集装箱信息
     * 
     * @param containers 集装箱信息
     * @return 结果
     */
    @Override
    public int insertContainers(Containers containers)
    {
        return containersMapper.insertContainers(containers);
    }

    /**
     * 修改集装箱信息
     * 
     * @param containers 集装箱信息
     * @return 结果
     */
    @Override
    public int updateContainers(Containers containers)
    {
        return containersMapper.updateContainers(containers);
    }

    /**
     * 批量删除集装箱信息
     * 
     * @param ids 需要删除的集装箱信息主键
     * @return 结果
     */
    @Override
    public int deleteContainersByIds(String ids)
    {
        return containersMapper.deleteContainersByIds(Convert.toStrArray(ids));
    }

    /**
     * 删除集装箱信息信息
     * 
     * @param id 集装箱信息主键
     * @return 结果
     */
    @Override
    public int deleteContainersById(Long id)
    {
        return containersMapper.deleteContainersById(id);
    }
}
