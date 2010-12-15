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

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.engine.GeneratorThread;
import org.openmrs.module.clinicalsummary.io.SummaryIO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 */
@Controller
public class GenerateProcessCheckerController {
	
	@RequestMapping(value = "/module/clinicalsummary/checkGenerationStatus", method = RequestMethod.GET)
	public void checkStatus(HttpServletResponse response) throws IOException {
		if (Context.isAuthenticated()) {
			
			response.setContentType("application/json");
			
			ObjectMapper mapper = new ObjectMapper();
			JsonFactory factory = mapper.getJsonFactory();
			
			JsonGenerator generator = factory.createJsonGenerator(response.getOutputStream(), JsonEncoding.UTF8);
			generator.useDefaultPrettyPrinter();
			
			generator.writeStartObject();
			generator.writeStringField("patient", GeneratorThread.currentPatient().toString());
			generator.writeBooleanField("running", GeneratorThread.isRunning());
			
			double total = GeneratorThread.getTotal();
			double processed = GeneratorThread.getProcessed();
			generator.writeNumberField("total", total);
			generator.writeNumberField("processed", processed);
			
			double percentage = 0;
			if (SummaryIO.getTotal() > 0)
				percentage = Math.floor((processed / total) * 100);
			generator.writeNumberField("percentage", percentage);
			
			generator.writeEndObject();
			generator.close();
		}
	}
}
