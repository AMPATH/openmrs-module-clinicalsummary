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
package org.openmrs.module.clinicalsummary.cache;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Cohort;

/**
 * Key for the Cache object. Every CacheEntry in the Cache will be mapped by one unique
 * CacheEntryKey.
 * <ul>
 * <li>Cohort from which the CacheEntry come from</li>
 * <li>CacheEntryType determine what kind of data can be expected from the CacheEntry</li>
 * <li>CacheDescriptor will differentiate one CacheEntryKey and other key in the Cache</li>
 * </ul>
 * Every cache in the system have a 1 day lifetime because the regeneration process for clinical
 * summary happen every night
 */
class CacheEntryKey {
	
	private static final long ONE_DAY = 24 * 60 * 60;
	
	/**
	 * List of patient from which the data inside the CacheEntry comes from
	 */
	private Cohort cohort;
	
	/**
	 * Type of data that can be expected from the CacheEntry, currently support only Obs and
	 * Encounter
	 */
	private CacheEntryType entryType;
	
	/**
	 * Differentiating element of the CacheEntryKey and other key in the Cache
	 */
	private CacheDescriptor<BaseOpenmrsObject> descriptors;
	
	/**
	 * How long the CacheEntry will be expected in the Cache
	 */
	private long expires;
	
	/**
	 * Create a CacheEntryKey using all element of the key.
	 * 
	 * @param cohort the cohort
	 * @param entryType the type
	 * @param descriptors the descriptors
	 */
	public CacheEntryKey(Cohort cohort, CacheEntryType entryType, CacheDescriptor<BaseOpenmrsObject> descriptors) {
		this.cohort = cohort;
		this.entryType = entryType;
		this.descriptors = descriptors;
		this.expires = new Date().getTime() + ONE_DAY;
	}
	
	/**
	 * Getter for the cohort
	 * 
	 * @return the cohort
	 */
	public Cohort getCohort() {
		return cohort;
	}
	
	/**
	 * Setter for the cohort
	 * 
	 * @param cohort the cohort to set
	 */
	public void setCohort(Cohort cohort) {
		this.cohort = cohort;
	}
	
	/**
	 * Getter for the entryType
	 * 
	 * @return the entryType
	 */
	public CacheEntryType getEntryType() {
		return entryType;
	}
	
	/**
	 * The setter for the entryType
	 * 
	 * @param entryType the entryType to set
	 */
	public void setEntryType(CacheEntryType entryType) {
		this.entryType = entryType;
	}
	
	/**
	 * Getter for the descriptors
	 * 
	 * @return the descriptors
	 */
	public CacheDescriptor<BaseOpenmrsObject> getDescriptors() {
		return descriptors;
	}
	
	/**
	 * The setter for the descriptors
	 * 
	 * @param descriptors the descriptors to set
	 */
	public void setDescriptors(CacheDescriptor<BaseOpenmrsObject> descriptors) {
		this.descriptors = descriptors;
	}
	
	/**
	 * Getter for the expires
	 * 
	 * @return the expires
	 */
	public long getExpires() {
		return expires;
	}
	
	/**
	 * The setter for the expires
	 * 
	 * @param expires the expires to set
	 */
	public void setExpires(long expires) {
		this.expires = expires;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (obj == null)
			return false;
		
		if (obj instanceof CacheEntryKey) {
			CacheEntryKey d = (CacheEntryKey) obj;
			
			if (!getCohort().equals(d.getCohort()))
				return false;
			
			if (!getEntryType().equals(d.getEntryType()))
				return false;
			
			if (!getDescriptors().equals(d.getDescriptors()))
				return false;
			
			return true;
		}
		
		return super.equals(obj);
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 8;
		hash = 31 * getCohort().hashCode() + hash;
		hash = 31 * getEntryType().hashCode() + hash;
		hash = 31 * getDescriptors().hashCode() + hash;
		return hash;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("cohort", getCohort());
		builder.append("entry type", getEntryType());
		builder.append("descriptor", getDescriptors());
		return builder.toString();
	}
	
}
