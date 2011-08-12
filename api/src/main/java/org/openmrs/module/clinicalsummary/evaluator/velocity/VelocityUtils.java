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

package org.openmrs.module.clinicalsummary.evaluator.velocity;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.result.Result;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.clinicalsummary.rule.util.RuleUtils;
import org.openmrs.module.clinicalsummary.service.EvaluatorService;
import org.openmrs.util.OpenmrsUtil;

/**
 *
 */
public class VelocityUtils {

	private static final Log log = LogFactory.getLog(VelocityUtils.class);

	private static final String CURRENT_DATETIME_FORMAT = "dd-MMM-yyyy hh:mm:ss.SSSS";

	private static final String DATE_FORMAT = "dd-MMM-yyyy";

	/**
	 * Format the result based on the result datatype
	 *
	 * @param result the result
	 * @return the String representation of the result object
	 */
	public String format(final Result result) {
		if (result == null)
			return StringUtils.EMPTY;

		// processing multi results
		if (CollectionUtils.size(result) > 1) {
			StringBuilder builder = new StringBuilder();
			// iterate each result and format them
			Integer counter = 0;
			while (counter < CollectionUtils.size(result)) {
				Result resultElement = result.get(counter++);
				builder.append(format(resultElement));
				if (counter < CollectionUtils.size(result))
					builder.append(", ");
			}
			return builder.toString();
		}

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

				DecimalFormat decimalFormat = new DecimalFormat("#.00");
				return decimalFormat.format(result.toNumber());
			}
		}

		// special case hack
		if (result.getDatatype() == null && result.toDatetime() != null)
			return format(result.toDatetime());

		return StringEscapeUtils.escapeXml(result.toString());
	}

	/**
	 * Format double object and remove the decimal section of the value
	 *
	 * @param value the decimal to be formatted
	 * @return String representation of the decimal values
	 */
	public String format(final Double value) {
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
	public String format(final Date date) {
		if (date == null)
			return StringUtils.EMPTY;
		String format = DATE_FORMAT;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		return simpleDateFormat.format(date);
	}

	/**
	 * Format the name of a concept with the best name for the locale or a shorter name for the concept
	 *
	 * @param concept the concept
	 * @return concept name with a relatively shorter name
	 */
	public String format(final Concept concept) {
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
		return StringEscapeUtils.escapeXml(name);
	}

	/**
	 * Extract a property of an object and return the String representation of the property value
	 *
	 * @param object   the object
	 * @param property the property name
	 * @return the string representation of the property value
	 */
	public String extractProperty(final Object object, final String property) {
		try {
			Object propertyValue = PropertyUtils.getProperty(object, property);
			if (propertyValue != null) {
				if (ClassUtils.isAssignable(propertyValue.getClass(), Date.class))
					return format((Date) propertyValue);
				else if (ClassUtils.isAssignable(propertyValue.getClass(), Concept.class))
					return format((Concept) propertyValue);
			}
			return StringEscapeUtils.escapeXml(BeanUtils.getProperty(object, property));
		} catch (Exception e) {
			return StringUtils.EMPTY;
		}
	}

	/**
	 * Get the current version of a module based on the module id
	 *
	 * @param moduleId the module id
	 * @return the module version number
	 */
	public String getModuleVersion(final String moduleId) {
		Module module = ModuleFactory.getModuleById(moduleId);
		if (module != null)
			return module.getVersion();
		return StringUtils.EMPTY;
	}

	/**
	 * Get the current date and time based on the server clock
	 *
	 * @return the current date and time
	 */
	public String getCurrentDatetime() {
		String format = CURRENT_DATETIME_FORMAT;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
		return simpleDateFormat.format(new Date());
	}

	/**
	 * This evaluation method are meant to be used inside velocity template. The method will return empty result when any exception happens during the
	 * execution of the rule.
	 *
	 * @param patient
	 * @param token
	 * @param parameters
	 * @return
	 * @see EvaluatorService#evaluate(Patient, String, java.util.Map)
	 */
	public Result evaluate(final Patient patient, final String token, final Map<String, Object> parameters) {
		Result result = new Result();
		try {
			EvaluatorService evaluatorService = Context.getService(EvaluatorService.class);
			result = evaluatorService.evaluate(patient, token, parameters);
		} catch (Exception e) {
			log.error("Evaluating token " + token + " on patient " + patient.getPatientId() + " failed ...", e);
		}
		return result;
	}

	/**
	 * @param expression
	 * @return
	 * @see EvaluatorService#parseExpression(String)
	 */
	public LogicCriteria parseExpression(final String expression) {
		return Context.getLogicService().parse(expression);
	}
}
