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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.SummaryConstants;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SummaryFileDownloaderController {
	
	private static final Log log = LogFactory.getLog(SummaryFileDownloaderController.class);
	
	private static final int BUFFER_SIZE = 4096;
	
	private static final String ZIP_FILE = "zip";
	
	@RequestMapping("/module/clinicalsummary/download")
	public void download(@RequestParam(required = true, value = "filename") String filename,
	                     @RequestParam(required = true, value = "type") String type, HttpServletResponse response) {
		if (Context.isAuthenticated())
			try {
				String folder = SummaryConstants.ENCRYPTION_LOCATION;
				String contentType = "text/plain";
				if (type.equals(ZIP_FILE)) {
					folder = SummaryConstants.ZIPPED_LOCATION;
					filename = filename + "." + ZIP_FILE;
					contentType = "application/zip";
				}
				
				File inputPath = OpenmrsUtil.getDirectoryInApplicationDataDirectory(folder);
				File summariesFile = new File(inputPath, filename);
				
				response.setHeader("Content-Disposition", "attachment; filename=" + filename);
				response.setHeader("Content-Length", String.valueOf(summariesFile.length()));
				response.setContentType(contentType);
				response.flushBuffer();
				
				BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(summariesFile));
				
				ReadableByteChannel input = Channels.newChannel(inputStream);
				WritableByteChannel output = Channels.newChannel(response.getOutputStream());
				ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
				
				while (input.read(buffer) != -1) {
					buffer.flip();
					output.write(buffer);
					buffer.clear();
				}
				
				input.close();
				output.close();
			}
			catch (IOException e) {
				log.error("Downloading file failed ...", e);
			}
	}
	
}
