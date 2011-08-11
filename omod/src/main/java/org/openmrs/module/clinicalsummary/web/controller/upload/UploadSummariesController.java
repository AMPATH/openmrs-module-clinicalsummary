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

package org.openmrs.module.clinicalsummary.web.controller.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.clinicalsummary.Constants;
import org.openmrs.module.clinicalsummary.io.SummariesTaskInstance;
import org.openmrs.module.clinicalsummary.io.utils.TaskConstants;
import org.openmrs.module.clinicalsummary.web.controller.WebUtils;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 */
@Controller
@RequestMapping("/module/clinicalsummary/upload/uploadSummaries")
public class UploadSummariesController {

	private static final Log log = LogFactory.getLog(UploadSummariesController.class);

	@RequestMapping(method = RequestMethod.GET)
	public void populatePage(final ModelMap map) {
	}

	@RequestMapping(method = RequestMethod.POST)
	public void processSubmit(final @RequestParam(required = true, value = "password") String password,
	                          final @RequestParam(required = true, value = "passphrase") String passphrase,
	                          final @RequestParam(required = true, value = "secretFile") MultipartFile secret,
	                          final @RequestParam(required = true, value = "summaries") MultipartFile summaries) {
		try {
			String filename = WebUtils.prepareFilename(null, null);

			File initVectorPath = OpenmrsUtil.getDirectoryInApplicationDataDirectory(Constants.ENCRYPTION_LOCATION);
			String secretFilename = StringUtils.join(Arrays.asList(filename, TaskConstants.FILE_TYPE_SECRET), ".");
			OutputStream secretOutputStream = new FileOutputStream(new File(initVectorPath, secretFilename));
			FileCopyUtils.copy(secret.getInputStream(), secretOutputStream);

			File zippedPath = OpenmrsUtil.getDirectoryInApplicationDataDirectory(Constants.ZIPPED_LOCATION);
			String zippedFilename = StringUtils.join(Arrays.asList(filename, TaskConstants.FILE_TYPE_ZIP), ".");
			OutputStream summariesOutputStream = new FileOutputStream(new File(zippedPath, zippedFilename));
			FileCopyUtils.copy(summaries.getInputStream(), summariesOutputStream);

			SummariesTaskInstance.getInstance().startUploading(password, passphrase, filename);
		} catch (IOException e) {
			log.error("Uploading zipped file failed ...", e);
		}
	}
}
