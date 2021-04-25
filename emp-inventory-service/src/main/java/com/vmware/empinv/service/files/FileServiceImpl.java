package com.vmware.empinv.service.files;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vmware.empinv.entities.FileContent;
import com.vmware.empinv.enums.FileContentStatus;
import com.vmware.empinv.exceptions.ValidationException;
import com.vmware.empinv.repositories.FileRepository;
import com.vmware.empinv.service.models.FileMetadata;
import com.vmware.empinv.utils.CommonsUtil;

@Component
public class FileServiceImpl implements FileService {
	
	@Autowired
	private CommonsUtil commonsUtil;
	
	@Autowired
	private FileRepository fileRepository;
	
	@Override
	public Long createFileContent(FileMetadata fileMetadata, Long taskId, FileContentStatus status) throws ValidationException {
		if(fileMetadata == null) {
			throw new ValidationException("File metadata cannot be null");
		}
		FileContent fileContent = FileContent.builder()
				.filepath(commonsUtil.getContentServerFilePath(fileMetadata.getFileName()))
				.md5Checksum(fileMetadata.getExpectedMD5())
				.taskId(taskId)
				.uploadStatus(status)
				.originalFileName(fileMetadata.getOriginalFileName())
				.build();
		fileRepository.save(fileContent);
		return fileContent.getId();
	}
	
	@Override
	public void updateFileContentStatus(Long id, FileContentStatus status) throws ValidationException {
		Optional<FileContent> content = fileRepository.findById(id);
		FileContent record = content
				.orElseThrow(() -> new ValidationException("No file record available for id - " + id));
		record.setUploadStatus(status);
		fileRepository.save(record);
	}
	
}
