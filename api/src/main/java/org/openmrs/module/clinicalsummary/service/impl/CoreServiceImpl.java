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

package org.openmrs.module.clinicalsummary.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.clinicalsummary.db.CoreDAO;
import org.openmrs.module.clinicalsummary.service.CoreService;
import org.openmrs.module.clinicalsummary.util.FetchRestriction;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Actual implementation of the core service contract
 */
public class CoreServiceImpl extends BaseOpenmrsService implements CoreService {

	private static final Log log = LogFactory.getLog(CoreServiceImpl.class);

	private CoreDAO coreDAO;

	/**
	 * Setter for the DAO interface reference that will be called by Spring to inject the actual implementation of the DAO layer
	 *
	 * @param coreDAO
	 * 		the coreDAO to be injected
	 */
	public void setCoreDAO(final CoreDAO coreDAO) {
		if (log.isDebugEnabled())
			log.debug("Wiring up CoreDAO with CoreService ...");

		this.coreDAO = coreDAO;
	}

	/**
	 * @see CoreService#getCohort(org.openmrs.Location, java.util.Date, java.util.Date)
	 */
	public Cohort getCohort(final Location location, final Date startDate, final Date endDate) throws APIException {
		return coreDAO.getCohort(location, startDate, endDate);
	}

	/**
	 * @see CoreService#getPatientEncounters(Integer, java.util.Map, org.openmrs.module.clinicalsummary.util.FetchRestriction)
	 */
	public List<Encounter> getPatientEncounters(final Integer patientId, final Map<String, Collection<OpenmrsObject>> restrictions,
	                                            final FetchRestriction fetchRestriction) throws APIException {
		return coreDAO.getPatientEncounters(patientId, restrictions, fetchRestriction);
	}

	/**
	 * @see CoreService#getPatientObservations(Integer, java.util.Map, org.openmrs.module.clinicalsummary.util.FetchRestriction)
	 */
	public List<Obs> getPatientObservations(final Integer patientId, final Map<String, Collection<OpenmrsObject>> restrictions,
	                                        final FetchRestriction fetchRestriction) throws APIException {
		return coreDAO.getPatientObservations(patientId, restrictions, fetchRestriction);
	}
}
