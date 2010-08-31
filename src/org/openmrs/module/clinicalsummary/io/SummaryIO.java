/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.clinicalsummary.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.Cipher;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.SummaryConstants;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 *
 */
public abstract class SummaryIO implements Runnable {
	
	protected static final Log log = LogFactory.getLog(SummaryIO.class);
	
	public static final String DOWNLOAD = "Download";
	
	public static final String UPLOAD = "Upload";
	
	protected static final String ZIP_EXTENSION = ".zip";
	
	// crypto params
	protected static final String SECRET_KEY_FACTORY = "PBKDF2WithHmacSHA1";
	
	protected static final String CIPHER_CONFIGURATION = "AES/CBC/PKCS5Padding";
	
	protected static final String KEY_SPEC = "AES";
	
	protected static final int BUFFER_SIZE = 4096;
	
	protected String password;
	
	protected String passphrase;
	
	protected Cipher cipher;
	
	// index file params
	protected String databaseUsername;
	
	protected String databasePassword;
	
	protected String databaseName;
	
	protected static ExecutorService executorService = Executors.newCachedThreadPool();
	
	// base filename that will be processed
	protected static String filename;
	
	// only one process can run at a time
	// either download or upload
	protected static String type;
	
	protected static SummaryIOStatus status = SummaryIOStatus.IDLE;
	
	protected static long processed;
	
	protected static long total;
	
	protected static SummaryIO instance;
	
	public SummaryIO(String password, String passphrase, String filename) throws GeneralSecurityException {
		initialize(password, passphrase, filename);
		prepareDatabaseProperties();
	}
	
	private void initialize(String password, String passphrase, String filename) {
		this.password = password;
		this.passphrase = passphrase;
		
		SummaryIO.filename = filename;
		SummaryIO.type = StringUtils.EMPTY;
		SummaryIO.status = SummaryIOStatus.IDLE;
		SummaryIO.processed = 0;
		SummaryIO.total = 0;
	}
	
	/**
	 * @param password
	 * @param passphrase
	 * @param filename
	 */
	public static void startDownloadTask(String password, String passphrase, String filename) {
		SummaryDownloadTask.startTask(password, passphrase, filename);
	}
	
	/**
	 * @param password
	 * @param passphrase
	 * @param filename
	 */
	public static void startUploadTask(String password, String passphrase, String filename) {
		SummaryUploadTask.startTask(password, passphrase, filename);
	}
	
	/**
	 */
	public static void cancelTask() {
		if (executorService != null)
			executorService.shutdown();
		status = SummaryIOStatus.CANCELLED;
	}
	
	public static void resetTask() {
		cleanupResources();
		if (executorService != null)
			executorService.shutdown();
		status = SummaryIOStatus.IDLE;
	}
	
	/**
	 */
	private static void cleanupResources() {
		// clean the file system
		File zippedPath = OpenmrsUtil.getDirectoryInApplicationDataDirectory(SummaryConstants.ZIPPED_LOCATION);
		// clean up old upload and download
		for (File file : zippedPath.listFiles())
			if (!file.getName().startsWith(filename))
				file.delete();
		
		File initVectorPath = OpenmrsUtil.getDirectoryInApplicationDataDirectory(SummaryConstants.ENCRYPTION_LOCATION);
		for (File file : initVectorPath.listFiles())
			if (!file.getName().startsWith(filename))
				file.delete();
	}
	
	/**
	 * @return
	 */
	public static boolean isRunning() {
		return (status == SummaryIOStatus.RUNNING);
	}
	
	/**
	 * @return
	 */
	public static boolean isFinished() {
		return (status == SummaryIOStatus.FINISHED);
	}
	
	/**
	 * @return
	 */
	public static String getExecutionStatus() {
		// this is meant to give some feedback status to the users
		String executionStatus = status.toString();
		if (!StringUtils.isBlank(type) && !SummaryIOStatus.IDLE.equals(status))
			executionStatus = executionStatus + " (" + type + ")";
		return executionStatus;
	}
	
	/**
	 * @return
	 */
	public static String getFilename() {
		return filename;
	}
	
	/**
	 * @return
	 */
	public static long getTotal() {
		return total;
	}
	
	/**
	 * @return
	 */
	public static long getProcessed() {
		return processed;
	}
	
	/**
	 */
	protected abstract void processIndex() throws IOException, InterruptedException;
	
	/**
	 * @throws IOException
	 */
	protected abstract void processSummaries() throws IOException;
	
	/**
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	protected abstract void processInitVector() throws IOException, GeneralSecurityException;
	
	/**
	 * @throws GeneralSecurityException
	 */
	protected abstract void initializeCipher() throws GeneralSecurityException;
	
	/**
	 */
	private void prepareDatabaseProperties() {
		Properties props = Context.getRuntimeProperties();
		
		databaseUsername = props.getProperty("database.username");
		if (StringUtils.isBlank(databaseUsername))
			databaseUsername = props.getProperty("connection.username", "test");
		
		databasePassword = props.getProperty("database.password");
		if (StringUtils.isBlank(databasePassword))
			databasePassword = props.getProperty("connection.password", "test");
		
		databaseName = "openmrs";
		String connectionString = props.getProperty("connection.url");
		if (!StringUtils.isBlank(connectionString)) {
			connectionString = props.getProperty("connection.url");
			int questionMark = connectionString.lastIndexOf("?");
			int slash = connectionString.lastIndexOf("/");
			databaseName = connectionString.substring(slash + 1, questionMark);
		}
	}
	
	/**
	 * @param commands
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected void executeCommand(File workingDirectory, String[] commands) throws IOException, InterruptedException {
		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		if (OpenmrsConstants.UNIX_BASED_OPERATING_SYSTEM)
			process = runtime.exec(commands, null, workingDirectory);
		else
			process = runtime.exec(commands);
		
		StreamHandler errorHandler = new StreamHandler(process.getErrorStream(), "ERROR");
		StreamHandler outputHandler = new StreamHandler(process.getInputStream(), "OUTPUT");
		
		if (executorService == null)
			executorService = Executors.newCachedThreadPool();
		
		executorService.execute(errorHandler);
		executorService.execute(outputHandler);
		
		int exitValue = process.waitFor();
		log.info("Process execution completed with exit value: " + exitValue + " ...");
	}
	
	/**
	 * 
	 */
	class StreamHandler implements Runnable {
		
		private final InputStream stream;
		
		private final String source;
		
		public StreamHandler(InputStream stream, String source) {
			this.stream = stream;
			this.source = source;
		}
		
		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				String line = null;
				while ((line = reader.readLine()) != null)
					log.info(source + ": " + line);
				reader.close();
			}
			catch (IOException e) {
				log.error("Handling stream from runtime exec failed ...", e);
			}
		}
	}
}
