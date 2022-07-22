package com.example.elasticsearch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterRequestDto {
    private Map<String, Object> match;
    private Map<String, RangeFilterDto> range;
}
