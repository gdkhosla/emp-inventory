package com.vmware.empinv.controllers.files;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import com.sun.istack.NotNull;
import com.vmware.empinv.converters.EmployeeControllerConverter;
import com.vmware.empinv.exceptions.DataNotFound;
import com.vmware.empinv.exceptions.ValidationException;
import com.vmware.empinv.models.Employee;
import com.vmware.empinv.service.employee.EmployeeService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class EmployeeController {
	
	@Autowired
	private EmployeeControllerConverter employeeConverter;
	
	@Autowired
	private EmployeeService empService;
	
	
	@RequestMapping(
			value = {"/v1/employee/{empId}", "/v1/employee"},
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<List<Employee>> getEmployeeById(@PathVariable(name = "empId", required = false) Long empId) throws ValidationException, DataNotFound {
		log.debug("Getting employee by ID - {}", empId);
		List <com.vmware.empinv.service.models.Employee> employees = empService.getEmployee(empId);
		ResponseEntity<List<Employee>> response = null;
		if(employees == null || employees.isEmpty()) {
			response = new ResponseEntity<List<Employee>>(HttpStatus.NOT_FOUND);
		}else {
			List<Employee> restEmployees = new ArrayList<>();
			employees.forEach(emp -> restEmployees.add(employeeConverter.toRestModel(emp)));
			response = new ResponseEntity<List<Employee>>(restEmployees, HttpStatus.OK);
		}
		return response;
	}
	
	
	@RequestMapping(
			value = "/v1/employee/{empId}",
            method = RequestMethod.PUT)
	@ResponseBody
    @ResponseStatus(value = HttpStatus.ACCEPTED)
	public void updateEmployee(@RequestBody @Validated Employee employee, @PathVariable(name="empId") @NotNull Long empId) throws ValidationException, DataNotFound {
		log.debug("Updating employee detail for id {} with data {}", empId, employee.toString());
		empService.updateEmpById(employeeConverter.toServiceModel(employee), empId);
	}
	
	
	@RequestMapping(
			value = "/v1/employee/{empId}",
            method = RequestMethod.DELETE)
	@ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
	public void deleteEmployee(@PathVariable(name="empId") @NotNull Long empId) throws ValidationException, DataNotFound {
		log.debug("Deleting employee by ID - {}", empId);
		empService.deleteById(empId);
	}
	
	@RequestMapping(
			value = "/v1/employee",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<String> createEmployee(@RequestBody Employee employee) throws ValidationException, DataNotFound {
		log.debug("Creating employee record with data - {}", employee.toString());
		long empId = empService.persistEmployee(employeeConverter.toServiceModel(employee));
		UriComponents location =
                MvcUriComponentsBuilder.fromMethodCall(
                                MvcUriComponentsBuilder.on(EmployeeController.class)
                                        .getEmployeeById(empId))
                        .buildAndExpand(empId);
		HttpHeaders responseHeaders = new HttpHeaders();
	    responseHeaders.set("location", 
	    		location.getPath());
	    return ResponseEntity.ok().headers(responseHeaders).build();
	}

}
