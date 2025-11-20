package com.xiwen.automrv2;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * XXX
 *
 * @author xw
 * @date 2025/11/15
 */
public class MyConfig {

    public static void main(String[] args) throws Exception {

        Path configPath = Paths.get(System.getProperty("user.home"), "IdeaProjects/MyProject/service", "AutoMR-config.json");

        System.out.println("路径为:" + configPath);

        List<String> projectBranches = GitLabService.getProjectBranches("pm-project-middle-end");

        System.out.println("项目分支:"+projectBranches);


    }


}
