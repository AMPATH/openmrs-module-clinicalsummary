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

import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.service.IndexService;
import org.openmrs.module.clinicalsummary.web.controller.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 */
@Controller
@RequestMapping("/module/clinicalsummary/utils/initialSummaries")
public class InitialSummariesController {

	private static final Log log = LogFactory.getLog(InitialSummariesController.class);

	@RequestMapping(method = RequestMethod.GET)
	public void populatePage(final ModelMap map) {
	}

	@RequestMapping(method = RequestMethod.POST)
	public void processSubmit(final @RequestParam(required = false, value = "locationId") Integer locationId,
	                          final @RequestParam(required = false, value = "initialDate") String initial) {

		Location location = Context.getLocationService().getLocation(locationId);
		if (location != null) {
			IndexService indexService = Context.getService(IndexService.class);
			Date initialDate = WebUtils.parse(initial, indexService.getInitialDate(location));
			indexService.saveInitialDate(location, initialDate);
		}
	}
}
