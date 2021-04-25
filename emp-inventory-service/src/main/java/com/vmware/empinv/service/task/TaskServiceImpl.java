package com.vmware.empinv.service.task;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vmware.empinv.exceptions.DataNotFound;
import com.vmware.empinv.exceptions.ValidationException;
import com.vmware.empinv.repositories.TaskRepository;
import com.vmware.empinv.service.models.Task;

@Service
public class TaskServiceImpl implements TaskService {
	
	@Autowired TaskRepository taskRepository;
	
	@Override
   public Task createTask(Task task) throws ValidationException {
		if(task == null) {
			throw new ValidationException("Task cannot be null");
		}
	   com.vmware.empinv.entities.Task  taskRecord = com.vmware.empinv.entities.Task
			   .builder().description(task.getDescription())
			   .status(task.getStatus()).build();
	   taskRepository.save(taskRecord);
	   return Task.builder()
			   .description(taskRecord.getDescription())
			   .queryUrl("v1/task/"+taskRecord.getId())
			   .status(taskRecord.getStatus())
			   .taskId(Long.toString(taskRecord.getId()))
			   .build();
   }
	
	@Override
   public void updateTask(Task task) throws ValidationException {
	   if(task == null) {
			throw new ValidationException("Task cannot be null");
		}
	   
	   if(task.getTaskId() == null || task.getTaskId().isEmpty()) {
		   throw new ValidationException("Task ID cannot be null or emptys");
	   }
	   com.vmware.empinv.entities.Task  taskRecord = com.vmware.empinv.entities.Task
			   .builder().description(task.getDescription())
			   .status(task.getStatus()).build();
	   taskRecord.setId(Long.parseLong(task.getTaskId()));
	   taskRepository.save(taskRecord);
   }
   
	@Override
   public Task getTask(Long taskId) throws ValidationException, DataNotFound {
	   if(taskId == null) {
		   throw new ValidationException("Task ID cannot be null");
	   }
	   Optional<com.vmware.empinv.entities.Task> taskOptional = taskRepository.findById(taskId);
	   com.vmware.empinv.entities.Task taskRecord = taskOptional.orElseThrow(() -> new DataNotFound(String.format("Task not found by Id",taskId)));
	   return Task.builder()
			   .description(taskRecord.getDescription())
			   .queryUrl("v1/task/"+taskRecord.getId())
			   .status(taskRecord.getStatus())
			   .taskId(Long.toString(taskRecord.getId()))
			   .build();
   }
}
