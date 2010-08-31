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
package org.openmrs.module.clinicalsummary.encounter;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;

/**
 *
 */
public class TypeRegistry {
	
	private static Log log = LogFactory.getLog(TypeRegistry.class);
	
	/**
	 * Single instance of the class
	 */
	private static TypeRegistry instance;
	
	/**
	 * Mapping between the encounter type name and the encounter type
	 */
	private Map<String, EncounterType> encounterTypes;
	
	/**
	 * Initialize the singleton class
	 */
	private TypeRegistry() {
		initialize();
	}
	
	/**
	 * Perform reflection on all registered encounter type and then cached the type to reduce
	 * database read
	 */
	private void initialize() {
		Class<TypeConstants> constantsClass = TypeConstants.class;
		Field[] fields = constantsClass.getDeclaredFields();
		for (Field field : fields) {
			try {
				String value = (String) field.get(constantsClass);
				registerType(value);
			}
			catch (Exception e) {
				log.error("Exception thrown when trying to process registered encounter type ...", e);
			}
		}
	}
	
	/**
	 * Private entry point to the TypeRegistry class
	 * 
	 * @return the instance
	 */
	private static synchronized TypeRegistry getInstance() {
		
		if (log.isDebugEnabled())
			log.debug("Fetching instance and the instance is " + instance + " ... ");
		
		if (instance == null)
			instance = new TypeRegistry();
		
		return instance;
	}
	
	/**
	 * Return the value of the encounterTypes
	 * 
	 * @return the encounterTypes
	 */
	private final Map<String, EncounterType> getEncounterTypes() {
		if (encounterTypes == null)
			encounterTypes = new HashMap<String, EncounterType>();
		return encounterTypes;
	}
	
	/**
	 * Get encounter type based on the encounter type name
	 * 
	 * @param typeName name of the encounter type
	 * @return the encounter type or null if there's no encounter type with the parameter name
	 */
	private EncounterType getEncounterType(String typeName) {
		return getEncounterTypes().get(typeName);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.encounter.TypeRegistry#getEncounterType(String)
	 */
	public static EncounterType getCachedEncounterType(String typeName) {
		return getInstance().getEncounterType(typeName);
	}
	
	/**
	 * Get all encounter types that already been registered to the registry
	 * 
	 * @return all registered encounter types
	 */
	public static Collection<EncounterType> getCachedEncounterTypes() {
		return getInstance().getEncounterTypes().values();
	}
	
	/**
	 * Register an encounter type
	 * 
	 * @param typeName the encounter type name
	 */
	private void registerType(String typeName) {
		if (!getEncounterTypes().containsKey(typeName)) {
			EncounterType encounterType = Context.getEncounterService().getEncounterType(typeName);
			if (encounterType != null)
				getEncounterTypes().put(typeName, encounterType);
		}
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.encounter.TypeRegistry#registerType(String)
	 */
	public static void learnTypes(String typeName) {
		getInstance().registerType(typeName);
	}
}
