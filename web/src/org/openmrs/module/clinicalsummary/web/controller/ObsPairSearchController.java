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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.SummaryService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ObsPairSearchController {
	
	private static final Log log = LogFactory.getLog(ObsPairSearchController.class);
	
	@SuppressWarnings("unchecked")
	@RequestMapping("/module/clinicalsummary/obsPairSearch")
	public void createJsonContent(@RequestParam(required = false, value = "iDisplayStart") int displayStart,
	                              @RequestParam(required = false, value = "iDisplayLength") int displayLength,
	                              @RequestParam(required = false, value = "sEcho") int echo,
	                              @RequestParam(required = false, value = "sSearch") String search,
	                              HttpServletResponse response, HttpServletRequest request) throws IOException,
	                                                                                       ServletRequestBindingException {
		if (Context.isAuthenticated()) {
			
			response.setContentType("application/json");
			
			SummaryService service = Context.getService(SummaryService.class);
			
			if (StringUtils.length(search) < 3)
				search = StringUtils.EMPTY;
			
			List pairs = service.getObsPairs(StringUtils.trim(search), displayStart, displayLength);
			Integer totalPairs = service.countObsPairs(StringUtils.EMPTY);
			Integer filtered = service.countObsPairs(StringUtils.trim(search));
			
			if (log.isDebugEnabled())
				log.debug("Total pairs found: " + pairs.size());
			
			JsonFactory factory = new JsonFactory();
			JsonGenerator generator = factory.createJsonGenerator(response.getOutputStream(), JsonEncoding.UTF8);
			generator.useDefaultPrettyPrinter();
			
			generator.writeStartObject();
			
			generator.writeArrayFieldStart("aaData");
			
			for (Object object : pairs) {
				generator.writeStartArray();
				
				Object[] results = (Object[]) object;
				Patient patient = (Patient) results[1];
				generator.writeString(String.valueOf(patient.getPatientId()));
				generator.writeString(patient.getPatientIdentifier().getIdentifier());
				generator.writeString(patient.getGivenName());
				generator.writeString(patient.getMiddleName());
				generator.writeString(patient.getFamilyName());
				
				Integer counter = (Integer) results[0];
				generator.writeNumber(counter);
				
				generator.writeEndArray();
			}
			
			generator.writeEndArray();
			
			generator.writeNumberField("sEcho", echo);
			generator.writeNumberField("iTotalRecords", totalPairs);
			generator.writeNumberField("iTotalDisplayRecords", filtered);
			generator.writeEndObject();
			
			generator.close();
		}
	}
}
