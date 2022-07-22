package com.example.elasticsearch.dto;

import com.example.elasticsearch.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponseWithStatsDto {
    private StatsDto stats;
    private List<Employee> employees;
}
