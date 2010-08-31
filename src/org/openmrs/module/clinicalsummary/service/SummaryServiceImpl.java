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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.datasource.LogicDataSource;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.SummaryError;
import org.openmrs.module.clinicalsummary.SummaryIndex;
import org.openmrs.module.clinicalsummary.SummaryService;
import org.openmrs.module.clinicalsummary.SummaryTemplate;
import org.openmrs.module.clinicalsummary.db.SummaryDAO;

public class SummaryServiceImpl implements SummaryService {
	
	public SummaryDAO summaryDAO;
	
	/**
	 * @param summaryDAO the summaryDAO to set
	 */
	public void setSummaryDAO(SummaryDAO summaryDAO) {
		this.summaryDAO = summaryDAO;
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#saveTemplate(org.openmrs.module.clinicalsummary.SummaryTemplate)
	 */
	public SummaryTemplate saveTemplate(SummaryTemplate summary) throws APIException {
		return summaryDAO.saveTemplate(summary);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#saveTemplate(org.openmrs.module.clinicalsummary.SummaryTemplate)
	 */
	public SummaryTemplate retireTemplate(SummaryTemplate summary) throws APIException {
		return summaryDAO.retireTemplate(summary);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getAllTemplates()
	 */
	public List<SummaryTemplate> getAllTemplates() throws APIException {
		return summaryDAO.getAllTemplates(false);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getTemplate(java.lang.Integer)
	 */
	public SummaryTemplate getTemplate(Integer id) throws APIException {
		return summaryDAO.getTemplate(id);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getPreferredTemplate()
	 */
	public SummaryTemplate getPreferredTemplate() throws APIException {
		List<SummaryTemplate> summaryTemplates = getAllTemplates();
		for (SummaryTemplate summaryTemplate : summaryTemplates) {
			if (summaryTemplate.getPreferred())
				return summaryTemplate;
		}
		
		if (!summaryTemplates.isEmpty())
			return summaryTemplates.get(0);
		
		return null;
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getCohortByLocation(org.openmrs.Location)
	 */
	public Cohort getCohortByLocation(Location location) {
		return summaryDAO.getPatientsByLocation(location, null, null);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getCohortByLocation(java.lang.Integer, java.util.Date)
	 */
	public Cohort getCohortByLocation(Location location, Date startDate, Date endDate) {
		return summaryDAO.getPatientsByLocation(location, startDate, endDate);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getErrorsByPatient(org.openmrs.Patient)
	 */
	public List<SummaryError> getAllErrors() throws APIException {
		return summaryDAO.getAllErrors();
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#saveError(org.openmrs.module.clinicalsummary.SummaryError)
	 */
	public SummaryError saveError(SummaryError error) throws APIException {
		return summaryDAO.saveError(error);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#deleteError(org.openmrs.module.clinicalsummary.SummaryError)
	 */
	public void deleteError(SummaryError summaryError) throws APIException {
		summaryDAO.deleteError(summaryError);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getAllIndexes()
	 */
	public List<SummaryIndex> getAllIndexes() throws APIException {
		return summaryDAO.getAllIndexes();
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getEarliestIndex(org.openmrs.Location)
	 */
	@Override
    public Date getEarliestIndex(Location location) throws APIException {
	    return summaryDAO.getEarliestIndex(location);
    }
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#updateIndexesInitialDate(org.openmrs.Location, Date)
	 */
	@Override
	public Integer updateIndexesInitialDate(Location location, Date initialDate) throws APIException {
		return summaryDAO.updateIndexesInitialDate(location, initialDate);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getIndexes(org.openmrs.Location, java.util.Date, java.util.Date)
	 */
	public List<SummaryIndex> getIndexes(Location location, SummaryTemplate template, Date startReturnDate, Date endReturnDate) throws APIException {
		return summaryDAO.getIndexes(location, template, startReturnDate, endReturnDate);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getIndexes(org.openmrs.Location, java.util.Date, java.util.Date)
	 */
	public SummaryIndex getIndex(Patient patient, SummaryTemplate template) throws APIException {
		return summaryDAO.getIndex(patient, template);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#updateIndexesInitialDate(java.lang.Integer, Date)
	 */
	public SummaryIndex getIndex(Integer indexId) throws APIException {
		return summaryDAO.getIndex(indexId);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#saveIndex(org.openmrs.module.clinicalsummary.SummaryIndex)
	 */
	public SummaryIndex saveIndex(SummaryIndex summaryIndex) throws APIException {
		return summaryDAO.saveSummaryIndex(summaryIndex);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getEncountersByType(org.openmrs.Cohort, org.openmrs.EncounterType)
	 */
	@Override
	public List<Encounter> getEncountersByType(Cohort cohort, EncounterType encounterType) {
		return summaryDAO.getEncounters(cohort, Arrays.asList(encounterType));
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getObservationsByEncounterType(org.openmrs.Cohort, org.openmrs.Concept, List)
	 */
	@Override
	public List<Obs> getObservationsByEncounterType(Cohort cohort, Concept concept, Collection<EncounterType> encounterTypes) {
		return summaryDAO.getObservations(cohort, concept, encounterTypes);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#evalCriteria(org.openmrs.Patient, org.openmrs.logic.LogicCriteria)
	 */
	public Result evalCriteria(Patient patient, LogicCriteria criteria) {
		return Context.getLogicService().eval(patient, criteria);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#evalCriteria(org.openmrs.Patient, java.lang.String, java.util.Map)
	 */
	public Result evalCriteria(Patient patient, String token, Map<String, Object> parameters) {
		return Context.getLogicService().eval(patient, token, parameters);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#evalCriteria(org.openmrs.Patient, java.lang.String, java.util.Map)
	 */
	public LogicCriteria parseToken(String token) {
		return Context.getLogicService().parse(token);
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getIndexes(java.lang.String, String, int, int, int)
	 */
	@Override
    public List<SummaryIndex> getIndexes(String search, String sortOrder, int sortColumn, int displayStart, int displayLength) {
	    return summaryDAO.findIndexes(search, sortOrder, sortColumn, displayStart, displayLength);
    }

	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getIndexes(java.lang.String, String, int, int, int)
	 */
	@Override
    public Integer countIndexes(String search) {
	    return summaryDAO.countIndexes(search);
    }
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getLogicDataSource(java.lang.String)
	 */
	@Override
	public LogicDataSource getLogicDataSource(String hint) {
		return Context.getLogicService().getLogicDataSource(hint);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#setLogicDataSource(java.lang.String, org.openmrs.logic.datasource.LogicDataSource)
	 */
	@Override
	public void setLogicDataSource(String hint, LogicDataSource dataSource) throws APIException {
		Context.getLogicService().registerLogicDataSource(hint, dataSource);
	}
}
