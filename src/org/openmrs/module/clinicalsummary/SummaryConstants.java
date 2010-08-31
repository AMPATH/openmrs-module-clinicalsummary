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

public interface SummaryConstants {
	
	/*
	 * Clinical summaries privileges
	 */
	final String PRIV_MANAGE_SUMMARY = "Manage Summaries";
	
	final String PRIV_VIEW_SUMMARY = "View Summaries";
	
	final String PRIV_GENERATE_SUMMARY = "Generate Summaries";
	
	final String PRIV_PRINT_SUMMARY = "Print Summaries";
	
	/*
	 * Results location
	 */
	final String OUTPUT_LOCATION_PROPERTY = "clinicalsummary.generated";
	
	final String GENERATED_PDF_LOCATION = "clinicalsummary/generated";
	
	final String ENCRYPTION_LOCATION_PROPERTY = "clinicalsummary.encryption";
	
	final String ENCRYPTION_LOCATION = "clinicalsummary/encryption";
	
	final String ZIPPED_LOCATION_PROPERTY = "clinicalsummary.zipped";
	
	final String ZIPPED_LOCATION = "clinicalsummary/zipped";
	
	/*
	 * Misc constants
	 */

	final int MAX_COHORT_SIZE = 100;
}
