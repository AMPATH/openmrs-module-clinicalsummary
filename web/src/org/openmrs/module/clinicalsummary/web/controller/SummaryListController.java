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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.SummaryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/module/clinicalsummary/summaryList")
public class SummaryListController {
	
	/** Logger for this class and subclasses */
	private static final Log log = LogFactory.getLog(SummaryListController.class);
	
	@RequestMapping(method = RequestMethod.GET)
	public void preparePage(ModelMap map) {
		if (Context.isAuthenticated()) {
			if (log.isDebugEnabled())
				log.debug("Preparing list of summary templates");
			
			SummaryService summaryService = Context.getService(SummaryService.class);
			map.addAttribute("summaries", summaryService.getAllTemplates());
		}
	}
}
