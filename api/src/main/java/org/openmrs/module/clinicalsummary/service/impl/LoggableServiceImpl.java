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
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.clinicalsummary.Loggable;
import org.openmrs.module.clinicalsummary.db.LoggableDAO;
import org.openmrs.module.clinicalsummary.service.LoggableService;

import java.util.List;

/**
 */
public class LoggableServiceImpl extends BaseOpenmrsService implements LoggableService {

	private static final Log log = LogFactory.getLog(LoggableServiceImpl.class);

	private LoggableDAO loggableDAO;

	/**
	 * Setter for the DAO interface reference that will be called by Spring to inject the actual implementation of the DAO layer
	 *
	 * @param loggableDAO
	 * 		the loggableDAO to be injected
	 */
	public void setLoggableDAO(final LoggableDAO loggableDAO) {
		if (log.isDebugEnabled())
			log.debug("Wiring up LoggableDAO with LoggableService ...");

		this.loggableDAO = loggableDAO;
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.service.LoggableService#saveLoggable(org.openmrs.module.clinicalsummary.Loggable)
	 */
	public Loggable saveLoggable(final Loggable loggable) throws APIException {
		return loggableDAO.saveLoggable(loggable);
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.service.LoggableService#getLoggable(Integer)
	 */
	public Loggable getLoggable(final Integer id) throws APIException {
		return loggableDAO.getLoggable(id);
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.service.LoggableService#getLoggables(org.openmrs.Patient)
	 */
	public List<Loggable> getLoggables(final Patient patient) throws APIException {
		return loggableDAO.getLoggables(patient);
	}
}
