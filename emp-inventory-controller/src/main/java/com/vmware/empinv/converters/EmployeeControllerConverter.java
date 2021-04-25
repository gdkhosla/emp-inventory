package com.vmware.empinv.converters;

import org.springframework.stereotype.Component;

import com.vmware.empinv.exceptions.ValidationException;
import com.vmware.empinv.models.Employee;

@Component
public class EmployeeControllerConverter {

	public  Employee toRestModel(com.vmware.empinv.service.models.Employee employee) {
		if(employee == null)
			throw  new RuntimeException("Employee cannot be  null");
		
		return Employee.builder().age(employee.getAge())
				.name(employee.getName())
				.id(employee.getEmpId())
				.build();
		
	}
	
	public com.vmware.empinv.service.models.Employee toServiceModel(Employee emp){
		if(emp == null)
			throw  new RuntimeException("Employee cannot be  null");
		
		return com.vmware.empinv.service.models.Employee.builder()
				.age(emp.getAge())
				.name(emp.getName())
				.empId(emp.getId())
				.build();
	}
}
