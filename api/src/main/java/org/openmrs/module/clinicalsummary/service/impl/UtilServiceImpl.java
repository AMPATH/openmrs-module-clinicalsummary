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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.OpenmrsObject;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.clinicalsummary.db.UtilDAO;
import org.openmrs.module.clinicalsummary.enumeration.AgeUnit;
import org.openmrs.module.clinicalsummary.enumeration.Gender;
import org.openmrs.module.clinicalsummary.enumeration.StatusType;
import org.openmrs.module.clinicalsummary.service.UtilService;
import org.openmrs.module.clinicalsummary.util.obs.OrderedObs;
import org.openmrs.module.clinicalsummary.util.response.MedicationResponse;
import org.openmrs.module.clinicalsummary.util.response.ReminderResponse;
import org.openmrs.module.clinicalsummary.util.weight.WeightStandard;

/**
 */
public class UtilServiceImpl extends BaseOpenmrsService implements UtilService {

	private static final Log log = LogFactory.getLog(UtilServiceImpl.class);

	private UtilDAO utilDAO;

	/**
	 * Setter for the DAO interface reference that will be called by Spring to inject the actual implementation of the DAO layer
	 *
	 * @param utilDAO
	 * 		the utilDAO to be injected
	 */
	public void setUtilDAO(final UtilDAO utilDAO) {
		if (log.isDebugEnabled())
			log.debug("Wiring up UtilDAO with UtilService ...");

		this.utilDAO = utilDAO;
	}

	/**
	 * @see UtilService#saveWeightStandard(org.openmrs.module.clinicalsummary.util.weight.WeightStandard)
	 */
	public WeightStandard saveWeightStandard(final WeightStandard weightStandard) throws APIException {
		return utilDAO.saveWeightStandard(weightStandard);
	}

	/**
	 * @see UtilService#getWeightStandard(Integer)
	 */
	public WeightStandard getWeightStandard(final Integer id) throws APIException {
		return utilDAO.getWeightStandard(id);
	}

	/**
	 * @see UtilService#getWeightStandard(org.openmrs.module.clinicalsummary.enumeration.Gender, org.openmrs.module.clinicalsummary.enumeration.AgeUnit,
	 *      Integer)
	 */
	public WeightStandard getWeightStandard(final Gender gender, final AgeUnit ageUnit, final Integer age) throws APIException {
		return utilDAO.getWeightStandard(gender, ageUnit, age);
	}

	/**
	 * @see UtilService#saveOrderedObs(org.openmrs.module.clinicalsummary.util.obs.OrderedObs)
	 */
	public OrderedObs saveOrderedObs(final OrderedObs orderedObs) throws APIException {
		return utilDAO.saveOrderedObs(orderedObs);
	}

	/**
	 * @see UtilService#getOrderedObs(Integer)
	 */
	public OrderedObs getOrderedObs(final Integer id) throws APIException {
		return utilDAO.getOrderedObs(id);
	}

	/**
	 * @see UtilService#getOrderedObs(org.openmrs.Patient)
	 */
	public List<OrderedObs> getOrderedObs(final Patient patient) throws APIException {
		return utilDAO.getOrderedObs(patient);
	}

	/**
	 * @see UtilService#getOrderedObs(java.util.Map, java.util.Date, java.util.Date)
	 */
	public List<OrderedObs> getOrderedObs(final Map<String, Collection<OpenmrsObject>> restrictions,
	                                      final Date startTime, final Date endTime) throws APIException {
		return utilDAO.getOrderedObs(restrictions, startTime, endTime);
	}

	/**
	 * @see UtilService#deleteOrderedObs(java.util.List
	 */
	public Integer deleteOrderedObs(final List<Patient> patients) throws APIException {
		return utilDAO.deleteOrderedObs(patients);
	}

	/**
	 * @see UtilService#aggregateOrderedObs(java.util.Map, java.util.Collection, org.openmrs.module.clinicalsummary.enumeration.StatusType, java.util.Date, java.util.Date)
	 */
	public List<Object[]> aggregateOrderedObs(final Map<String, Collection<OpenmrsObject>> restrictions, final Collection<String> groupingProperties,
	                                          final StatusType statusType, final Date startTime, final Date endTime) throws APIException {
		return utilDAO.aggregateOrderedObs(restrictions, groupingProperties, statusType, startTime, endTime);
	}

	/**
	 * @see UtilService#aggregateOrderedObs(java.util.Map, java.util.Collection)
	 */
	public List<Object[]> aggregateOrderedObs(Map<String, Collection<OpenmrsObject>> restrictions, Collection<String> groupingProperty)
			throws APIException {
		return aggregateOrderedObs(restrictions, groupingProperty, null, null, null);
	}

	/**
	 * @see UtilService#saveResponses(java.util.List)
	 * @param responses
	 */
	public List<? extends BaseOpenmrsData> saveResponses(final List<? extends BaseOpenmrsData> responses) {
		return utilDAO.saveResponses(responses);
	}

	/**
	 * @see UtilService#getMedicationResponses(org.openmrs.Patient)
	 */
	public List<MedicationResponse> getMedicationResponses(final Patient patient) {
		return utilDAO.getMedicationResponses(patient);
	}

	/**
	 * @see UtilService#getMedicationResponses(org.openmrs.Patient)
	 */
	public List<ReminderResponse> getReminderResponses(final Patient patient) {
		return utilDAO.getReminderResponses(patient);
	}
}
