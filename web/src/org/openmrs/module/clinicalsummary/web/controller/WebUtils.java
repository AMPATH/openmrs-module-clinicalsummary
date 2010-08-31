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

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;

/**
 *
 */
public final class WebUtils {
	
	private static final Log log = LogFactory.getLog(WebUtils.class);
	
	private WebUtils() {
	}
	
	public static final Date parse(String dateString, Date defaultDate) {
		Date date = null;
		try {
			date = Context.getDateFormat().parse(dateString);
		}
		catch (ParseException e) {
			log.error("Fail parsing date string. Ignoring failing date parameter ...", e);
			date = defaultDate;
		}
		return date;
	}
	
}