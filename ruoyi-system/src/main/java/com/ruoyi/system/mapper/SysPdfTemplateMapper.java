package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysPdfTemplate;

/**
 * PDF模版配置Mapper接口
 * 
 * @author Richard
 * @date 2026-01-28
 */
public interface SysPdfTemplateMapper {
    /**
     * 根据模版编码查询PDF模版
     * 
     * @param templateCode 模版编码
     * @return PDF模版
     */
    public SysPdfTemplate selectSysPdfTemplateByCode(String templateCode);

    /**
     * 查询PDF模版
     * 
     * @param templateId 模版ID
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
     * 删除PDF模版
     * 
     * @param templateId PDF模版ID
     * @return 结果
     */
    public int deleteSysPdfTemplateById(Long templateId);
}
