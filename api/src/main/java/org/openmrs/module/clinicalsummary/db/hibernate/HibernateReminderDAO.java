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

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.openmrs.OpenmrsObject;
import org.openmrs.Patient;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.clinicalsummary.Reminder;
import org.openmrs.module.clinicalsummary.db.ReminderDAO;

/**
 */
public class HibernateReminderDAO implements ReminderDAO {

	private static final Log log = LogFactory.getLog(HibernateReminderDAO.class);

	private SessionFactory sessionFactory;

	/**
	 * Method that will be called by Spring to inject the Hibernate's SessionFactory.
	 *
	 * @param sessionFactory
	 * 		the session factory to be injected
	 */
	public void setSessionFactory(final SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @see ReminderDAO#saveReminder(org.openmrs.module.clinicalsummary.Reminder)
	 */
	@Override
	public Reminder saveReminder(final Reminder reminder) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(reminder);
		return reminder;
	}

	/**
	 * @see ReminderDAO#getReminder(Integer)
	 */
	@Override
	public Reminder getReminder(final Integer id) throws DAOException {
		return (Reminder) sessionFactory.getCurrentSession().get(Reminder.class, id);
	}

	/**
	 * @see ReminderDAO#getReminders(org.openmrs.Patient)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Reminder> getReminders(final Patient patient) throws DAOException {
		// DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Reminder.class);
		// detachedCriteria.add(Restrictions.eq("patient", patient));
		// detachedCriteria.setProjection(Projections.max("dateCreated"));

		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Reminder.class);
		criteria.add(Restrictions.eq("patient", patient));
		// criteria.add(Property.forName("dateCreated").eq(detachedCriteria));
		criteria.addOrder(Order.desc("dateCreated"));
		return criteria.list();
	}

	/**
	 * @see ReminderDAO#getLatestReminders(org.openmrs.Patient)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Reminder> getLatestReminders(final Patient patient) throws DAOException {
		// we need to do two queries here because we can't do the detached query to get the max date without the time
		Criteria criteria;

		criteria = sessionFactory.getCurrentSession().createCriteria(Reminder.class);
		criteria.add(Restrictions.eq("patient", patient));
		criteria.setProjection(Projections.max("reminderDatetime"));
		Date date = (Date) criteria.uniqueResult();

		// remove the time part of the date
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.SECOND, -calendar.get(Calendar.SECOND));
		calendar.add(Calendar.MINUTE, -calendar.get(Calendar.MINUTE));
		calendar.add(Calendar.HOUR, -calendar.get(Calendar.HOUR));

		Date truncatedDate = calendar.getTime();

		criteria = sessionFactory.getCurrentSession().createCriteria(Reminder.class);
		criteria.add(Restrictions.eq("patient", patient));
		criteria.add(Property.forName("dateCreated").ge(truncatedDate));
		criteria.addOrder(Order.desc("dateCreated"));
		return criteria.list();
	}

	/**
	 * @see ReminderDAO#getReminders(java.util.Map, java.util.Date, java.util.Date)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Reminder> getReminders(final Map<String, Collection<OpenmrsObject>> restrictions,
	                                   final Date reminderStart, final Date reminderEnd) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Reminder.class);

		if (MapUtils.isNotEmpty(restrictions)) {
			Reminder reminder = new Reminder();
			for (String property : restrictions.keySet()) {
				Collection<OpenmrsObject> objects = restrictions.get(property);
				if (CollectionUtils.isNotEmpty(objects) && PropertyUtils.isReadable(reminder, property))
					criteria.add(Restrictions.in(property, objects));
			}
		}

		if (reminderStart != null)
			criteria.add(Restrictions.ge("reminderDatetime", reminderStart));

		if (reminderEnd != null)
			criteria.add(Restrictions.le("reminderDatetime", reminderEnd));

		return criteria.list();
	}

	/**
	 * @see ReminderDAO#aggregateReminders(java.util.Map, java.util.Collection, java.util.Date, java.util.Date)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Object[]> aggregateReminders(final Map<String, Collection<OpenmrsObject>> restrictions, final Collection<String> groupingProperties,
	                                         final Date reminderStart, final Date reminderEnd) throws DAOException {

		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Reminder.class);

		if (MapUtils.isNotEmpty(restrictions)) {
			Reminder reminder = new Reminder();
			for (String property : restrictions.keySet()) {
				Collection<OpenmrsObject> objects = restrictions.get(property);
				if (CollectionUtils.isNotEmpty(objects) && PropertyUtils.isReadable(reminder, property))
					criteria.add(Restrictions.in(property, objects));
			}
		}

		if (reminderStart != null)
			criteria.add(Restrictions.ge("reminderDatetime", reminderStart));

		if (reminderEnd != null)
			criteria.add(Restrictions.le("reminderDatetime", reminderEnd));

		ProjectionList projectionList = Projections.projectionList();
		for (String groupingProperty : groupingProperties) {
			// group by the property and order by the same property desc and the property must not null
			criteria.add(Restrictions.isNotNull(groupingProperty));
			projectionList.add(Projections.groupProperty(groupingProperty));
			criteria.addOrder(Order.asc(groupingProperty));
		}
		// add the row count projection to the projection list
		projectionList.add(Projections.rowCount());
		criteria.setProjection(projectionList);

		return criteria.list();
	}
}
