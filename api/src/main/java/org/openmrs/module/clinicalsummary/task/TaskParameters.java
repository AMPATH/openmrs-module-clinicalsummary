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

package org.openmrs.module.clinicalsummary.task;

import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;

/**
 */
public interface TaskParameters {

	String TESTS_ORDERED = EvaluableNameConstants.TESTS_ORDERED;

	// the property for last processed location group
	String PROCESSOR_COUNTER = "clinicalsummary.batch.counter";

	// the property for last processed location group
	String PROCESSOR_INITIALIZED = "clinicalsummary.batch.initialized";

	// the property name for all known location grouping
	String LOCATION_GROUP_LIST = "clinicalsummary.location.group";

	// cluster separator
	String CLUSTER_SEPARATOR = ",";
}
