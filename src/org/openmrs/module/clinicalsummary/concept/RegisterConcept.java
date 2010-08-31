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

import org.apache.commons.lang.StringUtils;

/**
 * Annotation for a concept to tell the concept cache that this concept should be cached
 * 
 * <pre>
 * 
 * &#064;RegisterConcept
 * String BLOOD_TYPING = &quot;BLOOD TYPING&quot;;
 * </pre>
 * 
 * The above means that &quot;BLOOD TYPING&quot; is a concept that should be buffered
 */
@Target( { ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RegisterConcept {
	
	/**
	 * Register a String substitution for display when the concept need to be displayed in the
	 * summary
	 * 
	 * @return the substitution String
	 */
	String substitute() default StringUtils.EMPTY;
}
