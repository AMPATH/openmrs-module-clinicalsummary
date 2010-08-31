package org.openmrs.module.clinicalsummary.deprecated.db.hibernate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.api.db.hibernate.HibernatePatientSetDAO;
import org.openmrs.module.clinicalsummary.deprecated.ClinicalSummary;
import org.openmrs.module.clinicalsummary.deprecated.db.ClinicalSummaryDAO;

public class HibernateClinicalSummaryDAO implements ClinicalSummaryDAO {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) { 
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.module.clinicalsummary.deprecated.db.ClinicalSummaryDAO#createClinicalSummary(org.openmrs.module.clinicalsummary.deprecated.ClinicalSummary)
	 */
	public void createClinicalSummary(ClinicalSummary summary) {
		sessionFactory.getCurrentSession().save(summary);
		if (summary.getPreferred())
			setPreferred(summary);
	}
	
	
	/**
	 * @see org.openmrs.module.clinicalsummary.deprecated.db.ClinicalSummaryDAO#getClinicalSummary(java.lang.Integer)
	 */
	public ClinicalSummary getClinicalSummary(Integer id) {
		return (ClinicalSummary) sessionFactory.getCurrentSession().get(ClinicalSummary.class, id);
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.deprecated.db.ClinicalSummaryDAO#getClinicalSummaries()
	 */
	@SuppressWarnings("unchecked")
	public List<ClinicalSummary> getClinicalSummaries() {
		return sessionFactory.getCurrentSession().createQuery(
			"from ClinicalSummary order by clinicalSummaryId").list();
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.deprecated.db.ClinicalSummaryDAO#updateClinicalSummary(org.openmrs.module.clinicalsummary.deprecated.ClinicalSummary)
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
}
