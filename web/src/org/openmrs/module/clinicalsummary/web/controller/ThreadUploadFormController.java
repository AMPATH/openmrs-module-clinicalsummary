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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.SummaryConstants;
import org.openmrs.module.clinicalsummary.io.SummaryIO;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 */
@Controller
public class ThreadUploadFormController {
	
	private static final Log log = LogFactory.getLog(ThreadUploadFormController.class);
	
	private static final int BUFFER_SIZE = 4096;
	
	private static final String ZIP_EXTENSION = ".zip";
	
	@RequestMapping(value = "/module/clinicalsummary/threadUpload", method = RequestMethod.GET)
	public void preparePage() {
	}
	
	@RequestMapping(value = "/module/clinicalsummary/threadUpload", method = RequestMethod.POST)
	public void uploadSummaries(@RequestParam(required = true, value = "password") String password,
	                            @RequestParam(required = true, value = "passphrase") String passphrase,
	                            @RequestParam(required = true, value = "secretFile") MultipartFile secretFile,
	                            @RequestParam(required = true, value = "summaries") MultipartFile returnedData,
	                            HttpServletRequest request) {
		if (Context.isAuthenticated())
			try {
				HttpSession httpSession = request.getSession();
				int interval = httpSession.getMaxInactiveInterval();
				httpSession.setMaxInactiveInterval(-1);
				
				String time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
				String filename = "Summaries_" + time;
				
				File initVectorPath = OpenmrsUtil
				        .getDirectoryInApplicationDataDirectory(SummaryConstants.ENCRYPTION_LOCATION);
				// clean up old upload and download
				
				InputStream secretFileInputStream = secretFile.getInputStream();
				
				File initVectorFile = new File(initVectorPath, filename);
				BufferedOutputStream initVectorOutputStream = new BufferedOutputStream(new FileOutputStream(initVectorFile));
				
				copyStream(secretFileInputStream, initVectorOutputStream);
				secretFileInputStream.close();
				initVectorOutputStream.close();
				
				File zippedPath = OpenmrsUtil.getDirectoryInApplicationDataDirectory(SummaryConstants.ZIPPED_LOCATION);
				
				InputStream summariesInputStream = returnedData.getInputStream();
				File summariesFile = new File(zippedPath, filename + ZIP_EXTENSION);
				BufferedOutputStream summariesOutputStream = new BufferedOutputStream(new FileOutputStream(summariesFile));
				copyStream(summariesInputStream, summariesOutputStream);
				summariesInputStream.close();
				summariesOutputStream.close();
				
				SummaryIO.startUploadTask(password, passphrase, filename);
				
				httpSession.setMaxInactiveInterval(interval);
			}
			catch (Exception e) {
				log.error("Uploading zipped file failed ...", e);
				SummaryIO.cancelTask();
			}
	}
	
	private void copyStream(InputStream input, OutputStream output) throws IOException {
		byte data[] = new byte[BUFFER_SIZE];
		int count;
		while ((count = input.read(data, 0, BUFFER_SIZE)) != -1)
			output.write(data, 0, count);
	}
	
}
