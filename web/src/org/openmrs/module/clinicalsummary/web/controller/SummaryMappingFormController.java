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
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.EncounterType;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SummaryMappingFormController {
	
	private static final Log log = LogFactory.getLog(SummaryMappingFormController.class);
	
	@RequestMapping("/module/clinicalsummary/summaryMapping")
	public void tokenAutoComplete(@RequestParam("q") String partialName, HttpServletResponse response) throws IOException {
		if (Context.isAuthenticated()) {
			EncounterService encounterService = Context.getEncounterService();
			List<EncounterType> encounterTypes = encounterService.findEncounterTypes(partialName);
			String lineSeparator = System.getProperty("line.separator");
			OutputStream stream = response.getOutputStream();
			for (EncounterType encounterType : encounterTypes) {
				if (log.isDebugEnabled())
					log.debug("Adding encounter type name: " + encounterType.getName());
				String typeName = encounterType.getName();
				stream.write(typeName.getBytes());
				stream.write(lineSeparator.getBytes());
			}
			stream.close();
		}
	}
}
