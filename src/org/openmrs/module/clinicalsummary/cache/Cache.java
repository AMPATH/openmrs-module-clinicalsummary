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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Cohort;

/**
 * Cache to hold buffer data that will be used to generate clinical summary. The buffer support all
 * Subclass of the BaseOpenMRSData because we might need other stuff in the future for example patient's
 * program or some other type of data.
 */
class Cache<T extends BaseOpenmrsData> {
	
	private static final Log log = LogFactory.getLog(Cache.class);
	
	/**
	 * Map that will hold all cached information.
	 */
	private Map<CacheEntryKey, CacheEntry<T>> cache;
	
	/**
	 * Method to get the map that's backing this caching system. Should never be called by other classes.
	 * All interaction with the cache should be done through get and put of the Cache object.
	 * 
	 * @return map that back this Cache class
	 */
	private Map<CacheEntryKey, CacheEntry<T>> getCache() {
		if (cache == null)
			cache = new HashMap<CacheEntryKey, CacheEntry<T>>();
		
		return cache;
	}
	
	/**
	 * Clean out expired values from the cache
	 */
	public void clear() {
		long now = new Date().getTime();
		for (CacheEntryKey key : getCache().keySet())
			if (key.getExpires() < now)
				getCache().remove(key);
	}
	
	/**
	 * Put an entry to the cache system. Other class that will use the Cache must use this method
	 * to cache the information.
	 * 
	 * @param cohort list of patient id that have this information
	 * @param templateId which template id this information can be used
	 * @param entryType type of entry in the cache. For now we only support Obs and Encounter
	 * @param entry the information that needs to be cached
	 */
	public void put(Cohort cohort, CacheEntryType entryType, CacheDescriptor<BaseOpenmrsObject> descriptor, CacheEntry<T> entry) {
		CacheEntryKey cacheKey = new CacheEntryKey(cohort, entryType, descriptor);
		put(cacheKey, entry);
	}
	
	/**
	 * Search the Cache for an entry that match partial part of the CacheEntryKey. First this method will
	 * search for all key that looks similar for the input. Return NULL if no key match the partial key
	 * part
	 * 
	 * @param entryType entry type to be searched
	 * @param descriptor list of discriminating elements of the partial key
	 * @return any entry in the Cache that match the partial key or NULL when none is found
	 */
	public CacheEntry<T> get(CacheEntryType entryType, CacheDescriptor<BaseOpenmrsObject> descriptor) {
		CacheEntryKey entryKey = null;
		for (CacheEntryKey cacheEntryKey : getCache().keySet())
			if (cacheEntryKey.getDescriptors().equals(descriptor) && cacheEntryKey.getEntryType().equals(entryType))
				// get the latest key in case we see multiple similar key
				if (entryKey == null || entryKey.getExpires() < cacheEntryKey.getExpires())
					entryKey = cacheEntryKey;
		return get(entryKey);
	}
	
	/**
	 * Search the cache for full key input.
	 * 
	 * @param cohort list of the patient
	 * @param entryType type of the entry in the Cache
	 * @param descriptor list of discriminating elements from the key
	 * @return any entry in the Cache that match the partial key or NULL when none is foung
	 */
	public CacheEntry<T> get(Cohort cohort, CacheEntryType entryType, CacheDescriptor<BaseOpenmrsObject> descriptor) {
		CacheEntryKey cacheKey = new CacheEntryKey(cohort, entryType, descriptor);
		return get(cacheKey);
	}
	
	/**
	 * Get an entry from the cache
	 * 
	 * @param cacheKey
	 * @return any entry in the Cache that match the partial key or NULL when none is found
	 */
	private CacheEntry<T> get(CacheEntryKey cacheKey) {
		CacheEntry<T> entry = getCache().get(cacheKey);
		
		if (entry == null)
			return null;
		
		return entry;
	}
	
	/**
	 * Put an entry in the cache
	 * 
	 * @param cacheKey
	 * @param entry
	 */
	private void put(CacheEntryKey cacheKey, CacheEntry<T> entry) {
		log.debug("Inserting cache key: " + cacheKey.toString());
		getCache().put(cacheKey, entry);
	}
}
