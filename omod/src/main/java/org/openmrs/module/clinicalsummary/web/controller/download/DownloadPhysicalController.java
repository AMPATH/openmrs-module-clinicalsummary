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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.clinicalsummary.Constants;
import org.openmrs.module.clinicalsummary.io.utils.TaskConstants;
import org.openmrs.module.clinicalsummary.web.controller.MimeType;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 */
@Controller
@RequestMapping("/module/clinicalsummary/download/downloadPhysical")
public class DownloadPhysicalController {

	private static final Log log = LogFactory.getLog(DownloadPhysicalController.class);

	@RequestMapping(method = RequestMethod.POST)
	public void retrieveFile(final @RequestParam(required = true, value = "type") String type,
	                         final @RequestParam(required = true, value = "filename") String filename,
	                         final HttpServletResponse response) {
		try {
			String folder = Constants.ENCRYPTION_LOCATION;
			String contentType = MimeType.TEXT_PLAIN;
			if (StringUtils.equals(type, TaskConstants.FILE_TYPE_ZIP)) {
				folder = Constants.ZIPPED_LOCATION;
				contentType = MimeType.APPLICATION_ZIP;
			}

			File directory = OpenmrsUtil.getDirectoryInApplicationDataDirectory(folder);
			InputStream inputStream = new BufferedInputStream(new FileInputStream(new File(directory, filename)));

			response.setHeader("Content-Disposition", "attachment; filename=" + filename);
			response.setContentType(contentType);
			FileCopyUtils.copy(inputStream, response.getOutputStream());
		} catch (IOException e) {
			log.error("Download process failed for: " + filename, e);
		}
	}
}
