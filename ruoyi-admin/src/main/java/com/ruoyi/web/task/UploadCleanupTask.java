package com.ruoyi.web.task;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ruoyi.common.config.RuoYiConfig;

/**
 * 定期清理上传文件区的旧文件
 * 每天凌晨2点执行，删除超过7天的文件
 */
@Component
public class UploadCleanupTask {
    private static final Logger log = LoggerFactory.getLogger(UploadCleanupTask.class);

    /** 文件保留天数 */
    private static final int RETENTION_DAYS = 7;

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanOldUploads() {
        String uploadPath = RuoYiConfig.getUploadPath();
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists() || !uploadDir.isDirectory()) {
            log.info("Upload directory does not exist: {}", uploadPath);
            return;
        }

        long cutoff = Instant.now().minus(RETENTION_DAYS, ChronoUnit.DAYS).toEpochMilli();
        int count = cleanDirectory(uploadDir, cutoff);
        log.info("Upload cleanup completed. Deleted {} files older than {} days from {}", count, RETENTION_DAYS,
                uploadPath);
    }

    /**
     * 递归清理目录中的旧文件（不删除 templates 子目录中的文件）
     */
    private int cleanDirectory(File dir, long cutoffMillis) {
        int deleted = 0;
        File[] files = dir.listFiles();
        if (files == null)
            return 0;

        for (File f : files) {
            // 跳过 templates 目录（模版文件需要保留）
            if (f.isDirectory() && "templates".equals(f.getName())) {
                continue;
            }
            if (f.isDirectory()) {
                deleted += cleanDirectory(f, cutoffMillis);
                // 如果子目录为空则删除
                String[] remaining = f.list();
                if (remaining != null && remaining.length == 0) {
                    f.delete();
                }
            } else if (f.lastModified() < cutoffMillis) {
                if (f.delete()) {
                    deleted++;
                }
            }
        }
        return deleted;
    }
}
