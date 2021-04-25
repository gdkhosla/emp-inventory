package com.vmware.empinv.repositories;

import org.springframework.data.repository.CrudRepository;

import com.vmware.empinv.entities.FileContent;

public interface FileRepository extends CrudRepository<FileContent, Long>{

}
