package com.vmware.empinv.client.dropbox;

import com.dropbox.core.DbxAuthInfo;
import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.NetworkIOException;
import com.dropbox.core.RetryException;
import com.dropbox.core.json.JsonReader;
import com.dropbox.core.util.IOUtil.ProgressListener;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.DbxPathV2;
import com.dropbox.core.v2.files.CommitInfo;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.UploadSessionCursor;
import com.dropbox.core.v2.files.UploadSessionFinishErrorException;
import com.dropbox.core.v2.files.UploadSessionLookupErrorException;
import com.dropbox.core.v2.files.WriteMode;
import com.vmware.empinv.exceptions.FileUploadException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * An example command-line application that runs through the web-based OAuth
 * flow (using {@link DbxWebAuth}).
 */

@Component
public class DropboxClient implements ContentServerClient {
    // Adjust the chunk size based on your network speed and reliability. Larger chunk sizes will
    // result in fewer network requests, which will be faster. But if an error occurs, the entire
    // chunk will be lost and have to be re-uploaded. Use a multiple of 4MiB for your chunk size.
    private static final long CHUNKED_UPLOAD_CHUNK_SIZE = 8L << 20; // 8MiB
    private static final int CHUNKED_UPLOAD_MAX_ATTEMPTS = 5;
    
    @Value(value = "${empinv.dropbox.authfile}")
    private String authFilePath;
    
    @Autowired
    private ApplicationContext context;

    private  void printProgress(long uploaded, long size) {
        System.out.printf("Uploaded %12d / %12d bytes (%5.2f%%)\n", uploaded, size, 100 * (uploaded / (double) size));
    }

    private  void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            // just exit
            System.err.println("Error uploading to Dropbox: interrupted during backoff.");
            System.exit(1);
        }
    }

    @Override
    public  void uploadFile(String localPath, String dropboxPath) throws IOException, FileUploadException {
        if(StringUtils.isEmpty(localPath)) {
        	throw new RuntimeException("Local path value for file to uploade cannot be null or empty");
        }
        
        if(StringUtils.isEmpty(dropboxPath)) {
        	throw new RuntimeException("Drop box file path value for file to uploade cannot be null or empty");
        }

        String pathError = DbxPathV2.findError(dropboxPath);
        if (pathError != null) {
        	throw new FileUploadException("Invalid <dropbox-path>: " + pathError);
        }

        File localFile = new File(localPath);
        if (!localFile.exists()) {
        	throw new FileUploadException("Invalid <local-path>: file does not exist.");
        }

        if (!localFile.isFile()) {
        	throw new FileUploadException("Invalid <local-path>: not a file.");
        }


        DbxClientV2 dbxClient = getDbxClient();
        // upload the file with simple upload API if it is small enough, otherwise use chunked
        // upload API for better performance. Arbitrarily chose 2 times our chunk size as the
        // deciding factor. This should really depend on your network.
        if (localFile.length() <= (2 * CHUNKED_UPLOAD_CHUNK_SIZE)) {
            uploadFile(dbxClient, localFile, dropboxPath);
        } else {
            chunkedUploadFile(dbxClient, localFile, dropboxPath);
        }
    }
    
    @Override
    public String readFileFromServer(DbxClientV2 dbxClient, String dropboxPath)
			throws DownloadErrorException, DbxException, IOException {
		DbxDownloader<FileMetadata> fileDownloader = dbxClient.files().download(dropboxPath);
		InputStream inputStream = fileDownloader.getInputStream();
		InputStreamReader isReader = new InputStreamReader(inputStream);
		//Creating a BufferedReader object
		BufferedReader reader = new BufferedReader(isReader);
		StringBuffer sb = new StringBuffer();
		String str;
		System.out.println("reading file uploaded");
		while((str = reader.readLine())!= null){
		   sb.append(str).append(System.lineSeparator());
		}
		System.out.println(sb.toString());
		return sb.toString();
	}
    
    private  DbxClientV2 getDbxClient() throws FileUploadException, IOException {
    	if(StringUtils.isEmpty(authFilePath)) {
    		throw new RuntimeException("auth file path for dropbox is empty or null.");
    	}
    	// Read auth info file.
        DbxAuthInfo authInfo;
        try {
        	String filePath = context.getResource(authFilePath).getFile().getAbsolutePath();
            authInfo = DbxAuthInfo.Reader.readFromFile(filePath);
        } catch (JsonReader.FileLoadException ex) {
            throw new IOException("Error loading drop box auth file: " + ex.getMessage());
        }

     // Create a DbxClientV2, which is what you use to make API calls.
        DbxRequestConfig requestConfig = new DbxRequestConfig("examples-upload-file");
        return new DbxClientV2(requestConfig, authInfo.getAccessToken(), authInfo.getHost());
    	
    }
    
    
    /**
     * Uploads a file in a single request. This approach is preferred for small files since it
     * eliminates unnecessary round-trips to the servers.
     *
     * @param dbxClient Dropbox user authenticated client
     * @param localFIle local file to upload
     * @param dropboxPath Where to upload the file to within Dropbox
     * @throws FileUploadException 
     */
    private void uploadFile(DbxClientV2 dbxClient, File localFile, String dropboxPath) throws FileUploadException {
        try (InputStream in = new FileInputStream(localFile)) {
            ProgressListener progressListener = l -> printProgress(l, localFile.length());

            FileMetadata metadata = dbxClient.files().uploadBuilder(dropboxPath)
                .withMode(WriteMode.ADD)
                .withClientModified(new Date(localFile.lastModified()))
                .uploadAndFinish(in, progressListener);
            System.out.println(metadata.toString());
            
        } catch (Exception ex) {
        	throw new FileUploadException("Error uploading to Dropbox: " + ex.getMessage());
        }
    }    

    /**
     * Uploads a file in chunks using multiple requests. This approach is preferred for larger files
     * since it allows for more efficient processing of the file contents on the server side and
     * also allows partial uploads to be retried (e.g. network connection problem will not cause you
     * to re-upload all the bytes).
     *
     * @param dbxClient Dropbox user authenticated client
     * @param localFIle local file to upload
     * @param dropboxPath Where to upload the file to within Dropbox
     * @throws FileUploadException 
     */
    private  void chunkedUploadFile(DbxClientV2 dbxClient, File localFile, String dropboxPath) throws FileUploadException {
        long size = localFile.length();

        // assert our file is at least the chunk upload size. We make this assumption in the code
        // below to simplify the logic.
        if (size < CHUNKED_UPLOAD_CHUNK_SIZE) {
            System.err.println("File too small, use upload() instead.");
            System.exit(1);
            return;
        }

        long uploaded = 0L;
        DbxException thrown = null;

        ProgressListener progressListener = new ProgressListener() {
            long uploadedBytes = 0;
            @Override
            public void onProgress(long l) {
                printProgress(l + uploadedBytes, size);
                if (l == CHUNKED_UPLOAD_CHUNK_SIZE) uploadedBytes += CHUNKED_UPLOAD_CHUNK_SIZE;
            }
        };

        // Chunked uploads have 3 phases, each of which can accept uploaded bytes:
        //
        //    (1)  Start: initiate the upload and get an upload session ID
        //    (2) Append: upload chunks of the file to append to our session
        //    (3) Finish: commit the upload and close the session
        //
        // We track how many bytes we uploaded to determine which phase we should be in.
        String sessionId = null;
        for (int i = 0; i < CHUNKED_UPLOAD_MAX_ATTEMPTS; ++i) {
            if (i > 0) {
                System.out.printf("Retrying chunked upload (%d / %d attempts)\n", i + 1, CHUNKED_UPLOAD_MAX_ATTEMPTS);
            }

            try (InputStream in = new FileInputStream(localFile)) {
                // if this is a retry, make sure seek to the correct offset
                in.skip(uploaded);

                // (1) Start
                if (sessionId == null) {
                    sessionId = dbxClient.files().uploadSessionStart()
                        .uploadAndFinish(in, CHUNKED_UPLOAD_CHUNK_SIZE, progressListener)
                        .getSessionId();
                    uploaded += CHUNKED_UPLOAD_CHUNK_SIZE;
                    printProgress(uploaded, size);
                }

                UploadSessionCursor cursor = new UploadSessionCursor(sessionId, uploaded);

                // (2) Append
                while ((size - uploaded) > CHUNKED_UPLOAD_CHUNK_SIZE) {
                    dbxClient.files().uploadSessionAppendV2(cursor)
                        .uploadAndFinish(in, CHUNKED_UPLOAD_CHUNK_SIZE, progressListener);
                    uploaded += CHUNKED_UPLOAD_CHUNK_SIZE;
                    printProgress(uploaded, size);
                    cursor = new UploadSessionCursor(sessionId, uploaded);
                }

                // (3) Finish
                long remaining = size - uploaded;
                CommitInfo commitInfo = CommitInfo.newBuilder(dropboxPath)
                    .withMode(WriteMode.ADD)
                    .withClientModified(new Date(localFile.lastModified()))
                    .build();
                FileMetadata metadata = dbxClient.files().uploadSessionFinish(cursor, commitInfo)
                    .uploadAndFinish(in, remaining, progressListener);

                System.out.println(metadata.toStringMultiline());
                return;
            } catch (RetryException ex) {
                thrown = ex;
                // RetryExceptions are never automatically retried by the client for uploads. Must
                // catch this exception even if DbxRequestConfig.getMaxRetries() > 0.
                sleepQuietly(ex.getBackoffMillis());
                continue;
            } catch (NetworkIOException ex) {
                thrown = ex;
                // network issue with Dropbox (maybe a timeout?) try again
                continue;
            } catch (UploadSessionLookupErrorException ex) {
                if (ex.errorValue.isIncorrectOffset()) {
                    thrown = ex;
                    // server offset into the stream doesn't match our offset (uploaded). Seek to
                    // the expected offset according to the server and try again.
                    uploaded = ex.errorValue
                        .getIncorrectOffsetValue()
                        .getCorrectOffset();
                    continue;
                } else {
                    // Some other error occurred, give up.
                    throw new FileUploadException("Error uploading to Dropbox: " + ex.getMessage());
                }
            } catch (UploadSessionFinishErrorException ex) {
                if (ex.errorValue.isLookupFailed() && ex.errorValue.getLookupFailedValue().isIncorrectOffset()) {
                    thrown = ex;
                    // server offset into the stream doesn't match our offset (uploaded). Seek to
                    // the expected offset according to the server and try again.
                    uploaded = ex.errorValue
                        .getLookupFailedValue()
                        .getIncorrectOffsetValue()
                        .getCorrectOffset();
                    continue;
                } else {
                    // some other error occurred, give up.
                	throw new FileUploadException("Error uploading to Dropbox: " + ex.getMessage());
                }
            } catch (DbxException ex) {
            	throw new FileUploadException("Error uploading to Dropbox: " + ex.getMessage());
            } catch (IOException ex) {
            	throw new FileUploadException("Error reading from file \"" + localFile + "\": " + ex.getMessage());
            }
        }

        // if we made it here, then we must have run out of attempts
        throw new FileUploadException("Maxed out upload attempts to Dropbox. Most recent error: " + thrown.getMessage());
    }
}