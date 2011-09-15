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

package org.openmrs.module.clinicalsummary.web.controller.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.PersonName;

public class ExtendedData {

	private static final Log log = LogFactory.getLog(ExtendedData.class);

	private static final String FIELD_SEPARATOR = ",";

	private Integer duplicates;

	private Integer patientId;

	private String identifier;

	private PersonName personName;

	private String age;

	private String gender;

	private Location primaryLocation;

	private Encounter initialEncounter;

	private Integer returnedVisit;

	private Obs whoStage;

	private Obs cdcClass;

	private Obs motherStatus;

	private Obs fatherStatus;

	/**
	 * @return
	 */
	public Integer getDuplicates() {
		return duplicates;
	}

	/**
	 * @param duplicates
	 */
	public void setDuplicates(final Integer duplicates) {
		this.duplicates = duplicates;
	}

	/**
	 * @return
	 */
	public Integer getPatientId() {
		return patientId;
	}

	/**
	 * @param patientId
	 */
	public void setPatientId(final Integer patientId) {
		this.patientId = patientId;
	}

	/**
	 * @return
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier
	 */
	public void setIdentifier(final String identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return
	 */
	public PersonName getPersonName() {
		return personName;
	}

	/**
	 * @param personName
	 */
	public void setPersonName(final PersonName personName) {
		this.personName = personName;
	}

	/**
	 * @return
	 */
	public String getAge() {
		return age;
	}

	/**
	 * @param age
	 */
	public void setAge(final String age) {
		this.age = age;
	}

	/**
	 * @return
	 */
	public String getGender() {
		return gender;
	}

	/**
	 * @param gender
	 */
	public void setGender(final String gender) {
		this.gender = gender;
	}

	/**
	 * @return
	 */
	public Location getPrimaryLocation() {
		return primaryLocation;
	}

	/**
	 * @param primaryLocation
	 */
	public void setPrimaryLocation(final Location primaryLocation) {
		this.primaryLocation = primaryLocation;
	}

	/**
	 * @return
	 */
	public Encounter getInitialEncounter() {
		return initialEncounter;
	}

	/**
	 * @param initialEncounter
	 */
	public void setInitialEncounter(final Encounter initialEncounter) {
		this.initialEncounter = initialEncounter;
	}

	/**
	 * @return
	 */
	public Integer getReturnedVisit() {
		return returnedVisit;
	}

	/**
	 * @param returnedVisit
	 */
	public void setReturnedVisit(final Integer returnedVisit) {
		this.returnedVisit = returnedVisit;
	}

	/**
	 * @return
	 */
	public Obs getWhoStage() {
		return whoStage;
	}

	/**
	 * @param whoStage
	 */
	public void setWhoStage(final Obs whoStage) {
		this.whoStage = whoStage;
	}

	/**
	 * @return
	 */
	public Obs getCdcClass() {
		return cdcClass;
	}

	/**
	 * @param cdcClass
	 */
	public void setCdcClass(final Obs cdcClass) {
		this.cdcClass = cdcClass;
	}

	/**
	 * @return
	 */
	public Obs getMotherStatus() {
		return motherStatus;
	}

	/**
	 * @param motherStatus
	 */
	public void setMotherStatus(final Obs motherStatus) {
		this.motherStatus = motherStatus;
	}

	/**
	 * @return
	 */
	public Obs getFatherStatus() {
		return fatherStatus;
	}

	/**
	 * @param fatherStatus
	 */
	public void setFatherStatus(final Obs fatherStatus) {
		this.fatherStatus = fatherStatus;
	}

	/**
	 * Returns a string representation of the object. In general, the
	 * <code>toString</code> method returns a string that
	 * "textually represents" this object. The result should
	 * be a concise but informative representation that is easy for a
	 * person to read.
	 * It is recommended that all subclasses override this method.
	 * <p/>
	 * The <code>toString</code> method for class <code>Object</code>
	 * returns a string consisting of the name of the class of which the
	 * object is an instance, the at-sign character `<code>@</code>', and
	 * the unsigned hexadecimal representation of the hash code of the
	 * object. In other words, this method returns a string equal to the
	 * value of:
	 * <blockquote>
	 * <pre>
	 * getClass().getName() + '@' + Integer.toHexString(hashCode())
	 * </pre></blockquote>
	 *
	 * @return a string representation of the object.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getDuplicates()).append(FIELD_SEPARATOR);
		return builder.toString();
	}
}
