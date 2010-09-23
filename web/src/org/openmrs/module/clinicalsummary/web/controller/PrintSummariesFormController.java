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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Location;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.SummaryConstants;
import org.openmrs.module.clinicalsummary.SummaryIndex;
import org.openmrs.module.clinicalsummary.SummaryService;
import org.openmrs.module.clinicalsummary.SummaryTemplate;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;

@Controller
@RequestMapping("/module/clinicalsummary/printSummaries")
public class PrintSummariesFormController {
	
	private static final String PDF_EXTENSION = ".pdf";
	
	private static final String MIME_TYPE = "application/pdf";
	
	private static final int BUFFER_SIZE = 4096;
	
	static final Log log = LogFactory.getLog(PrintSummariesFormController.class);
	
	@RequestMapping(method = RequestMethod.GET)
	public void preparePage(ModelMap map) {
		map.addAttribute("locations", Context.getLocationService().getAllLocations());
		
		SummaryService service = Context.getService(SummaryService.class);
		map.addAttribute("templates", service.getAllTemplates());
		map.addAttribute("preferredTemplate", service.getPreferredTemplate());
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public String printSummaries(@RequestParam(required = false, value = "patientIdentifiers") String patientIdentifiers,
	                             @RequestParam(required = false, value = "locationId") String locationId,
	                             @RequestParam(required = false, value = "templateId") String[] templateId,
	                             @RequestParam(required = false, value = "endReturnDate") String endReturn,
	                             @RequestParam(required = false, value = "startReturnDate") String startReturn,
	                             HttpServletRequest request, HttpServletResponse response) {
		
		HttpSession httpSession = request.getSession();
		
		if (Context.isAuthenticated())
			// user must be authenticated (avoids auth errors)
			try {
				httpSession.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);
				
				File folder = OpenmrsUtil.getDirectoryInApplicationDataDirectory(SummaryConstants.GENERATED_PDF_LOCATION);
				
				// create a temporary file that will hold all copied pdf file
				File summaryCollectionsFile = File.createTempFile("Summary", PDF_EXTENSION);
				summaryCollectionsFile.deleteOnExit();
				
				SummaryService summaryService = Context.getService(SummaryService.class);
				
				FileOutputStream outputStream = new FileOutputStream(summaryCollectionsFile);
				Document document = new Document();
				PdfCopy copy = new PdfCopy(document, outputStream);
				document.open();
				
				if (StringUtils.isNotBlank(patientIdentifiers)) {
					
					String[] patientIdentifier = StringUtils.split(patientIdentifiers);
					String template = StringUtils.defaultString(templateId[0], "*");
					PatientSetService patientSetService = Context.getPatientSetService();
					// TODO: buggy code from openmrs
					Cohort cohort = patientSetService.convertPatientIdentifier(Arrays.asList(patientIdentifier));
					
					PdfReader reader = null;
					for (Integer patientId : cohort.getMemberIds()) {
						
						File file = new File(folder, patientId + "_" + template + PDF_EXTENSION);
						// if the patient is not generated yet, then just skip him ...
						if (!file.exists())
							continue;
						
						try {
							// when one pdf fail, then we just need to skip that file
							// instead of failing for the whole pdfs collection
							reader = new PdfReader(file.getAbsolutePath());
							copy.addPage(copy.getImportedPage(reader, 1));
						}
						catch (Exception e) {
							log.error("Failed to add summary for patient: " + patientId, e);
						}
					}
				} else if (StringUtils.isNotBlank(locationId)) {
					
					SummaryTemplate template = summaryService.getTemplate(NumberUtils.toInt(templateId[0], -1));
					Location location = Context.getLocationService().getLocation(NumberUtils.toInt(locationId, -1));
					
					Date startReturnDate = WebUtils.parse(startReturn, new Date());
					Date endReturnDate = WebUtils.parse(endReturn, startReturnDate);
					
					List<SummaryIndex> summaryIndexs = summaryService.getIndexes(location, template, startReturnDate, endReturnDate);
					
					PdfReader reader = null;
					for (SummaryIndex summaryIndex : summaryIndexs) {
						
						Integer patientId = summaryIndex.getPatient().getPatientId();
						File file = new File(folder, patientId + "_" + template.getTemplateId() + PDF_EXTENSION);
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
							log.error("Failed to add summary for patient: " + patientId, e);
						}
					}
				}
				
				copy.close();
				document.close();
				outputStream.close();
				
				String downloadFilename = "PreGeneratedSummary-" + summaryCollectionsFile.getName();
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
				String message = "No pregenerated summaries found / failed collecting summaries for the patient(s). Please check the log file to see the problem(s)";
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, message);
				
				log.error("No pregenerated summaries found / failed collecting summaries for the patient(s) ...", e);
			}
		return "redirect:printSummaries.form";
	}
	
}
