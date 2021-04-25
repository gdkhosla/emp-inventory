package com.vmware.empinv.client.dropbox;

import java.io.IOException;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.vmware.empinv.exceptions.FileUploadException;

public interface ContentServerClient {

	void uploadFile(String localPath, String dropboxPath) throws IOException, FileUploadException;

	String readFileFromServer(DbxClientV2 dbxClient, String dropboxPath)
			throws DownloadErrorException, DbxException, IOException;

}