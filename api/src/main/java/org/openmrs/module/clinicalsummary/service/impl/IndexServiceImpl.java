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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.Index;
import org.openmrs.module.clinicalsummary.Mapping;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.db.IndexDAO;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.evaluator.IndexGeneratorRule;
import org.openmrs.module.clinicalsummary.service.EvaluatorService;
import org.openmrs.module.clinicalsummary.service.IndexService;
import org.openmrs.module.clinicalsummary.service.SummaryService;

/**
 */
public class IndexServiceImpl extends BaseOpenmrsService implements IndexService {

	private static final Log log = LogFactory.getLog(IndexServiceImpl.class);

	public static final String RETURN_VISIT_DATE = "RETURN VISIT DATE";

	private IndexDAO indexDAO;

	/**
	 * Setter for the DAO interface reference that will be called by Spring to inject the actual implementation of the DAO layer
	 *
	 * @param indexDAO the indexDAO to be injected
	 */
	public void setIndexDAO(final IndexDAO indexDAO) {
		if (log.isDebugEnabled())
			log.debug("Wiring up IndexDAO with IndexService ...");

		this.indexDAO = indexDAO;
	}

	/**
	 * @see IndexService#saveIndex(org.openmrs.module.clinicalsummary.Index)
	 */
	@Override
	public Index saveIndex(final Index index) throws APIException {
		return indexDAO.saveIndex(index);
	}

	/**
	 * @see IndexService#generateIndex(org.openmrs.Patient, org.openmrs.module.clinicalsummary.Summary)
	 */
	@Override
	public Index generateIndex(final Patient patient, final Summary summary) throws APIException {

		Index index = getIndex(patient, summary);
		if (index == null)
			index = new Index(patient, summary, new Date());

		List<Mapping> mappings = Context.getService(SummaryService.class).getMappings(summary, null, null);
		List<String> typeNames = new ArrayList<String>();
		for (Mapping mapping : mappings) {
			EncounterType encounterType = mapping.getEncounterType();
			typeNames.add(encounterType.getName());
		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(EvaluableConstants.ENCOUNTER_TYPE, typeNames);

		EvaluatorService evaluatorService = Context.getService(EvaluatorService.class);
		Result encounterResults = evaluatorService.evaluate(patient, IndexGeneratorRule.TOKEN, parameters);
		if (CollectionUtils.isNotEmpty(encounterResults)) {
			Result encounterResult = encounterResults.latest();
			index.setLocation((Location) encounterResult.getResultObject());
			index.setGeneratedDate(encounterResult.getResultDate());
			index.setReturnDate(encounterResult.toDatetime());
		}

		return index;
	}

	/**
	 * @see IndexService#getIndex(Integer)
	 */
	@Override
	public Index getIndex(final Integer id) throws APIException {
		return indexDAO.getIndex(id);
	}

	/**
	 * @see IndexService#getIndex(Integer)
	 */
	@Override
	public Index getIndex(final Patient patient, final Summary summary) throws APIException {
		return indexDAO.getIndex(patient, summary);
	}

	/**
	 * @see IndexService#getIndexes(org.openmrs.Patient)
	 */
	@Override
	public List<Index> getIndexes(final Patient patient) throws APIException {
		return indexDAO.getIndexes(patient);
	}

	/**
	 * @see IndexService#getIndexes(org.openmrs.Location, org.openmrs.module.clinicalsummary.Summary, java.util.Date, java.util.Date)
	 */
	@Override
	public List<Index> getIndexes(final Location location, final Summary summary, final Date startVisitDate, final Date endVisitDate) throws APIException {
		return indexDAO.getIndexes(location, summary, startVisitDate, endVisitDate);
	}

	/**
	 * @see IndexService#getIndexes(org.openmrs.Location, org.openmrs.module.clinicalsummary.Summary, java.util.Date, java.util.Date)
	 */
	@Override
	public Cohort getIndexCohort(final Location location, final Summary summary, final Date startVisitDate, final Date endVisitDate) throws APIException {
		return indexDAO.getIndexCohort(location, summary, startVisitDate, endVisitDate);
	}

	/**
	 * @see IndexService#saveInitialDate(org.openmrs.Location, java.util.Date)
	 */
	@Override
	public Integer saveInitialDate(final Location location, final Date date) throws APIException {
		return indexDAO.saveInitialDate(location, date);
	}

	/**
	 * @see IndexService#getInitialDate(org.openmrs.Location)
	 */
	@Override
	public Date getInitialDate(final Location location) throws APIException {
		return indexDAO.getInitialDate(location);
	}
}
