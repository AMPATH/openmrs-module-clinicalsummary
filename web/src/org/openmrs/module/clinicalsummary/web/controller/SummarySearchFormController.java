package org.openmrs.module.clinicalsummary.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class SummarySearchFormController {
	
	@RequestMapping(value = "/module/clinicalsummary/summarySearch", method = RequestMethod.GET)
	public void preparePage(ModelMap map) {
	}
}
