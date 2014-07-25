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

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
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
		String secretFile = StringUtils.EMPTY;
		File encryptedFiles = OpenmrsUtil.getDirectoryInApplicationDataDirectory(Constants.ENCRYPTION_LOCATION);
        File[] files = encryptedFiles.listFiles();
        if (files != null) {
            for (File file : files) {
                secretFile = file.getName();
            }
        }
		map.put("encryptedFile", secretFile);
	}

	@RequestMapping(method = RequestMethod.GET)
	public void populatePage(final ModelMap map) {
		preparePage(map);
	}

	@RequestMapping(method = RequestMethod.POST)
	public void processSubmit(final @RequestParam(required = true, value = "password") String password,
                              final @RequestParam(required = false, value = "partial") Boolean partial,
	                          final ModelMap map) {
        if (Context.isAuthenticated()) {
            // prepare the page elements
            preparePage(map);
            // start the download process
            String filename = WebUtils.prepareFilename();
            if (BooleanUtils.isTrue(partial))
                filename = "Partial_" + filename;
            SummariesTaskInstance.getInstance().startDownloading(password, filename, partial);
        }
	}
}
