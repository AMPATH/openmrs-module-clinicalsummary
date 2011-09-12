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

package org.openmrs.module.clinicalsummary.web.controller.evaluator;

import java.util.Date;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.service.CoreService;
import org.openmrs.module.clinicalsummary.web.controller.WebUtils;
import org.openmrs.module.clinicalsummary.web.controller.evaluator.engine.RuleCohortEvaluatorInstance;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = "/module/clinicalsummary/evaluator/evaluateRule")
public class EvaluateRuleController {

	private static final Log log = LogFactory.getLog(EvaluateRuleController.class);

	@RequestMapping(method = RequestMethod.GET)
	public void populatePage(final ModelMap map) {
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processForm(final @RequestParam(required = false, value = "locationId") String locationId,
	                          final @RequestParam(required = false, value = "obsStartDate") String obsStartDate,
	                          final @RequestParam(required = false, value = "obsEndDate") String obsEndDate,
	                          final HttpSession session) {

		int maxInactiveInterval = session.getMaxInactiveInterval();
		session.setMaxInactiveInterval(-1);

		if (StringUtils.isBlank(locationId)) {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "clinicalsummary.invalid.parameters");
		} else {
			Location location = Context.getLocationService().getLocation(NumberUtils.toInt(locationId, -1));
			Date startDate = WebUtils.parse(obsStartDate, null);
			Date endDate = WebUtils.parse(obsEndDate, null);
			Cohort cohort = Context.getService(CoreService.class).getDateCreatedCohort(location, startDate, endDate);
			RuleCohortEvaluatorInstance.getInstance().evaluate(cohort);
		}
		session.setMaxInactiveInterval(maxInactiveInterval);
		return "redirect:evaluateRule.form";
	}

}
