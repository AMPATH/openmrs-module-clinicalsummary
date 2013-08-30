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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.EncounterType;

import java.util.HashMap;
import java.util.Map;

public class EncounterTypeFactory {

	private static final Log log = LogFactory.getLog(EncounterTypeFactory.class);

	/**
     * Create an instance of encounter type based on the encounter type id. This encounter type object can be used
     * for the hibernate query because hibernate query will match the encounter type object using the encounter type's
     * id.
     *
     * @param encounterTypeId the encounter type id.
	 * @return the mock object of encounter type based on the encounter type id.
     * @see EncounterType#equals(Object)
     * @see org.openmrs.EncounterType#hashCode()
	 */
	public static EncounterType getEncounterType(final Integer encounterTypeId) {
        return new EncounterType(encounterTypeId);
	}
}
