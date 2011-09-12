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

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.web.controller.evaluator.engine.ReminderCohortEvaluatorInstance;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 */
@Controller
@RequestMapping(value = "/module/clinicalsummary/evaluator/evaluateReminder")
public class EvaluateReminderController {

	private static final Log log = LogFactory.getLog(EvaluateReminderController.class);

	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.GET)
	public void populatePage(final ModelMap map) {
		map.put("cohorts", Context.getCohortService().getAllCohorts());
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processForm(final @RequestParam(required = false, value = "cohort") Integer cohortId,
	                          final HttpSession session) {
		int maxInactiveInterval = session.getMaxInactiveInterval();
		session.setMaxInactiveInterval(-1);
		Cohort cohort = Context.getCohortService().getCohort(cohortId);
		ReminderCohortEvaluatorInstance.getInstance().evaluate(new Cohort(cohort.getMemberIds()));
		session.setMaxInactiveInterval(maxInactiveInterval);
		return "redirect:evaluatePatients.form";
	}
}
