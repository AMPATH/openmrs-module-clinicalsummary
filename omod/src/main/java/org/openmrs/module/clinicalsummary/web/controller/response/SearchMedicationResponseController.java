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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.enumeration.ResponseDisplayType;
import org.openmrs.module.clinicalsummary.service.UtilService;
import org.openmrs.module.clinicalsummary.util.response.MedicationResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/module/clinicalsummary/response/medicationResponseSearch")
public class SearchMedicationResponseController {

	private static final Log log = LogFactory.getLog(SearchMedicationResponseController.class);

	@RequestMapping(method = RequestMethod.POST)
	public
	@ResponseBody
	Map<Integer, List<MedicationResponseForm>> searchResponses(final @RequestParam(required = true, value = "locationId") String locationId,
	                                                           final @RequestParam(required = false, value = "displayType") ResponseDisplayType displayType) {

		Map<Integer, List<MedicationResponseForm>> responseMap = new HashMap<Integer, List<MedicationResponseForm>>();
		if (Context.isAuthenticated()) {
			// prepare the calendar to limit the returned responses
			Calendar calendar = Calendar.getInstance();
			// end date are determined by the parameter value passed by the user
			Date startDate;
			switch (displayType) {
				case DISPLAY_PAST_WEEK_RESPONSES:
					calendar.add(Calendar.DATE, -7);
					startDate = calendar.getTime();
					break;
				case DISPLAY_PAST_MONTH_RESPONSES:
					calendar.add(Calendar.MONTH, -1);
					startDate = calendar.getTime();
					break;
				case DISPLAY_PAST_2_MONTHS_RESPONSES:
					calendar.add(Calendar.MONTH, -1);
					startDate = calendar.getTime();
					break;
				case DISPLAY_PAST_6_MONTHS_RESPONSES:
					calendar.add(Calendar.MONTH, -1);
					startDate = calendar.getTime();
					break;
				case DISPLAY_PAST_12_MONTHS_RESPONSES:
					calendar.add(Calendar.MONTH, -1);
					startDate = calendar.getTime();
					break;
				default:
					startDate = null;
			}
			// search for the location passed by the user
			Location location = Context.getLocationService().getLocation(NumberUtils.toInt(locationId, 0));
			// search for all matching responses from the database
			UtilService service = Context.getService(UtilService.class);
			List<MedicationResponse> responses = service.getResponses(MedicationResponse.class, location, startDate, new Date());
			for (MedicationResponse response : responses) {
				try {
					Integer patientId = response.getPatient().getPatientId();
					// search if the medication forms for teh patient is already in the map or not
					List<MedicationResponseForm> responseForms = responseMap.get(patientId);
					if (responseForms == null) {
						// initialize when we don't have the list yet
						responseForms = new ArrayList<MedicationResponseForm>();
						responseMap.put(patientId, responseForms);
					}
					// add the current response to the list
					MedicationResponseForm responseForm = new MedicationResponseForm();
					BeanUtils.copyProperties(responseForm, response);
					responseForm.setPatientId(response.getPatient().getPatientId());
					responseForm.setPatientName(response.getPatient().getPersonName().getFullName());
					responseForm.setProviderName(response.getProvider().getPersonName().getFullName());
					responseForm.setLocationName(response.getLocation().getName());
					responseForm.setMedicationName(response.getMedication().getName(Context.getLocale()).getName());
					responseForm.setDatetime(Context.getDateFormat().format(response.getDatetime()));
					if (response.getActionType() != null)
						responseForm.setAction(response.getActionType().getValue());
					// add to the output list
					responseForms.add(responseForm);
				} catch (Exception e) {
					log.error("Exception thrown when trying to copy response object.", e);
				}
			}
		}
		// return the formatted output
		return responseMap;
	}

}
