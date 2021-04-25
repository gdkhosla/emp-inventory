package com.vmware.empinv.service.employee;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.vmware.empinv.exceptions.DataNotFound;
import com.vmware.empinv.exceptions.ValidationException;
import com.vmware.empinv.repositories.EmployeeRepository;
import com.vmware.empinv.service.models.Employee;
import com.vmware.empinv.service.models.Task;

@Service
public class EmployeeServiceImpl implements EmployeeService {
	
	@Value(value="${empinv.emp.persist.batchSize:50}")
	private int empPersistBatchSize;
	
	@Autowired
	private EmployeeRepository empRepository;
	
	@Autowired
	private EmployeeConverter employeeConverter;
	
    @Override
	public void persistEmployeesBatch(List<Employee> employees, Task task) throws ValidationException{
    	if(employees == null || employees.isEmpty()) {
    		throw new ValidationException("Employees list cannot be null or empty");
    	}
    	List<List<Employee>> batchedRecords = ListUtils.partition(employees, empPersistBatchSize);
    	Long taskId = task == null ? null: Long.parseLong(task.getTaskId());
    	for(List<Employee> emps : batchedRecords) {
    		List<com.vmware.empinv.entities.Employee> empEntities = new ArrayList<>();
    		
    		for(Employee emp : emps) {
    			com.vmware.empinv.entities.Employee empEntity = employeeConverter.toEntityModel(emp);
    			empEntity.setTaskId(taskId);
    			empEntities.add(empEntity);
    		}
    		empRepository.saveAll(empEntities);
    	}
    }
    
    
    @Override
	public long persistEmployee(Employee emp) throws ValidationException {
    	if(emp == null) {
    		throw new  ValidationException("Employees cannot be null");
    	}
    	com.vmware.empinv.entities.Employee empEntity = employeeConverter.toEntityModel(emp);
    	empRepository.save(empEntity);
    	return empEntity.getId();
    }
    
    
    @Override
	public List<Employee> getEmployee(Long id) throws ValidationException, DataNotFound {
    	List<Employee> empRecs = new ArrayList<>();
    	if(id != null) {
    		Optional<com.vmware.empinv.entities.Employee> empOptional = empRepository.findById(id);
        	com.vmware.empinv.entities.Employee empRec = empOptional.orElseThrow(() -> new DataNotFound("Employee not found for ID - "+id));
        	empRecs.add(employeeConverter.toServiceModel(empRec));
    	}else {
    		Iterable<com.vmware.empinv.entities.Employee> allEmployees = empRepository.findAll();
			if (null != allEmployees) {
				allEmployees.forEach(emp -> empRecs.add(employeeConverter.toServiceModel(emp)));
			}
    	}
    	return empRecs;
    }
    
    @Override
	public void updateEmpById( Employee emp, Long empId) throws ValidationException, DataNotFound {
    	if(emp == null) {
    		throw new  ValidationException("Employee data cannot be null");
    	}
    	if(empId == null) {
    		throw new  ValidationException("Employee ID must be provided");
    	}
    	Optional<com.vmware.empinv.entities.Employee> empOptional = empRepository.findById(empId);
    	com.vmware.empinv.entities.Employee empRec = empOptional
    			.orElseThrow(() -> new DataNotFound("Employee not found for ID - "+empId));
    	empRec.setAge(emp.getAge());
    	empRec.setName(emp.getName());
    	empRepository.save(empRec);
    }
    
    @Override
	public void deleteById(Long id) throws ValidationException, DataNotFound {
    	if(id == null) {
    		throw new  ValidationException("Employee ID must be provided");	
    	}
    	Optional<com.vmware.empinv.entities.Employee> empOptional = empRepository.findById(id);
    	empOptional.orElseThrow(() -> new DataNotFound("Employee not found for ID - "+id));
    	empRepository.deleteById(id);
    }
    
    @Override
	public void patchEmployee(Employee emp, Long id) throws ValidationException, DataNotFound {
    	if(emp == null) {
    		throw new  ValidationException("Employee data cannot be null");
    	}
    	if(emp.getEmpId() == null) {
    		throw new  ValidationException("Employee ID must be provided");
    	}
    	Optional<com.vmware.empinv.entities.Employee> empOptional = empRepository.findById(id);
    	com.vmware.empinv.entities.Employee empRec = empOptional
    			.orElseThrow(() -> new DataNotFound("Employee not found for ID - "+id));
    	if(emp.getAge() != null) {
    		empRec.setAge(emp.getAge());
    	}
    	if(emp.getName() != null && !emp.getName().isEmpty()) {
    		empRec.setName(emp.getName());
    	}
    	empRepository.save(empRec);
    }
}
