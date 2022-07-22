package com.example.elasticsearch.controller;

import com.example.elasticsearch.dto.SearchQueryDto;
import com.example.elasticsearch.entity.Employee;
import com.example.elasticsearch.repository.EmployeeRepository;
import com.example.elasticsearch.service.EmployeeService;
import com.example.elasticsearch.service.SearchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestController
public class EmployeeController {
    @Autowired
    private EmployeeService service;

    @Autowired
    private SearchService searchService;


    @PostMapping("/addEmployee")
    public Employee addEmployee(@RequestBody Employee employee) throws IOException{
        return service.save(employee);
    }

    @PostMapping("/addMultipleEmployees")
    public List<Employee> addMultipleEmployees(@RequestBody List<Employee> employees) throws IOException{
        return service.saveAll(employees);
    }

    @GetMapping("/getEmployee/{id}")
    public ResponseEntity<Object> getEmployeeById(@PathVariable String id) throws JsonMappingException, JsonProcessingException {
        try {
            Employee employee = service.getEmp(id);
            if(employee != null) return new ResponseEntity<Object>(employee, HttpStatus.OK);
            else return new ResponseEntity<Object>("No employee with id: " + id, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<Object>("Internal Server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getAllEmployees")
    public ResponseEntity<Iterable<Employee>> getAllEmployees(@RequestParam(required = false) Integer limit) throws IOException{
        Iterable<Employee> employees = service.getAll(limit);
        return new ResponseEntity<Iterable<Employee>>(employees, HttpStatus.OK);
    }

    @GetMapping("/findByFirstName/{firstName}")
    public ResponseEntity<List<Employee>> findByFirstName(@PathVariable String firstName) {
        List<Employee> employee = service.getByFirstName(firstName);
        return new ResponseEntity<List<Employee>>(employee, HttpStatus.OK);
    }

    @GetMapping("/findBySalaryBetween")
    public ResponseEntity<Iterable<Employee>> findBySalaryBetween(@RequestParam Integer from, @RequestParam Integer to) {
        return new ResponseEntity<Iterable<Employee>>(service.findBySalaryBetween(from, to), HttpStatus.OK);
    }


    @PostMapping("/search")
    public Object search(@RequestBody SearchQueryDto searchQueryDto,
                                 @RequestParam(required = false) String sortBy,
                                 @RequestParam(required = false) Integer sortOrder,
                                 @RequestParam(required = false) String statsKey) throws IOException {
        if(statsKey != null) {
            return service.searchWithStats(searchQueryDto, sortBy, sortOrder, statsKey);
        } else {
            return service.search(searchQueryDto, sortBy, sortOrder);
        }
    }

    @GetMapping("/searchText/{query}")
    public List<Employee> searchText(@PathVariable String query) throws IOException{
        return service.searchText(query);
    }

}
