package com.vmware.empinv.service.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Employee {
   private Long empId;
   private String name;
   private Integer age;
}
