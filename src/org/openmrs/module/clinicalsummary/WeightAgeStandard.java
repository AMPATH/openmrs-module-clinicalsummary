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

/**
 *
 */
public class WeightAgeStandard extends BaseOpenmrsData {
	
	private Double lValue;
	
	private Double mValue;
	
	private Double sValue;
	
	private String gender;
	
	private Integer age;
	
	private String ageUnit;
	
	private Integer standardId;
	
	/**
	 * Return the value of the lValue
	 * 
	 * @return the lValue
	 */
	public Double getlValue() {
		return lValue;
	}
	
	/**
	 * Set the lValue with the lValue value
	 * 
	 * @param lValue the lValue to set
	 */
	public void setlValue(Double lValue) {
		this.lValue = lValue;
	}
	
	/**
	 * Return the value of the mValue
	 * 
	 * @return the mValue
	 */
	public Double getmValue() {
		return mValue;
	}
	
	/**
	 * Set the mValue with the mValue value
	 * 
	 * @param mValue the mValue to set
	 */
	public void setmValue(Double mValue) {
		this.mValue = mValue;
	}
	
	/**
	 * Return the value of the sValue
	 * 
	 * @return the sValue
	 */
	public Double getsValue() {
		return sValue;
	}
	
	/**
	 * Set the sValue with the sValue value
	 * 
	 * @param sValue the sValue to set
	 */
	public void setsValue(Double sValue) {
		this.sValue = sValue;
	}
	
	/**
	 * Return the value of the gender
	 * 
	 * @return the gender
	 */
	public String getGender() {
		return gender;
	}
	
	/**
	 * Set the gender with the gender value
	 * 
	 * @param gender the gender to set
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	/**
	 * Return the value of the age
	 * 
	 * @return the age
	 */
	public Integer getAge() {
		return age;
	}
	
	/**
	 * Set the age with the age value
	 * 
	 * @param age the age to set
	 */
	public void setAge(Integer age) {
		this.age = age;
	}
	
	/**
	 * Return the value of the ageUnit
	 * 
	 * @return the ageUnit
	 */
	public String getAgeUnit() {
		return ageUnit;
	}
	
	/**
	 * Set the ageUnit with the ageUnit value
	 * 
	 * @param ageUnit the ageUnit to set
	 */
	public void setAgeUnit(String ageUnit) {
		this.ageUnit = ageUnit;
	}
	
	/**
	 * Return the value of the standardId
	 * 
	 * @return the standardId
	 */
	public Integer getStandardId() {
		return standardId;
	}
	
	/**
	 * Set the standardId with the standardId value
	 * 
	 * @param standardId the standardId to set
	 */
	public void setStandardId(Integer standardId) {
		this.standardId = standardId;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getStandardId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setStandardId(id);
	}
	
}
