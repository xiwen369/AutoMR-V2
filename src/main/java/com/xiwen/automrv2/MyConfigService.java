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
        Path configPath = Paths.get(System.getProperty("user.home"), "IdeaProjects/MyProject/service", "AutoMR-config.json");

        System.out.println("路径为:" + configPath);
        return configPath;
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
            JsonNode projectOptionsNode = jsonNode.get("PROJECT_OPTIONS");

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


}
