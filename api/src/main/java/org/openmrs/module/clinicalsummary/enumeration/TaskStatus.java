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

/**
 */
public enum TaskStatus implements StringEnum {

	TASK_RUNNING_DOWNLOAD("Running Download"), TASK_RUNNING_UPLOAD("Running Upload"),
	TASK_IDLE("Idle"), TASK_FAILED("Failed");

	private final String value;

	private TaskStatus(final String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
