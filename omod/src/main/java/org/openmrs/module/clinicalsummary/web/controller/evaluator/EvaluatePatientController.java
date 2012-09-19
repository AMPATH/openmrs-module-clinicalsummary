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

package org.openmrs.module.clinicalsummary.web.controller.evaluator;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlgraphics.util.MimeConstants;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.evaluator.Evaluator;
import org.openmrs.module.clinicalsummary.evaluator.EvaluatorUtils;
import org.openmrs.module.clinicalsummary.evaluator.velocity.VelocityEvaluator;
import org.openmrs.module.clinicalsummary.rule.ResultCacheInstance;
import org.openmrs.module.clinicalsummary.service.EvaluatorService;
import org.openmrs.module.clinicalsummary.service.IndexService;
import org.openmrs.module.clinicalsummary.service.SummaryService;
import org.openmrs.module.clinicalsummary.web.controller.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

/**
 */
@Controller
@RequestMapping(value = "/module/clinicalsummary/evaluator/evaluatePatient")
public class EvaluatePatientController {

	private static final Log log = LogFactory.getLog(EvaluatePatientController.class);

	@RequestMapping(method = RequestMethod.GET)
	public String populatePage(final @RequestParam("patientId") Integer patientId,
	                           final HttpServletRequest request,
	                           final HttpServletResponse response) {

		EvaluatorService evaluatorService = Context.getService(EvaluatorService.class);
		IndexService indexService = Context.getService(IndexService.class);

		Patient patient = Context.getPatientService().getPatient(patientId);
		try {
			File attachmentFile = new File(System.getProperty("java.io.tmpdir"), WebUtils.prepareFilename(patientId, Evaluator.FILE_TYPE_PDF));
			FileOutputStream outputStream = new FileOutputStream(attachmentFile);

			Document document = new Document();
			PdfCopy copy = new PdfCopy(document, outputStream);
			document.open();

			Evaluator evaluator = new VelocityEvaluator();

			SummaryService summaryService = Context.getService(SummaryService.class);
			for (Summary summary : summaryService.getSummaries(patient)) {
				double start = System.currentTimeMillis();

				evaluator.evaluate(summary, patient, Boolean.TRUE);
				indexService.saveIndex(indexService.generateIndex(patient, summary));

				double elapsed = System.currentTimeMillis() - start;
				log.info("Velocity evaluator running for " + elapsed + "ms (" + (elapsed / 1000) + "s)");

				File outputDirectory = EvaluatorUtils.getOutputDirectory(summary);
				File summaryFile = new File(outputDirectory, StringUtils.join(Arrays.asList(patientId, Evaluator.FILE_TYPE_PDF), "."));
				try {
					PdfReader reader = new PdfReader(new FileInputStream(summaryFile));
					int pageCount = reader.getNumberOfPages();
					for (int i = 1; i <= pageCount; i++)
						copy.addPage(copy.getImportedPage(reader, i));
				} catch (Exception e) {
					log.error("Failed adding summary for patient " + patientId + " with " + summaryFile.getName(), e);
				}

                ResultCacheInstance.getInstance().clearCache(patient);
			}

			document.close();
			outputStream.close();

			attachAndPurgeFile(response, attachmentFile);
			return null;
		} catch (Exception e) {
			log.error("Failed generating attachment for patient " + patientId, e);
		}

		return "redirect:" + request.getHeader("Referer");
	}

	/**
	 * Attach the file to the servlet response body
	 *
	 * @param response the response
	 * @param file     the file
	 * @throws Exception
	 */
	private void attachAndPurgeFile(final HttpServletResponse response, final File file) throws Exception {
		response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
		response.setContentType(MimeConstants.MIME_PDF);
		response.setContentLength((int) file.length());

		FileCopyUtils.copy(new FileInputStream(file), response.getOutputStream());

		file.delete();
	}

}
