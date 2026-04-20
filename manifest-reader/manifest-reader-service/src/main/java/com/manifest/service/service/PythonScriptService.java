package com.manifest.service.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Python 脚本执行服务
 * 封装对 mammoth, python-docx, htmldocx 等脚本的调用
 */
@Slf4j
@Service
public class PythonScriptService {

    private static final String SCRIPT_DIR = System.getProperty("user.dir") + "/scripts/";

    /**
     * 调用 Python 替换引擎 (template_replace.py)
     */
    public void replaceTemplate(String inputPath, String outputPath, List<Map<String, String>> mappings) {
        String mappingsPath = System.getProperty("java.io.tmpdir") + "/mappings_" + IdUtil.simpleUUID() + ".json";
        try {
            // 1. 写入临时映射文件
            FileUtil.writeString(JSONUtil.toJsonStr(mappings), mappingsPath, StandardCharsets.UTF_8);

            // 2. 执行脚本
            String scriptPath = SCRIPT_DIR + "template_replace.py";
            runPythonScript("python3", scriptPath, inputPath, outputPath, mappingsPath);
            
            log.info("[Python] 模板替换完成: {} -> {}", inputPath, outputPath);
        } finally {
            FileUtil.del(mappingsPath);
        }
    }

    /**
     * Docx 转 HTML (docx_to_html.py)
     */
    public String docxToHtml(String docxPath) {
        String scriptPath = SCRIPT_DIR + "docx_to_html.py";
        String output = runPythonScript("python3", scriptPath, docxPath);
        Map<String, Object> result = JSONUtil.toBean(output, Map.class);
        if ("success".equals(result.get("status"))) {
            return (String) result.get("html");
        }
        throw new RuntimeException("Docx转HTML失败: " + result.get("message"));
    }

    /**
     * HTML 转 Docx (html_to_docx.py)
     */
    public void htmlToDocx(String htmlPath, String outputPath) {
        String scriptPath = SCRIPT_DIR + "html_to_docx.py";
        runPythonScript("python3", scriptPath, htmlPath, outputPath);
        log.info("[Python] HTML转Docx完成: {}", outputPath);
    }

    private String runPythonScript(String... commands) {
        try {
            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("[Python] 脚本执行失败 (exit={}): {}", exitCode, output);
                throw new RuntimeException("Python脚本执行失败: " + output);
            }
            return output.toString();
        } catch (Exception e) {
            log.error("[Python] 执行异常", e);
            throw new RuntimeException("Python脚本调用异常: " + e.getMessage());
        }
    }
}
