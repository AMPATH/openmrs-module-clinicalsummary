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

import java.util.Collection;
import java.util.HashSet;

import org.openmrs.Concept;
import org.openmrs.EncounterType;

/**
 *
 */
public class CachedConcept {
	
	/**
	 * The registered and cached concept itself
	 */
	private Concept concept;
	
	/**
	 * String that will substitute the concept. This need to be registered manually
	 */
	private String substitution;
	
	/**
	 * List of all encounter types on which the observation for this concept should be cached
	 */
	private Collection<EncounterType> encounterTypes;
	
	/**
	 * All answers or concept sets for this concept that also need to be cached
	 */
	private Collection<Concept> childConcepts;
	
	/**
	 * Return the value of the concept
	 * 
	 * @return the concept
	 */
	public final Concept getConcept() {
		return concept;
	}
	
	/**
	 * Set the concept with the concept value
	 * 
	 * @param concept the concept to set
	 */
	public final void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	/**
	 * Return the value of the substitution
	 * 
	 * @return the substitution
	 */
	public final String getSubstitution() {
		return substitution;
	}
	
	/**
	 * Set the substitution with the substitution value
	 * 
	 * @param substitution the substitution to set
	 */
	public final void setSubstitution(String substitution) {
		this.substitution = substitution;
	}
	
	/**
	 * Return the value of the encounterTypes
	 * 
	 * @return the encounterTypes
	 */
	public final Collection<EncounterType> getEncounterTypes() {
		return encounterTypes;
	}
	
	/**
	 * Set the encounterTypes with the encounterTypes value
	 * 
	 * @param encounterTypes the encounterTypes to set
	 */
	public final void setEncounterTypes(Collection<EncounterType> encounterTypes) {
		this.encounterTypes = encounterTypes;
	}
	
	/**
	 * Return the value of the childConcepts
	 * 
	 * @return the childConcepts
	 */
	public final Collection<Concept> getChildConcepts() {
		if (childConcepts == null)
			childConcepts = new HashSet<Concept>();
		return childConcepts;
	}
	
	/**
	 * Set the childConcepts with the childConcepts value
	 * 
	 * @param childConcepts the childConcepts to set
	 */
	public final void setChildConcepts(Collection<Concept> childConcepts) {
		this.childConcepts = childConcepts;
	}
	
	/**
	 * Convenient method to add a concept to the this object
	 * 
	 * @param child the child concept
	 */
	public final void addChildConcept(Concept child) {
		getChildConcepts().add(child);
	}
	
}
