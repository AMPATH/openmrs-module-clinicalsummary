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

package org.openmrs.module.clinicalsummary.web.controller.printer;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Date;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlgraphics.util.MimeConstants;
import org.openmrs.Cohort;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.evaluator.Evaluator;
import org.openmrs.module.clinicalsummary.evaluator.EvaluatorUtils;
import org.openmrs.module.clinicalsummary.service.IndexService;
import org.openmrs.module.clinicalsummary.service.SummaryService;
import org.openmrs.module.clinicalsummary.web.controller.WebUtils;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 */
@Controller
@RequestMapping(value = "/module/clinicalsummary/printer/printSummaries")
public class PrintSummariesController {

	private static final Log log = LogFactory.getLog(PrintSummariesController.class);

	@RequestMapping(method = RequestMethod.GET)
	public void populatePage(final ModelMap map) {
		map.addAttribute("summaries", Context.getService(SummaryService.class).getAllSummaries());
	}

	@RequestMapping(method = RequestMethod.POST)
	public String printSummaries(final @RequestParam(required = false, value = "patientIdentifiers") String patientIdentifiers,
	                             final @RequestParam(required = false, value = "summaryIdentifier") String summaryIdentifier,
	                             final @RequestParam(required = false, value = "locationId") String locationId,
	                             final @RequestParam(required = false, value = "summaryLocation") String summaryLocation,
	                             final @RequestParam(required = false, value = "endReturnDate") String endReturn,
	                             final @RequestParam(required = false, value = "startReturnDate") String startReturn,
	                             final HttpServletResponse response, final HttpSession session) {


		if (StringUtils.isBlank(locationId) && StringUtils.isBlank(patientIdentifiers)) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, " clinicalsummary.invalid.parameters");
		} else {
			try {
				File attachmentFile = new File(System.getProperty("java.io.tmpdir"), WebUtils.prepareFilename(null, Evaluator.FILE_TYPE_PDF));
				FileOutputStream outputStream = new FileOutputStream(attachmentFile);

				Document document = new Document();
				PdfCopy copy = new PdfCopy(document, outputStream);
				document.open();

				Cohort cohort = new Cohort();

				Summary summary = null;
				// collect the cohort and then get the template to be printed
				if (StringUtils.isNotBlank(patientIdentifiers)) {
					String[] patientIdentifier = StringUtils.split(patientIdentifiers);
                    Cohort patients = Context.getPatientSetService().convertPatientIdentifier(Arrays.asList(patientIdentifier));
					summary = Context.getService(SummaryService.class).getSummary(NumberUtils.toInt(summaryIdentifier, -1));
                    cohort = Context.getService(IndexService.class).getIndexCohort(patients, summary);
				} else if (StringUtils.isNotBlank(locationId)) {
					Location location = Context.getLocationService().getLocation(NumberUtils.toInt(locationId, -1));
					summary = Context.getService(SummaryService.class).getSummary(NumberUtils.toInt(summaryLocation, -1));
					Date startReturnDate = WebUtils.parse(startReturn, new Date());
					Date endReturnDate = WebUtils.parse(endReturn, startReturnDate);
					cohort = Context.getService(IndexService.class).getIndexCohort(location, summary, startReturnDate, endReturnDate);
				}

				File outputDirectory = EvaluatorUtils.getOutputDirectory(summary);
				for (Integer patientId : cohort.getMemberIds()) {
					File summaryFile = new File(outputDirectory, StringUtils.join(Arrays.asList(patientId, Evaluator.FILE_TYPE_PDF), "."));
					try {
						PdfReader reader = new PdfReader(new FileInputStream(summaryFile));
						int pageCount = reader.getNumberOfPages();
						for (int i = 1; i <= pageCount; i++)
							copy.addPage(copy.getImportedPage(reader, i));
					} catch (Exception e) {
						log.error("Failed adding summary for patient " + patientId + " with " + summaryFile.getName(), e);
					}
				}

				document.close();
				outputStream.close();

				attachAndPurgeFile(response, attachmentFile);
				return null;
			} catch (Exception e) {
				log.error("Failed generating attachment for patients", e);
			}
		}

		return "redirect:printSummaries.form";
	}

	/**
	 * Attach the file to the servlet response body
	 *
	 * @param response
	 * 		the response
	 * @param file
	 * 		the file
	 *
	 * @throws Exception
	 */
	private void attachAndPurgeFile(HttpServletResponse response, File file) throws Exception {
		response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
		response.setContentType(MimeConstants.MIME_PDF);
		response.setContentLength((int) file.length());

		FileCopyUtils.copy(new FileInputStream(file), response.getOutputStream());

		if (!file.delete())
            log.info("Deleting temporary file failed!");
	}
}
