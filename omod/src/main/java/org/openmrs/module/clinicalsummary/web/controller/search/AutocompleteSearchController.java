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

package org.openmrs.module.clinicalsummary.web.controller.search;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.service.UtilService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 */
@Controller
public class AutocompleteSearchController {

	private static final Log log = LogFactory.getLog(AutocompleteSearchController.class);

	@RequestMapping(value = "/module/clinicalsummary/search/autocompleteLocation", method = RequestMethod.GET)
	public
	@ResponseBody
	Collection<String> searchLocation(final @RequestParam(required = true, value = "term") String nameFragment) {
		Collection<String> names = new TreeSet<String>();
		Collection<Location> locations = Context.getLocationService().getLocations(nameFragment);
		for (Location location : locations)
			names.add(location.getName());
		return names;
	}

	@RequestMapping(value = "/module/clinicalsummary/search/autocompleteConcept", method = RequestMethod.GET)
	public
	@ResponseBody
	Collection<String> searchConcept(final @RequestParam(required = true, value = "term") String nameFragment) {
		UtilService utilService = Context.getService(UtilService.class);
		List<Object[]> objects = utilService.aggregateOrderedObs(Collections.<String, Collection<OpenmrsObject>>emptyMap(), Arrays.asList("concept"));

		Collection<String> names = new TreeSet<String>();
		for (Object[] object : objects) {
			for (Object o : object) {
				if (ClassUtils.isAssignable(o.getClass(), Concept.class)) {
					Concept concept = (Concept) o;
					names.add(concept.getName(Context.getLocale()).getName());
				}
			}
		}
		return names;
	}

	@RequestMapping(value = "/module/clinicalsummary/search/autocompleteValueCoded", method = RequestMethod.GET)
	public
	@ResponseBody
	Collection<String> searchValueCoded(final @RequestParam(required = true, value = "term") String nameFragment) {
		UtilService utilService = Context.getService(UtilService.class);
		List<Object[]> objects = utilService.aggregateOrderedObs(Collections.<String, Collection<OpenmrsObject>>emptyMap(), Arrays.asList("valueCoded"));

		Collection<String> names = new TreeSet<String>();
		for (Object[] object : objects) {
			for (Object o : object) {
				if (ClassUtils.isAssignable(o.getClass(), Concept.class)) {
					Concept concept = (Concept) o;
					names.add(concept.getName(Context.getLocale()).getName());
				}
			}
		}
		return names;
	}

	@RequestMapping(value = "/module/clinicalsummary/search/autocompleteToken", method = RequestMethod.GET)
	public
	@ResponseBody
	Collection<String> searchToken(final @RequestParam(required = true, value = "term") String nameFragment) {
		return Context.getLogicService().getTokens(nameFragment);
	}
}
