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
import org.openmrs.module.clinicalsummary.enumeration.TaskStatus;
import org.openmrs.util.OpenmrsUtil;

/**
 */
public class SummariesTaskInstance {

	private static SummariesTaskInstance ourInstance;

	private SummariesTask summariesTask;

	private SummariesTaskInstance() {
	}

	public static synchronized SummariesTaskInstance getInstance() {
		if (ourInstance == null)
			ourInstance = new SummariesTaskInstance();
		return ourInstance;
	}

	public static synchronized void removeInstance() {
		ourInstance = null;
	}

	public void startDownloading(final String password, final String passphrase, final String filename) {
		if (!isRunning()) {
			summariesTask = new DownloadSummariesTask(password, passphrase, filename);
			new Thread(summariesTask).start();
		}
	}

	public void startUploading(final String password, final String passphrase, final String filename) {
		if (!isRunning()) {
			summariesTask = new UploadSummariesTask(password, passphrase, filename);
			new Thread(summariesTask).start();
		}
	}

	public String getSummariesFilename() {
		if (summariesTask != null)
			return summariesTask.getFilename();
		return StringUtils.EMPTY;
	}

	public String getCurrentFilename() {
		if (isRunning())
			return summariesTask.getProcessedFilename();
		return StringUtils.EMPTY;
	}

	public String getCurrentTask() {
		if (isRunning())
			return summariesTask.getStatus().getValue();
		return StringUtils.EMPTY;
	}

	public Boolean isRunning() {
		if (summariesTask == null)
			return Boolean.FALSE;

		if (OpenmrsUtil.nullSafeEquals(TaskStatus.TASK_RUNNING_UPLOAD, summariesTask.getStatus())
				|| OpenmrsUtil.nullSafeEquals(TaskStatus.TASK_RUNNING_DOWNLOAD, summariesTask.getStatus()))
			return Boolean.TRUE;

		return Boolean.FALSE;
	}
}
