package com.vmware.empinv.service.files;

import com.vmware.empinv.exceptions.InventoryException;
import com.vmware.empinv.exceptions.ValidationException;
import com.vmware.empinv.service.models.Task;

public interface ProcessEmpData {

	void processData(String filepath, String filename,  Task task, String md5) throws  InventoryException, ValidationException;

}