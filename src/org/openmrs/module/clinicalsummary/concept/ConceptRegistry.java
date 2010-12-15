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
package org.openmrs.module.clinicalsummary.concept;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSet;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

/**
 * Class that will hold all concept that has been registered. This class is written to prevent the
 * back and forth process to fetch the concept from database. This is a singleton class the concept
 * will always stay the same across generation process.
 */
public class ConceptRegistry {
	
	private static Log log = LogFactory.getLog(ConceptRegistry.class);
	
	/**
	 * Singleton instance that will hold this class
	 */
	private static ConceptRegistry instance;
	
	/**
	 * New map that will back this object
	 */
	private Map<CachedConceptKey, CachedConcept> cachedConcept;
	
	/**
	 * Initialize the singleton object
	 */
	private ConceptRegistry() {
		initialize();
	}
	
	/**
	 * Initialize the singleton object by performing reflection on classes that hold the information
	 * of all concepts that need to be registered. There are three annotations that will be read in
	 * this methods.
	 * <ul>
	 * <li>RegisterConcept determine if this concept should be cached or not</li>
	 * <li>BufferObservations determine if any observation on the concept should be buffered or not</li>
	 * <li>DisplayString determine if there should other string displayed instead of the concept
	 * name in the summary sheet</li>
	 * </ul>
	 */
	private void initialize() {
		
		Class<?>[] classes = { AnteNatalConceptConstants.class, StandardConceptConstants.class };
		
		Class<RegisterConcept> registerClass = RegisterConcept.class;
		Class<BufferObservations> bufferClass = BufferObservations.class;
		
		for (Class<?> clazz : classes) {
			
			Field[] classFields = clazz.getDeclaredFields();
			for (Field field : classFields)
				// only process field with RegisterConcept annotation
				if (field.isAnnotationPresent(registerClass))
					try {
						
						RegisterConcept registerAnnotation = field.getAnnotation(registerClass);
						
						String conceptName = (String) field.get(clazz);
						
						Collection<String> typeNames = null;
						if (field.isAnnotationPresent(bufferClass)) {
							BufferObservations bufferAnnotation = field.getAnnotation(bufferClass);
							typeNames = new HashSet<String>(Arrays.asList(bufferAnnotation.type()));
						}
						
						registerConcept(conceptName, typeNames);
						if (!StringUtils.isEmpty(registerAnnotation.substitute()))
							registerSubstitution(conceptName, typeNames, registerAnnotation.substitute());
					}
					catch (Exception e) {
						log.error("Processing concept constants throwing exception ...", e);
					}
		}
	}
	
	/**
	 * Entry point to access the Concept cache
	 * 
	 * @return ConceptRegistry that will cache the Concept informations
	 */
	private static synchronized ConceptRegistry getInstance() {
		
		if (log.isDebugEnabled())
			log.debug("Fetching instance and the instance is " + instance + " ... ");
		
		if (instance == null)
			instance = new ConceptRegistry();
		
		return instance;
	}
	
	/**
	 * Return the value of the cachedConcept
	 * 
	 * @return the cachedConcept
	 */
	private final Map<CachedConceptKey, CachedConcept> getCachedConcept() {
		if (cachedConcept == null)
			cachedConcept = new HashMap<CachedConceptKey, CachedConcept>();
		return cachedConcept;
	}
	
	/**
	 * Get all registered concepts in this concept registry
	 * 
	 * @return all registered cache concept
	 */
	public static Collection<CachedConcept> getAllCachedConcepts() {
		return getInstance().getCachedConcept().values();
	}
	
	/**
	 * Return the parent concept of a child concept
	 * 
	 * @param child the child concept
	 * @param parent the parent concept
	 * @return true if child is the child concept of parent concept
	 */
	private boolean isParentOf(Concept parent, Concept child) {
		for (CachedConcept cachedConcept : getCachedConcept().values()) {
			// there will be multiple concept registered with different type of encounter type
			// if we can't find the child in the first one, then we won't find it on the others
			if (OpenmrsUtil.nullSafeEquals(parent, cachedConcept.getConcept())) {
				if (cachedConcept.getChildConcepts().contains(child))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.concept.ConceptRegistry#isParentOf(Concept, Concept)
	 */
	public static boolean isCachedParentOf(Concept parent, Concept child) {
		return getInstance().isParentOf(parent, child);
	}
	
	/**
	 * Search for a concept in the registered concept
	 * 
	 * @param conceptName the concept name
	 * @param typeNames the encounter type names
	 * @return
	 */
	private Concept getConcept(String conceptName, Collection<String> typeNames) {
		CachedConceptKey cachedConceptKey = new CachedConceptKey(conceptName, typeNames);
		CachedConcept cachedConcept = getCachedConcept().get(cachedConceptKey);
		if (cachedConcept != null)
			return cachedConcept.getConcept();
		return null;
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.concept.ConceptRegistry#getConcept(String,
	 *      Collection)
	 */
	public static Concept getCachedConcept(String conceptName, Collection<String> typeNames) {
		return getInstance().getConcept(conceptName, typeNames);
	}
	
	/**
	 * Convenient method to get a concept
	 * 
	 * @param conceptName the concept name
	 * @return the concept
	 */
	private Concept getConcept(String conceptName) {
		Concept concept = getConcept(conceptName, null);
		if (concept == null)
			for (CachedConceptKey cachedConceptKey : getCachedConcept().keySet())
				if (StringUtils.equals(conceptName, cachedConceptKey.getConceptName())) {
					CachedConcept cachedConcept = getCachedConcept().get(cachedConceptKey);
					concept = cachedConcept.getConcept();
				}
		return concept;
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.concept.ConceptRegistry#getConcept(String)
	 */
	public static Concept getCachedConcept(String conceptName) {
		return getInstance().getConcept(conceptName);
	}
	
	/**
	 * Register a new concept to the registry.
	 * 
	 * @param conceptName the concept name
	 * @param typeNames the type names
	 */
	private void registerConcept(String conceptName, Collection<String> typeNames) {
		CachedConceptKey cachedConceptKey = new CachedConceptKey(conceptName, typeNames);
		CachedConcept cachedConcept = getCachedConcept().get(cachedConceptKey);
		// only teach the registry about the new concept when it's not there yet
		if (cachedConcept == null) {
			// only register not null concept
			// there's no point of registering null concept duh!!!!!!
			Concept concept = Context.getConceptService().getConcept(conceptName);
			if (concept != null) {
				cachedConcept = new CachedConcept();
				cachedConcept.setConcept(concept);
				// cached concept will have null for encounter type if there will no buffering for obs on that concept
				if (typeNames != null) {
					Collection<EncounterType> encounterTypes = new HashSet<EncounterType>();
					for (String typeName : typeNames) {
						EncounterType encounterType = Context.getEncounterService().getEncounterType(typeName);
						if (encounterType != null)
							encounterTypes.add(encounterType);
					}
					cachedConcept.setEncounterTypes(encounterTypes);
				}
				// cache the child too for coded concept and concept set
				if (concept.getAnswers() != null)
					for (ConceptAnswer answer : concept.getAnswers()) {
						Concept answerConcept = answer.getAnswerConcept();
						if (answerConcept == null)
							continue;
						// add this a child
						cachedConcept.addChildConcept(answerConcept);
						// not sure if we also need to register the child
						// TODO: might need to comment out this part
						for (ConceptName name : answerConcept.getNames())
							registerChildConcept(name.getName(), answerConcept);
					}
				if (concept.getConceptSets() != null)
					for (ConceptSet set : concept.getConceptSets()) {
						Concept conceptSet = set.getConcept();
						// just making sure it's null
						if (conceptSet == null)
							continue;
						// add this a child
						cachedConcept.addChildConcept(conceptSet);
						// not sure if we also need to register the child
						// TODO: might need to comment this out
						for (ConceptName name : conceptSet.getNames())
							registerChildConcept(name.getName(), conceptSet);
					}
				getCachedConcept().put(cachedConceptKey, cachedConcept);
			}
		}
	}
	
	/**
	 * Helper method to register a concept under a certain name
	 * 
	 * @param conceptName the name of the concept
	 * @param concept the concept
	 */
	private void registerChildConcept(String conceptName, Concept concept) {
		CachedConceptKey cachedConceptKey = new CachedConceptKey(conceptName);
		if (!getCachedConcept().containsKey(cachedConceptKey)) {
			CachedConcept cachedConcept = new CachedConcept();
			cachedConcept.setConcept(concept);
			getCachedConcept().put(cachedConceptKey, cachedConcept);
		}
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.concept.ConceptRegistry#learnConcept(String,
	 *      Collection)
	 */
	public static void learnConcept(String conceptName, Collection<String> typeNames) {
		getInstance().registerConcept(conceptName, typeNames);
	}
	
	/**
	 * Convenient method to register a concept under a certain name
	 * 
	 * @param conceptName the concept name
	 * @param concept the concept
	 */
	private void registerConcept(String conceptName) {
		// only register this concept when there's actually a concept there
		registerConcept(conceptName, null);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.concept.ConceptRegistry#learnConcept(String)
	 */
	public static void learnConcept(String conceptName) {
		getInstance().registerConcept(conceptName);
	}
	
	/**
	 * Register a new string substitution for a concept. One concept can only have one substitution
	 * string.
	 * 
	 * @param conceptName the concept name
	 * @param substitution the value to be displayed
	 */
	private void registerSubstitution(String conceptName, Collection<String> typeNames, String substitution) {
		CachedConceptKey cachedConceptKey = new CachedConceptKey(conceptName, typeNames);
		CachedConcept cachedConcept = getCachedConcept().get(cachedConceptKey);
		if (cachedConcept != null)
			cachedConcept.setSubstitution(substitution);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.concept.ConceptRegistry#registerSubstitutions(String,
	 *      String)
	 */
	public static void cacheSubstitution(String conceptName, Collection<String> typeNames, String substitution) {
		getInstance().registerSubstitution(conceptName, typeNames, substitution);
	}
	
	/**
	 * Get the substitution String for a concept
	 * 
	 * @param conceptName the concept name
	 * @param typeNames the collection of type
	 * @return the substitution String
	 */
	private String getSubstitution(String conceptName, Collection<String> typeNames) {
		String substitution = null;
		CachedConceptKey cachedConceptKey = new CachedConceptKey(conceptName, typeNames);
		CachedConcept cachedConcept = getCachedConcept().get(cachedConceptKey);
		if (cachedConcept != null)
			substitution = cachedConcept.getSubstitution();
		return substitution;
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.concept.ConceptRegistry#getSubstitution(String,
	 *      Collection)
	 */
	public static String getCachedSubstitution(String conceptName, Collection<String> typeNames) {
		return getInstance().getSubstitution(conceptName, typeNames);
	}
	
	/**
	 * Method to search for a key in the cached concept based on the concept name and partial of the
	 * type names
	 * 
	 * @param conceptName the concept name
	 * @param partialTypeNames sub-collection of all the type name in the concept registry
	 * @return cached concept key matching the criteria or null
	 */
	private CachedConceptKey findCachedConceptDescription(String conceptName, Collection<String> partialTypeNames) {
		for (CachedConceptKey cachedConceptKey : getCachedConcept().keySet()) {
			if (cachedConceptKey.getConceptName().equals(conceptName))
				if (cachedConceptKey.getTypeNames() != null
				        && CollectionUtils.isSubCollection(cachedConceptKey.getTypeNames(), partialTypeNames))
					return cachedConceptKey;
		}
		
		return null;
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.concept.ConceptRegistry#findCachedConceptDescription(String,
	 *      Collection)
	 */
	public static Collection<String> findTypeNames(String conceptName, Collection<String> partialTypeNames) {
		CachedConceptKey cachedConceptKey = getInstance().findCachedConceptDescription(conceptName, partialTypeNames);
		if (cachedConceptKey == null)
			return Collections.emptyList();
		return cachedConceptKey.getTypeNames();
	}
	
	/**
	 * Destroy the cache object
	 */
	public static void clearInstance() {
		if (instance != null)
			instance = null;
	}
}
