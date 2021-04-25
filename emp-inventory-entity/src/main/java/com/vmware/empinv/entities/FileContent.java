package com.vmware.empinv.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import com.vmware.empinv.enums.FileContentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "file_content")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileContent extends SuperEntity{
	
	@Column(name="filepath")
    private String filepath;
	
	@Column(name="upload_status")
	@Enumerated
    private FileContentStatus uploadStatus;
	
	@Column(name="md5_checksum")
    private String md5Checksum;
	
	@Column(name="task_id")
    private long taskId;
	
	@Column(name="original_filename")
	private String originalFileName;
}
