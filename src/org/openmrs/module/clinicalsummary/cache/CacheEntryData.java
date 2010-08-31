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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Encounter;
import org.openmrs.Obs;

/**
 * Internal cache storage for each patient data. Internal representation:
 * <ul>
 * <li>Latest Data</li>
 * <li>Earliest Data</li>
 * <li>Raw Data</li>
 * </ul>
 * Further enhancement can be done to include more statistic data to this storage. This will reduce
 * the calculation time to get the latest or the earliest data and some other statistic.
 * 
 * @param <T> Type of the CacheEntryData that will be stored. Currently support Obs and Encounter
 *            only
 */
class CacheEntryData<T extends BaseOpenmrsData> {
	
	/**
	 * List of all raw encounter or observations in this object
	 */
	private List<T> data;
	
	/**
	 * Getter for the raw data
	 * 
	 * @return the data
	 */
	public List<T> getData() {
		if (data == null)
			data = new ArrayList<T>();
		return data;
	}
	
	/**
	 * Setter for the raw data
	 * 
	 * @param data the data to set
	 */
	public void setData(List<T> data) {
		this.data = data;
	}
	
	/**
	 * Helper method to add a new element to the raw data
	 * 
	 * @param T element to be added
	 */
	public void add(T t) {
		getData().add(t);
	}
	
	/* *********** hacks for observations cache *********** */
	// Only for observations. Map of encounter to collection of observations
	// We do this because apparently observation is lazy to encounter
	// We don't want to hit the database so many times to get all observations that we need to process.
	/**
	 * Map backing the mapping between encounter to observations
	 */
	private Map<Encounter, List<Obs>> dataMap;
	
	/**
	 * Getter for the map backing object
	 * 
	 * @return the dataMap
	 */
	public Map<Encounter, List<Obs>> getDataMap() {
		if (dataMap == null)
			dataMap = new HashMap<Encounter, List<Obs>>();
		return dataMap;
	}
	
	/**
	 * Setter for the map backing object
	 * 
	 * @param dataMap the dataMap to set
	 */
	public void setDataMap(Map<Encounter, List<Obs>> dataMap) {
		this.dataMap = dataMap;
	}
	
	/**
	 * Helper method to get the list of observations for a certain encounter in this CacheEntryData
	 * 
	 * @param key the encounter from which the observations come from
	 * @return list of all observations from the encounter or empty list if there's no observation
	 */
	public List<Obs> getEncounterObservations(Encounter key) {
		List<Obs> list = getDataMap().get(key);
		if (list == null)
			list = new ArrayList<Obs>();
		return list;
	}
	
	/**
	 * Helper method to put an observations under a certain encounter in this CacheEntryData
	 * 
	 * @param key the encounter from which the observations come from
	 * @param value the observation that will be added
	 */
	public void putEncounterObservation(Encounter key, Obs value) {
		List<Obs> list = getDataMap().get(key);
		if (list == null) {
			list = new ArrayList<Obs>();
			getDataMap().put(key, list);
		}
		list.add(value);
	}
}
