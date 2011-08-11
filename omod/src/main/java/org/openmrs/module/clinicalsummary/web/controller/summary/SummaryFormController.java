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

package org.openmrs.module.clinicalsummary.web.controller.summary;

import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.service.SummaryService;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 */
@Controller
@RequestMapping("/module/clinicalsummary/summary/summaryForm")
public class SummaryFormController {

	private static final Log log = LogFactory.getLog(SummaryFormController.class);

	@RequestMapping(method = RequestMethod.GET)
	public void populatePage(final @RequestParam(value = "id", required = false) Integer id, Model model) {
		if (log.isDebugEnabled())
			log.debug("Processing new summary object ...");

		Summary summary = null;
		if (id != null)
			summary = Context.getService(SummaryService.class).getSummary(id);

		if (summary == null)
			summary = new Summary();

		model.addAttribute("summary", summary);
	}

	@RequestMapping(method = RequestMethod.POST)
	public void processPage(final @ModelAttribute("summary") Summary summary, final HttpSession session) {
		Context.getService(SummaryService.class).saveSummary(summary);
		session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "clinicalsummary.summary.saved");
	}
}
