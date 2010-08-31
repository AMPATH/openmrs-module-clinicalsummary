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

import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.clinicalsummary.SummaryError;
import org.openmrs.module.clinicalsummary.SummaryIndex;
import org.openmrs.module.clinicalsummary.SummaryTemplate;

public interface SummaryDAO {
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#saveTemplate(SummaryTemplate)
	 */
	public SummaryTemplate saveTemplate(SummaryTemplate summary) throws DAOException;
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#retireTemplate(SummaryTemplate)
	 */
	public SummaryTemplate retireTemplate(SummaryTemplate summary) throws DAOException;
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getTemplate(Integer)
	 */
	public SummaryTemplate getTemplate(Integer templateId) throws DAOException;
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getAllTemplates()
	 */
	public List<SummaryTemplate> getAllTemplates(boolean includeRetired) throws DAOException;
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getCohortByLocation(Location, Date, Date)
	 */
	public Cohort getPatientsByLocation(Location location, Date startDate, Date endDate);
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#saveError(SummaryError)
	 */
	public SummaryError saveError(SummaryError summaryError) throws DAOException;
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getAllErrors()
	 */
	public List<SummaryError> getAllErrors();
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#deleteError(SummaryError)
	 */
	public void deleteError(SummaryError summaryError);
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#saveIndex(SummaryIndex)
	 */
	public SummaryIndex saveSummaryIndex(SummaryIndex summaryError) throws DAOException;
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getAllIndexes()
	 */
	public List<SummaryIndex> getAllIndexes();

	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getEarliestIndex(Location)
	 */
	public Date getEarliestIndex(Location location);
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getLatestObservation(Patient)
	 */
	public Obs getLatestObservation(Patient patient);
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#updateIndexesInitialDate(Location,
	 *      Date)
	 */
	public Integer updateIndexesInitialDate(Location location, Date initialDate);
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getIndex(Patient, SummaryTemplate)
	 */
	public SummaryIndex getIndex(Patient patient, SummaryTemplate template);

	/**
     * @see org.openmrs.module.clinicalsummary.SummaryService#getIndex(java.lang.Integer)
     */
    public SummaryIndex getIndex(Integer indexId);
	
	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getIndexes(Location, SummaryTemplate, Date, Date)
	 */
	public List<SummaryIndex> getIndexes(Location location, SummaryTemplate template, Date startReturnDate, Date endReturnDate) throws DAOException;

	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getObservationsByEncounterType(Cohort, Concept, List)
	 */
	public List<Obs> getObservations(Cohort cohort, Concept concept, Collection<EncounterType> encounterTypes);

	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getEncounters(Cohort, List)
	 */
	public List<Encounter> getEncounters(Cohort cohort, Collection<EncounterType> encounterTypes) throws DAOException;

	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#getIndexes(String, String, int, int, int)
	 */
	public List<SummaryIndex> findIndexes(String search, String sortOrder, int sortColumn, int displayStart, int displayLength);

	/**
	 * @see org.openmrs.module.clinicalsummary.SummaryService#countIndexes(String)
	 */
	public Integer countIndexes(String search);
}
