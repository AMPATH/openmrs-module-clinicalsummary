package org.openmrs.module.clinicalsummary.db.hibernate;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.api.db.hibernate.HibernatePatientSetDAO;
import org.openmrs.module.clinicalsummary.ClinicalSummary;
import org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem;
import org.openmrs.module.clinicalsummary.ClinicalSummaryUtil;
import org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS;
import org.openmrs.module.clinicalsummary.db.ClinicalSummaryDAO;

public class HibernateClinicalSummaryDAO implements ClinicalSummaryDAO {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) { 
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.ClinicalSummaryDAO#createClinicalSummary(org.openmrs.module.clinicalsummary.ClinicalSummary)
	 */
	public void createClinicalSummary(ClinicalSummary summary) {
		sessionFactory.getCurrentSession().save(summary);
		if (summary.getPreferred())
			setPreferred(summary);
	}
	
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.ClinicalSummaryDAO#getClinicalSummary(java.lang.Integer)
	 */
	public ClinicalSummary getClinicalSummary(Integer id) {
		return (ClinicalSummary) sessionFactory.getCurrentSession().get(ClinicalSummary.class, id);
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.db.ClinicalSummaryDAO#getClinicalSummaries()
	 */
	@SuppressWarnings("unchecked")
	public List<ClinicalSummary> getClinicalSummaries() {
		return sessionFactory.getCurrentSession().createQuery(
			"from ClinicalSummary order by clinicalSummaryId").list();
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.db.ClinicalSummaryDAO#updateClinicalSummary(org.openmrs.module.clinicalsummary.ClinicalSummary)
	 */
	public void updateClinicalSummary(ClinicalSummary summary) {
		sessionFactory.getCurrentSession().merge(summary);
		if (summary.getPreferred())
			setPreferred(summary);
	}
	
	/**
	 * Clear the preferred flag for all other summaries (assumes that the
	 * preferred flag for the given summary has already been set)
	 * @param summary the preferred summary
	 */
	@SuppressWarnings("unchecked")
	private void setPreferred(ClinicalSummary summary) {
		List<ClinicalSummary> preferredSummaries = sessionFactory.getCurrentSession().createQuery(
				"from ClinicalSummary c where c.preferred = 1 and c.clinicalSummaryId <> :cid")
				.setParameter("cid", summary.getClinicalSummaryId())
				.list();
		if (preferredSummaries != null && preferredSummaries.size() > 0)
			for (ClinicalSummary c : preferredSummaries)
				c.setPreferred(false);
	}
	
	/**
	 * Returns a map from patient id to list of obs
	 * 
	 * @param encounters
	 * @param c
	 * @param attributes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<Integer, List<Object>> getObservationsForEncounters(Collection<Encounter> encounters, Concept c) {
		Map<Integer, List<Object>> ret = new HashMap<Integer, List<Object>>();
		
		if (encounters.size() == 0)
			return ret;
		
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria("org.openmrs.Obs", "obs");
		List<String> columns = new Vector<String>();
		
		Boolean conditional = false; 
		columns = HibernatePatientSetDAO.findObsValueColumnName(c);
		if (columns.size() > 1)
			conditional = true;			
		
		String aliasName = "obs";
		
		// set up the query
		ProjectionList projections = Projections.projectionList();
		projections.add(Projections.property("obs.personId"));
		for (String col : columns) {
			if (col.contains("."))
				projections.add(Projections.property(col));
			else
				projections.add(Projections.property(aliasName + "." + col));
		}
		criteria.setProjection(projections);
		
		
		// restrict to only the encounters passed in
		criteria.add(Restrictions.in("obs.encounter", encounters));
		
		
		criteria.add(Expression.eq("obs.concept", c));
		criteria.add(Expression.eq("obs.voided", false));
		
		criteria.addOrder(org.hibernate.criterion.Order.desc("obs.obsDatetime"));
		criteria.addOrder(org.hibernate.criterion.Order.desc("obs.voided"));
		
		log.debug("criteria: " + criteria);
		
		List<Object[]> rows = criteria.list();
		
		// set up the return map
		for (Object[] rowArray : rows) {
			//log.debug("row[0]: " + row[0] + " row[1]: " + row[1] + (row.length > 2 ? " row[2]: " + row[2] : ""));
			Integer ptId = (Integer)rowArray[0];
			
			Object value = rowArray[1];
			if (conditional && value == null)
				value = rowArray[2];
			
			// if we haven't seen a different row for this patient already:
			if (!ret.containsKey(ptId)) {
				List<Object> arr = new Vector<Object>();
				arr.add(value);
				ret.put(ptId, arr);
			}
			// if we have seen a row for this patient already
			else {
				List<Object> oldArr = ret.get(ptId);
				oldArr.add(value);
				ret.put(ptId, oldArr);
			}
		}
		
		return ret;
		
	}

	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.ClinicalSummaryDAO#createQueueItem(org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem)
	 */
	public Integer createQueueItem(ClinicalSummaryQueueItem item) {
		Integer id = Integer.valueOf(sessionFactory.getCurrentSession().save(item).toString());
		return id;
	}
	

	/**
	 * @see org.openmrs.module.clinicalsummary.db.ClinicalSummaryDAO#deleteQueueItem(org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem)
	 */
	public void deleteQueueItem(ClinicalSummaryQueueItem item) {
		sessionFactory.getCurrentSession().delete(item);
	}
		
	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#getQueueItems(java.util.Date, java.util.List)
	 */
	@SuppressWarnings("unchecked")
	public List<ClinicalSummaryQueueItem> getQueueItems(Date beforeOrEqualToDate, List<String> status) {
		return getQueueItems(beforeOrEqualToDate, null, null, status, null, 0, 0);
	}
	
	/**
	 * Returns ClinicalSummaryQueueItems ordered by Location, PatientIdentifier
	 * where PatientIdentifer is either 
	 * 1. preferred or (if there is no preferred identifier)
	 * 2. the last created PatientIdentifier for a patient.
	 * 
	 * @return List<ClinicalSummaryQueueItem>
	 */
	@SuppressWarnings("unchecked")
	public List<ClinicalSummaryQueueItem> getQueueItems(Date begin, Date end, List<String> locations, List<String> statuses, ClinicalSummaryUtil.ORDER order, int offset, int limit) {
			
		SQLQuery queueItemsByIdentifier = sessionFactory.getCurrentSession().createSQLQuery(
				MysqlQuery.getQueueItems(begin, end, locations, statuses, order, offset, limit)
		).addEntity(ClinicalSummaryQueueItem.class);
		/*
		log.debug("ClinicalSummaryModule getQueueItems: " + 
					MysqlQuery.getQueueItems(begin, end, locations, statuses, order, offset, limit));
		*/
		return queueItemsByIdentifier.list();
	}
		
	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryService#getQueueItem(Integer)
	 */
	@SuppressWarnings("unchecked")
	public ClinicalSummaryQueueItem getQueueItem(Integer queueId) {
				
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ClinicalSummaryQueueItem.class);
		
		criteria.add(Expression.idEq(queueId));
		
		if (criteria.list().isEmpty()) return null;
		
		return (ClinicalSummaryQueueItem)criteria.list().get(0);
	}


	/**
	 * @see org.openmrs.module.clinicalsummary.db.ClinicalSummaryDAO#updateQueueItem(org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem)
	 */
	public void updateQueueItem(ClinicalSummaryQueueItem item) {
		sessionFactory.getCurrentSession().merge(item);
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.ClinicalSummaryDAO#getQueuePatientIds(java.util.List, org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS)
	 */
	public List<?> getQueuePatientIds(List<Integer> queueIds, CLINICAL_SUMMARY_QUEUE_STATUS statusToSet) {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ClinicalSummaryQueueItem.class);
		
		criteria.setProjection(Projections.distinct(Projections.property("patient.personId")));
		
		criteria.add(Expression.in("clinicalSummaryQueueId", queueIds));
		
		List<?> returnList = criteria.list();
		
		setQueueStatus(queueIds, statusToSet);
		
		return returnList;
		
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.db.ClinicalSummaryDAO#setQueueStatus(java.util.List, org.openmrs.module.clinicalsummary.ClinicalSummaryQueueItem.CLINICAL_SUMMARY_QUEUE_STATUS)
	 */
	public void setQueueStatus(List<Integer> queueIds, CLINICAL_SUMMARY_QUEUE_STATUS statusToSet) {
		
		if (statusToSet.equals(CLINICAL_SUMMARY_QUEUE_STATUS.PRINTED)) {
			Query query = sessionFactory.getCurrentSession()
				.createQuery("update ClinicalSummaryQueueItem queue set queue.status = :status, queue.datePrinted = :datePrinted where queue.clinicalSummaryQueueId in (:queueIds)")
				.setString("status", statusToSet.name())
				.setDate("datePrinted", new Date())
				.setParameterList("queueIds", queueIds);
			query.executeUpdate();
		}
		else {
			Query query = sessionFactory.getCurrentSession()
				.createQuery("update ClinicalSummaryQueueItem queue set queue.status = :status where queue.clinicalSummaryQueueId in (:queueIds)")
				.setString("status", statusToSet.name())
				.setParameterList("queueIds", queueIds);
			query.executeUpdate();
		}
		
	}

	/**
	 * Nested Private Class for Building MySQL Queries within HibernateClinicalSummaryDAO Class
	 * 
	 * Specifically intended to build the following query:
	 * MySql query to fetch ClinicalSummaryQueueItem and order results where PatientIdentifer is either 
	 * 1. preferred or (if there is no preferred identifier)
	 * 2. the last created PatientIdentifier for a patient.
	 *
	 * static String selectQueueItemsByIdentifier = 
	 * 	"( SELECT " +
	 *		"c.clinicalsummary_queue_id, c.patient_id, pid.identifier, c.location_id, l.name loc_name, c.encounter_datetime, " +
	 *		"c.date_printed, c.status, c.file_name, c.error_message, c.date_created, pid.preferred, pid.date_created " +
	 *		"FROM clinical_summary_print_queue c, patient pt, patient_identifier pid, location l " +
	 *		"WHERE c.patient_id=pid.patient_id AND c.patient_id=pt.patient_id AND pid.patient_id=pt.patient_id " +
	 * 		"AND l.location_id=c.location_id AND pid.preferred=1 " +
	 *  	//
	 * 		// Add more 'where' constraints here as desired.
	 *      //
	 *	") UNION ( SELECT " +
	 *		"c.clinicalsummary_queue_id, c.patient_id, pid.identifier, c.location_id, l.name loc_name, c.encounter_datetime, " +
	 * 	    "c.date_printed, c.status, c.file_name, c.error_message, c.date_created, pid.preferred, pid.date_created " +
	 *		"FROM clinical_summary_print_queue c, patient pt, patient_identifier pid, location l " +
	 *		"WHERE c.patient_id=pid.patient_id AND c.patient_id=pt.patient_id AND pid.patient_id=pt.patient_id " +
	 *		"AND l.location_id=c.location_id AND NOT EXISTS " +
	 *			"( SELECT pip.preferred FROM patient_identifier pip " +
	 *				"WHERE pip.patient_id = pid.patient_id AND c.patient_id=pid.patient_id AND pip.preferred=1 ) " +
	 *		"AND pid.date_created = " +
	 *			"( SELECT max(patient_identifier.date_created) m FROM patient_identifier " +
	 *				"WHERE patient_identifier.patient_id = pid.patient_id AND c.patient_id=pid.patient_id " +
	 *				"GROUP BY patient_identifier.patient_id ) " +
	 *      //
	 * 		// Add the same 'where' constraints here as were added above.
	 *      //
	 *	") ORDER BY loc_name, identifier";
	 */	
	private static class MysqlQuery {		
		/**
		 * Builds a MySql query to fetch Clinical Summary Queue Items
		 * 
		 * unionBegin + selectItems + where + wherePreferred + [add more where's] + 
		 * unionMiddle + selectItems + where + whereNonPreferred + [add more where's] + unionEnd
		 * 
		 * @return String query
		 */
		static String getQueueItems(Date begin, Date end, List<String> locations, List<String> statuses, ClinicalSummaryUtil.ORDER order, int offset, int limit) {
			StringBuilder query = new StringBuilder(4000);
			query.append(unionBegin + selectItems + where + wherePreferred);
			if (begin != null)
				query.append(" AND c.encounter_datetime >= '" + mysqlFormat.format(begin) + "' ");
			if (end != null)
				query.append(" AND c.encounter_datetime <= '" + mysqlFormat.format(end) + "' " );
			if (locations != null && !locations.get(0).contains("null") && locations.get(0).length() > 2) 
				query.append(" AND l.name IN " + locations + " ");
			if (statuses != null && !statuses.get(0).contains("null") && statuses.get(0).length() > 2)
				query.append(" AND c.status IN " + statuses + " ");
			query.append(unionMiddle + selectItems + where + whereNonPreferred);
			if (begin != null)
				query.append(" AND c.encounter_datetime >= '" + mysqlFormat.format(begin) + "' ");
			if (end != null)
				query.append(" AND c.encounter_datetime <= '" + mysqlFormat.format(end) + "' " );
			if (locations != null && !locations.get(0).contains("null") && locations.get(0).length() > 2) 
				query.append(" AND l.name IN " + locations + " ");
			if (statuses != null && !statuses.get(0).contains("null") && statuses.get(0).length() > 2)
				query.append(" AND c.status IN " + statuses + " ");
			if (order == ClinicalSummaryUtil.ORDER.ENCOUNTER_DATE)
				query.append(orderByDate);
			else
				query.append(orderByIdentifier);
			if (limit > 0) {
				if (offset < 0) offset = 0;
				query.append(" LIMIT " + offset + ", " + limit);
			}
			return query.toString().replaceAll("\\[", "(").replaceAll("\\]", ")");
		}
		
		static SimpleDateFormat mysqlFormat = new SimpleDateFormat("yyyy-MM-dd");
		// SELECT
		private static String unionBegin = "( SELECT ";	
		private static String selectItems = 
			"c.clinicalsummary_queue_id, c.patient_id, pid.identifier, c.encounter_id, c.location_id, l.name loc_name, c.encounter_datetime, " +
			"c.date_printed, c.status, c.file_name, c.error_message, c.date_created, pid.preferred, pid.date_created " +
			"FROM clinical_summary_print_queue c, patient pt, patient_identifier pid, location l ";	
		// WHERE
		private static String where = "WHERE c.patient_id=pid.patient_id AND c.patient_id=pt.patient_id AND pid.patient_id=pt.patient_id AND l.location_id=c.location_id ";
		private static String wherePreferred = "AND pid.preferred=1 ";
		// UNION
		private static String unionMiddle = ") UNION ( SELECT ";	
		// WHERE
		private static String whereNonPreferred = "AND NOT EXISTS " +
			"( SELECT pip.preferred FROM patient_identifier pip " +
				"WHERE pip.patient_id = pid.patient_id AND c.patient_id=pid.patient_id AND pip.preferred=1 ) " +
			"AND pid.date_created = " +
				"( SELECT max(patient_identifier.date_created) m FROM patient_identifier " +
					"WHERE patient_identifier.patient_id = pid.patient_id AND c.patient_id=pid.patient_id " +
					"GROUP BY patient_identifier.patient_id ) ";
		// ORDER BY
		private static String orderByIdentifier = ") ORDER BY loc_name, identifier";
		private static String orderByDate = ") ORDER BY loc_name, encounter_datetime desc";	
	}
			
}
