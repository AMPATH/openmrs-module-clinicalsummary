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

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.SummaryService;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 */
@Controller
public class InitialSummaryFormController {
	
	private static final Log log = LogFactory.getLog(InitialSummaryFormController.class);
	
	@RequestMapping(method = RequestMethod.GET, value = "/module/clinicalsummary/initialSummaryDate")
	public void getInitialSummaryDate(@RequestParam(required = false, value = "locationId") String locationId,
	                                  HttpServletResponse response) {
		if (Context.isAuthenticated()) {
			Location location = Context.getLocationService().getLocation(NumberUtils.toInt(locationId, -1));
			if (location != null) {
				try {
					response.setContentType("application/json");
					
					ObjectMapper mapper = new ObjectMapper();
					JsonFactory factory = mapper.getJsonFactory();
					
					JsonGenerator generator = factory.createJsonGenerator(response.getOutputStream(), JsonEncoding.UTF8);
					generator.useDefaultPrettyPrinter();
					
					JsonNode rootNode = mapper.createObjectNode();
					
					Date date = Context.getService(SummaryService.class).getEarliestIndex(location);
					String dateString = "N/A";
					if (date != null)
						dateString = Context.getDateFormat().format(date);
					((ObjectNode) rootNode).put("date", dateString);
					
					mapper.writeTree(generator, rootNode);
					generator.close();
				}
				catch (IOException e) {
					log.error("Failed fetching earliest summary date for location: " + location.getName(), e);
				}
			}
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/module/clinicalsummary/initialSummary")
	public void preparePage(ModelMap map) {
		map.addAttribute("locations", Context.getLocationService().getAllLocations());
		Date date = Context.getService(SummaryService.class).getEarliestIndex(null);
		String dateString = "N/A";
		if (date != null)
			dateString = Context.getDateFormat().format(date);
		map.addAttribute("defaultDate", dateString);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/module/clinicalsummary/initialSummary")
	public void processInitialDate(@RequestParam(required = false, value = "locationId") String locationId,
	                               @RequestParam(required = false, value = "initialDate") String initial,
	                               HttpServletRequest request) {
		if (Context.isAuthenticated()) {
			Location location = Context.getLocationService().getLocation(NumberUtils.toInt(locationId, -1));
			if (location != null) {
				SummaryService service = Context.getService(SummaryService.class);
				Date initialDate = WebUtils.parse(initial, new Date());
				Date savedInitialDate = service.getEarliestIndex(location);
				Date todayDate = new Date();
				// no need to update when they are already the same date
				HttpSession session = request.getSession();
				if (!initialDate.after(todayDate)) {
					if (!initialDate.equals(savedInitialDate)) {
						int counter = service.updateIndexesInitialDate(location, initialDate);
						String message = "Updating completed. " + counter + " index updated.";
						session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, message);
					} else {
						String message = "New initial date equals to currently saved date.";
						session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, message);
					}
				} else {
					String message = "Initial date can't be set to future date.";
					session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, message);
				}
			}
		}
	}
}
