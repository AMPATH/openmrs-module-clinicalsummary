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

package org.openmrs.module.clinicalsummary.util.response;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.module.clinicalsummary.enumeration.ActionType;
import org.openmrs.module.clinicalsummary.enumeration.MedicationType;

public class MedicationResponse extends Response {

	private static final Log log = LogFactory.getLog(MedicationResponse.class);

	private Integer id;

	private Patient patient;

	private Person provider;

	private Location location;

	private Concept medication;

	private MedicationType medicationType;

	private Date medicationDatetime;

	private Integer status;

	private Person reviewer;

	private Date dateReviewed;

	private ActionType actionType;

	/**
	 * @return id - The unique Identifier for the object
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id - The unique Identifier for the object
	 */
	public void setId(final Integer id) {
		this.id = id;
	}

	/**
	 * @return
	 */
	public Patient getPatient() {
		return patient;
	}

	/**
	 * @param patient
	 */
	public void setPatient(final Patient patient) {
		this.patient = patient;
	}

	/**
	 * @return
	 */
	public Person getProvider() {
		return provider;
	}

	/**
	 * @param provider
	 */
	public void setProvider(final Person provider) {
		this.provider = provider;
	}

	/**
	 * @return
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @param location
	 */
	public void setLocation(final Location location) {
		this.location = location;
	}

	/**
	 * @return
	 */
	public Concept getMedication() {
		return medication;
	}

	/**
	 * @param medication
	 */
	public void setMedication(final Concept medication) {
		this.medication = medication;
	}

	/**
	 * @return
	 */
	public MedicationType getMedicationType() {
		return medicationType;
	}

	/**
	 * @param medicationType
	 */
	public void setMedicationType(final MedicationType medicationType) {
		this.medicationType = medicationType;
	}

	/**
	 * @return
	 */
	public Date getMedicationDatetime() {
		return medicationDatetime;
	}

	/**
	 * @param medicationDatetime
	 */
	public void setMedicationDatetime(final Date medicationDatetime) {
		this.medicationDatetime = medicationDatetime;
	}

	/**
	 * @return
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * @param status
	 */
	public void setStatus(final Integer status) {
		this.status = status;
	}

	/**
	 * @return
	 */
	public Person getReviewer() {
		return reviewer;
	}

	/**
	 * @param reviewer
	 */
	public void setReviewer(final Person reviewer) {
		this.reviewer = reviewer;
	}

	/**
	 * @return
	 */
	public Date getDateReviewed() {
		return dateReviewed;
	}

	/**
	 * @param dateReviewed
	 */
	public void setDateReviewed(final Date dateReviewed) {
		this.dateReviewed = dateReviewed;
	}

	/**
	 * @return
	 */
	public ActionType getActionType() {
		return actionType;
	}

	/**
	 * @param actionType
	 */
	public void setActionType(final ActionType actionType) {
		this.actionType = actionType;
	}
}
