package com.example.elasticsearch.service;

import com.example.elasticsearch.dto.SearchQueryDto;
import com.example.elasticsearch.dto.SearchResponseWithStatsDto;
import com.example.elasticsearch.entity.Employee;
import com.example.elasticsearch.repository.EmployeeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository repo;

    @Autowired
    private SearchService searchService;

    private ObjectMapper mapper;

    private String EMPLOYEES_INDEX = "companydatabase";

    private final RestHighLevelClient client;

    @Autowired
    public EmployeeService(RestHighLevelClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    public Employee save(Employee employee) throws IOException {
        Boolean isEmployeeSaved = searchService.save(employee);
        return isEmployeeSaved ? employee : null;
    }

    public List<Employee> saveAll(List<Employee> employees) throws IOException{
        Boolean areAllEmployeesSaved = searchService.saveAll(employees);
        return areAllEmployeesSaved ? employees : null;
    }

    public Iterable<Employee> getAll(Integer limit) throws IOException {
        if(limit != null) {
            return getEmployeesFromHits(searchService.findAll(limit));
        } else {
            return repo.findAll();
        }
    }

    public List<Employee> getByFirstName(String firstName) {
        List<Employee> employees = repo.findByFirstName(firstName);
        return employees;
    }

    public Employee getEmp(String id) {
        return repo.findById(id).get();
    }


    public List<Employee> search(SearchQueryDto searchQueryDto, String sortBy, Integer sortOrder) throws IOException {
        SearchHit[] hits = searchService.search(searchQueryDto, sortBy, sortOrder);
        List<Employee> employees = new ArrayList<>();
        for(SearchHit hit: hits) {
            employees.add(mapper.readValue(hit.getSourceAsString(), Employee.class));
        }
        return employees;
    }

    public SearchResponseWithStatsDto searchWithStats(
            SearchQueryDto searchQueryDto,
            String sortBy,
            Integer sortOrder,
            String statsKey
    ) throws IOException {
        SearchResponseWithStatsDto searchResponse = searchService.searchWithStats(searchQueryDto, sortBy, sortOrder, statsKey);
        return searchResponse;
    }

    public List<Employee> searchText(String query) throws IOException {
        SearchHit[] hits = searchService.searchText(query);
        return getEmployeesFromHits(hits);
    }

    public List<Employee> getEmployeesFromHits(SearchHit[] hits) throws JsonProcessingException, IOException {
        List<Employee> employees = new ArrayList<>();
        for(SearchHit hit: hits) {
            Employee employee = mapper.readValue(hit.getSourceAsString(), Employee.class);
            employee.setId(hit.getId());
            employees.add(employee);
        }
        return employees;
    }

    public Iterable<Employee> findBySalaryBetween(Integer from, Integer to) {
        return repo.findBySalaryBetween(from, to);
    }
}
