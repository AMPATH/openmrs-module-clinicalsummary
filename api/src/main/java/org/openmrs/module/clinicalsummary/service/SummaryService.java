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
package org.openmrs.module.clinicalsummary.service;

import java.util.List;

import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.clinicalsummary.Constants;
import org.openmrs.module.clinicalsummary.Mapping;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.enumeration.MappingType;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service contract for operation on Summary object
 */
@Transactional
public interface SummaryService extends OpenmrsService {

    /**
     * Save a summary object to the database.
     *
     * @param summary summary object that will saved to the database
     * @return saved summary summary
     * @throws APIException
     * @should save summary object to the database
     * @should update the summary object to the database
     */
    @Authorized({Constants.PRIVILEGE_MANAGE_SUMMARY})
    Summary saveSummary(final Summary summary) throws APIException;

    /**
     * Get a summary object based on the id of the summary
     *
     * @param id of the summary object to be retrieved
     * @return summary object when a summary with specified id is in the database or null when no summary object found in the
     *         database
     * @throws APIException
     * @should return summary object with the input id
     * @should return null when no summary found with the input id
     */
    @Transactional(readOnly = true)
    @Authorized({Constants.PRIVILEGE_VIEW_SUMMARY})
    Summary getSummary(final Integer id) throws APIException;

    /**
     * Get all registered summaries from the database.
     *
     * @return all registered summaries
     * @throws APIException
     * @should return all registered summaries
     * @should return empty list when no summary are registered
     */
    @Transactional(readOnly = true)
    @Authorized({Constants.PRIVILEGE_VIEW_SUMMARY})
    List<Summary> getAllSummaries() throws APIException;

    /**
     * Get all valid summary that can be executed for a certain patient
     *
     * @param patient the patient
     * @return all registered summaries that valid for the patient
     * @throws APIException
     * @should return all registered summaries
     * @should return empty list when no summary are registered
     */
    @Transactional(readOnly = true)
    @Authorized({Constants.PRIVILEGE_VIEW_SUMMARY})
    List<Summary> getSummaries(Patient patient) throws APIException;

    /**
     * Create a new mapping between a summary and an encounter type in the system. Each summary can be mapped to multiple encounter
     * types
     *
     * @param mapping the summary mapping to be saved
     * @return saved summary mapping
     * @throws APIException
     * @should saved the newly created summary mapping
     */
    @Authorized({Constants.PRIVILEGE_MANAGE_SUMMARY})
    Mapping saveMapping(final Mapping mapping) throws APIException;

    /**
     * Get mapping object based on the id of the mapping
     *
     * @param id of the summary object to be retrieved
     * @return mapping object when a mapping with specified id is in the database or null when no mapping object found in the
     *         database
     * @throws APIException
     * @should return mapping object with the input id
     * @should return null when no mapping found with the input id
     */
    @Transactional(readOnly = true)
    @Authorized({Constants.PRIVILEGE_VIEW_SUMMARY})
    Mapping getMapping(final Integer id) throws APIException;

    /**
     * Get all available summary mappings from the system.
     *
     * @return all available summary mappings from the database
     * @throws APIException
     * @should return all saved summary mappings
     */
    @Transactional(readOnly = true)
    @Authorized({Constants.PRIVILEGE_VIEW_SUMMARY})
    List<Mapping> getAllMappings() throws APIException;

    /**
     * Return all mapping for a given summary
     *
     * @param summary the summary that was mapped
     * @return list of all summary mapping
     * @throws APIException
     * @should return all mappings for a certain summary
     */
    @Transactional(readOnly = true)
    @Authorized({Constants.PRIVILEGE_VIEW_SUMMARY})
    List<Mapping> getMappings(final Summary summary, final EncounterType encounterType, final MappingType mappingType) throws APIException;

    /**
     * Return all summaries for a given mapping type
     *
     * @param mappingType the type of the mapping between summary and encounter type
     * @return list of all summaries
     * @throws APIException
     * @should return all summaries for a certain mapping type
     */
    @Transactional(readOnly = true)
    @Authorized({Constants.PRIVILEGE_VIEW_SUMMARY})
    List<Summary> getSummariesByMappingType(final MappingType mappingType) throws APIException;
}
