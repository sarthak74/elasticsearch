package com.example.elasticsearch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatsDto {
    private Double sum;
    private Double min;
    private Double max;
    private Double avg;
    private String count;
    private Integer displayCount;
}
