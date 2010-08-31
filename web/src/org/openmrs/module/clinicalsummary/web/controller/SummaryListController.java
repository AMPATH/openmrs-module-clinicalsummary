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
