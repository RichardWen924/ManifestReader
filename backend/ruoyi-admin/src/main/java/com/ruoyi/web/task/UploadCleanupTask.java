package com.ruoyi.web.task;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ruoyi.common.config.RuoYiConfig;


@Component
public class UploadCleanupTask {
    private static final Logger log = LoggerFactory.getLogger(UploadCleanupTask.class);

    /** 文件保留时间 (10分钟) */
    private static final long RETENTION_MILLIS = 600 * 1000;

    @Scheduled(fixedRate = 600000)
    public void cleanOldUploads() {
        String uploadPath = RuoYiConfig.getUploadPath();
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists() || !uploadDir.isDirectory()) {
            log.info("Upload directory does not exist: {}", uploadPath);
            return;
        }

        long cutoff = System.currentTimeMillis() - RETENTION_MILLIS;
        int count = cleanDirectory(uploadDir, cutoff);
        log.info("Upload cleanup completed. Deleted {} files older than 600 seconds from {}", count, uploadPath);
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
