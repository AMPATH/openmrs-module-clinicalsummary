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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Location;
import org.openmrs.OpenmrsObject;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.cache.CacheUtils;
import org.openmrs.module.clinicalsummary.db.hibernate.type.StringEnum;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

/**
 *
 */
public final class WebUtils {

	private static final Log log = LogFactory.getLog(WebUtils.class);

	private static final String TEMP_FILE_PREFIX = "PreGeneratedSummaries";

	private static final String TEMP_FILE_DATE_FORMAT = "ddMMMyyyy_HHmmss";

	/**
	 * Parse a date string or return a default date value when the parsing failed
	 *
	 * @param dateString  the date string
	 * @param defaultDate the default date
	 * @return date object based on the date string or default date when the parsing failed
	 */
	public static Date parse(final String dateString, final Date defaultDate) {
		Date date;
		try {
			date = Context.getDateFormat().parse(dateString);
		} catch (ParseException e) {
			log.error("Parsing " + dateString + " to Date object failed. Ignoring and using default value.");
			date = defaultDate;
		}
		return date;
	}

	/**
	 * Parse expression into a list of string. This method will extract any value between with " and "
	 *
	 * @param expression the string expression
	 * @return list of string element. Each element is a non-blank string
	 * @see org.apache.commons.lang.StringUtils#isNotBlank(String)
	 */
	public static Collection<String> parse(final String expression) {
		String processedExpression = expression;
		Collection<String> terms = new TreeSet<String>();
		if (expression.contains("\"")) {
			for (int i = 0; i < expression.length(); i++) {
				String s = expression.substring(i, i + 1);
				if (StringUtils.equals(s, "\"")) {
					// we already make sure that (i + 1) is a valid character, now check the next one after (i + 1)
					int j = i + 1;
					boolean found = false;
					while (j < expression.length() && !found) {
						s = expression.substring(j, j + 1);
						if (StringUtils.equals(s, "\""))
							found = true;
						j++;
					}
					// get the actual string value
					String term = expression.substring(i, j);
					// skip until the end of the param name
					i = i + term.length();
					processedExpression = expression.substring(i);
					// add the new term to the term list
					terms.add(term.replace("\"", ""));
				}
			}
		}

		for (String term : processedExpression.split("\\s*,\\s*"))
			if (!StringUtils.isNotEmpty(StringUtils.strip(term)))
				terms.add(term);

		return terms;
	}

	/**
	 * Prepare the attachment filename
	 *
	 * @param extension Optional parameters to specify the file extension
	 * @param patientId Optional parameters to add patient id to the filename
	 * @return the filename of the attachment
	 */
	public static String prepareFilename(final Integer patientId, final String extension) {
		List<String> elements = new ArrayList<String>();
		elements.add(TEMP_FILE_PREFIX);

		SimpleDateFormat format = new SimpleDateFormat(TEMP_FILE_DATE_FORMAT);
		String currentTime = format.format(new Date());
		elements.add(currentTime);

		if (patientId != null)
			elements.add(String.valueOf(patientId));

		String filename = StringUtils.join(elements, "_");

		if (extension != null)
			filename = StringUtils.join(Arrays.asList(filename, extension), ".");

		return filename;
	}

	/**
	 * @param object
	 * @return
	 */
	public static String getStringValue(final Object object) {
		String value = StringUtils.EMPTY;
		if (object != null) {
			if (ClassUtils.isAssignable(object.getClass(), Location.class)) {
				value = ((Location) object).getName();
			} else if (ClassUtils.isAssignable(object.getClass(), Concept.class)) {
				ConceptName conceptName = ((Concept) object).getName(Context.getLocale());
				if (conceptName != null)
					value = conceptName.getName();
			} else if (ClassUtils.isAssignable(object.getClass(), Patient.class)) {
				PersonName personName = ((Patient) object).getPersonName();
				if (personName != null)
					value = personName.getFullName();
			} else if (ClassUtils.isAssignable(object.getClass(), Person.class)) {
				PersonName personName = ((Person) object).getPersonName();
				if (personName != null)
					value = personName.getFullName();
			} else if (ClassUtils.isAssignable(object.getClass(), StringEnum.class)) {
				value = ((StringEnum) object).getValue();
			} else {
				value = String.valueOf(object);
			}
		}
		return value;
	}

	/**
	 * @param names
	 * @param type
	 * @return
	 */
	public static Collection<OpenmrsObject> getOpenmrsObjects(final Collection<String> names, final Class<? extends OpenmrsObject> type) {
		Collection<OpenmrsObject> objects = new ArrayList<OpenmrsObject>();
		if (CollectionUtils.isEmpty(names))
			return objects;

		if (ClassUtils.isAssignable(type, Location.class)) {
			for (String name : names) {
				Location location = Context.getLocationService().getLocation(name);
				if (location != null)
					objects.add(location);
			}
		} else if (ClassUtils.isAssignable(type, Concept.class)) {
			for (String name : names) {
				Concept concept = CacheUtils.getConcept(name);
				if (concept != null)
					objects.add(concept);
			}
		}

		return objects;
	}

	/**
	 * @param object
	 * @return
	 */
	public static String getIdValue(final Object object) {
		String value = StringUtils.EMPTY;
		if (object != null && ClassUtils.isAssignable(object.getClass(), OpenmrsObject.class)) {
			Integer id = ((OpenmrsObject) object).getId();
			if (id != null)
				value = String.valueOf(id);
		}
		return value;
	}
}
