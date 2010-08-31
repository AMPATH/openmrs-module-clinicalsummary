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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.SummaryService;
import org.openmrs.module.clinicalsummary.concept.CachedConcept;
import org.openmrs.module.clinicalsummary.concept.ConceptRegistry;
import org.openmrs.module.clinicalsummary.encounter.TypeRegistry;

/**
 * Helper class to prepare and use the cached data to generate the clinical summary. This class will
 * be passed to the velocity evaluation context. The velocity template can call all methods in the
 * class or pass the reference to the Rule object for more complex processing of the cached object.
 */
public class DataProvider {
	
	/**
	 * List of all patients that have their data cached
	 */
	private final Cohort cohort;
	
	/**
	 * The Cache object that will hold all data for the cohort
	 */
	private final Cache<BaseOpenmrsData> cache;
	
	/**
	 * Initialize this object.
	 * 
	 * @param cohort the cohort
	 */
	public DataProvider(Cohort cohort) {
		this.cohort = cohort;
		this.cache = new Cache<BaseOpenmrsData>();
		this.initialize();
	}
	
	/**
	 * Initialize the cache. This cache will only contains observations with concept that already
	 * registered and encounters with encounter type that already registered before
	 */
	private void initialize() {
		
		SummaryService service = Context.getService(SummaryService.class);
		
		/*
		 * This section will read all registered encounter type and buffer the encounters.
		 */

		// the descriptor for encounters is the encounter type only
		Collection<EncounterType> types = TypeRegistry.getCachedEncounterTypes();
		// buffer the encounter objects
		for (EncounterType encounterType : types) {
			List<Encounter> encounters = service.getEncountersByType(cohort, encounterType);
			if (!encounters.isEmpty()) {
				CacheEntry<BaseOpenmrsData> entry = new CacheEntry<BaseOpenmrsData>();
				for (Encounter encounter : encounters)
					entry.add(encounter);
				CacheDescriptor<BaseOpenmrsObject> descriptor = new CacheDescriptor<BaseOpenmrsObject>();
				descriptor.addDescriptor(CacheDescriptorType.ENCOUNTER_TYPE, encounterType);
				cache.put(cohort, CacheEntryType.ENCOUNTER, descriptor, entry);
			}
		}
		
		/*
		 * There's a trade off here between two approaches that we can take from here on to prepare the
		 * observations.
		 * 
		 * 1. Query and get all observations on a concept for a cohort
		 *    -- Potentially return a lot of observations
		 *    -- Only one query
		 *    -- Need to do after query processing
		 * 2. Query and get all observations after the latest initial for a specific person
		 *    -- Potentially faster for each query
		 *    -- Need to do a lot of query because we need to do this for each patient
		 */

		// for observation, the descriptor is the concept and the encounter type
		for (CachedConcept cachedConcept : ConceptRegistry.getAllCachedConcepts()) {
			Collection<EncounterType> encounterTypes = cachedConcept.getEncounterTypes();
			// if there's encounter types, then that means we need to buffer the obs
			if (encounterTypes != null) {
				Concept concept = cachedConcept.getConcept();
				List<Obs> observations = service.getObservationsByEncounterType(cohort, concept, encounterTypes);
				if (!observations.isEmpty()) {
					CacheEntry<BaseOpenmrsData> entry = new CacheEntry<BaseOpenmrsData>();
					for (Obs obs : observations) {
						// if numeric but no numeric value, then skip it
						// TODO: this is a hack because Result.java (799) will throw NPE because if we keep data like this
						if (concept.isNumeric() && obs.getValueNumeric() == null)
							continue;
						entry.add(obs);
					}
					CacheDescriptor<BaseOpenmrsObject> descriptor = new CacheDescriptor<BaseOpenmrsObject>();
					descriptor.addDescriptor(CacheDescriptorType.OBSERVED_CONCEPT, concept);
					for (EncounterType encounterType : encounterTypes)
						descriptor.addDescriptor(CacheDescriptorType.ENCOUNTER_TYPE, encounterType);
					cache.put(cohort, CacheEntryType.OBSERVATION, descriptor, entry);
				}
			}
		}
	}
	
	/**
	 * Get list of all patient's observations for a concept on a certain type of encounter
	 * 
	 * @param patientId the patient
	 * @param concept the concept
	 * @param encounterTypes list of all applicable encounter types
	 * @return list of all observations matching the above criteria or empty list if there's no
	 *         observations matching above criteria
	 */
	public List<Obs> getObservations(Integer patientId, String conceptName, Collection<String> typeNames) {
		
		ConceptRegistry.learnConcept(conceptName, typeNames);
		
		List<Obs> observations = new ArrayList<Obs>();
		CacheDescriptor<BaseOpenmrsObject> descriptor = new CacheDescriptor<BaseOpenmrsObject>();
		descriptor.addDescriptor(CacheDescriptorType.OBSERVED_CONCEPT, ConceptRegistry.getCachedConcept(conceptName));
		for (String typeName : typeNames)
			descriptor.addDescriptor(CacheDescriptorType.ENCOUNTER_TYPE, TypeRegistry.getCachedEncounterType(typeName));
		
		CacheEntry<BaseOpenmrsData> entry = cache.get(CacheEntryType.OBSERVATION, descriptor);
		if (entry != null) {
			List<BaseOpenmrsData> collections = entry.getData(patientId);
			for (BaseOpenmrsData data : collections)
				observations.add((Obs) data);
		}
		
		return observations;
	}
	
	/**
	 * Get list of all patient's observations for a certain encounter for a certain concept.
	 * 
	 * @param patientId the patient
	 * @param concept the concept
	 * @param encounter encounter
	 * @return list of all observations matching the above criteria or empty list when there's no
	 *         observation matching the criteria
	 */
	public List<Obs> getEncounterObservations(Integer patientId, String conceptName, Collection<String> typeNames, String which) {
		
		ConceptRegistry.learnConcept(conceptName, typeNames);
		
		List<Obs> observations = new ArrayList<Obs>();
		
		CacheDescriptor<BaseOpenmrsObject> descriptor = new CacheDescriptor<BaseOpenmrsObject>();
		descriptor.addDescriptor(CacheDescriptorType.OBSERVED_CONCEPT, ConceptRegistry.getCachedConcept(conceptName));
		
		List<Encounter> encounters = new ArrayList<Encounter>();
		
		for (String typeName : typeNames) {
			descriptor.addDescriptor(CacheDescriptorType.ENCOUNTER_TYPE, TypeRegistry.getCachedEncounterType(typeName));
			encounters.addAll(getEncountersByType(patientId, typeName));
		}
		
		CacheEntry<BaseOpenmrsData> entry = cache.get(CacheEntryType.OBSERVATION, descriptor);
		if (entry != null) {
			
			if (typeNames.size() > 1)
				Collections.sort(encounters, new EncounterComparator());
			
			if (!encounters.isEmpty()) {
				if (SummaryDataSource.LATEST_ENCOUNTER.equals(which))
					observations.addAll(entry.getObservations(patientId, encounters.get(0)));
				else
					observations.addAll(entry.getObservations(patientId, encounters.get(encounters.size() - 1)));
			}
		}
		
		return observations;
	}
	
	public List<Encounter> getEncountersByType(Integer patientId, String typeName) {
		
		TypeRegistry.learnTypes(typeName);
		
		List<Encounter> encounters = new ArrayList<Encounter>();
		
		CacheDescriptor<BaseOpenmrsObject> descriptor = new CacheDescriptor<BaseOpenmrsObject>();
		descriptor.addDescriptor(CacheDescriptorType.ENCOUNTER_TYPE, TypeRegistry.getCachedEncounterType(typeName));
		
		CacheEntry<BaseOpenmrsData> entry = cache.get(CacheEntryType.ENCOUNTER, descriptor);
		if (entry != null) {
			List<BaseOpenmrsData> collections = entry.getData(patientId);
			for (BaseOpenmrsData data : collections)
				encounters.add((Encounter) data);
		}
		
		return encounters;
	}
	
	/**
	 * Get all patient encounter based on the list of encounter type names
	 * 
	 * @param patientId the patient
	 * @param typeNames the encounter type names
	 * @return list of all encounters for the patient with the matching encounter type names or
	 *         empty list when there's no matching encounter
	 */
	public List<Encounter> getEncounters(Integer patientId, Collection<String> typeNames) {
		List<Encounter> encounters = new ArrayList<Encounter>();
		for (String typeName : typeNames)
			encounters.addAll(getEncountersByType(patientId, typeName));
		// not sure how expensive the sorting is but just to be safe if it's expensive
		if (typeNames.size() > 1)
			Collections.sort(encounters, new EncounterComparator());
		return encounters;
	}
}
