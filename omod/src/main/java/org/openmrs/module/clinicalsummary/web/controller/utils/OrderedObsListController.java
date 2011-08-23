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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.enumeration.ReportDisplayType;
import org.openmrs.module.clinicalsummary.enumeration.StatusType;
import org.openmrs.module.clinicalsummary.service.UtilService;
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
@RequestMapping("/module/clinicalsummary/utils/orderedObsList")
public class OrderedObsListController {

	private static final Log log = LogFactory.getLog(OrderedObsListController.class);

	@InitBinder
	public void registerEditor(final WebDataBinder binder) {
		binder.registerCustomEditor(Collection.class, new ListStringEditor());
	}

	@RequestMapping(method = RequestMethod.GET)
	public void populatePage(final ModelMap map) {
		preparePage(map);
	}

	@RequestMapping(method = RequestMethod.POST)
	public
	@ResponseBody
	List<String[]> processSubmit(final @RequestParam(required = true, value = "displayType") ReportDisplayType displayType,
	                             final @RequestParam(required = false, value = "locations") Collection<String> locationNames,
	                             final @RequestParam(required = false, value = "status") StatusType statusType,
	                             final @RequestParam(required = false, value = "concepts") Collection<String> conceptNames,
	                             final @RequestParam(required = false, value = "codedValues") Collection<String> codedValueNames,
	                             final @RequestParam(required = false, value = "startTime") String startTime,
	                             final @RequestParam(required = false, value = "endTime") String endTime) {
		Date startDate = null;
		if (StringUtils.isNotEmpty(startTime))
			startDate = WebUtils.parse(startTime, new Date());

		Date endDate = null;
		if (StringUtils.isNotEmpty(endTime))
			endDate = WebUtils.parse(endTime, new Date());

		Map<String, Collection<OpenmrsObject>> restrictions = new Hashtable<String, Collection<OpenmrsObject>>();
		restrictions.put("location", WebUtils.getOpenmrsObjects(locationNames, Location.class));
		restrictions.put("concept", WebUtils.getOpenmrsObjects(conceptNames, Concept.class));
		restrictions.put("valueCoded", WebUtils.getOpenmrsObjects(codedValueNames, Concept.class));

		Collection<String> groupingProperties = getGroupingProperties(displayType);

		UtilService service = Context.getService(UtilService.class);
		List<Object[]> objects = service.aggregateOrderedObs(restrictions, groupingProperties, statusType, startDate, endDate);
		return serialize(process(objects), statusType);
	}

	private Map<Object, Map<Object, Object[]>> process(final List<Object[]> objects) {
		Map<Object, Map<Object, Object[]>> objectMaps = new Hashtable<Object, Map<Object, Object[]>>();
		for (Object[] object : objects) {
			// see if we already have the second mapping in the first mapping
			Map<Object, Object[]> groupedObjects = objectMaps.get(object[0]);
			if (groupedObjects == null) {
				groupedObjects = new Hashtable<Object, Object[]>();
				objectMaps.put(object[0], groupedObjects);
			}
			groupedObjects.put(object[1], object);
		}
		return objectMaps;
	}

	/**
	 * @param firstMapping
	 * @param selectedStatusType
	 * @return
	 */
	private List<String[]> serialize(final Map<Object, Map<Object, Object[]>> firstMapping, final StatusType selectedStatusType) {

		List<StatusType> statuses = Arrays.asList(StatusType.values());
		if (selectedStatusType != null)
			statuses = Arrays.asList(selectedStatusType);

		List<String[]> serialized = new ArrayList<String[]> ();
		for (Object groupingObject : firstMapping.keySet()) {
			Map<Object, Object[]> groupedObjects = firstMapping.get(groupingObject);
			// when user don't pick any status, some grouping for a certain status might not have data
			for (StatusType statusType : statuses) {
				// name of the grouping element (ex: concept name, location name, person name)
				String groupingName = WebUtils.getStringValue(groupingObject);
				// id of the grouping element (ex: concept id, location id, person id)
				String groupingId = WebUtils.getIdValue(groupingObject);
				// the data is not there and we need to fill the missing values
				String[] strings = new String[]{groupingName, groupingId, statusType.getValue(), String.valueOf(0)};
				if (groupedObjects.containsKey(statusType)) {
					Object[] object = groupedObjects.get(statusType);
					// total number of the element in the grouping
					strings[strings.length - 1] = WebUtils.getStringValue(object[object.length - 1]);
				}
				CollectionUtils.addIgnoreNull(serialized, strings);
			}
		}
		return serialized;
	}

	/**
	 * @param map
	 */
	private void preparePage(final ModelMap map) {
		Map<StatusType, String> statusDescriptionMap = new TreeMap<StatusType, String>();
		statusDescriptionMap.put(StatusType.STATUS_DUPLICATE_RESULTS, "Duplicate Result For Test");
		statusDescriptionMap.put(StatusType.STATUS_NO_RESULT, "Test Ordered Without Result");
		statusDescriptionMap.put(StatusType.STATUS_NO_ORDER, "Result Without Order");

		map.addAttribute("displayTypes", Arrays.asList(ReportDisplayType.DISPLAY_REPORT_BY_TEST,
				ReportDisplayType.DISPLAY_REPORT_BY_RESULT, ReportDisplayType.DISPLAY_REPORT_BY_PROVIDER,
				ReportDisplayType.DISPLAY_REPORT_BY_LOCATION));
		map.addAttribute("statuses", statusDescriptionMap);
	}

	/**
	 * @param displayType
	 * @return
	 */
	private Collection<String> getGroupingProperties(final ReportDisplayType displayType) {
		Collection<String> groupingProperties = new ArrayList<String>();
		switch (displayType) {
			case DISPLAY_REPORT_BY_RESULT:
				groupingProperties.add("concept");
				break;
			case DISPLAY_REPORT_BY_TEST:
				groupingProperties.add("valueCoded");
				break;
			case DISPLAY_REPORT_BY_PROVIDER:
				groupingProperties.add("provider");
				break;
			// the default is aggregate by patient
			default:
				groupingProperties.add("location");
				break;
		}
		groupingProperties.add("status");
		return groupingProperties;
	}
}
