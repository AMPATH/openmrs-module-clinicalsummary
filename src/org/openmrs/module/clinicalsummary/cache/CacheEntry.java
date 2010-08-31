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
 * A class that will encapsulate entry element in the Cache class.
 * 
 * @param <T> Data that will be entered into the descriptor. Most likely it will BaseOpenMRSObject
 *            itself because we will enter multiple type of data into the entry class.
 */
class CacheEntry<T extends BaseOpenmrsData> {
	
	/**
	 * Map of patient internal identifier to the CacheEntryData object. The CacheEntryData will
	 * encapsulate the internal data that need to be cached
	 */
	private Map<Integer, CacheEntryData<T>> entryDataMap;
	
	/**
	 * Getter to the map that's backing this CacheEntry object
	 * 
	 * @return
	 */
	public Map<Integer, CacheEntryData<T>> getEntryDataMap() {
		if (entryDataMap == null)
			entryDataMap = new HashMap<Integer, CacheEntryData<T>>();
		return entryDataMap;
	}
	
	/**
	 * Setter to the map that's backing this CacheEntry object
	 * 
	 * @param entryDataMap
	 */
	public void setEntryDataMap(Map<Integer, CacheEntryData<T>> entryDataMap) {
		this.entryDataMap = entryDataMap;
	}
	
	/**
	 * Internal method to put an entry data into the map backing object
	 * 
	 * @param patientId key of the data
	 * @param entryData the data for the corresponding key
	 */
	private void put(Integer patientId, CacheEntryData<T> entryData) {
		getEntryDataMap().put(patientId, entryData);
	}
	
	/**
	 * Internal method to get an entry out of the map backing object
	 * 
	 * @param patientId key of the data
	 */
	private CacheEntryData<T> get(Integer patientId) {
		return getEntryDataMap().get(patientId);
	}
	
	/**
	 * Method to get the list of all internal data for a certain patient. This is a convenient
	 * method bypassing the need to process CacheEntryData at the consumer code
	 * 
	 * @param patientId key of the data that will be processed
	 * @return list of all data for the specific patient or empty list when there's no data for the
	 *         patient
	 */
	public List<T> getData(Integer patientId) {
		CacheEntryData<T> entryData = get(patientId);
		if (entryData == null)
			return new ArrayList<T>();
		return entryData.getData();
	}
	
	/**
	 * Add data into the CacheEntryData for a specific patient. Patient element is determined from
	 * the patient element of the data to be added. This is another convenient method to modify the
	 * CacheEntryData. <br/>
	 * This method also contains hack to register observations under a certain encounter. This will
	 * only happen for an obs type of T
	 * 
	 * @param t data that will be added to the CacheEntry
	 */
	public void add(T t) {
		
		Integer patientId = null;
		boolean isObservation = false;
		
		if (t instanceof Obs) {
			Obs obs = (Obs) t;
			patientId = obs.getPersonId();
			isObservation = true;
		} else if (t instanceof Encounter) {
			Encounter encounter = (Encounter) t;
			patientId = encounter.getPatientId();
		} else {
			// reject others
			return;
		}
		
		CacheEntryData<T> entryData = getEntryDataMap().get(patientId);
		if (entryData == null) {
			entryData = new CacheEntryData<T>();
			put(patientId, entryData);
		}
		entryData.add(t);
		
		/* *********** entry for the observations hack in the CacheEntryData *********** */
		if (isObservation) {
			Obs obs = (Obs) t;
			Encounter encounter = obs.getEncounter();
			entryData.putEncounterObservation(encounter, obs);
		}
	}
	
	/**
	 * Hack method to return the observations based on encounter of the observations.
	 * 
	 * @param patientId key of the data that will be processed
	 * @param encounter encounter from which the observations comes from
	 * @return list of all observations for the encounter or empty list when there's no observation
	 *         for that encounter
	 */
	/* *********** hack for observations type only *********** */
	public List<Obs> getObservations(Integer patientId, Encounter encounter) {
		CacheEntryData<T> cacheEntryData = get(patientId);
		if (cacheEntryData == null)
			return new ArrayList<Obs>();
		return cacheEntryData.getEncounterObservations(encounter);
	}
}
