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

package org.openmrs.module.clinicalsummary.web.controller.reminder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.enumeration.ReportDisplayType;
import org.openmrs.module.clinicalsummary.service.ReminderService;
import org.openmrs.module.clinicalsummary.web.controller.WebUtils;
import org.openmrs.module.clinicalsummary.web.editor.ListStringEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 */
@Controller
@RequestMapping("/module/clinicalsummary/reminder/reminderList")
public class ReminderListController {

	private static final Log log = LogFactory.getLog(ReminderListController.class);

	@InitBinder
	public void registerEditor(final WebDataBinder binder) {
		binder.registerCustomEditor(Collection.class, new ListStringEditor());
	}

	@RequestMapping(method = RequestMethod.GET)
	public void populatePage(final ModelMap map) {
		map.addAttribute("displayTypes", Arrays.asList(ReportDisplayType.DISPLAY_REPORT_BY_PROVIDER,
				ReportDisplayType.DISPLAY_REPORT_BY_LOCATION));
	}

	@RequestMapping(method = RequestMethod.POST)
	public
	@ResponseBody
	List<String[]> processSubmit(final @RequestParam(required = true, value = "displayType") ReportDisplayType displayType,
	                             final @RequestParam(required = false, value = "locations") Collection<String> locationNames,
	                             final @RequestParam(required = false, value = "tokens") Collection<String> tokens,
	                             final @RequestParam(required = false, value = "startTime") String startTime,
	                             final @RequestParam(required = false, value = "endTime") String endTime, final ModelMap map) {

		map.addAttribute("displayTypes", Arrays.asList(ReportDisplayType.DISPLAY_REPORT_BY_PROVIDER,
				ReportDisplayType.DISPLAY_REPORT_BY_LOCATION));

		Date startDate = null;
		if (StringUtils.isNotEmpty(startTime))
			startDate = WebUtils.parse(startTime, new Date());

		Date endDate = null;
		if (StringUtils.isNotEmpty(endTime))
			endDate = WebUtils.parse(endTime, new Date());

		Map<String, Collection<OpenmrsObject>> restrictions = new Hashtable<String, Collection<OpenmrsObject>>();
		restrictions.put("location", WebUtils.getOpenmrsObjects(locationNames, Location.class));

		Collection<String> groupingProperties = getGroupingProperties(displayType);

		ReminderService service = Context.getService(ReminderService.class);
		List<Object[]> objects = service.aggregateReminders(restrictions, groupingProperties, startDate, endDate);
		return serialize(objects);
	}

	private List<String[]> serialize(final List<Object[]> objects) {
		List<String[]> serialized = new ArrayList<String[]>();
		for (Object[] object : objects) {
			String[] strings = new String[object.length];
			for (int i = 0; i < object.length; i++)
				strings[i] = WebUtils.getStringValue(object[i]);
			serialized.add(strings);
		}
		return serialized;
	}

	private Collection<String> getGroupingProperties(final ReportDisplayType displayType) {
		Collection<String> groupingProperties = new ArrayList<String>();
		switch (displayType) {
			case DISPLAY_REPORT_BY_PROVIDER:
				groupingProperties.add("provider");
				break;
			// the default is aggregate by patient
			default:
				groupingProperties.add("location");
				break;
		}
		groupingProperties.add("token");
		return groupingProperties;
	}
}
