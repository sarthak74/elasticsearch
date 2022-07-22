package com.example.elasticsearch.service;

import com.example.elasticsearch.dto.*;
import com.example.elasticsearch.entity.Employee;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.stereotype.Service;
import com.example.elasticsearch.constant.Constants;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;
import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class SearchService {

    private String INDEX_NAME = Constants.employeeIndex;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Set<String> employeeFields = new HashSet<String>(Arrays.asList(
            new String[] {"firstName", "lastName", "dateOfJoining", "salary", "age", "interests", "designation", "gender", "maritalStatus"}
    ));

    private String getUUID() {
        return UUID.randomUUID().toString();
    }

    private Map<String, Object> convertProductToMap(Employee employee) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(employee);
        return objectMapper.readValue(json, Map.class);
    }

    @Autowired
    RestHighLevelClient restHighLevelClient;

    public RangeQueryBuilder getRangeQueryBuilder(FilterRequestDto filter, String keyToFilter){
        RangeFilterDto valueToFilter = filter.getRange().get(keyToFilter);

        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(keyToFilter);

        if (valueToFilter.getLte() != null) {
            rangeQueryBuilder.lte(Integer.parseInt(valueToFilter.getLte().toString()));
        }

        if (valueToFilter.getLt() != null) {
            rangeQueryBuilder.lt(Integer.parseInt(valueToFilter.getLt().toString()));
        }

        if (valueToFilter.getGt() != null) {
            rangeQueryBuilder.gt(Integer.parseInt(valueToFilter.getGt().toString()));
        }

        if (valueToFilter.getGte() != null) {
            rangeQueryBuilder.gte(Integer.parseInt(valueToFilter.getGte().toString()));
        }

        return rangeQueryBuilder;
    }

    public RangeQueryBuilder getDateRangeQueryBuilder(RangeFilterDto valueToFilter){
        // valueToFilter = hashMap of range of lt, gt, lte and gte
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("dateOfJoining");
        rangeQueryBuilder.format("uuuu-MM-dd");
        if (valueToFilter.getLte() != null) {
            rangeQueryBuilder.lte(LocalDate.parse(valueToFilter.getLte().toString()));
        }

        if (valueToFilter.getLt() != null) {
            rangeQueryBuilder.lt(LocalDate.parse(valueToFilter.getLt().toString()));
        }

        if (valueToFilter.getGt() != null) {
            rangeQueryBuilder.gt(LocalDate.parse(valueToFilter.getGt().toString()));
        }

        if (valueToFilter.getGte() != null) {
            rangeQueryBuilder.gte(LocalDate.parse(valueToFilter.getGte().toString()));
        }

        return rangeQueryBuilder;
    }

    public List<Employee> getEmployeesFromHits(SearchHit[] hits) throws JsonProcessingException, IOException {
        List<Employee> employees = new ArrayList<>();
        log.info("get from hits");
        for(SearchHit hit: hits) {
            String id = hit.getId();
            Employee employee = objectMapper.readValue(hit.getSourceAsString(), Employee.class);
            employee.setId(hit.getId());
            employees.add(employee);
        }
        return employees;
    }

    public Boolean save(Employee employee) throws IOException {
        IndexRequest indexRequest = Requests.indexRequest(INDEX_NAME)
                .id(employee.getId());
        indexRequest.source(objectMapper.writeValueAsString(employee), XContentType.JSON);

        IndexResponse response = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        return response!=null && response.status().equals(RestStatus.CREATED);
    }


    public Boolean saveAll(List<Employee> employees) throws IOException {

        BulkRequest bulkRequest = Requests.bulkRequest();
        employees.forEach(employee -> {
            try {
                IndexRequest indexRequest = Requests
                        .indexRequest(INDEX_NAME);
                indexRequest.source(objectMapper.writeValueAsString(employee), XContentType.JSON);
                bulkRequest.add(indexRequest);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                log.error("Exception at SearchService saveAll: " + e);
            }
        });

        RequestOptions options = RequestOptions.DEFAULT;
        BulkResponse response = restHighLevelClient.bulk(bulkRequest, options);
        return response!=null && response.status().equals(RestStatus.CREATED);
    }

    public SearchHit[] findAll(Integer limit) throws IOException {
        SearchRequest searchRequest = Requests.searchRequest(Constants.employeeIndex);
        QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
        SearchSourceBuilder searchSourceBuilder;
        if(limit!=null) {
            searchSourceBuilder = SearchSourceBuilder.searchSource()
                    .size(limit)
                    .query(queryBuilder);
        } else {
            searchSourceBuilder = SearchSourceBuilder.searchSource()
                    .query(queryBuilder);
        }
        searchRequest.source(searchSourceBuilder);
        return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT).getHits().getHits();
    }

    public SearchHit[] searchText(String query) throws IOException {
        SearchRequest searchRequest = Requests.searchRequest(Constants.employeeIndex);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for(String key: employeeFields) {
            if(key == "dateOfJoining" || key == "salary" || key == "age") continue;
            boolQueryBuilder.should(QueryBuilders.matchQuery(key, query));
        }
        SearchSourceBuilder searchSourceBuilder = SearchSourceBuilder.searchSource()
                .query(boolQueryBuilder);

        searchRequest.source(searchSourceBuilder);
        SearchHit[] searchHits = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT).getHits().getHits();
        return searchHits;
    }

    public BoolQueryBuilder filterTheQuery(FilterRequestDto filterRequestDto, BoolQueryBuilder boolQueryBuilder) {
        if (filterRequestDto != null) {
            FilterRequestDto filter = filterRequestDto;
            if (filter.getRange() != null) {
                for (String keyToFilter : filter.getRange().keySet()) {
                    if(employeeFields.contains(keyToFilter) == false) continue;
                    if(keyToFilter == "dateOfJoining") {
                        boolQueryBuilder.filter(getDateRangeQueryBuilder(filter.getRange().get("dateOfJoining")));
                    } else {
                        boolQueryBuilder.filter(getRangeQueryBuilder(filter, keyToFilter));
                    }
                }

            }
            if (filter.getMatch() != null) {
                for (String keyToFilter : filter.getMatch().keySet()) {
                    if(employeeFields.contains(keyToFilter) == false) continue;
                    Object valueToFilter = filter.getMatch().get(keyToFilter).toString().toLowerCase();
                    boolQueryBuilder.filter(QueryBuilders.matchQuery(keyToFilter, valueToFilter));
                }
            }
        }
        return boolQueryBuilder;
    }

    public SearchHit[] search(SearchQueryDto searchQuery, String sortBy, Integer sortOrder) throws IOException {
        SearchRequest searchRequest = Requests.searchRequest(Constants.employeeIndex);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if(searchQuery.getQuery() != "" && searchQuery.getQuery() != null){
            for(String key: employeeFields) {
                if(key == "dateOfJoining" || key == "salary" || key == "age") continue;
                boolQueryBuilder.should(QueryBuilders.matchQuery(key, searchQuery.getQuery()));
            }
        }

        boolQueryBuilder = filterTheQuery(searchQuery.getFilter(), boolQueryBuilder);
        SearchSourceBuilder searchSourceBuilder = SearchSourceBuilder.searchSource()
                .query(boolQueryBuilder);

        if(searchQuery.getPage() != null) {
            searchSourceBuilder.from(searchQuery.getPage() * searchQuery.getSize())
                    .size(searchQuery.getSize());
        } else {
            if (searchQuery.getSize() != null) {
                searchSourceBuilder.size(searchQuery.getSize());
            }
        }

        if(sortBy != null) {
            searchSourceBuilder = searchSourceBuilder
                    .sort(sortBy, (sortOrder == null || sortOrder == 0) ? SortOrder.ASC : SortOrder.DESC);
        }
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] searchHits = response
                .getHits().getHits();
        return searchHits;
    }



    public SearchResponseWithStatsDto searchWithStats(SearchQueryDto searchQuery,
                                                      String sortBy,
                                                      Integer sortOrder,
                                                      String statsKey
    ) throws IOException {

        SearchRequest searchRequest = Requests.searchRequest(Constants.employeeIndex);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if(searchQuery.getQuery() != "" && searchQuery.getQuery() != null){
            for(String key: employeeFields) {
                if(key == "dateOfJoining" || key == "salary" || key == "age") continue;
                boolQueryBuilder.should(QueryBuilders.matchQuery(key, searchQuery.getQuery()));
            }

        }
        boolQueryBuilder = filterTheQuery(searchQuery.getFilter(), boolQueryBuilder);
        SearchSourceBuilder searchSourceBuilder = SearchSourceBuilder.searchSource()
                .query(boolQueryBuilder);

        if(searchQuery.getPage() != null) {
            searchSourceBuilder.from(searchQuery.getPage() * searchQuery.getSize());
            searchSourceBuilder.size(searchQuery.getSize());
        } else {
            if (searchQuery.getSize() != null) {
                searchSourceBuilder.size(searchQuery.getSize());
            }
        }

        if(sortBy != null) {
            searchSourceBuilder = searchSourceBuilder
                    .sort(sortBy, (sortOrder == null || sortOrder == 0) ? SortOrder.ASC : SortOrder.DESC);
        }

        searchSourceBuilder = setAggregations(searchSourceBuilder, statsKey);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] searchHits = response.getHits().getHits();
        SearchResponseWithStatsDto searchResponse = new SearchResponseWithStatsDto();
        List<Employee> allEmployees = getEmployeesFromHits(searchHits);
        searchResponse.setEmployees(allEmployees);
        StatsDto stats = getStatsFromSearchResponse(response);
        stats.setDisplayCount(allEmployees.size());
        searchResponse.setStats(stats);
        return searchResponse;
    }

    public SearchSourceBuilder setAggregations(SearchSourceBuilder searchSourceBuilder, String statsKey){
        searchSourceBuilder.aggregation(AggregationBuilders.sum("sum").field(statsKey));
        searchSourceBuilder.aggregation(AggregationBuilders.min("min").field(statsKey));
        searchSourceBuilder.aggregation(AggregationBuilders.max("max").field(statsKey));
        searchSourceBuilder.aggregation(AggregationBuilders.avg("avg").field(statsKey));
        return searchSourceBuilder;
    }

    public StatsDto getStatsFromSearchResponse(SearchResponse response) {
        StatsDto statsDto = new StatsDto();
        Sum sum = response.getAggregations().get("sum");
        statsDto.setSum(sum.getValue());
        Avg avg = response.getAggregations().get("avg");
        statsDto.setAvg(avg.getValue());
        Min min = response.getAggregations().get("min");
        statsDto.setMin(min.getValue());
        Max max = response.getAggregations().get("max");
        statsDto.setMax(max.getValue());

        statsDto.setCount(response.getHits().getTotalHits().toString());
        return statsDto;
    }
}