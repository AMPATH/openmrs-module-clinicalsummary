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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.Constants;
import org.openmrs.module.clinicalsummary.io.utils.TaskStatus;
import org.openmrs.util.OpenmrsUtil;

import javax.crypto.Cipher;
import java.io.File;
import java.util.Properties;

/**
 *
 */
abstract class SummariesTask implements Runnable {

	private static final Log log = LogFactory.getLog(SummariesTask.class);

	// index file params
	protected String databaseUser;

	protected String databasePassword;

	protected String databaseName;

	protected String databaseHost;

	protected String databasePort;

	protected String processedFilename;

	protected Cipher cipher;

	protected final String password;

	protected final String passphrase;

	protected String filename;

	private TaskStatus status;

	public SummariesTask(final String password, final String passphrase, final String filename) {
		this.password = password;
		this.passphrase = passphrase;
		this.filename = filename;
		this.status = TaskStatus.TASK_IDLE;
	}

	/**
	 * @return the status
	 */
	public TaskStatus getStatus() {
		return status;
	}

	/**
	 * @param status
	 * 		the status
	 */
	public void setStatus(final TaskStatus status) {
		this.status = status;
	}

	/**
	 * @return the processed filename
	 */
	public String getProcessedFilename() {
		return processedFilename;
	}

	/**
	 * @param processedFilename
	 * 		the processed filename
	 */
	public void setProcessedFilename(final String processedFilename) {
		this.processedFilename = processedFilename;
	}

	/**
	 * @return
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename
	 */
	public void setFilename(final String filename) {
		this.filename = filename;
	}

	/**
	 * Method that will be called to process the summary collection file. The main process are preparing the cipher object and then perform either the
	 * ciphering or deciphering the summary collection files.
	 *
	 * @throws Exception
	 */
	protected abstract void process() throws Exception;

	/**
	 * Cleanup old files from the upload and download process
	 */
	private void cleanupResources() {
		// clean the file system
		File zipPath = OpenmrsUtil.getDirectoryInApplicationDataDirectory(Constants.ZIPPED_LOCATION);
		// clean up old upload and download
		for (File file : zipPath.listFiles())
			if (!file.getName().startsWith(filename))
				file.delete();

		File secretPath = OpenmrsUtil.getDirectoryInApplicationDataDirectory(Constants.ENCRYPTION_LOCATION);
		for (File file : secretPath.listFiles())
			if (!file.getName().startsWith(filename))
				file.delete();
	}


	/**
	 * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread causes the object's <code>run</code>
	 * method to be called in that separately executing thread.
	 * <p/>
	 * The general contract of the method <code>run</code> is that it may take any action whatsoever.
	 *
	 * @see Thread#run()
	 */
	public void run() {
		try {
			process();
			cleanupResources();
			setStatus(TaskStatus.TASK_IDLE);
		} catch (Exception e) {
			log.error("Task execution failed. Last task status was: " + getStatus().getValue(), e);
			setStatus(TaskStatus.TASK_FAILED);
		}
	}

	/**
	 * Method to extract the database properties used to run OpenMRS. This will be used to extract the index table along some of the necessary tables.
	 */
	protected void prepareDatabaseProperties() {
		Properties props = Context.getRuntimeProperties();

		databaseUser = props.getProperty("database.username");
		if (StringUtils.isBlank(databaseUser))

			databaseUser = props.getProperty("connection.username", "test");

		databasePassword = props.getProperty("database.password");
		if (StringUtils.isBlank(databasePassword))
			databasePassword = props.getProperty("connection.password", "test");

		databaseName = "openmrs";
		databaseHost = "localhost";
		databasePort = "3306";
		String connectionString = props.getProperty("connection.url");
		if (!StringUtils.isBlank(connectionString)) {
			connectionString = props.getProperty("connection.url");
			int questionMark = connectionString.lastIndexOf("?");
			int slashDatabase = StringUtils.ordinalIndexOf(connectionString, "/", 3);
			databaseName = connectionString.substring(slashDatabase + 1, questionMark);
			// get the host
			int slashHost = StringUtils.ordinalIndexOf(connectionString, "/", 2);
			String databasePath = connectionString.substring(slashHost + 1, slashDatabase);
			int colonMarker = databasePath.indexOf(":");
			databaseHost = databasePath.substring(0, colonMarker);
			databasePort = databasePath.substring(colonMarker + 1);
		}
	}

}
