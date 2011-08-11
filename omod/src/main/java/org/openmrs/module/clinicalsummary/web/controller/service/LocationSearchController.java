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

package org.openmrs.module.clinicalsummary.web.controller.service;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.clinicalsummary.web.controller.service.xstream.LocationConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 */
@Controller
@RequestMapping("/module/clinicalsummary/service/location/search")
public class LocationSearchController {

	private static final Log log = LogFactory.getLog(LocationSearchController.class);

	@RequestMapping(method = RequestMethod.GET)
	public void searchLocation(@RequestParam(required = false, value = "username") String username,
	                           @RequestParam(required = false, value = "password") String password,
	                           @RequestParam(required = false, value = "term") String term,
	                           HttpServletResponse response) throws IOException {
		try {
			if (!Context.isAuthenticated())
				Context.authenticate(username, password);
			// search the locations with matching the search term
			List<Location> locations = Context.getLocationService().getLocations(term);
			// serialize the locations
			XStream xStream = new XStream();
			xStream.alias("results", List.class);
			xStream.alias("location", Location.class);
			xStream.registerConverter(new LocationConverter());
			xStream.toXML(locations, response.getOutputStream());
		} catch (ContextAuthenticationException e) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}
}
