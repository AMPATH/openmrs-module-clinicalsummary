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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.pdfbox.util.PDFMergerUtility;
import org.apache.xmlgraphics.util.MimeConstants;
import org.openmrs.Cohort;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.evaluator.Evaluator;
import org.openmrs.module.clinicalsummary.evaluator.EvaluatorUtils;
import org.openmrs.module.clinicalsummary.evaluator.LoggerUtils;
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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Date;

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
                String filenamePdf = WebUtils.prepareFilename(null, Evaluator.FILE_TYPE_PDF);
                File attachmentFile = new File(System.getProperty("java.io.tmpdir"), filenamePdf);
                BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(attachmentFile));

                PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
                pdfMergerUtility.setDestinationStream(outputStream);

                FopFactory fopFactory = FopFactory.newInstance();
                FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
                TransformerFactory transformerFactory = TransformerFactory.newInstance();

                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

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

                if (summary != null) {
                    File outputDirectory = EvaluatorUtils.getOutputDirectory(summary);
                    for (Integer patientId : cohort.getMemberIds()) {
                        try {
                            String filenameXml = StringUtils.join(Arrays.asList(patientId, Evaluator.FILE_TYPE_XML), ".");
                            File summaryXmlFile = new File(outputDirectory, filenameXml);

                            File tempFile = File.createTempFile("summary", "temp");
                            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(tempFile));
                            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, bufferedOutputStream);

                            Source xsltSource = new StreamSource(new StringReader(summary.getXslt()));
                            Transformer transformer = transformerFactory.newTransformer(xsltSource);

                            Result result = new SAXResult(fop.getDefaultHandler());
                            Source source = new StreamSource(summaryXmlFile);
                            transformer.transform(source, result);

                            bufferedOutputStream.close();
                            pdfMergerUtility.addSource(tempFile);

                            LoggerUtils.extractLogInformation(documentBuilder.parse(summaryXmlFile), LoggerUtils.getViewingLogFile());
                        } catch (Exception e) {
                            log.error("Summary with type: " + summary.getName() + " for patient: " + patientId, e);
                        }
                    }
                    pdfMergerUtility.mergeDocuments();
                    attachAndPurgeFile(response, attachmentFile);
                }
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

		if (!file.delete()) {
            log.info("Deleting temporary file failed!");
        }
	}
}
