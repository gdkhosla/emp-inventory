package com.vmware.empinv.service.task;

import com.vmware.empinv.exceptions.DataNotFound;
import com.vmware.empinv.exceptions.ValidationException;
import com.vmware.empinv.service.models.Task;

public interface TaskService {

	Task createTask(Task task) throws ValidationException;

	void updateTask(Task task) throws ValidationException;

	Task getTask(Long taskId) throws ValidationException, DataNotFound;

	
}