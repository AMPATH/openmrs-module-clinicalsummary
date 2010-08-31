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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;

/**
 * Utility class to create Hibernate's Criteria object based on the parameter coming from the jQuery
 * datatable plugin. This Criteria will be used to search ClinicalSummaryIndex and then returned for
 * display.
 * 
 * TODO: Allow searching return date (must enter full date)
 * The logic would be:
 * - Try to parse the search term as Date, if fail then ignore. If worked fine, then search on return
 * TODO: Allow for full term search
 * The logic would be:
 * - Searching for "MTRH+MODULE+4" should return only record matching "MTRH MODULE 4"
 */
public class IndexFinder {
	
	/**
	 * List of available properties from the index that can be search and sorted
	 */
	private static final List<String> PROPERTIES = new ArrayList<String>(8);
	
	static {
		PROPERTIES.add("indexId");
		PROPERTIES.add("identifier.identifier");
		PROPERTIES.add("name.givenName");
		PROPERTIES.add("name.middleName");
		PROPERTIES.add("name.familyName");
		PROPERTIES.add("location.name");
		PROPERTIES.add("returnDate");
		PROPERTIES.add("template.name");
		PROPERTIES.add("initialDate");
		PROPERTIES.add("dateChanged");
	}
	
	private static final String DESCENDING_ORDER = "desc";
	
	private static final String ASCENDING_ORDER = "asc";
	
	/**
	 * Criteria object that will be manipulated in this class
	 */
	private final Criteria criteria;
	
	/**
	 * Boolean to determine whether we are searching based on patient identifier or no
	 */
	private boolean searchingIdentifier;
	
	/**
	 * Boolean to determine whether we are performing search or no. This flag will be used to decide
	 * whether we need to create more aliases for the Criteria or no
	 */
	private boolean searching;
	
	/**
	 * Create IndexFinder for the Criteria object
	 * 
	 * @param criteria the criteria object
	 */
	public IndexFinder(Criteria criteria) {
		this.criteria = criteria;
	}
	
	/**
	 * Set the search criteria based on the search String. Searching will be performed on all String
	 * property with OR connecting one search element with the other.
	 * 
	 * @param search the search term
	 */
	public void setSearch(String search) {
		// skip search when there's less than 3 characters in the search criteria
		if (!StringUtils.isBlank(search)) {
			searching = true;
			if (search.matches("\\d+\\D?")) {
				searchingIdentifier = true;
				criteria.createAlias("patient", "patient");
				criteria.createAlias("patient.identifiers", "identifier");
				criteria.add(Restrictions.eq("identifier.voided", false));
				criteria.add(Restrictions.ilike("identifier.identifier", search, MatchMode.ANYWHERE));
			} else {
				criteria.createAlias("patient", "patient");
				criteria.createAlias("patient.names", "name");
				criteria.add(Restrictions.eq("name.voided", false));
				
				criteria.createAlias("location", "location");
				criteria.add(Restrictions.eq("location.retired", false));
				
				criteria.createAlias("template", "template");
				criteria.add(Restrictions.eq("template.retired", false));
				
				LogicalExpression expression = null;
				String[] searchTerms = StringUtils.split(search);
				for (String string : searchTerms)
					if (expression == null)
						expression = createLogicalExpression(string);
					else
						expression = Expression.or(expression, createLogicalExpression(string));
				criteria.add(expression);
				
			}
		}
	}
	
	/**
	 * Create a Hibernate expression for all String properties
	 * 
	 * @param searchTerm the search term
	 * @return Hibernate criteria based on the search term on all String properties
	 */
	private LogicalExpression createLogicalExpression(String searchTerm) {
		
		SimpleExpression givenExpression = Expression.like("name.givenName", searchTerm, MatchMode.ANYWHERE);
		SimpleExpression middleExpression = Expression.like("name.middleName", searchTerm, MatchMode.ANYWHERE);
		SimpleExpression familyExpression = Expression.like("name.familyName", searchTerm, MatchMode.ANYWHERE);
		SimpleExpression locationExpression = Expression.like("location.name", searchTerm, MatchMode.ANYWHERE);
		SimpleExpression templateExpression = Expression.like("template.name", searchTerm, MatchMode.ANYWHERE);
		
		return Expression.or(givenExpression, Expression.or(middleExpression, Expression.or(familyExpression, Expression.or(
		    locationExpression, templateExpression))));
	}
	
	/**
	 * Set total number of record that will be returned and the offset from the first record.
	 * 
	 * @param start offset from the first record
	 * @param length total number of record to be returned
	 */
	public void setRecord(int start, int length) {
		criteria.setFirstResult(start);
		criteria.setMaxResults(length);
	}
	
	/**
	 * Set how the record should be ordered.
	 * 
	 * @param sortOrder how the record should be ordered
	 * @param sortColumn column on which the sort should be performed
	 */
	public void setOrdering(String sortOrder, int sortColumn) {
		prepareAlias(sortColumn);
		if (StringUtils.equalsIgnoreCase(sortOrder, DESCENDING_ORDER))
			criteria.addOrder(Order.desc(PROPERTIES.get(sortColumn)));
		else if (StringUtils.equalsIgnoreCase(sortOrder, ASCENDING_ORDER))
			criteria.addOrder(Order.asc(PROPERTIES.get(sortColumn)));
	}
	
	/**
	 * Method to determine whether alias is needed or not
	 * 
	 * @param orderPosition position of the properties on the all properties list 
	 * @return true when the property need alias
	 */
	private boolean aliasNeeded(int orderPosition) {
		// only need to add alias when we need to
		
		// alias will not be there if we are not doing search
		if (!searching)
			return true;
		
		// alias will be on identifier only if we search on identifier
		if (searchingIdentifier) {
			if (orderPosition > 0)
				return true;
		} else {
			// alias will not on other places otherwise
			if (orderPosition == 0)
				return true;
		}
		
		return false;
	}
	
	/**
	 * Add an alias based on the location of the property in the properties list
	 *  
	 * @param orderPosition position of the properties on the all properties list
	 */
	private void prepareAlias(int orderPosition) {
		// using switch incase we need to prepare aliases for other column
		switch (orderPosition) {
			case 5:
				if (aliasNeeded(orderPosition))
					criteria.createAlias("location", "location");
				break;
			case 7:
				if (aliasNeeded(orderPosition))
					criteria.createAlias("template", "template");
				break;
		}
	}
}
