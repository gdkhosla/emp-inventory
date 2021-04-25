package com.vmware.empinv.controllers.files;

import java.io.IOException;

import javax.naming.OperationNotSupportedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vmware.empinv.converters.TaskConverter;
import com.vmware.empinv.exceptions.InventoryException;
import com.vmware.empinv.exceptions.ValidationException;
import com.vmware.empinv.models.Task;
import com.vmware.empinv.service.files.FileProcessor;
import com.vmware.empinv.service.models.FileMetadata;

@RestController
public class FileController {
	
	@Autowired
	private FileProcessor fileService;
	
	@Autowired
	private TaskConverter taskConverter;
	
	@RequestMapping(
			value = "/api/employee",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = {"multipart/form-data"})
	@ResponseBody
    @ResponseStatus(value = HttpStatus.CREATED)
	public ResponseEntity<Task> uploadEmpBulkData(@RequestParam("file") MultipartFile file,
			@RequestHeader(name = "MD5-Checksum", required = false) String md5Checksum,
			@RequestParam(name = "action", required = false) String action) throws IOException, 
	        ValidationException, 
	        InventoryException, 
	        OperationNotSupportedException {
		FileMetadata fileMetadata = FileMetadata.builder()
				.action(action).expectedMD5(md5Checksum)
				.fileInputStream(file.getInputStream())
				.fileName(file.getOriginalFilename())
				.build();
		Task task =  taskConverter
				.toRestModel(
						fileService.processFile(fileMetadata));
		return new ResponseEntity<Task>(task, HttpStatus.CREATED);
	}    

}
