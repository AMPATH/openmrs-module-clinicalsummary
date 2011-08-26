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

package org.openmrs.module.clinicalsummary.enumeration;

import org.openmrs.module.clinicalsummary.db.hibernate.type.StringEnum;

public enum ResponseDisplayType implements StringEnum {

	DISPLAY_PAST_WEEK_RESPONSES("Past week"), DISPLAY_PAST_MONTH_RESPONSES("Past 1 month"),
	DISPLAY_PAST_2_MONTHS_RESPONSES("Past 2 months"), DISPLAY_PAST_6_MONTHS_RESPONSES("Past 6 months"),
	DISPLAY_PAST_12_MONTHS_RESPONSES("Past 12 months"), DISPLAY_ALL_RESPONSES("Any time");

	private final String value;

	private ResponseDisplayType(final String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
