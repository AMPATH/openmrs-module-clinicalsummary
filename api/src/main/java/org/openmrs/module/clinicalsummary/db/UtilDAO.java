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

package org.openmrs.module.clinicalsummary.db;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.Location;
import org.openmrs.OpenmrsObject;
import org.openmrs.Patient;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.clinicalsummary.enumeration.AgeUnit;
import org.openmrs.module.clinicalsummary.enumeration.Gender;
import org.openmrs.module.clinicalsummary.enumeration.StatusType;
import org.openmrs.module.clinicalsummary.util.obs.OrderedObs;
import org.openmrs.module.clinicalsummary.util.response.DeviceLog;
import org.openmrs.module.clinicalsummary.util.response.Response;
import org.openmrs.module.clinicalsummary.util.weight.WeightStandard;

/**
 *
 */
public interface UtilDAO {

	WeightStandard saveWeightStandard(final WeightStandard weightStandard) throws DAOException;

	WeightStandard getWeightStandard(final Integer id) throws DAOException;

	WeightStandard getWeightStandard(final Gender gender, final AgeUnit ageUnit, final Integer age) throws DAOException;

	OrderedObs saveOrderedObs(final OrderedObs orderedObs) throws DAOException;

	OrderedObs getOrderedObs(final Integer id) throws DAOException;

	List<OrderedObs> getOrderedObs(final Patient patient) throws DAOException;

	List<OrderedObs> getOrderedObs(final Map<String, Collection<OpenmrsObject>> restrictions,
	                               final Date startTime, final Date endTime) throws DAOException;

	Integer deleteOrderedObs(final List<Patient> patients) throws DAOException;

	List<Object[]> aggregateOrderedObs(final Map<String, Collection<OpenmrsObject>> restrictions, final Collection<String> groupingProperties,
	                                   final StatusType statusType, final Date startTime, final Date endTime) throws DAOException;

	<T extends Response> T saveResponse(final T response) throws DAOException;

	<T extends Response> T getResponse(final Class<T> clazz, final Integer id) throws DAOException;

	<T extends Response> List<T> saveResponses(List<T> responses) throws DAOException;

	<T extends Response> List<T> getResponses(final Class<T> clazz, final Patient patient) throws DAOException;

	<T extends Response> List<T> getResponses(final Class<T> clazz, final Location location,
	                                          final Date startDate, final Date endDate) throws DAOException;

	List<DeviceLog> saveDeviceLogs(List<DeviceLog> deviceLogs);
}
