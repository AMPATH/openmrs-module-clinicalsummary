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
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.SummaryConstants;
import org.openmrs.module.clinicalsummary.SummaryIndex;
import org.openmrs.module.clinicalsummary.SummaryService;
import org.openmrs.module.clinicalsummary.SummaryTemplate;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;

/**
 *
 */
@Controller
public class SummaryIndexPrinterController {
	
	private static final String PDF_EXTENSION = ".pdf";
	
	private static final String MIME_TYPE = "application/pdf";
	
	private static final int BUFFER_SIZE = 4096;
	
	private static final Log log = LogFactory.getLog(SummaryIndexPrinterController.class);
	
	@RequestMapping("/module/clinicalsummary/summaryIndexPrinter")
	public void printSummary(@RequestParam(required = false, value = "printedIndexes") int[] indexes,
	                         HttpServletRequest request, HttpServletResponse response) {
		if (Context.isAuthenticated())
			try {
				File folder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(SummaryConstants.GENERATED_PDF_LOCATION);
				
				// create a temporary file that will hold all copied pdf file
				String time = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
				File summaryCollectionsFile = File.createTempFile("Summary", PDF_EXTENSION);
				
				SummaryService summaryService = Context.getService(SummaryService.class);
				
				FileOutputStream outputStream = new FileOutputStream(summaryCollectionsFile);
				Document document = new Document();
				PdfCopy copy = new PdfCopy(document, outputStream);
				document.open();
				
				PdfReader reader = null;
				for (int index : indexes) {
					
					SummaryIndex summaryIndex = summaryService.getIndex(index);
					Patient patient = summaryIndex.getPatient();
					SummaryTemplate template = summaryIndex.getTemplate();
					
					File file = new File(folder, patient.getPatientId() + "_" + template.getTemplateId() + PDF_EXTENSION);
					// if the patient is not generated yet, then just skip him ...
					if (!file.exists())
						continue;
					
					// when one pdf fail, then we just need to skip that file
					// instead of failing for the whole pdfs collection
					try {
						reader = new PdfReader(file.getAbsolutePath());
						copy.addPage(copy.getImportedPage(reader, 1));
					}
					catch (Exception e) {
						log.error("Failed to add summary for patient: " + patient.getPatientId() + " ...", e);
					}
				}
				
				copy.close();
				document.close();
				outputStream.close();
				
				String downloadFilename = "PreGeneratedSummary-" + "Summary_" + time + PDF_EXTENSION;
				response.setHeader("Content-Disposition", "attachment; filename=" + downloadFilename);
				response.setContentType(MIME_TYPE);
				response.setContentLength((int) summaryCollectionsFile.length());
				response.flushBuffer();
				
				BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(summaryCollectionsFile));
				
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
			catch (Exception e) {
				HttpSession httpSession = request.getSession();
				String message = "No pregenerated summaries found / failed collecting summaries for the patient(s). Please check the log file to see the problem(s)";
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, message);
				
				log.error("No pregenerated summaries found / failed collecting summaries for the patient(s) ...", e);
			}
	}
}
