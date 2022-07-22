package com.example.elasticsearch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

@EnableElasticsearchRepositories(basePackages = "com.example.elasticsearch.repository")
@ComponentScan(basePackages = { "com.example.elasticsearch" })
@SpringBootApplication
@Slf4j
public class ElasticsearchApplication {
	public static void main(String[] args) {
		SpringApplication.run(ElasticsearchApplication.class, args);
	}
}
