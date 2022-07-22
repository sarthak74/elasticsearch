package com.example.elasticsearch.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;


@Document(indexName = "companydatabase")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
    @Id
    @Generated
    @Field(name = "id")
    private String id;
    @Field(name = "firstName")
    private String firstName;
    @Field(name = "lastName")
    private String lastName;
    @Field(name = "designation")
    private String designation;
    @Field(name = "salary")
    private Integer salary;
    @Field(type=FieldType.Date, format={}, pattern="uuuu-MM-dd", name = "dateOfJoining")
    private LocalDate dateOfJoining;
    @Field(name = "address")
    private String address;
    @Field(name = "gender")
    private String gender;
    @Field(name = "maritalStatus")
    private String maritalStatus;
    @Field(name = "interests")
    private String interests;
    @Field(name = "age")
    private Integer age;

}
