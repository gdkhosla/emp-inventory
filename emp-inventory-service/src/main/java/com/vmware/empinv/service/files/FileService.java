package com.vmware.empinv.service.files;

import com.vmware.empinv.enums.FileContentStatus;
import com.vmware.empinv.exceptions.ValidationException;
import com.vmware.empinv.service.models.FileMetadata;

public interface FileService {

	Long createFileContent(FileMetadata fileMetadata, Long taskId, FileContentStatus status) throws ValidationException;

	void updateFileContentStatus(Long id, FileContentStatus status) throws ValidationException;

}