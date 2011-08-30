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

package org.openmrs.module.clinicalsummary.web.controller.response;

import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.service.UtilService;
import org.openmrs.module.clinicalsummary.util.response.MedicationResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ViewEncounterController {

	private static final Log log = LogFactory.getLog(ViewEncounterController.class);

	@RequestMapping(method = RequestMethod.POST, value = "/module/clinicalsummary/response/viewEncounter")
	public
	@ResponseBody
	Integer processViewEncounter(final @RequestParam(required = true, value = "id") Integer responseId) {
		if (Context.isAuthenticated()) {
			UtilService service = Context.getService(UtilService.class);
			MedicationResponse medicationResponse = service.getResponse(MedicationResponse.class, responseId);
			List<Encounter> encounters = Context.getEncounterService().getEncountersByPatient(medicationResponse.getPatient());

			Integer counter = 0;
			Encounter medicationEncounter = null;
			while (counter < encounters.size() && medicationEncounter == null) {
				Encounter encounter = encounters.get(counter++);
				if (DateUtils.isSameDay(encounter.getEncounterDatetime(), medicationResponse.getDatetime()))
					medicationEncounter = encounter;
			}

			if (medicationEncounter != null)
				return medicationEncounter.getEncounterId();
		}

		return null;
	}

}
