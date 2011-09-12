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

package org.openmrs.module.clinicalsummary.web.controller.watcher;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.openmrs.module.clinicalsummary.web.controller.MimeType;
import org.openmrs.module.clinicalsummary.web.controller.evaluator.engine.ReminderCohortEvaluatorInstance;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 */
@Controller
@RequestMapping("/module/clinicalsummary/watcher/watchReminderEvaluation")
public class ReminderEvaluationWatcherController {

	private static final Log log = LogFactory.getLog(ReminderEvaluationWatcherController.class);

	@RequestMapping(method = RequestMethod.GET)
	public void watch(final HttpServletResponse response) throws IOException {
		response.setContentType(MimeType.APPLICATION_JSON);
		JsonFactory factory = new JsonFactory();
		JsonGenerator generator = factory.createJsonGenerator(response.getOutputStream(), JsonEncoding.UTF8);
		generator.useDefaultPrettyPrinter();

		generator.writeStartObject();

		ReminderCohortEvaluatorInstance instanceSummary = ReminderCohortEvaluatorInstance.getInstance();
		generator.writeBooleanField("running", instanceSummary.isRunning());
		generator.writeStringField("filename", instanceSummary.getCurrentPatient());
		generator.writeStringField("status", instanceSummary.getCurrentStatus());
		generator.writeStringField("counter", instanceSummary.getProcessed());
		generator.writeStringField("size", instanceSummary.getSize());

		generator.writeEndObject();

		generator.close();
	}
}
