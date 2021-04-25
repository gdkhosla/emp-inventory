package com.vmware.empinv.service.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.vmware.empinv.client.dropbox.ContentServerClient;
import com.vmware.empinv.enums.FileContentStatus;
import com.vmware.empinv.enums.TaskStatus;
import com.vmware.empinv.exceptions.FileUploadException;
import com.vmware.empinv.exceptions.InventoryException;
import com.vmware.empinv.exceptions.ValidationException;
import com.vmware.empinv.service.employee.EmployeeService;
import com.vmware.empinv.service.models.Employee;
import com.vmware.empinv.service.models.FileMetadata;
import com.vmware.empinv.service.models.Task;
import com.vmware.empinv.service.task.TaskService;
import com.vmware.empinv.utils.CommonsUtil;

@EnableAsync
@Component
public class AsyncProcessEmpDataImpl implements ProcessEmpData {

	@Autowired
	private ContentServerClient contentServerClient;

	@Autowired
	private TaskService taskService;
	
	@Autowired
	private FileService fileService;
	
	@Autowired
	private CommonsUtil commonsUtil;
	
	@Autowired
	private EmployeeService employeeService;

	@Async
	@Override
	public void processData(String filepath, String filename, Task task, String md5) throws InventoryException, ValidationException {
		if (StringUtils.isEmpty(filepath)) {
			throw new ValidationException("File path cannot be null");
		}
		try {
			persistEmpRecord(filepath, task);
			uploadFileToContentServer(filepath, filename, md5, task);
		} catch (Exception e) {
			throw new InventoryException("An error occurred during file processing");
		}
	}

	private void persistEmpRecord(String filepath, Task task) throws InventoryException, ValidationException {
		try (Stream<String> stream = Files.lines(Paths.get(filepath))) {
			List<Employee> employees = new ArrayList<>();
			stream.forEach( emp-> {
				String[] empTouple = emp.split(" ");
				Employee employee = Employee.builder().age(Integer.parseInt(empTouple[1])).name(empTouple[0]).build();
				employees.add(employee);
			});
			employeeService.persistEmployeesBatch(employees, task);
			task.setStatus(TaskStatus.FILEPROCESSED);
			taskService.updateTask(task);
		} catch (IOException e) {
			throw new InventoryException("Error occurred while processing  emp data upload -" + e.getMessage());
		}
	}

	private void uploadFileToContentServer(String filepath, String filename, String checksum, Task task)
			throws ValidationException, InventoryException {
		if (StringUtils.isEmpty(filepath)) {
			throw new ValidationException("File path cannot be null");
		}
		if (StringUtils.isEmpty(filename)) {
			throw new ValidationException("File name cannot be null");
		}
		Long contentId = null;
		try {
			String filePathToUpload = commonsUtil.getContentServerFilePath(filename);
			contentId = fileService.createFileContent(
					FileMetadata.builder()
					.action("Uploading build emp data")
					.expectedMD5(checksum)
					.fileName(filename)
					.originalFileName(filename)
					.build(), 
					Long.parseLong(task.getTaskId()), 
					FileContentStatus.UPLOADING);
			contentServerClient.uploadFile(filepath, filePathToUpload);
			fileService.updateFileContentStatus(contentId, FileContentStatus.UPLOADED);
		} catch (IOException | FileUploadException e) {
			fileService.updateFileContentStatus(contentId, FileContentStatus.ARCHIVING_FAILED);
			task.setStatus(TaskStatus.ARCHIVINGFAILED);
			taskService.updateTask(task);
			throw new InventoryException(e.getMessage());
		}
	}
}
