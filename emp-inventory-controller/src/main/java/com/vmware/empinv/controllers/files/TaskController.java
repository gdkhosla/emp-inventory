package com.vmware.empinv.controllers.files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vmware.empinv.converters.TaskConverter;
import com.vmware.empinv.exceptions.DataNotFound;
import com.vmware.empinv.exceptions.ValidationException;
import com.vmware.empinv.models.Task;
import com.vmware.empinv.service.task.TaskService;

@RestController
public class TaskController {
	
	@Autowired
	private TaskService taskService;
	
	@Autowired
	private TaskConverter taskConverter;
   
	@RequestMapping(
			value = "/v1/task/{taskId}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<Task> getTask(@PathVariable(value = "taskId") Long taskId) throws ValidationException, DataNotFound {
		com.vmware.empinv.service.models.Task task = taskService.getTask(taskId);
		ResponseEntity<Task> response = null;
		if(task == null) {
			response = new ResponseEntity<Task>(HttpStatus.NOT_FOUND);
		}else {
			response = new ResponseEntity<Task>(taskConverter.toRestModel(task), HttpStatus.OK);
		}
		return response;
	}
}
