package com.vmware.empinv.converters;

import org.springframework.stereotype.Component;

import com.vmware.empinv.exceptions.ValidationException;
import com.vmware.empinv.models.Task;

@Component
public class TaskConverter {
	
   public Task toRestModel(com.vmware.empinv.service.models.Task task) throws ValidationException {
	   if(task == null) {
		   throw new ValidationException("task cannot be null");
	   }
	   return Task.builder()
			   .queryUrl(task.getQueryUrl())
			   .status(task.getStatus())
			   .taskId(task.getTaskId())
			   .description(task.getDescription())
			   .build();
   }  
   
}
