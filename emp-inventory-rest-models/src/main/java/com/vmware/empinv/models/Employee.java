package com.vmware.empinv.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Employee {
    private String name;
    private Integer age;
    private Long id;
}
