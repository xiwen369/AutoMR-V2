package com.xiwen.automrv2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * XXX
 *
 * @author xw
 * @date 2025/11/15
 */
public class MyConfig {

    public static void main(String[] args) {

        Path configPath = Paths.get(System.getProperty("user.home"), "IdeaProjects/MyProject/service", "AutoMR-config.json");

        System.out.println("路径为:" + configPath);

        try {
            String configString = java.nio.file.Files.readString(configPath);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(configString);
            JsonNode projectOptionsNode = jsonNode.get("PROJECT_OPTIONS");

            if (projectOptionsNode != null && projectOptionsNode.isArray()) {
                List<String> projectOptions = new ArrayList<>();
                for (JsonNode node : projectOptionsNode) {
                    projectOptions.add(node.asText());
                }
                System.out.println("配置文件: " + projectOptions);
            }

        } catch (IOException e) {
            System.out.println("读取配置文件异常!");
        }


    }


}
