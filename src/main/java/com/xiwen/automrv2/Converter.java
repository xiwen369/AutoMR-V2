package com.xiwen.automrv2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * XXX
 *
 * @author xw
 * @date 2025/11/13
 */
@Component
public class Converter {

    List<ProjectDto> convertToProjectDto(String listMapString) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> listMap = mapper.readValue(listMapString, new TypeReference<>() {});

        List<ProjectDto> projectDtoList = new ArrayList<>();

        listMap.forEach(map -> {
            ProjectDto projectDto = new ProjectDto();
            projectDto.setId(Integer.parseInt(map.get("id").toString()));
            projectDto.setName(map.get("name").toString());
            projectDtoList.add(projectDto);
        });

        return projectDtoList;
    }

}
