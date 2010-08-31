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

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Patient;

/**
 * When a summary genaration process failed, a log will be created on the database. The log are represented using this class.
 * User can use this class to get a sense of how many failing occured on the process. This information can also be used to
 * re-generate the summary.
 */
public class SummaryError extends BaseOpenmrsData {
	
	private Integer summaryErrorId;
	
	private Patient patient;
	
	private String errorDetails;
	
	/**
	 * @return the summaryErrorId
	 */
	public Integer getSummaryErrorId() {
		return summaryErrorId;
	}
	
	/**
	 * @param summaryErrorId the summaryErrorId to set
	 */
	public void setSummaryErrorId(Integer summaryErrorId) {
		this.summaryErrorId = summaryErrorId;
	}
	
	/**
	 * @return the patient
	 */
	public Patient getPatient() {
		return patient;
	}
	
	/**
	 * @param patient the patient to set
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	/**
	 * @return the errorDetails
	 */
	public String getErrorDetails() {
		return errorDetails;
	}
	
	/**
	 * @param errorDetails the errorDetails to set
	 */
	public void setErrorDetails(String errorDetails) {
		this.errorDetails = errorDetails;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getSummaryErrorId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setSummaryErrorId(id);
	}
	
}
