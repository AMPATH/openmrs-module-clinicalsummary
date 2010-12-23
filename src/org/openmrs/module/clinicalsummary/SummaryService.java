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
package org.openmrs.module.clinicalsummary;

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
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.datasource.LogicDataSource;
import org.openmrs.logic.result.Result;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * All services available from the summary module
 */
@Transactional
public interface SummaryService {
	
	/**
	 * Save a summary template object to the database.
	 * 
	 * @param summary template object that will saved to the database
	 * @return saved summary template
	 * @throws APIException
	 * @should save summary template object to the database
	 * @should update the summary template object to the database
	 */
	@Authorized( { SummaryConstants.PRIV_MANAGE_SUMMARY })
	public SummaryTemplate saveTemplate(SummaryTemplate summary) throws APIException;
	
	/**
	 * @param summary
	 * @throws APIException
	 */
	@Authorized( { SummaryConstants.PRIV_MANAGE_SUMMARY })
	public SummaryTemplate retireTemplate(SummaryTemplate summary) throws APIException;
	
	/**
	 * Get a summary templte object based on the id of the summary template
	 * 
	 * @param id id of the summary template object to be retrieved
	 * @return summary template object when a summary with specified id is in the database or null
	 *         when no summary template object found in the database
	 * @throws APIException
	 * @should return summary template object with the input id
	 * @should return null when no summary template found with the input id
	 */
	@Transactional(readOnly = true)
	@Authorized( { SummaryConstants.PRIV_VIEW_SUMMARY })
	public SummaryTemplate getTemplate(Integer id) throws APIException;
	
	/**
	 * Get all registered templates on the database.
	 * 
	 * @return all registered templates
	 * @throws APIException
	 * @should return all registered templates
	 * @should return empty list when no template are registered
	 */
	@Transactional(readOnly = true)
	@Authorized( { SummaryConstants.PRIV_VIEW_SUMMARY })
	public List<SummaryTemplate> getAllTemplates() throws APIException;
	
	/**
	 * Get the preferred template based on the preffered flag of the summary template. When no
	 * preffered template is specified, then return the first template in the database or null if no
	 * template are registered.
	 * 
	 * @return preferred template if specified or the first template if no preferred template are
	 *         found in the database or null when no template are registered
	 * @throws APIException
	 * @should return preferred summary template
	 * @should return first summary template when no template are preferred
	 * @should return null when no template are registered
	 */
	@Transactional(readOnly = true)
	@Authorized( { SummaryConstants.PRIV_VIEW_SUMMARY })
	public SummaryTemplate getPreferredTemplate() throws APIException;
	
	/**
	 * Get all encounters for a patient based on the encounter types.
	 * 
	 * @param patient patient that will be queried for their encounters
	 * @param encounterType encounter type that will be used to narrow down number of encounters
	 *            retrieved
	 * @return all encounter(s) for a patient. An empty list if the patient doesn't have any
	 *         encounter.
	 * @throws APIException
	 * @should return all patient encounter(s)
	 * @should return empty list if the patient doesn't have any encounter(s)
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTERS, OpenmrsConstants.PRIV_VIEW_ENCOUNTER_TYPES })
	public List<Encounter> getEncountersByType(Cohort cohort, EncounterType encounterType) throws APIException;
	
	/**
	 * Get observations on a certain concept based on the encounter type from which the observations
	 * comes from
	 * 
	 * @param cohort the patients
	 * @param concept the concept
	 * @param encounterTypes the encounter types
	 * @return all observations matching the criteria
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_OBS })
	public List<Obs> getObservationsByEncounterType(Cohort cohort, Concept concept, Collection<EncounterType> encounterTypes) throws APIException;
	
	/**
	 * Get all patient id coming attached to a certain location on their latest encounter.
	 * 
	 * @param location location of the patient latest encounter
	 * @return all patient with the latest encounter matching the parameter location. Return empty
	 *         cohort when no patient are found to have the input location on their latest
	 *         encounter.
	 * @should return cohort of all patient with the input location on their latest encounter
	 * @should return empty list when no patient have the input location on their latest encounter
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS, OpenmrsConstants.PRIV_VIEW_PATIENTS })
	public Cohort getCohortByLocation(Location location) throws APIException;
	
	/**
	 * Get all patient id attached to a certain location on their observations where the
	 * observations are created between the start date and end date
	 * 
	 * @param locationId location id of the patient observation
	 * @param startDate min date of the observation created date
	 * @param endDate max date of the observation created date
	 * @return all patient id where the location of the patient's observation between start date and
	 *         end date is location. Return empty cohort when no patient id match the criteria.
	 * @should return all patient id with certain location on their observations between certain
	 *         date
	 * @should return empty cohort when no patient match the criteria
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS, OpenmrsConstants.PRIV_VIEW_PATIENTS })
	public Cohort getCohortByLocation(Location location, Date startDate, Date endDate) throws APIException;
	
	/**
	 * Save all errors during the summary generation process to the database. Error are generated
	 * per patient. This error can be used to review any problem encountered during the generation
	 * process and to re-generate summary for that particular patient.
	 * 
	 * @param summaryError error object that will be saved to the database
	 * @return saved error object
	 * @throws APIException
	 * @should save the error to the database
	 * @should update the error in the database
	 */
	@Authorized( { SummaryConstants.PRIV_MANAGE_SUMMARY })
	public SummaryError saveError(SummaryError summaryError) throws APIException;
	
	/**
	 * Get recorded errors for a certain patient.
	 * 
	 * @param patient patient from which the error are generated
	 * @return list of errors for a certain patient. Return empty list when no error comes from the
	 *         patient
	 * @throws APIException
	 * @should return list of all errors for a certain patient
	 * @should return empty list when no errors are found for a patient
	 */
	@Transactional(readOnly = true)
	@Authorized( { SummaryConstants.PRIV_MANAGE_SUMMARY })
	public Cohort getErrorCohort() throws APIException;
	
	/**
	 * Delete a certain error from the database.
	 * 
	 * @param summaryError error object that will be deleted
	 * @throws APIException
	 * @should delete certain error object
	 */
	@Authorized( { SummaryConstants.PRIV_MANAGE_SUMMARY })
	public void deleteError(SummaryError summaryError) throws APIException;
	
	/**
	 * Delete a certain error from the database.
	 * 
	 * @param summaryError error object that will be deleted
	 * @throws APIException
	 * @should delete certain error object
	 */
	@Authorized( { SummaryConstants.PRIV_MANAGE_SUMMARY })
	public void deleteError(Patient patient) throws APIException;

	/**
	 * Save the index for a generated summary to the database
	 * 
	 * @param summaryIndex object to be saved to the database
	 * @return saved SummaryIndex object
	 * @throws APIException
	 * @should save SummaryIndex
	 * @should update SummaryIndex
	 */
	public SummaryIndex saveIndex(SummaryIndex summaryIndex) throws APIException;
	
	/**
	 * Get all indexes for a certain location with return date between the <code>startDate</code>
	 * and <code>endDate</code>
	 * 
	 * @param location location of the index to be retrieved
	 * @param template template used to generate for this index
	 * @param startReturnDate min return date of index to be retrieved
	 * @param endReturnDate max return date of index to be retrieved
	 * @return all indexes for a certain location with return date between the data parameter.
	 *         Return an empty list if no index for the location or no index falls between the start
	 *         and end date.
	 * @throws APIException
	 * @should return all indexes for a certain location id and date
	 * @should return empty list when no indexes found for location and date input
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS, OpenmrsConstants.PRIV_VIEW_PATIENTS, OpenmrsConstants.PRIV_VIEW_OBS })
	public List<SummaryIndex> getIndexes(Location location, SummaryTemplate template, Date startReturnDate,
	                                     Date endReturnDate) throws APIException;
	
	/**
	 * Get index entry based on the template and the patient
	 * 
	 * @param patient the patient
	 * @param template the template
	 * @return index with matching template for a certain patient. This will be guaranteed a unique
	 *         index
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS, OpenmrsConstants.PRIV_VIEW_PATIENTS, OpenmrsConstants.PRIV_VIEW_OBS })
	public SummaryIndex getIndex(Patient patient, SummaryTemplate template) throws APIException;
	
	/**
	 * Get index entry based on the template and the patient
	 * 
	 * @param patient the patient
	 * @param template the template
	 * @return index with matching template for a certain patient. This will be guaranteed a unique
	 *         index
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS, OpenmrsConstants.PRIV_VIEW_PATIENTS, OpenmrsConstants.PRIV_VIEW_OBS })
	public List<SummaryIndex> getIndexes(List<Patient> patients) throws APIException;
	
	/**
	 * Get index entry based on the index primary key
	 * 
	 * @param indexId the primary key of the index
	 * @return the index with the matching primary key
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS, OpenmrsConstants.PRIV_VIEW_PATIENTS, OpenmrsConstants.PRIV_VIEW_OBS })
	public SummaryIndex getIndex(Integer indexId) throws APIException;
	
	/**
	 * Get all indexes from database
	 * 
	 * @return all indexes from the database. Return empty list if no indexes found in the database.
	 * @throws APIException
	 * @should return all indexes in the database
	 * @should return empty list if no indexes found in the database
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS, OpenmrsConstants.PRIV_VIEW_PATIENTS, OpenmrsConstants.PRIV_VIEW_OBS })
	public List<SummaryIndex> getAllIndexes() throws APIException;
	
	/**
	 * Get all indexes that match the location criteria
	 * 
	 * @param location location from which the index should come from
	 * @param initialDate TODO
	 * @return all indexes for the location
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS, OpenmrsConstants.PRIV_VIEW_PATIENTS, OpenmrsConstants.PRIV_VIEW_OBS })
	public Integer updateIndexesInitialDate(Location location, Date initialDate) throws APIException;
	
	/**
	 * Get the earliest date when a location receive a summary record
	 * 
	 * @param location location from which the index should come from
	 * @return the earliest date for a location when they receive index
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS, OpenmrsConstants.PRIV_VIEW_PATIENTS, OpenmrsConstants.PRIV_VIEW_OBS })
	public Date getEarliestIndex(Location location) throws APIException;
	
	/**
	 * Get the latest observations for a certain patient. This latest observations will be used as a
	 * reference on what date a summary was generated. All pdfs are generated when there's new
	 * observation coming to the database. This date will become handy when we need to regenerate
	 * pdfs for all patients in the system.
	 * 
	 * @param patient the patient
	 * @return the latest observation.
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS, OpenmrsConstants.PRIV_VIEW_PATIENTS, OpenmrsConstants.PRIV_VIEW_OBS })
	public Obs getLatestObservation(Patient patient) throws APIException;
	
	/**
	 * Implementation of the logic service eval with transactional read only annotation. Currently
	 * the default annotation of the logic service is transactional read-write. This will makes the
	 * generation process take a while to finish. With a read only annotation, this will speedup the
	 * generation process. See <a href="http://dev.openmrs.org/ticket/2230">ticket 3320</a> for more
	 * detail.
	 * 
	 * @param patient the patien on which this rule will be evaluated
	 * @param criteria logic expression that will be evaluated on this patient
	 * @return result of the rule evaluation
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTERS, OpenmrsConstants.PRIV_VIEW_PATIENTS,
	        OpenmrsConstants.PRIV_VIEW_OBS })
	public Result evalCriteria(Patient patient, LogicCriteria criteria) throws APIException;
	
	/**
	 * Implementation of the logic service eval with transactional read only annotation. Currently
	 * the default annotation of the logic service is transactional read-write. This will makes the
	 * generation process take a while to finish. With a read only annotation, this will speedup the
	 * generation process. See <a href="http://dev.openmrs.org/ticket/2230">ticket 3320</a> for more
	 * detail.
	 * 
	 * @param patient patient the patien on which this rule will be evaluated
	 * @param token logic token of the rule that will be evaluated
	 * @param parameters parameters of the rule execution
	 * @return result of the rule evaluation
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_ENCOUNTERS, OpenmrsConstants.PRIV_VIEW_PATIENTS,
	        OpenmrsConstants.PRIV_VIEW_OBS })
	public Result evalCriteria(Patient patient, String token, Map<String, Object> parameters) throws APIException;
	
	/**
	 * Implementation of the logic service parse with transactional read only annotation. Currently
	 * the default annotation of the logic service is transactional read-write. This will makes the
	 * generation process take a while to finish. With a read only annotation, this will speedup the
	 * generation process. See <a href="http://dev.openmrs.org/ticket/2230">ticket 3320</a> for more
	 * detail.
	 * 
	 * @param token logic token of the rule that will be parsed
	 * @return result of the rule evaluation
	 */
	@Transactional(readOnly = true)
	public LogicCriteria parseToken(String token) throws APIException;
	
	/**
	 * Implementation of the logic service with transactional read only annotation. Currently the
	 * default annotation of the logic service is transactional read-write. This will makes the
	 * generation process take a while to finish. With a read only annotation, this will speedup the
	 * generation process. See <a href="http://dev.openmrs.org/ticket/2230">ticket 3320</a> for more
	 * detail.
	 * 
	 * @param hint the hint to the data source
	 * @return the data source registered with the hint
	 * @see org.openmrs.logic.LogicService#getLogicDataSource(String)
	 */
	@Transactional(readOnly = true)
	public LogicDataSource getLogicDataSource(String hint) throws APIException;
	
	/**
	 * Implementation of the logic service with transactional read only annotation. Currently the
	 * default annotation of the logic service is transactional read-write. This will makes the
	 * generation process take a while to finish. With a read only annotation, this will speedup the
	 * generation process. See <a href="http://dev.openmrs.org/ticket/2230">ticket 3320</a> for more
	 * detail.
	 * 
	 * @param hint the hint to the data source
	 * @param dataSource the data source registered with the hint
	 * @see org.openmrs.logic.LogicService#registerLogicDataSource(String, LogicDataSource)
	 */
	@Transactional(readOnly = true)
	public void setLogicDataSource(String hint, LogicDataSource dataSource) throws APIException;
	
	/**
	 * Search index for a certain criteria. This is used by jQuery plugin
	 * 
	 * @param search string entered in the plugin search bar
	 * @param sortOrder ordering selected on the search column
	 * @param sortColumn column selected to be ordered on the
	 * @param displayStart first element to be returned from the database
	 * @param displayLength total number of element to be displayed in the tables
	 * @return all index that need to be displayed or empty list when there's no record founded
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS, OpenmrsConstants.PRIV_VIEW_PATIENTS, OpenmrsConstants.PRIV_VIEW_OBS })
	public List<SummaryIndex> getIndexes(String search, Integer displayStart, Integer displayLength) throws APIException;
	
	/**
	 * Count index for a certain criteria. This is used by jQuery plugin
	 * 
	 * @param search string entered in the plugin search bar
	 * @return total number of element that match the search string. Empty string will return all
	 *         records
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_LOCATIONS, OpenmrsConstants.PRIV_VIEW_PATIENTS, OpenmrsConstants.PRIV_VIEW_OBS })
	public Integer countIndexes(String search) throws APIException;
	
	/**
	 * @param age
	 * @param ageUnit
	 * @param gender
	 * @return
	 */
	@Transactional(readOnly = true)
	public WeightAgeStandard getWeightAgeStandard(Integer age, String ageUnit, String gender);
	
	/**
	 * @param obsPair
	 * @return
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENTS, OpenmrsConstants.PRIV_VIEW_OBS })
	public ObsPair saveObsPair(ObsPair obsPair);

	/**
	 * @param obsPair
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENTS, OpenmrsConstants.PRIV_VIEW_OBS })
	public void deleteObsPair(ObsPair obsPair);

	/**
	 * @param patient
	 * @return
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENTS, OpenmrsConstants.PRIV_VIEW_OBS })
	public List<ObsPair> getObsPairForPatient(Patient patient);

	/**
	 * @return
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENTS, OpenmrsConstants.PRIV_VIEW_OBS })
	public List<ObsPair> getAllObsPairs();
	
	/**
	 * @param search
	 * @param displayStart
	 * @param displayLength
	 * @return
	 * @throws APIException
	 */
	@SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENTS, OpenmrsConstants.PRIV_VIEW_OBS })
	public List getObsPairs(String search, int displayStart, int displayLength) throws APIException;
	
	/**
	 * @param search
	 * @return
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENTS, OpenmrsConstants.PRIV_VIEW_OBS })
	public Integer countObsPairs(String search) throws APIException;
}
