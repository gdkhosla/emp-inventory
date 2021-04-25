package com.vmware.empinv.service.employee;

import java.util.List;

import com.vmware.empinv.exceptions.DataNotFound;
import com.vmware.empinv.exceptions.ValidationException;
import com.vmware.empinv.service.models.Employee;
import com.vmware.empinv.service.models.Task;

public interface EmployeeService {

	void persistEmployeesBatch(List<Employee> employees, Task task) throws ValidationException;

	long persistEmployee(Employee emp) throws ValidationException;

	List<Employee> getEmployee(Long id) throws ValidationException, DataNotFound;

	void updateEmpById(Employee emp, Long id) throws ValidationException, DataNotFound;

	void deleteById(Long id) throws ValidationException, DataNotFound;

	void patchEmployee(Employee emp, Long id) throws ValidationException, DataNotFound;

}