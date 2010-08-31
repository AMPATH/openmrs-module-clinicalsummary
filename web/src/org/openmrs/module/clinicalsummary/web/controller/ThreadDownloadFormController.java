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
package org.openmrs.module.clinicalsummary.web.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.SummaryConstants;
import org.openmrs.module.clinicalsummary.io.SummaryIO;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 */
@Controller
public class ThreadDownloadFormController {
	
	private static final Log log = LogFactory.getLog(ThreadDownloadFormController.class);
	
	@RequestMapping(value = "/module/clinicalsummary/threadDownload", method = RequestMethod.GET)
	public void preparePage(ModelMap map) {
		if (Context.isAuthenticated()) {
			File outputZipPath = OpenmrsUtil.getDirectoryInApplicationDataDirectory(SummaryConstants.ZIPPED_LOCATION);
			map.addAttribute("zipFile", getLatestFileName(outputZipPath));
			File outputSecretPath = OpenmrsUtil.getDirectoryInApplicationDataDirectory(SummaryConstants.ENCRYPTION_LOCATION);
			map.addAttribute("secretFile", getLatestFileName(outputSecretPath));
		}
	}
	
	private String getLatestFileName(File path) {
		String filename = StringUtils.EMPTY;
		File[] files = path.listFiles();
		// get the latest file, there should be only one in here but just in case :)
		File latestFile = null;
		for (int i = 0; i < files.length; i++) {
			if (latestFile == null)
				latestFile = files[i];
			else if (latestFile.lastModified() < files[i].lastModified())
				latestFile = files[i];
		}
		
		if (latestFile != null)
			filename = FilenameUtils.removeExtension(latestFile.getName());
		
		return filename;
	}
	
	@RequestMapping(value = "/module/clinicalsummary/threadDownload", method = RequestMethod.POST)
	public void uploadSummaries(@RequestParam(required = true, value = "password") String password,
	                            @RequestParam(required = true, value = "passphrase") String passphrase, ModelMap map) {
		if (Context.isAuthenticated())
			try {
				
				preparePage(map);
				
				String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
				String filename = "Summaries_" + time;
				
				if (log.isDebugEnabled())
					log.debug("Processing file: " + filename + " ...");
				
				SummaryIO.startDownloadTask(password, passphrase, filename);
			}
			catch (Exception e) {
				log.error("Preparing zipped file failed ...", e);
				SummaryIO.cancelTask();
			}
	}
}
