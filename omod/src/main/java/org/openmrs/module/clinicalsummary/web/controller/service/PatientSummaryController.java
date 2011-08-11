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

package org.openmrs.module.clinicalsummary.web.controller.service;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.evaluator.Evaluator;
import org.openmrs.module.clinicalsummary.evaluator.EvaluatorUtils;
import org.openmrs.module.clinicalsummary.service.SummaryService;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 */
@Controller
@RequestMapping("/module/clinicalsummary/service/patient/summary")
public class PatientSummaryController {

	private static final Log log = LogFactory.getLog(PatientSummaryController.class);

	/**
	 * @param patientId
	 * @param summaryId
	 * @param response
	 * @should return summary data for patient and summary
	 * @should return empty data when no index found for the patient and summary
	 */
	@RequestMapping(method = RequestMethod.GET)
	public void searchSummary(@RequestParam(required = false, value = "username") String username,
	                          @RequestParam(required = false, value = "password") String password,
	                          @RequestParam(required = false, value = "patientId") String patientId,
	                          @RequestParam(required = false, value = "summaryId") Integer summaryId,
	                          HttpServletResponse response) throws IOException {

		try {
			if (!Context.isAuthenticated())
				Context.authenticate(username, password);

			Summary summary = Context.getService(SummaryService.class).getSummary(summaryId);

			File outputDirectory = EvaluatorUtils.getOutputDirectory(summary);
			File summaryFile = new File(outputDirectory, StringUtils.join(Arrays.asList(patientId, Evaluator.FILE_TYPE_XML), "."));
			if (summaryFile.exists()) {
				FileInputStream inputStream = new FileInputStream(summaryFile);
				FileCopyUtils.copy(inputStream, response.getOutputStream());
			}
		} catch (ContextAuthenticationException e) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}
}
