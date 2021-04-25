package com.vmware.empinv.repositories;

import org.springframework.data.repository.CrudRepository;

import com.vmware.empinv.entities.Task;

public interface TaskRepository extends CrudRepository<Task, Long>{

}
