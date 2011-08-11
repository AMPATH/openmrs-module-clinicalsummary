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
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.Mapping;
import org.openmrs.module.clinicalsummary.MappingType;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.service.SummaryService;
import org.openmrs.module.clinicalsummary.web.editor.EncounterTypeEditor;
import org.openmrs.module.clinicalsummary.web.editor.SummaryEditor;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 */
@Controller
@RequestMapping("/module/clinicalsummary/summary/mappingForm")
public class MappingFormController {

	private static final Log log = LogFactory.getLog(MappingFormController.class);

	@InitBinder
	public void registerEditor(final WebDataBinder binder) {
		binder.registerCustomEditor(EncounterType.class, new EncounterTypeEditor());
		binder.registerCustomEditor(Summary.class, new SummaryEditor());
	}

	@RequestMapping(method = RequestMethod.GET)
	public void populatePage(final @RequestParam(value = "id", required = false) Integer id, final Model model) {
		if (log.isDebugEnabled())
			log.debug("Processing new summary object ...");

		Mapping mapping = null;
		if (id != null)
			mapping = Context.getService(SummaryService.class).getMapping(id);

		if (mapping == null)
			mapping = new Mapping();

		model.addAttribute("mapping", mapping);
		model.addAttribute("encounterTypes", Context.getEncounterService().getAllEncounterTypes());
		model.addAttribute("summaries", Context.getService(SummaryService.class).getAllSummaries());
		model.addAttribute("mappingTypes", MappingType.values());
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processPage(final @ModelAttribute("mapping") Mapping mapping, final HttpSession session) {
		Context.getService(SummaryService.class).saveMapping(mapping);
		session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "clinicalsummary.mapping.saved");
		return "redirect:mappingList.list";
	}
}
