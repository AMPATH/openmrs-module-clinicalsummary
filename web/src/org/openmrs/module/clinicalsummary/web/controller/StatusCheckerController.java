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

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.io.SummaryIO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 */
@Controller
public class StatusCheckerController {
	
	@RequestMapping(value = "/module/clinicalsummary/checkStatus", method = RequestMethod.GET)
	public void checkStatus(HttpServletResponse response) throws IOException {
		if (Context.isAuthenticated()) {
			
			response.setContentType("application/json");
			
			JsonFactory factory = new JsonFactory();
			
			JsonGenerator generator = factory.createJsonGenerator(response.getOutputStream(), JsonEncoding.UTF8);
			generator.useDefaultPrettyPrinter();
			
			generator.writeStartObject();
			
			generator.writeStringField("filename", SummaryIO.getFilename());
			generator.writeBooleanField("running", SummaryIO.isRunning());
			
			String total = FileUtils.byteCountToDisplaySize(SummaryIO.getTotal());
			String processed = FileUtils.byteCountToDisplaySize(SummaryIO.getProcessed());
			generator.writeStringField("total", total);
			generator.writeStringField("processed", processed);
			
			double percentage = 0;
			if (SummaryIO.getTotal() > 0)
				percentage = Math.floor(((double) SummaryIO.getProcessed() / (double) SummaryIO.getTotal()) * 100);
			generator.writeNumberField("percentage", percentage);
			
			generator.writeStringField("status", SummaryIO.getExecutionStatus());
			
			if (SummaryIO.isFinished())
				SummaryIO.resetTask();
			
			generator.writeEndObject();
			
			generator.close();
		}
	}
}
