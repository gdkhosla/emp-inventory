package com.vmware.empinv.service.models;

import java.io.InputStream;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FileMetadata {
	private InputStream fileInputStream;
	private String fileName;
	private String expectedMD5;
	private String action;
	private String originalFileName;
}
