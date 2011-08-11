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

package org.openmrs.module.clinicalsummary.rule.post;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 */
class StudyRandomizer {

	private static final Log log = LogFactory.getLog(StudyRandomizer.class);

	private static final String SPACE_CHARACTER = " ";

	private static final Integer MAXIMUM_RANDOM_VALUE = 1000;

	public static Integer getRandomizedValue(String propertyName) {

		if (log.isDebugEnabled())
			log.debug("Reading random number from the global property ...");

		List<Integer> randomizedValues = new ArrayList<Integer>();

		String randomNumberString = Context.getAdministrationService().getGlobalProperty(propertyName);
		if (StringUtils.isNotBlank(randomNumberString)) {
			String[] stringNumbers = StringUtils.split(randomNumberString, SPACE_CHARACTER);
			for (String stringNumber : stringNumbers)
				randomizedValues.add(NumberUtils.toInt(stringNumber));
		}

		if (CollectionUtils.isEmpty(randomizedValues))
			randomizedValues = generateRandomizedValues();

		Integer randomizedValue = randomizedValues.remove(0);

		GlobalProperty globalProperty = Context.getAdministrationService().getGlobalPropertyObject(propertyName);
		if (globalProperty == null)
			globalProperty = new GlobalProperty(propertyName, StringUtils.EMPTY, "Auto generated random number for patients");
		globalProperty.setPropertyValue(StringUtils.join(randomizedValues, SPACE_CHARACTER));

		if (log.isDebugEnabled())
			log.debug("Saving random number to the global property ...");

		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);
		Context.getAdministrationService().saveGlobalProperty(globalProperty);
		Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_GLOBAL_PROPERTIES);

		return randomizedValue;
	}

	private static List<Integer> generateRandomizedValues() {
		List<Integer> randomizedValues = new ArrayList<Integer>();

		int controlCount = 0;
		int interventionCount = 0;

		Random random = new Random();
		while (controlCount < 2 && interventionCount < 2) {
			int randomizedValue = random.nextInt(MAXIMUM_RANDOM_VALUE);
			if (randomizedValue < MAXIMUM_RANDOM_VALUE / 2) {
				controlCount++;
				randomizedValues.add(0);
			} else {
				interventionCount++;
				randomizedValues.add(1);
			}
		}

		int fillValue = 0;
		if (interventionCount < controlCount)
			fillValue = 1;

		for (int i = randomizedValues.size(); i < 4; i++)
			randomizedValues.add(fillValue);

		return randomizedValues;
	}
}
