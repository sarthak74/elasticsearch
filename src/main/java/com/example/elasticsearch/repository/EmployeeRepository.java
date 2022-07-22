package com.example.elasticsearch.repository;

import com.example.elasticsearch.dto.SearchQueryDto;
import com.example.elasticsearch.entity.Employee;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;

@Repository
public interface EmployeeRepository extends ElasticsearchRepository<Employee, String> {
    List<Employee> findByFirstName(String firstName);
    List<Employee> findBySalaryBetween(Integer from, Integer to);
}
