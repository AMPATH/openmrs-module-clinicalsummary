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

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.SummaryService;
import org.openmrs.module.clinicalsummary.engine.GeneratorEngine;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/module/clinicalsummary/generateSummaries")
public class GenerateSummariesFormController {
	
	private static final String LOCALIZED_MESSAGE = "No location or patient identifiers are being passed to the generator";
	
	private static final Log log = LogFactory.getLog(GenerateSummariesFormController.class);
	
	@RequestMapping(method = RequestMethod.GET)
	public void preparePage(ModelMap map) {
		if (Context.isAuthenticated())
			map.addAttribute("locations", Context.getLocationService().getAllLocations());
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public void generateSummaries(@RequestParam(required = false, value = "patientIdentifiers") String patientIdentifiers,
	                              @RequestParam(required = false, value = "locationId") String locationId,
	                              @RequestParam(required = false, value = "endReturnDate") String endReturnDate,
	                              @RequestParam(required = false, value = "startReturnDate") String startReturnDate,
	                              HttpServletRequest request) {
		
		if (Context.isAuthenticated()) {
			
			HttpSession httpSession = request.getSession();
			// don't let the session expire
			httpSession.setMaxInactiveInterval(-1);
			
			SummaryService summaryService = Context.getService(SummaryService.class);
			PatientService patientService = Context.getPatientService();
			
			Cohort cohort = new Cohort();
			
			if (StringUtils.isBlank(locationId) && StringUtils.isBlank(patientIdentifiers)) {
				// TODO: make this message localized
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, LOCALIZED_MESSAGE);
			} else {
				// only use one of the following way to get the cohort
				// priority is patient identifiers
				if (StringUtils.isNotBlank(patientIdentifiers)) {
					String[] patientIdentifier = StringUtils.split(patientIdentifiers);
					for (int i = 0; i < ArrayUtils.getLength(patientIdentifier); i++) {
						List<Patient> patients = patientService.getPatients(patientIdentifier[i]);
						for (Patient patient : patients)
							cohort.addMember(patient.getPatientId());
					}
				} else if (StringUtils.isNotBlank(locationId)) {
					Location location = Context.getLocationService().getLocation(NumberUtils.toInt(locationId, -1));
					Date startDate = WebUtils.parse(startReturnDate, new Date());
					Date endDate = WebUtils.parse(endReturnDate, new Date());
					cohort = summaryService.getCohortByLocation(location, startDate, endDate);
				}
				
				GeneratorEngine.generateSummary(cohort);
			}
		}
	}
}
