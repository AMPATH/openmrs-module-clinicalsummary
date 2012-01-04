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

package org.openmrs.module.clinicalsummary.web.controller.download;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.clinicalsummary.Constants;
import org.openmrs.module.clinicalsummary.io.SummariesTaskInstance;
import org.openmrs.module.clinicalsummary.web.controller.WebUtils;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 */
@Controller
@RequestMapping("/module/clinicalsummary/download/downloadSummaries")
public class DownloadSummariesController {

	private static final Log log = LogFactory.getLog(DownloadSummariesController.class);

	private void preparePage(final ModelMap map) {
		String zipFile = StringUtils.EMPTY;
		File zipPath = OpenmrsUtil.getDirectoryInApplicationDataDirectory(Constants.ZIPPED_LOCATION);
		for (File file : zipPath.listFiles())
			zipFile = file.getName();
		map.put("zipFile", zipFile);

		String secretFile = StringUtils.EMPTY;
		File secretPath = OpenmrsUtil.getDirectoryInApplicationDataDirectory(Constants.ENCRYPTION_LOCATION);
		for (File file : secretPath.listFiles())
			secretFile = file.getName();
		map.put("secretFile", secretFile);
	}

	@RequestMapping(method = RequestMethod.GET)
	public void populatePage(final ModelMap map) {
		preparePage(map);
	}

	@RequestMapping(method = RequestMethod.POST)
	public void processSubmit(final @RequestParam(required = true, value = "password") String password,
	                          final ModelMap map) {
		// prepare the page elements
		preparePage(map);
		// start the download process
		SummariesTaskInstance.getInstance().startDownloading(password, WebUtils.prepareFilename(null, null));
	}
}
