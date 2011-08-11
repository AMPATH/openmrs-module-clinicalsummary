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

package org.openmrs.module.clinicalsummary.web.controller.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.service.IndexService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 */
@Controller
@RequestMapping("/module/clinicalsummary/utils/initialSummariesSearch")
public class InitialSummariesSearchController {

	private static final Log log = LogFactory.getLog(InitialSummariesSearchController.class);

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	public String getInitialDate(final @RequestParam(required = false, value = "locationId") Integer locationId) {
		Location location = Context.getLocationService().getLocation(locationId);
		if (location != null) {
			Date initialDate = Context.getService(IndexService.class).getInitialDate(location);
			if (initialDate != null)
				return Context.getDateFormat().format(initialDate);
		}
		return StringUtils.EMPTY;
	}
}
