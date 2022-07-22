package com.example.elasticsearch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RangeFilterDto {
    private Object lte;
    private Object gte;
    private Object lt;
    private Object gt;
}