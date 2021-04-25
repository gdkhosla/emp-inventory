package com.vmware.empinv.service.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import javax.naming.OperationNotSupportedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.vmware.empinv.enums.TaskStatus;
import com.vmware.empinv.exceptions.InventoryException;
import com.vmware.empinv.exceptions.ValidationException;
import com.vmware.empinv.service.models.FileMetadata;
import com.vmware.empinv.service.models.Task;
import com.vmware.empinv.service.task.TaskService;
import com.vmware.empinv.utils.CommonsUtil;

@Service
public class FileProcessorImpl implements FileProcessor {
	
	@Autowired 
	private TaskService taskService;
	
	@Autowired
	private ProcessEmpData processEmpData;
	
	@Autowired
	private CommonsUtil commonsUtil;
	
	@Override
	public Task uploadFile(InputStream fileInputStream, String fileName, String expectedMD5) throws ValidationException, InventoryException {
		if (fileInputStream == null) {
			throw new ValidationException("Input stream for file to upload cannot be null");
		}

		if (StringUtils.isEmpty(fileName)) {
			throw new ValidationException("File name of file to upload cannot be null or empty");
		}
		Task createdTask = null;
		try {
			File tempDir = Files.createTempDirectory("EmpInventory" + "_").toFile();
			String tempFilePath = tempDir.getPath() + File.separator + fileName;
			//Prepare a temperory file on local filesystem
			writeToFile(fileInputStream, tempFilePath);
			
			//Validate file checksum - md5
			if(expectedMD5 != null) {
				String calculatedChecksum = commonsUtil.calculateMD5CheckSumMatches(tempFilePath, expectedMD5);
				if(!expectedMD5.equals(calculatedChecksum)) {
					throw new ValidationException(String.format("Expected checksum - "
							+ "%s does not match with the calculated checksum - %s", 
							expectedMD5, calculatedChecksum));
				}
				
			}
			//Prepare the task and task ID
			createdTask = taskService.createTask(Task.builder()
					.description("Uploading employee data").status(TaskStatus.INPROGRESS).build());
			
			//Process this file data asynchronously and upload data to content server
			processEmpData.processData(tempFilePath, fileName, createdTask, expectedMD5);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return createdTask;
	}
	
	private void writeToFile(InputStream uploadedInputStream,
			String uploadedFileLocation) throws InventoryException {
		OutputStream out = null;
		try {
			out = new FileOutputStream(new File(uploadedFileLocation));
			int read = 0;
			byte[] bytes = new byte[1024];

			out = new FileOutputStream(new File(uploadedFileLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			throw new InventoryException("Error occurred while writing file - " + e.getMessage());
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					throw new InventoryException("Error occurred while writing file - " + e.getMessage());
				}
			}
		}

	}
	
	@Override
	public Task processFile(FileMetadata fileMetadata) throws ValidationException, InventoryException, OperationNotSupportedException {
		if(fileMetadata == null) {
			throw new ValidationException("File metadata cannot be null");
		}
		Task task = null;
		switch (fileMetadata.getAction()) {
		case "upload":
			task = uploadFile(fileMetadata.getFileInputStream(), fileMetadata.getFileName(),
					fileMetadata.getExpectedMD5());
			break;
		default:
			throw new OperationNotSupportedException(
					String.format("Action type %s is not supported for file processing", fileMetadata.getAction()));
		}
		return task;
	}
	
	
}
