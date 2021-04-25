package com.vmware.empinv.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CommonsUtil {
	
	@Value(value = "${empinv.contentserver.basedir:}")
	private String contentServerBaseDir;
	
   public String getContentServerFilePath(String filename) {
	   if(filename == null || filename.isEmpty()) {
		   throw new RuntimeException("File name cannot be null or empty");
	   }
	   
	   return contentServerBaseDir+File.separator+filename+"_"+Instant.now().getEpochSecond();
   }
   
   public String calculateMD5CheckSumMatches(String filePath, String expectedMD5) {
		if(StringUtils.isEmpty(filePath)) {
			throw new RuntimeException("File path cannot be null for check sum validation");
		}
		
		if(StringUtils.isEmpty(expectedMD5)) {
			throw new RuntimeException("Expected MD5 is not provided for checksum validation");
		}
		String md5 = null;
		try (InputStream is = Files.newInputStream(Paths.get(filePath))) {
		     md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
		} catch (IOException e) {
			throw new RuntimeException("Error occurred while checking file integrity");
		}
		return md5;
	}
}
