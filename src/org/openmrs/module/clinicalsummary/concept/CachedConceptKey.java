package org.openmrs.module.clinicalsummary.concept;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

public class CachedConceptKey {
	
	/**
	 * The registered name of the concept
	 */
	private final String conceptName;
	
	/**
	 * List of all encounter types name on which the observation for this concept should be cached
	 */
	private final Collection<String> typeNames;
	
	/**
	 * @param conceptName
	 * @param typeNames
	 */
	public CachedConceptKey(String conceptName, Collection<String> typeNames) {
		this.conceptName = conceptName;
		this.typeNames = typeNames;
	}
	
	/**
	 * @param conceptName
	 */
	public CachedConceptKey(String conceptName) {
		this.conceptName = conceptName;
		this.typeNames = null;
	}
	
	/**
	 * Return the value of the conceptName
	 * 
	 * @return the conceptName
	 */
	public final String getConceptName() {
		return conceptName;
	}
	
	/**
	 * Return the value of the typeNames
	 * 
	 * @return the typeNames
	 */
	public final Collection<String> getTypeNames() {
		return typeNames;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		
		if (obj == null)
			return false;
		
		if (obj instanceof CachedConceptKey) {
			CachedConceptKey cachedConceptKey = (CachedConceptKey) obj;
			
			if (StringUtils.equals(getConceptName(), cachedConceptKey.getConceptName())) {
				// if both have null type names, then they are equal. empty list will be considered different
				if (getTypeNames() == null && cachedConceptKey.getTypeNames() == null)
					return true;
				// if both not null, then check if both contains exactly the same stuff
				if (getTypeNames() != null && cachedConceptKey.getTypeNames() != null)
					return CollectionUtils.isEqualCollection(getTypeNames(), cachedConceptKey.getTypeNames());
			}
		}
		
		return super.equals(obj);
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 8;
		hash = 31 * getConceptName().hashCode() + hash;
		if (getTypeNames() != null)
			
			for (String typeName : getTypeNames())
				hash = 31 * typeName.hashCode() + hash;
		return hash;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("concept name", getConceptName());
		if (getTypeNames() != null)
			for (String typeName : getTypeNames())
				builder.append("type name", typeName);
		return builder.toString();
	}
}
