package com.vmware.empinv.service.employee;

import org.springframework.stereotype.Component;

import com.vmware.empinv.entities.Employee;

@Component
public class EmployeeConverter {
    
	public com.vmware.empinv.service.models.Employee toServiceModel(Employee employee) {
    	return com.vmware.empinv.service.models.Employee.builder()
    			.age(employee.getAge())
    			.name(employee.getName())
    			.empId(employee.getId())
    			.build();
    }
	
	public Employee toEntityModel(com.vmware.empinv.service.models.Employee employee) {
		return Employee.builder().age(employee.getAge())
				.name(employee.getName())
				.build();
	}
}
