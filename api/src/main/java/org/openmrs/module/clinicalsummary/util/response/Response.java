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

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;

public abstract class Response extends BaseOpenmrsData {
	// dummy class that will act as the superclass of all response classes

	protected Integer id;

	protected Patient patient;

	protected Person provider;

	protected Location location;

	protected Date datetime;

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
	public Date getDatetime() {
		return datetime;
	}

	/**
	 * @param datetime
	 */
	public void setDatetime(final Date datetime) {
		this.datetime = datetime;
	}
}
