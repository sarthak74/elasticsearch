package com.example.elasticsearch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchQueryDto {
    private String query;
    private FilterRequestDto filter;
    private Integer page;
    private Integer size;
}
