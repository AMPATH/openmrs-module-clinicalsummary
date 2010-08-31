package org.openmrs.module.clinicalsummary.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.EncounterType;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.MappingPosition;
import org.openmrs.module.clinicalsummary.SummaryService;
import org.openmrs.module.clinicalsummary.SummaryTemplate;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/module/clinicalsummary/summaryForm")
public class SummaryFormController {
	
	/** Logger for this class and subclasses */
	private static final Log log = LogFactory.getLog(SummaryFormController.class);
	
	@RequestMapping(method = RequestMethod.POST)
	public String saveTemplate(
	                           @ModelAttribute("summary") SummaryTemplate summaryTemplate,
	                           @RequestParam(required = false, value = "encounterTypeNames") List<String> encounterTypeNames,
	                           HttpServletRequest request) {
		
		if (Context.isAuthenticated()) {
			
			HttpSession httpSession = request.getSession();
			
			SummaryService css = Context.getService(SummaryService.class);
			
			EncounterService encounterService = Context.getEncounterService();
			for (String name : encounterTypeNames) {
				EncounterType encounterType = encounterService.getEncounterType(name);
				
				if (log.isDebugEnabled())
					log.debug("Searching for encounter type with name: " + name + ", with result: " + encounterType);
				
				if (encounterType != null)
					summaryTemplate.addEncounterType(encounterType);
			}
			
			css.saveTemplate(summaryTemplate);
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "clinicalsummary.saved");
			
			if (log.isDebugEnabled())
				log.debug("Saving summary template: " + summaryTemplate.getName());
		}
		
		return "redirect:summaryList.list";
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public void preparePage(@RequestParam(required = false, value = "templateId") Integer templateId, ModelMap map) {
		if (Context.isAuthenticated()) {
			SummaryTemplate summary = null;
			
			if (templateId != null) {
				SummaryService css = Context.getService(SummaryService.class);
				summary = css.getTemplate(templateId);
			}
			
			if (summary == null)
				summary = new SummaryTemplate();
			
			map.addAttribute("summary", summary);
			map.addAttribute("positions", MappingPosition.values());
		}
	}
}
