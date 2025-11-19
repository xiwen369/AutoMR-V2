package com.xiwen.automrv2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 配置文件服务
 *
 * @author xw
 * @date 2025/11/15
 */
public class MyConfigService {


    Path getConfigPath() {

        String configPathProperty = System.getProperty("AUTOMR_CONFIG_PATH");

        if (configPathProperty == null || configPathProperty.isEmpty()) {

            // 创建文件选择器
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setDialogTitle("请选择配置文件");
            fileChooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);

            // 设置文件过滤器，只显示常见的配置文件类型
            javax.swing.filechooser.FileNameExtensionFilter filter =
                    new javax.swing.filechooser.FileNameExtensionFilter(
                            "配置文件 (*.json, *.properties, *.yaml, *.yml)",
                            "json", "properties", "yaml", "yml");
            fileChooser.setFileFilter(filter);

            // 显示文件选择对话框
            int result = fileChooser.showOpenDialog(null);
            if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File selectedFile = fileChooser.getSelectedFile();
                String selectedPath = selectedFile.getAbsolutePath();
                System.setProperty("AUTOMR_CONFIG_PATH", selectedPath);
                System.out.println("配置文件路径: " + selectedPath);
                return Paths.get(selectedPath);
            } else {
                // 用户取消选择或关闭对话框
                com.intellij.openapi.ui.Messages.showErrorDialog(
                        "未选择配置文件，操作已取消！", "配置文件缺失");
                throw new RuntimeException("用户未选择配置文件!");
            }

        }

        System.out.println("配置文件路径: " + configPathProperty);

        return Paths.get(configPathProperty);

    }


    String readConfigByKey(String key) {

        Path configPath = getConfigPath();

        String configValue = "";

        try {
            String configString = Files.readString(configPath);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(configString);
            configValue = jsonNode.get(key).asText();

            System.out.println("配置文件: " + configValue);
        } catch (Exception e) {
            System.out.println("读取配置文件异常!");
        }

        return configValue;

    }


    List<String> readConfigListByKey(String key) {

        Path configPath = getConfigPath();

        List<String> projectOptions = new ArrayList<>();

        try {
            String configString = java.nio.file.Files.readString(configPath);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(configString);
            JsonNode projectOptionsNode = jsonNode.get(key);

            if (projectOptionsNode != null && projectOptionsNode.isArray()) {

                for (JsonNode node : projectOptionsNode) {
                    projectOptions.add(node.asText());
                }

                System.out.println("配置文件: " + projectOptions);
            }

        } catch (Exception e) {
            System.out.println("读取配置文件异常!");
        }

        return projectOptions;

    }

    /**
     * 写入配置文件
     */
    public void writeConfig(String key, String value) {

        Path configPath = getConfigPath();

        try {
            // 读取现有配置文件内容
            String configContent = Files.readString(configPath);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(configContent);

            // 如果根节点不是对象，则创建一个新的对象节点
            if (!(jsonNode instanceof com.fasterxml.jackson.databind.node.ObjectNode)) {
                jsonNode = mapper.createObjectNode();
            }

            // 更新或添加键值对
            ((com.fasterxml.jackson.databind.node.ObjectNode) jsonNode).put(key, value);

            // 将更新后的JSON写回文件
            String updatedConfigContent = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
            Files.writeString(configPath, updatedConfigContent);

            System.out.println("配置已更新: " + key + "=" + value);
        } catch (Exception e) {
            System.err.println("写入配置文件异常: " + e.getMessage());
            e.printStackTrace();
        }

    }


}
