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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.Mapping;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.db.SummaryDAO;
import org.openmrs.module.clinicalsummary.enumeration.MappingType;
import org.openmrs.module.clinicalsummary.rule.evaluator.SummaryValidatorRule;
import org.openmrs.module.clinicalsummary.service.EvaluatorService;
import org.openmrs.module.clinicalsummary.service.SummaryService;

/**
 */
public class SummaryServiceImpl extends BaseOpenmrsService implements SummaryService {

	private static final Log log = LogFactory.getLog(SummaryServiceImpl.class);

	private SummaryDAO summaryDAO;

	/**
	 * Setter for the DAO interface reference that will be called by Spring to inject the actual implementation of the DAO layer
	 *
	 * @param summaryDAO the summaryDAO to be injected
	 */
	public void setSummaryDAO(final SummaryDAO summaryDAO) {
		if (log.isDebugEnabled())
			log.debug("Wiring up SummaryDAO with SummaryService ...");

		this.summaryDAO = summaryDAO;
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.service.SummaryService#saveSummary(org.openmrs.module.clinicalsummary.Summary)
	 */
	@Override
	public Summary saveSummary(final Summary summary) throws APIException {
		// increase the revision before saving the template
		summary.setRevision(summary.getRevision() + 1);
		return summaryDAO.saveSummary(summary);
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.service.SummaryService#getSummary(Integer)
	 */
	@Override
	public Summary getSummary(final Integer id) throws APIException {
		return summaryDAO.getSummary(id);
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.service.SummaryService#getAllSummaries()
	 */
	@Override
	public List<Summary> getAllSummaries() throws APIException {
		return summaryDAO.getAllSummaries(Boolean.FALSE);
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.service.SummaryService#getSummaries(Patient)
	 */
	@Override
	public List<Summary> getSummaries(Patient patient) throws APIException {
		List<Summary> summaries = new ArrayList<Summary>();

		Map<String, Object> parameters = new HashMap<String, Object>();

		EvaluatorService evaluatorService = Context.getService(EvaluatorService.class);
		Result summaryResults = evaluatorService.evaluate(patient, SummaryValidatorRule.TOKEN, parameters);
		for (Result summaryResult : summaryResults)
			summaries.add((Summary) summaryResult.getResultObject());

		return summaries;
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.service.SummaryService#saveMapping(org.openmrs.module.clinicalsummary.Mapping)
	 */
	@Override
	public Mapping saveMapping(final Mapping mapping) throws APIException {
		return summaryDAO.saveMapping(mapping);
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.service.SummaryService#getMapping(Integer)
	 */
	@Override
	public Mapping getMapping(final Integer id) throws APIException {
		return summaryDAO.getMapping(id);
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.service.SummaryService#getAllMappings()
	 */
	@Override
	public List<Mapping> getAllMappings() throws APIException {
		return summaryDAO.getAllMappings();
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.service.SummaryService#getMappings(org.openmrs.module.clinicalsummary.Summary,
	 *      org.openmrs.EncounterType, org.openmrs.module.clinicalsummary.enumeration.MappingType)
	 */
	@Override
	public List<Mapping> getMappings(final Summary summary, final EncounterType encounterType, final MappingType mappingType) throws APIException {
		return summaryDAO.getMappings(summary, encounterType, mappingType);
	}

    /**
     * @see SummaryService#getSummariesByMappingType(org.openmrs.module.clinicalsummary.enumeration.MappingType)
     */
    @Override
    public List<Summary> getSummariesByMappingType(final MappingType mappingType) throws APIException {
        List<Mapping> mappings = getMappings(null, null, mappingType);
        List<Summary> summaries = new ArrayList<Summary>();
        for (Mapping mapping : mappings)
            summaries.add(mapping.getSummary());
        return summaries;
    }
}
