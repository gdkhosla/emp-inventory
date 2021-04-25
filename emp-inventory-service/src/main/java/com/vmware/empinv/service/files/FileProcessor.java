package com.vmware.empinv.service.files;

import java.io.InputStream;

import javax.naming.OperationNotSupportedException;

import com.vmware.empinv.exceptions.InventoryException;
import com.vmware.empinv.exceptions.ValidationException;
import com.vmware.empinv.service.models.FileMetadata;
import com.vmware.empinv.service.models.Task;

public interface FileProcessor {

	public Task uploadFile(InputStream fileInputStream, String fileName, String expectedMD5) throws ValidationException, InventoryException;
	public Task processFile(FileMetadata fileMetadata) throws ValidationException, InventoryException, OperationNotSupportedException;
	
}