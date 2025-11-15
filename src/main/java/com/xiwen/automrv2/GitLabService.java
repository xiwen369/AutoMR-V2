package com.xiwen.automrv2;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;

@Component
public class GitLabService {
    
    private static final String GITLAB_API_URL = "https://git.yyrd.com/api/v4";
    private static final String PRIVATE_TOKEN = "glpat-ZgmFCs12Vd1mp6nUVty3"; // 建议从配置文件或环境变量读取

    private final Converter converter = new Converter();

    /**
     * 获取当前用户有权访问的所有项目
     */
    public List<ProjectDto> getAccessibleProjects() throws Exception{
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GITLAB_API_URL + "/projects?per_page=100"))
                .header("PRIVATE-TOKEN", PRIVATE_TOKEN)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            List<ProjectDto> projectDtoList = converter.convertToProjectDto(response.body());
            return projectDtoList;
        } else {
            throw new RuntimeException("Failed to fetch projects: " + response.statusCode());
        }
    }

    /**
     * 提交合并请求
     */
    public void createMergeRequest(String projectName, String sourceBranch, List<String> targetBranchList, String title) throws Exception {

        Integer projectId = getProjectId(projectName);

        HttpClient client = HttpClient.newHttpClient();

        // 构造创建合并请求的URL
        String url = GITLAB_API_URL + "/projects/" + projectId + "/merge_requests";

        for (String targetBranch : targetBranchList) {
            // 构造请求体
            String requestBody = String.format(
                    "{\"source_branch\":\"%s\",\"target_branch\":\"%s\",\"title\":\"%s\"}",
                    sourceBranch,
                    targetBranch,
                    title + ":" + sourceBranch + "->" +targetBranch
            );

            // 创建HTTP请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("PRIVATE-TOKEN", PRIVATE_TOKEN)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // 发送请求
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 处理响应
            if (response.statusCode() != 201) {
                throw new RuntimeException("Failed to create merge request: " + response.statusCode() + " - " + response.body());
            }

            //等待3秒
            Thread.sleep(2000);

        }

    }

    private static Integer getProjectId(String projectName) throws Exception {
        GitLabService gitLabService = new GitLabService();

        //获取项目信息
        List<ProjectDto> projectDtoList = gitLabService.getAccessibleProjects();
        projectDtoList.forEach(projectDto -> System.out.println(projectDto.getId()+":"+projectDto.getName()));
        //projectDtoList转map
        HashMap<String, Integer> nameIdMap = new HashMap<>();
        projectDtoList.forEach(projectDto -> nameIdMap.put(projectDto.getName(), projectDto.getId()));
        Integer projectId = nameIdMap.get(projectName);
        return projectId;
    }


}
