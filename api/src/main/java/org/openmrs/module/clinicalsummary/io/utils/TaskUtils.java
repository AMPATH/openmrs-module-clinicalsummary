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

package org.openmrs.module.clinicalsummary.io.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.clinicalsummary.Constants;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 */
public class TaskUtils {

	private static final Log log = LogFactory.getLog(TaskUtils.class);

	/**
	 * Method to execute a command inside the shell. The command is usually the mysqldump or mysql command.
	 *
	 * @param commands
	 *
	 * @throws InterruptedException
	 */
	public static void executeCommand(final File workingDirectory, final String[] commands) throws Exception {
		Runtime runtime = Runtime.getRuntime();
		Process process;
		if (OpenmrsConstants.UNIX_BASED_OPERATING_SYSTEM)
			process = runtime.exec(commands, null, workingDirectory);
		else
			process = runtime.exec(commands);

		StreamHandler errorHandler = new StreamHandler(process.getErrorStream(), "ERROR");
		StreamHandler outputHandler = new StreamHandler(process.getInputStream(), "OUTPUT");

		ExecutorService executorService = Executors.newCachedThreadPool();

		executorService.execute(errorHandler);
		executorService.execute(outputHandler);

		int exitValue = process.waitFor();
		log.info("Process execution completed with exit value: " + exitValue + " ...");
	}

	/**
	 * Return the output folder for the secret file used for encryption and decryption process
	 *
	 * @return the folder of the secret file
	 */
	public static File getSecretOutputPath() {
		return OpenmrsUtil.getDirectoryInApplicationDataDirectory(Constants.ENCRYPTION_LOCATION);
	}

	/**
	 * Return the output folder for the zipped file used for encryption and decryption process
	 *
	 * @return the folder of the zipped file
	 */
	public static File getZippedOutputPath() {
		return OpenmrsUtil.getDirectoryInApplicationDataDirectory(Constants.ZIPPED_LOCATION);
	}

	/**
	 * Return the root folder of the summary folder output structure
	 *
	 * @return the root folder of the summary folder output structure
	 */
	public static File getSummaryOutputPath() {
		return OpenmrsUtil.getDirectoryInApplicationDataDirectory(Constants.GENERATED_PDF_LOCATION);
	}
}
