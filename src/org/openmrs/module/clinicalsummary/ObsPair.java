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

import java.util.Date;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Patient;

/**
 *
 */
public class ObsPair extends BaseOpenmrsData {
	
	private Integer id;
	
	private Patient patient;
	
	private Date obsDatetime;
	
	private Concept concept;
	
	private Concept answer;
	
	private Double value;
	
	private String status;
	
	/**
	 */
	public ObsPair() {
	}
	
	/**
	 * @param patient
	 * @param obsDatetime
	 * @param concept
	 * @param answer
	 * @param value
	 * @param status
	 */
	public ObsPair(Patient patient, Date obsDatetime, Concept concept, Concept answer, Double value, String status) {
		super();
		this.patient = patient;
		this.obsDatetime = obsDatetime;
		this.concept = concept;
		this.answer = answer;
		this.value = value;
		this.status = status;
	}
	
	/**
	 * @param patient
	 * @param obsDatetime
	 * @param concept
	 */
	public ObsPair(Patient patient, Date obsDatetime, Concept concept) {
		this.patient = patient;
		this.obsDatetime = obsDatetime;
		this.concept = concept;
	}
	
	/**
	 * Return the value of the patient
	 * 
	 * @return the patient
	 */
	public Patient getPatient() {
		return patient;
	}
	
	/**
	 * Set the patient with the patient value
	 * 
	 * @param patient the patient to set
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	/**
	 * Return the value of the obsDatetime
	 * 
	 * @return the obsDatetime
	 */
	public Date getObsDatetime() {
		return obsDatetime;
	}
	
	/**
	 * Set the obsDatetime with the obsDatetime value
	 * 
	 * @param obsDatetime the obsDatetime to set
	 */
	public void setObsDatetime(Date obsDatetime) {
		this.obsDatetime = obsDatetime;
	}
	
	/**
	 * Return the value of the concept
	 * 
	 * @return the concept
	 */
	public Concept getConcept() {
		return concept;
	}
	
	/**
	 * Set the concept with the concept value
	 * 
	 * @param concept the concept to set
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	/**
	 * Return the value of the answer
	 * 
	 * @return the answer
	 */
	public Concept getAnswer() {
		return answer;
	}
	
	/**
	 * Set the answer with the answer value
	 * 
	 * @param answer the answer to set
	 */
	public void setAnswer(Concept answer) {
		this.answer = answer;
	}
	
	/**
	 * Return the value of the value
	 * 
	 * @return the value
	 */
	public Double getValue() {
		return value;
	}
	
	/**
	 * Set the value with the value value
	 * 
	 * @param value the value to set
	 */
	public void setValue(Double value) {
		this.value = value;
	}
	
	/**
	 * Return the value of the status
	 * 
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	
	/**
	 * Set the status with the status value
	 * 
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return id;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		this.id = id;
	}
	
}
