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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark that a observation with the concept should be buffered from all encounters
 * with matching encounter type.
 * 
 * <pre>
 * &#064;BufferObservations(type = CollectionType.ANC_ENCOUNTER_TYPE)
 * String MEDICATION_ADDED = &quot;MEDICATION ADDED&quot;;
 * </pre>
 * 
 * The above code means that all observations on the &quot;MEDICATION ADDED&quot; concept should be buffered
 * and the observations should come from encounter with encounter type defined by the
 * ANC_ENCOUNTER_TYPE collection
 */
@Target( { ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface BufferObservations {
	
	/**
	 * Buffer all observations on this concept for a certain encounter types
	 * 
	 * @return the CollectionType of the encounter type where the observations come from
	 */
	String[] type() default {};
	
}
