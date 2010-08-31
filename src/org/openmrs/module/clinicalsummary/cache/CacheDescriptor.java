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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.openmrs.BaseOpenmrsObject;

/**
 * Class that will be the discriminating factor in the Cache key object. This class can contains any
 * subclass object of the BaseOpenMRSObject. This decision was largely because the difference
 * between the parent class for Concept, extending BaseOpenMRSObject rather than BaseOpenMRSMetadata
 * like other metadata type classes like EncounterType.<br/><br/>
 * 
 * Used alongside CacheDescriptorType to determine the type of descriptor object that will be added to
 * this class.
 * 
 * @param <T> Data that will be entered into the descriptor. Most likely it will BaseOpenMRSObject
 *            itself because we will enter multiple type of data into the descriptor
 */
class CacheDescriptor<T extends BaseOpenmrsObject> {
	
	/**
	 * Map of the type of descriptor to list of object that discriminating the key to the Cache
	 */
	private Map<CacheDescriptorType, List<T>> cacheDescriptors;
	
	/**
	 * Add a concept type into the descriptor.
	 * 
	 * @param concept concept that will be added into the descriptors list
	 * @deprecated use addDescriptor(CacheDescriptorType, T) instead
	 */
	@Deprecated
	public void addConcept(T concept) {
		process(CacheDescriptorType.OBSERVED_CONCEPT, concept);
	}
	
	/**
	 * Add an encounter type to the list of discriminating factor to the Cache key object
	 * 
	 * @param encounterType encounterType that will be added into the descriptors list
	 * @deprecated use addDescriptor(CacheDescriptorType, T) instead
	 */
	@Deprecated
	public void addEncounterType(T encounterType) {
		process(CacheDescriptorType.ENCOUNTER_TYPE, encounterType);
	}
	
	/**
	 * Add an object to the descriptor list
	 * 
	 * @param type type of the object that will be added
	 * @param t the object itself
	 */
	public void addDescriptor(CacheDescriptorType type, T t) {
		process(type, t);
	}
	
	/**
	 * Internal process that will put the object to the correct list
	 * 
	 * @param type type of the object that will be added
	 * @param t the object itself
	 */
	private void process(CacheDescriptorType type, T t) {
		List<T> descriptors = get(type);
		if (descriptors == null) {
			descriptors = new ArrayList<T>();
			put(type, descriptors);
		}
		descriptors.add(t);
	}
	
	/**
	 * Internal process to get a list of specific descriptor type
	 * 
	 * @param type type of the object
	 * @return list of all object based on the input type
	 */
	private List<T> get(CacheDescriptorType type) {
		return getCacheDescriptors().get(type);
	}
	
	/**
	 * Internal process to set the list of a certain type in the descriptor
	 * 
	 * @param type type of the object
	 * @param descriptors list of all object for the type
	 */
	private void put(CacheDescriptorType type, List<T> descriptors) {
		getCacheDescriptors().put(type, descriptors);
	}
	
	/**
	 * Getter for the map backing this descriptor object
	 * 
	 * @return the cacheDescriptors
	 */
	public Map<CacheDescriptorType, List<T>> getCacheDescriptors() {
		
		if (cacheDescriptors == null)
			cacheDescriptors = new HashMap<CacheDescriptorType, List<T>>();
		
		return cacheDescriptors;
	}
	
	/**
	 * Setter for the map backing this descriptor object
	 * 
	 * @param cacheDescriptors the cacheDescriptors to set
	 */
	public void setCacheDescriptors(Map<CacheDescriptorType, List<T>> cacheDescriptors) {
		this.cacheDescriptors = cacheDescriptors;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (obj == null)
			return false;
		
		if (obj instanceof CacheDescriptor<?>) {
			CacheDescriptor<?> d = (CacheDescriptor<?>) obj;
			
			if (!SetUtils.isEqualSet(getCacheDescriptors().keySet(), d.getCacheDescriptors().keySet()))
				return false;
			
			for (CacheDescriptorType descriptorType : getCacheDescriptors().keySet()) {
				List<?> objects = getCacheDescriptors().get(descriptorType);
				List<?> dObjects = d.getCacheDescriptors().get(descriptorType);
				if (!CollectionUtils.isEqualCollection(objects, dObjects))
					return false;
			}
			
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
		for (CacheDescriptorType descriptorType : getCacheDescriptors().keySet()) {
			hash = 31 * descriptorType.hashCode() + hash;
			List<T> objects = getCacheDescriptors().get(descriptorType);
			for (T t : objects)
				hash = 31 * t.hashCode() + hash;
		}
		return hash;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		for (CacheDescriptorType descriptorType : getCacheDescriptors().keySet()) {
			builder.append("descriptor type", descriptorType);
			List<T> objects = getCacheDescriptors().get(descriptorType);
			for (T t : objects)
				builder.append("openmrs object", t.getUuid());
		}
		return builder.toString();
	}
	
}
