package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysPdfTemplate;

/**
 * PDF模版配置Service接口
 * 
 * @author Richard
 * @date 2026-02-10
 */
public interface ISysPdfTemplateService {
    /**
     * 查询PDF模版
     * 
     * @param templateId PDF模版ID
     * @return PDF模版
     */
    public SysPdfTemplate selectSysPdfTemplateById(Long templateId);

    /**
     * 查询PDF模版列表
     * 
     * @param sysPdfTemplate PDF模版
     * @return PDF模版集合
     */
    public List<SysPdfTemplate> selectSysPdfTemplateList(SysPdfTemplate sysPdfTemplate);

    /**
     * 新增PDF模版
     * 
     * @param sysPdfTemplate PDF模版
     * @return 结果
     */
    public int insertSysPdfTemplate(SysPdfTemplate sysPdfTemplate);

    /**
     * 修改PDF模版
     * 
     * @param sysPdfTemplate PDF模版
     * @return 结果
     */
    public int updateSysPdfTemplate(SysPdfTemplate sysPdfTemplate);

    /**
     * 批量删除PDF模版
     * 
     * @param templateIds 需要删除的PDF模版ID
     * @return 结果
     */
    public int deleteSysPdfTemplateByIds(Long[] templateIds);

    /**
     * 删除PDF模版信息
     * 
     * @param templateId PDF模版ID
     * @return 结果
     */
    public int deleteSysPdfTemplateById(Long templateId);
}
