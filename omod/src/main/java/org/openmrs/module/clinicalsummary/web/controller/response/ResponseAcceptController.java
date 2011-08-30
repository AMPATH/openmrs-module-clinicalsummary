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

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.cache.CacheUtils;
import org.openmrs.module.clinicalsummary.enumeration.ActionType;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.service.UtilService;
import org.openmrs.module.clinicalsummary.util.response.MedicationResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ResponseAcceptController {

	private static final Log log = LogFactory.getLog(ResponseAcceptController.class);

	@RequestMapping(method = RequestMethod.POST, value = "/module/clinicalsummary/response/acceptResponse")
	public
	@ResponseBody
	Boolean processAccept(final @RequestParam(required = true, value = "id") Integer responseId,
	                      final @RequestParam(required = true, value = "comment") String comment) {

		if (Context.isAuthenticated()) {
			UtilService service = Context.getService(UtilService.class);
			MedicationResponse medicationResponse = service.getResponse(MedicationResponse.class, responseId);

			if (medicationResponse.getStatus() == 1) {
				// search for the encounter
				List<Encounter> encounters = Context.getEncounterService().getEncountersByPatient(medicationResponse.getPatient());

				Integer counter = 0;
				Encounter medicationEncounter = null;
				while (counter < encounters.size() && medicationEncounter == null) {
					Encounter encounter = encounters.get(counter++);
					if (DateUtils.isSameDay(encounter.getEncounterDatetime(), medicationResponse.getDatetime()))
						medicationEncounter = encounter;
				}

				if (medicationEncounter != null) {
					Obs obs = new Obs();
					obs.setObsDatetime(new Date());
					obs.setEncounter(medicationEncounter);
					obs.setPerson(medicationEncounter.getPatient());
					obs.setConcept(CacheUtils.getConcept(EvaluableNameConstants.MEDICATION_ADDED));
					obs.setValueCoded(medicationResponse.getMedication());
					obs.setLocation(medicationEncounter.getLocation());
					Context.getObsService().saveObs(obs, comment);

					String extraComment = " --obs id = " + obs.getId();

					medicationResponse.setReviewer(Context.getAuthenticatedUser().getPerson());
					medicationResponse.setDateReviewed(new Date());
					medicationResponse.setReviewComment(comment + extraComment);
					medicationResponse.setActionType(ActionType.ACTION_ACCEPT);
					service.saveResponse(medicationResponse);

					return Boolean.TRUE;
				}

			} else if (medicationResponse.getStatus() == -1) {
				medicationResponse.setReviewer(Context.getAuthenticatedUser().getPerson());
				medicationResponse.setDateReviewed(new Date());
				medicationResponse.setReviewComment(comment);
				medicationResponse.setActionType(ActionType.ACTION_ACCEPT);
				service.saveResponse(medicationResponse);

				return Boolean.TRUE;
			}
		}

		return Boolean.FALSE;
	}

}
