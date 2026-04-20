package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.SysPdfTemplateMapper;
import com.ruoyi.system.domain.SysPdfTemplate;
import com.ruoyi.system.service.ISysPdfTemplateService;

/**
 * PDF模版配置Service业务层处理
 * 
 * @author Richard
 * @date 2026-02-10
 */
@Service
public class SysPdfTemplateServiceImpl implements ISysPdfTemplateService {
    @Autowired
    private SysPdfTemplateMapper sysPdfTemplateMapper;

    /**
     * 查询PDF模版
     * 
     * @param templateId PDF模版ID
     * @return PDF模版
     */
    @Override
    public SysPdfTemplate selectSysPdfTemplateById(Long templateId) {
        return sysPdfTemplateMapper.selectSysPdfTemplateById(templateId);
    }

    /**
     * 查询PDF模版列表
     * 
     * @param sysPdfTemplate PDF模版
     * @return PDF模版
     */
    @Override
    public List<SysPdfTemplate> selectSysPdfTemplateList(SysPdfTemplate sysPdfTemplate) {
        return sysPdfTemplateMapper.selectSysPdfTemplateList(sysPdfTemplate);
    }

    /**
     * 新增PDF模版
     * 
     * @param sysPdfTemplate PDF模版
     * @return 结果
     */
    @Override
    public int insertSysPdfTemplate(SysPdfTemplate sysPdfTemplate) {
        return sysPdfTemplateMapper.insertSysPdfTemplate(sysPdfTemplate);
    }

    /**
     * 修改PDF模版
     * 
     * @param sysPdfTemplate PDF模版
     * @return 结果
     */
    @Override
    public int updateSysPdfTemplate(SysPdfTemplate sysPdfTemplate) {
        return sysPdfTemplateMapper.updateSysPdfTemplate(sysPdfTemplate);
    }

    /**
     * 批量删除PDF模版
     * 
     * @param templateIds 需要删除的PDF模版ID
     * @return 结果
     */
    @Override
    public int deleteSysPdfTemplateByIds(Long[] templateIds) {
        int result = 0;
        for (Long templateId : templateIds) {
            result += sysPdfTemplateMapper.deleteSysPdfTemplateById(templateId);
        }
        return result;
    }

    /**
     * 删除PDF模版信息
     * 
     * @param templateId PDF模版ID
     * @return 结果
     */
    @Override
    public int deleteSysPdfTemplateById(Long templateId) {
        return sysPdfTemplateMapper.deleteSysPdfTemplateById(templateId);
    }
}
