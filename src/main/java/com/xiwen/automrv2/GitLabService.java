package com.xiwen.automrv2;

import com.xiwen.automrv2.dto.BranchDto;
import com.xiwen.automrv2.dto.ProjectDto;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;

import static com.intellij.openapi.ui.Messages.showErrorDialog;

@Component
public class GitLabService {

    private static final Converter converter = new Converter();

    /**
     * 获取当前用户有权访问的所有项目
     */
    public List<ProjectDto> getAccessibleProjects() throws Exception{

        MyConfigService myConfigService = new MyConfigService();
        String gitlabToken = myConfigService.readConfigByKey("GITLAB_TOKEN");
        String gitlabApiUrl = myConfigService.readConfigByKey("GITLAB_API_URL");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(gitlabApiUrl + "/projects?per_page=100"))
                .header("PRIVATE-TOKEN", gitlabToken)
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

        MyConfigService myConfigService = new MyConfigService();
        String gitlabToken = myConfigService.readConfigByKey("GITLAB_TOKEN");
        String gitlabApiUrl = myConfigService.readConfigByKey("GITLAB_API_URL");

        Integer projectId = getProjectId(projectName);

        HttpClient client = HttpClient.newHttpClient();

        // 构造创建合并请求的URL
        String url = gitlabApiUrl + "/projects/" + projectId + "/merge_requests";

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
                    .header("PRIVATE-TOKEN", gitlabToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // 发送请求
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 处理响应
            if(response.statusCode() == 409) {
                showErrorDialog("合并请求已存在",
                   "已经有其他人提交了相同的合并请求!" +
                   sourceBranch + " -> " + targetBranch);
            }


            if (response.statusCode() != 201 && response.statusCode() != 409) {
                throw new RuntimeException("Failed to create merge request: " + response.statusCode() + " - " + response.body());
            }

            //等待3秒
            Thread.sleep(100);

        }

    }

    /**
     * 获取项目ID
     */
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

    /**
     * 获取项目分支
     */
    public static List<String> getProjectBranches(String projectName) throws Exception {

        Integer projectId = getProjectId(projectName);

        // 获取配置信息
        MyConfigService myConfigService = new MyConfigService();
        String gitlabToken = myConfigService.readConfigByKey("GITLAB_TOKEN");
        String gitlabApiUrl = myConfigService.readConfigByKey("GITLAB_API_URL");

        // 构建请求URL
        String url = gitlabApiUrl + "/projects/" + projectId + "/repository/branches?per_page=9999";

        // 创建HTTP客户端和请求
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("PRIVATE-TOKEN", gitlabToken)
                .build();

        // 发送请求
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 处理响应
        if (response.statusCode() == 200) {
            // 使用converter转换分支信息
            List<BranchDto> branchDtoList = converter.convertToBranchDto(response.body());
            // 提取分支名称列表
            return branchDtoList.stream()
                    .map(BranchDto::getName)
                    .collect(java.util.stream.Collectors.toList());
        } else {
            throw new RuntimeException("Failed to fetch branches: " + response.statusCode() + " - " + response.body());
        }

    }


}
