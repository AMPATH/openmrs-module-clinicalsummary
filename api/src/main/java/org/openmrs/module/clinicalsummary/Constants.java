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
package org.openmrs.module.clinicalsummary;

/**
 * Constants for the Clinical Summary Module
 */
public interface Constants {

	// constants for the module privileges
	String PRIVILEGE_MANAGE_SUMMARY = "Manage Summaries";

	String PRIVILEGE_VIEW_SUMMARY = "View Summaries";

	String PRIVILEGE_GENERATE_SUMMARY = "Generate Summaries";

	String PRIVILEGE_PRINT_SUMMARY = "Print Summaries";

	// folder structure for the generation process output
	String GENERATED_PDF_LOCATION = "clinicalsummary/generated";

	String ENCRYPTION_LOCATION = "clinicalsummary/encryption";

	String ZIPPED_LOCATION = "clinicalsummary/zipped";

	// maximum number of patient to be processed on a single batch generation process
	int MAX_COHORT_SIZE = 100;

	String POST_EVALUATION_TOKEN = "clinicalsummary.post.tokens";
}
