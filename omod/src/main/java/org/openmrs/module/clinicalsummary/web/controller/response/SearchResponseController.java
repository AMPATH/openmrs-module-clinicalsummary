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
import java.util.List;

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
@RequestMapping("/module/clinicalsummary/response/responseSearch")
public class SearchResponseController {

	private static final Log log = LogFactory.getLog(SearchResponseController.class);

	@RequestMapping(method = RequestMethod.POST)
	public
	@ResponseBody
	List<MedicationResponse> searchResponses(final @RequestParam(required = true, value = "locationId") String locationId,
	                                         final @RequestParam(required = false, value = "displayType") ResponseDisplayType displayType) {

		if (Context.isAuthenticated()) {
			// prepare the calendar to limit the returned responses
			Calendar calendar = Calendar.getInstance();
			// end date are determined by the parameter value passed by the user
			Date startDate;
			switch (displayType) {
				case DISPLAY_THIS_WEEK_RESPONSES:
					calendar.add(Calendar.DATE, -7);
					startDate = calendar.getTime();
					break;
				case DISPLAY_THIS_MONTH_RESPONSES:
					calendar.add(Calendar.MONTH, -1);
					startDate = calendar.getTime();
					break;
				default:
					startDate = new Date();
			}
			// search for the location passed by the user
			Location location = Context.getLocationService().getLocation(NumberUtils.toInt(locationId, 0));
			// search for all matching responses from the database
			return Context.getService(UtilService.class).getResponses(MedicationResponse.class, location, startDate, new Date());
		}
		// return the formatted output
		return new ArrayList<MedicationResponse>();
	}

}
