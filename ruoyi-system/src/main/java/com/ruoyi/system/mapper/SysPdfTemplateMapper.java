package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.SysPdfTemplate;

/**
 * PDF模版配置Mapper接口
 * 
 * @author ruoyi
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
}
