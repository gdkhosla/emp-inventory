package com.vmware.empinv.repositories;

import org.springframework.data.repository.CrudRepository;

import com.vmware.empinv.entities.Employee;

public interface EmployeeRepository extends CrudRepository<Employee, Long>{

}
