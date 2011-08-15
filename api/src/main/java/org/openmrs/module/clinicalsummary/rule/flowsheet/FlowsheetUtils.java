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

package org.openmrs.module.clinicalsummary.rule.flowsheet;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.util.RuleUtils;
import org.openmrs.util.OpenmrsUtil;

/**
 */
public class FlowsheetUtils {

	private static final Log log = LogFactory.getLog(FlowsheetUtils.class);

	/**
	 * Slice the flow-sheet to match the total number and record configuration needed
	 *
	 * @param results the result
	 * @return results object arranged in a certain way
	 */
	public static Result slice(final Result results) {

		if (CollectionUtils.isEmpty(results))
			return results;

		Result slicedResults = new Result();

		// result is ordered descending, last result is the earliest result
		Result firstResult = results.get(CollectionUtils.size(results) - 1);
		slicedResults.add(firstResult);

		if (results.size() > slicedResults.size()) {
			//  get the last four results
			Integer counter = 0;
			Result otherResults = new Result();
			while (counter < results.size() - 1 && counter < FlowsheetParameters.FLOWSHEET_MAX_SIZE - 1) {
				Result result = results.get(counter++);
				// skip some not not valid results
				if (DateUtils.isSameDay(firstResult.getResultDate(), result.getResultDate())
						&& OpenmrsUtil.nullSafeEquals(firstResult.getDatatype(), result.getDatatype())) {
					if (OpenmrsUtil.nullSafeEquals(result.getDatatype(), Result.Datatype.NUMERIC)
							&& OpenmrsUtil.nullSafeEquals(firstResult.toNumber(), result.toNumber())
							|| (OpenmrsUtil.nullSafeEquals(result.getDatatype(), Result.Datatype.CODED)
							&& OpenmrsUtil.nullSafeEquals(firstResult.toConcept(), result.toConcept())))
						continue;
				}
				// get the latest results
				otherResults.add(result);
			}

			// flip them to make the order from earliest to latest
			Collections.reverse(otherResults);
			// add all of them to the oldest result, we now have results from earliest to latest
			slicedResults.addAll(otherResults);
			// flip them back to make the order from latest to the earliest
			Collections.reverse(slicedResults);
		}

		return slicedResults;
	}

	/**
	 * Format the result based on the result data type
	 *
	 * @param result the result
	 * @return the String representation of the result object
	 */
	public static String format(final Result result) {
		if (result == null)
			return StringUtils.EMPTY;

		if (OpenmrsUtil.nullSafeEquals(result.getDatatype(), Result.Datatype.CODED))
			return format(result.toConcept());
		else if (OpenmrsUtil.nullSafeEquals(result.getDatatype(), Result.Datatype.DATETIME))
			return format(result.toDatetime());
		else if (OpenmrsUtil.nullSafeEquals(result.getDatatype(), Result.Datatype.NUMERIC)) {
			Object object = result.getResultObject();
			if (RuleUtils.isValidObsObject(object)) {
				Concept concept = ((Obs) object).getConcept();
				if (concept.isNumeric()) {
					ConceptNumeric conceptNumeric = Context.getConceptService().getConceptNumeric(concept.getConceptId());
					if (!conceptNumeric.isPrecise())
						return format(result.toNumber());
				}
			}

			DecimalFormat decimalFormat = new DecimalFormat("#.00");
			return decimalFormat.format(result.toNumber());
		}

		// special case hack
		if (result.getDatatype() == null && result.toDatetime() != null)
			return format(result.toDatetime());

		return result.toString();
	}

	/**
	 * Format double object and remove the decimal section of the value
	 *
	 * @param value the decimal to be formatted
	 * @return String representation of the decimal values
	 */
	public static String format(final Double value) {
		if (value == null)
			return StringUtils.EMPTY;
		DecimalFormat decimalFormat = new DecimalFormat("#");
		return decimalFormat.format(value);
	}

	/**
	 * Format date to a string according to the date format
	 *
	 * @param date the date
	 * @return string representation of the date
	 */
	public static String format(final Date date) {
		if (date == null)
			return StringUtils.EMPTY;
		String format = "dd-MMM-yyyy";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		return simpleDateFormat.format(date);
	}

	/**
	 * Format the name of a concept with the best name for the locale or a shorter name for the concept
	 *
	 * @param concept the concept
	 * @return concept name with a relatively shorter name
	 */
	public static String format(final Concept concept) {
		if (concept == null)
			return StringUtils.EMPTY;

		// use the best name as the default name
		String name = concept.getName(Context.getLocale()).getName();
		// when the name is too long, then use the concept's short name for display
		if (StringUtils.length(name) > 10) {
			ConceptName conceptName = concept.getShortNameInLocale(Context.getLocale());
			if (conceptName != null)
				name = conceptName.getName();
		}

		return name;
	}
}
