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
package org.openmrs.module.clinicalsummary.db.hibernate;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.clinicalsummary.ObsPair;
import org.openmrs.module.clinicalsummary.SummaryError;
import org.openmrs.module.clinicalsummary.SummaryIndex;
import org.openmrs.module.clinicalsummary.SummaryTemplate;
import org.openmrs.module.clinicalsummary.WeightAgeStandard;
import org.openmrs.module.clinicalsummary.db.SummaryDAO;

/**
 * Hibernate operation from the summary module
 */
public class HibernateSummaryDAO implements SummaryDAO {
	
	private SessionFactory sessionFactory;
	
	/**
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#saveTemplate(org.openmrs.module.clinicalsummary.SummaryTemplate)
	 */
	@Override
	public SummaryTemplate saveTemplate(SummaryTemplate summary) throws DAOException {
		if (summary.isPreferred()) {
			String stringQuery = "UPDATE SummaryTemplate s SET s.preferred = :preferred";
			sessionFactory.getCurrentSession().createQuery(stringQuery).setBoolean("preferred", false).executeUpdate();
		}
		sessionFactory.getCurrentSession().saveOrUpdate(summary);
		return summary;
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#saveTemplate(org.openmrs.module.clinicalsummary.SummaryTemplate)
	 */
	@Override
	public SummaryTemplate retireTemplate(SummaryTemplate summary) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(summary);
		return summary;
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getTemplate(java.lang.Integer)
	 */
	@Override
	public SummaryTemplate getTemplate(Integer id) throws DAOException {
		return (SummaryTemplate) sessionFactory.getCurrentSession().get(SummaryTemplate.class, id);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getClinicalSummaries()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<SummaryTemplate> getAllTemplates(boolean includeRetired) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(SummaryTemplate.class);
		criteria.add(Restrictions.eq("retired", includeRetired));
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getPatientsEncountersByTypes(org.openmrs.Cohort, java.util.List)
	 */
	public Encounter getLatestEncounter(Patient patient) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Encounter.class);
		
		criteria.createAlias("patient", "patient");
		criteria.add(Restrictions.eq("patient.patientId", patient.getPatientId()));
		
		criteria.add(Restrictions.eq("voided", false));
		
		return (Encounter) criteria.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getPatientsEncountersByTypes(org.openmrs.Cohort, java.util.List)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Encounter> getEncounters(Cohort cohort, Collection<EncounterType> encounterTypes) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Encounter.class);
		
		criteria.createAlias("patient", "patient");
		criteria.add(Restrictions.in("patient.patientId", cohort.getMemberIds()));
		
		criteria.add(Restrictions.eq("voided", false));
		criteria.add(Restrictions.in("encounterType", encounterTypes));
		
		criteria.addOrder(Order.desc("patient.patientId"));
		criteria.addOrder(Order.desc("encounterId"));
		criteria.addOrder(Order.desc("encounterDatetime"));
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getObservationsByEncounterType(org.openmrs.Cohort, org.openmrs.Concept, List)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Obs> getObservations(Cohort cohort, Concept concept, Collection<EncounterType> encounterTypes) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
		criteria.createAlias("person", "person");
		
		
		if (!encounterTypes.isEmpty()) {
			criteria.createAlias("encounter", "encounter");
			criteria.add(Restrictions.in("encounter.encounterType", encounterTypes));
		}
		
		criteria.add(Restrictions.in("person.personId", cohort.getMemberIds()));
		criteria.add(Restrictions.eq("concept", concept));
		criteria.add(Restrictions.eq("voided", false));
		
		criteria.addOrder(Order.desc("person.personId"));
		criteria.addOrder(Order.desc("obsDatetime"));
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getPatientsByLocation(org.openmrs.Location, java.util.Date, java.util.Date)
	 */
	@Override
	public Cohort getPatientsByLocation(Location location, Date startDate, Date endDate) throws DAOException {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
		criteria.createAlias("encounter", "encounter");
		criteria.createAlias("person", "person");
		
		if (location != null)
			criteria.add(Restrictions.eq("encounter.location", location));
		else
			criteria.add(Restrictions.isNull("encounter.location"));
		
		if (startDate != null)
			criteria.add(Restrictions.ge("dateCreated", startDate));
		
		if (endDate != null)
			criteria.add(Restrictions.le("dateCreated", endDate));
		
		criteria.add(Restrictions.eq("voided", false));
		criteria.addOrder(Order.desc("person.personId"));
		
		criteria.setProjection(Projections.property("person.personId"));
		return new Cohort(criteria.list());
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#saveError(org.openmrs.module.clinicalsummary.SummaryError)
	 */
	@Override
	public SummaryError saveError(SummaryError summaryError) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(summaryError);
		return summaryError;
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getAllErrors()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<SummaryError> getAllErrors() throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(SummaryError.class);
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#deleteError(org.openmrs.module.clinicalsummary.SummaryError)
	 */
	@Override
	public void deleteError(SummaryError summaryError) throws DAOException {
		sessionFactory.getCurrentSession().delete(summaryError);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#saveSummaryIndex(org.openmrs.module.clinicalsummary.SummaryIndex)
	 */
	@Override
	public SummaryIndex saveSummaryIndex(SummaryIndex summaryIndex) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(summaryIndex);
		return summaryIndex;
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getAllIndexes()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<SummaryIndex> getAllIndexes() throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(SummaryIndex.class);
		return criteria.list();
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getEarliestIndex(org.openmrs.Location)
	 */
	@Override
    public Date getEarliestIndex(Location location) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(SummaryIndex.class);
		
		if (location != null)
			criteria.add(Restrictions.eq("location", location));
		else
			criteria.add(Restrictions.isNull("location"));
		
		criteria.setProjection(Projections.min("initialDate"));
		return (Date) criteria.uniqueResult();
    }

	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getEarliestIndex(org.openmrs.Location)
	 */
	@Override
    public Obs getLatestObservation(Patient patient) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
		criteria.createAlias("person", "person");
		
		criteria.add(Restrictions.eq("person.personId", patient.getPatientId()));
		criteria.add(Restrictions.eq("voided", false));

		criteria.addOrder(Order.desc("dateCreated"));
		
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		return (Obs) criteria.uniqueResult();
    }

	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#updateIndexesInitialDate(org.openmrs.Location, java.util.Date)
	 */
	@Override
	public Integer updateIndexesInitialDate(Location location, Date initialDate) throws DAOException {
		String hqlString = "UPDATE SummaryIndex i SET i.initialDate = :initialDate WHERE i.location = :location";
		int totalUpdated = sessionFactory.getCurrentSession().createQuery(hqlString).setDate("initialDate", initialDate)
		        .setParameter("location", location).executeUpdate();
		return totalUpdated;
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getIndexByPatient(org.openmrs.Patient)
	 */
	@Override
	public SummaryIndex getIndex(Patient patient, SummaryTemplate template) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(SummaryIndex.class);
		criteria.add(Restrictions.eq("patient", patient));
		criteria.add(Restrictions.eq("template", template));
		return (SummaryIndex) criteria.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getIndexes(org.openmrs.Patient)
	 */
	@Override
	@SuppressWarnings("unchecked")
    public List<SummaryIndex> getIndexes(List<Patient> patients) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(SummaryIndex.class);
		criteria.add(Restrictions.in("patient", patients));
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getIndexByPatient(org.openmrs.Patient)
	 */
	@Override
	public SummaryIndex getIndex(Integer indexId) throws DAOException {
		return (SummaryIndex) sessionFactory.getCurrentSession().get(SummaryIndex.class, indexId);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getIndexes(org.openmrs.Location,
	 *      org.openmrs.module.clinicalsummary.SummaryTemplate, java.util.Date, java.util.Date,
	 *      java.util.Date, java.util.Date)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<SummaryIndex> getIndexes(Location location, SummaryTemplate template, Date startReturnDate, Date endReturnDate) throws DAOException {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(SummaryIndex.class);
		criteria.createAlias("patient", "patient");
	
		
		if (location != null)
			criteria.add(Restrictions.eq("location", location));
		else
			criteria.add(Restrictions.isNull("location"));
		
		if (template != null)
			criteria.add(Restrictions.eq("template", template));
		
		if (startReturnDate != null)
			criteria.add(Restrictions.ge("returnDate", startReturnDate));
		
		if (endReturnDate != null)
			criteria.add(Restrictions.le("returnDate", endReturnDate));
		
		Date earliestDate = getEarliestIndex(location);
		criteria.add(Restrictions.ge("generatedDate", earliestDate));
		
		criteria.addOrder(Order.desc("returnDate"));
		
		return criteria.list();
	}
	
	/*
	 * Two methods for the Datatables plugin 
	 */

	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#findIndexes(java.lang.String, java.lang.String[], int, int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<SummaryIndex> findIndexes(String search, Integer displayStart, Integer displayLength) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(SummaryIndex.class);
		preparePatientCriteria(criteria, search);
		
		if (displayStart != null)
			criteria.setFirstResult(displayStart);
		
		if (displayLength != null)
			criteria.setMaxResults(displayLength);
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#countIndexes(java.lang.String, java.lang.String[])
	 */
	@Override
	public Integer countIndexes(String search) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(SummaryIndex.class);
		preparePatientCriteria(criteria, search);
		criteria.setProjection(Projections.rowCount());
	    return (Integer) criteria.uniqueResult();
    }
	
	/**
	 * Set the search criteria based on the search String. Searching will be performed on all String
	 * property with OR connecting one search element with the other.
	 */
	private void preparePatientCriteria(Criteria criteria, String search) {
		// skip search when there's less than 3 characters in the search criteria
		if (!StringUtils.isBlank(search)) {
			if (search.matches(".*\\d+.*")) {
				criteria.createAlias("patient", "patient");
				criteria.createAlias("patient.identifiers", "identifier");
				criteria.add(Restrictions.eq("identifier.voided", false));
				criteria.add(Restrictions.ilike("identifier.identifier", search, MatchMode.START));
				criteria.addOrder(Order.desc("identifier.identifier"));
			} else {
				criteria.createAlias("patient", "patient");
				criteria.createAlias("patient.names", "name");
				criteria.add(Restrictions.eq("name.voided", false));
				
				LogicalExpression expression = null;
				String[] searchTerms = StringUtils.split(search);
				for (String string : searchTerms)
					if (expression == null)
						expression = prepareIndexExpression(string);
					else
						expression = Expression.or(expression, prepareIndexExpression(string));
				criteria.add(expression);
				criteria.addOrder(Order.desc("name.familyName"));
				criteria.addOrder(Order.desc("name.givenName"));
			}
		}
	}
	
	/**
	 * Create a Hibernate expression for all String properties
	 * 
	 * @param searchTerm the search term
	 * @return Hibernate criteria based on the search term on all String properties
	 */
	private LogicalExpression prepareIndexExpression(String searchTerm) {
		
		SimpleExpression givenExpression = Expression.like("name.givenName", searchTerm, MatchMode.START);
		SimpleExpression middleExpression = Expression.like("name.middleName", searchTerm, MatchMode.START);
		SimpleExpression familyExpression = Expression.like("name.familyName", searchTerm, MatchMode.START);
		
		return Expression.or(givenExpression, Expression.or(middleExpression, familyExpression));
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getWeightAgeStandard(java.lang.Integer,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public WeightAgeStandard getWeightAgeStandard(Integer age, String ageUnit, String gender) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(WeightAgeStandard.class);
		criteria.add(Restrictions.eq("age", age));
		criteria.add(Restrictions.eq("ageUnit", ageUnit));
		criteria.add(Restrictions.eq("gender", gender));
		return (WeightAgeStandard) criteria.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#deleteObsPair(org.openmrs.module.clinicalsummary.ObsPair)
	 */
	@Override
	public void deleteObsPair(ObsPair obsPair) throws DAOException {
		sessionFactory.getCurrentSession().delete(obsPair);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getObsPairForPatient(org.openmrs.Patient)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<ObsPair> getObsPairForPatient(Patient patient) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ObsPair.class);
		criteria.add(Restrictions.eq("patient", patient));
		criteria.addOrder(Order.desc("obsDatetime"));
		return criteria.list();
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getAllObsPairs()
	 */
	@SuppressWarnings("unchecked")
    @Override
    public List<ObsPair> getAllObsPairs() throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ObsPair.class);
		criteria.addOrder(Order.desc("patient"));
		return criteria.list();
    }
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#saveObsPair(org.openmrs.module.clinicalsummary.ObsPair)
	 */
	@Override
	public ObsPair saveObsPair(ObsPair obsPair) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(obsPair);
		return obsPair;
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#getObsPairs()
	 */
	@SuppressWarnings("unchecked")
    @Override
	public List getObsPairs(String search, Integer start, Integer length) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ObsPair.class);
		preparePatientCriteria(criteria, search);
		
		if (start != null)
			criteria.setFirstResult(start);
		
		if (length != null)
			criteria.setMaxResults(length);
		
		criteria.setProjection(Projections.projectionList()
			.add(Projections.rowCount())
			.add(Projections.groupProperty("patient")));
		
		criteria.addOrder(Order.desc("patient"));
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.SummaryDAO#countObsPair(java.lang.String)
	 */
	@Override
	public Integer countObsPair(String search) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ObsPair.class);
		preparePatientCriteria(criteria, search);
		criteria.setProjection(Projections.countDistinct("patient"));
		return (Integer) criteria.uniqueResult();
	}
}
