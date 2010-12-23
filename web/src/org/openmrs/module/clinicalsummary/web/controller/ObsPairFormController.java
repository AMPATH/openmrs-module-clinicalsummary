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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.ObsPair;
import org.openmrs.module.clinicalsummary.SummaryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ObsPairFormController {
	
	@RequestMapping(value = "/module/clinicalsummary/obsPairForm", method = RequestMethod.POST)
	public void preparePage(@RequestParam(required = false, value = "patientId") Integer patientId, ModelMap map) {
		if (Context.isAuthenticated()) {
			SummaryService service = Context.getService(SummaryService.class);
			Patient patient = Context.getPatientService().getPatient(patientId);
			
			List<ObsPair> pairs = service.getObsPairForPatient(patient);
			Map<Integer, String> conceptNames = new HashMap<Integer, String>();
			for (ObsPair obsPair : pairs) {
				Concept concept = obsPair.getConcept();
				String name = concept.getName(Context.getLocale(), false).getName();
				conceptNames.put(concept.getConceptId(), name);
				
				// this answer could be null
				Concept answer = obsPair.getAnswer();
				if (answer != null) {
					String answerName = answer.getName(Context.getLocale(), false).getName();
					conceptNames.put(answer.getConceptId(), answerName);
				}
			}
			
			map.addAttribute("patient", patient);
			map.addAttribute("conceptNames", conceptNames);
			map.addAttribute("obsPairs", pairs);
		}
	}
}
