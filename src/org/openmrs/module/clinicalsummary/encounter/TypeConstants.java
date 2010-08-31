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
package org.openmrs.module.clinicalsummary.encounter;

/**
 *
 */
public interface TypeConstants {
	
	@RegisterType
	public static final String ANC_INITIAL = "ANCINITIAL";
	
	@RegisterType
	public static final String ANC_RETURN = "ANCRETURN";
	
	@RegisterType
	public static final String ADULT_INITIAL = "ADULTINITIAL";
	
	@RegisterType
	public static final String ADULT_RETURN = "ADULTRETURN";
	
	@RegisterType
	public static final String PEDS_INITIAL = "PEDSINITIAL";
	
	@RegisterType
	public static final String PEDS_RETURN = "PEDSRETURN";
	
	@RegisterType
	public static final String ADULT_NONCLINICAL = "ADULTNONCLINICALMEDICATION";
	
	@RegisterType
	public static final String PEDS_NONCLINICAL = "PEDSNONCLINICALMEDICATION";
}
