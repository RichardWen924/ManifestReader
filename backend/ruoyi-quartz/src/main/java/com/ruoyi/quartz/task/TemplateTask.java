package com.ruoyi.quartz.task;

import java.io.File;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.system.domain.SysPdfTemplate;
import com.ruoyi.system.service.ISysPdfTemplateService;

/**
 * 模版管理定时任务
 * 
 * @author Richard
 */
@Component("templateTask")
public class TemplateTask {
    private static final Logger log = LoggerFactory.getLogger(TemplateTask.class);

    @Autowired
    private ISysPdfTemplateService pdfTemplateService;

    /**
     * 定期检查模版文件是否存在，不存在在数据库中将数据进行删除
     */
    public void checkTemplateFiles() {
        log.info("开始检查模版物理文件是否存在...");

        List<SysPdfTemplate> list = pdfTemplateService.selectSysPdfTemplateList(new SysPdfTemplate());
        int removedCount = 0;

        for (SysPdfTemplate template : list) {
            String filePath = template.getTemplateFilePath();
            if (StringUtils.isEmpty(filePath)) {
                log.warn("模版 ID {} 的文件路径为空，正在跳过或考虑清理...", template.getTemplateId());
                continue;
            }

            // 转换资源前缀为物理路径
            String localPath = RuoYiConfig.getProfile() + filePath.replaceFirst(Constants.RESOURCE_PREFIX, "");
            File file = new File(localPath);

            if (!file.exists()) {
                log.info("检测到物理文件不存在，清理数据库记录: [ID: {}, Name: {}, Path: {}]",
                        template.getTemplateId(), template.getTemplateName(), localPath);

                try {
                    pdfTemplateService.deleteSysPdfTemplateById(template.getTemplateId());
                    removedCount++;
                } catch (Exception e) {
                    log.error("删除数据库模版记录失败, ID: " + template.getTemplateId(), e);
                }
            }
        }

        log.info("模版文件检查完成，共清理 {} 条无效记录。", removedCount);
    }
}
