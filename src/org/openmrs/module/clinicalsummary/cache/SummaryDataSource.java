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
package org.openmrs.module.clinicalsummary.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.LogicExpression;
import org.openmrs.logic.LogicExpressionBinary;
import org.openmrs.logic.LogicTransform;
import org.openmrs.logic.datasource.LogicDataSource;
import org.openmrs.logic.op.ComparisonOperator;
import org.openmrs.logic.op.Operand;
import org.openmrs.logic.op.OperandCollection;
import org.openmrs.logic.op.OperandText;
import org.openmrs.logic.op.Operator;
import org.openmrs.logic.op.TransformOperator;
import org.openmrs.logic.result.Result;

/**
 */
public class SummaryDataSource implements LogicDataSource {
	
	public static final String ENCOUNTER = "encounter";
	
	public static final String ENCOUNTER_TYPE = "encounter.type";
	
	public static final String ENCOUNTER_LOCATION = "encounter.location";
	
	public static final String ENCOUNTER_PROVIDER = "encounter.provider";
	
	public static final String ENCOUNTER_CREATOR = "encounter.creator";

	public static final String CONCEPT = "concept";
	
	public static final String LATEST_ENCOUNTER = "encounter.latest";
	
	public static final String EARLIEST_ENCOUNTER = "encounter.earliest";
	
	private final DataProvider dataProvider;
	
	/**
	 * Initialize this data source with the correct data provider. The data provider will inject
	 * data that have been pre-cached before any rule evaluation occur.
	 * 
	 * @param dataProvider the data provider.
	 */
	public SummaryDataSource(DataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}
	
	/**
	 * @see org.openmrs.logic.datasource.LogicDataSource#getDefaultTTL()
	 */
	@Override
	public int getDefaultTTL() {
		return 0;
	}
	
	/**
	 * @see org.openmrs.logic.datasource.LogicDataSource#getKeys()
	 */
	@Override
	public Collection<String> getKeys() {
		return null;
	}
	
	/**
	 * @see org.openmrs.logic.datasource.LogicDataSource#hasKey(java.lang.String)
	 */
	@Override
	public boolean hasKey(String key) {
		return false;
	}
	
	/**
	 * @see org.openmrs.logic.datasource.LogicDataSource#read(org.openmrs.logic.LogicContext,
	 *      org.openmrs.Cohort, org.openmrs.logic.LogicCriteria)
	 */
	@Override
	public Map<Integer, Result> read(LogicContext context, Cohort patients, LogicCriteria criteria) throws LogicException {
		Map<Integer, Result> map = new HashMap<Integer, Result>();
		for (Integer patientId : patients.getMemberIds())
			map.put(patientId, handleCriteria(patientId, criteria.getExpression()));
		return map;
	}
	
	/**
	 * Handle the logic criteria that will be evaluated. The expression supported are only for
	 * concept and observations. Example of supported operations are:<br/>
	 * For Obs:
	 * 
	 * <pre>
	 * LogicCriteria conceptCriteria = service.parseToken(SummaryLogicDataSource.CONCEPT);
	 * conceptCriteria.equals(&quot;CD4 COUNT&quot;);
	 * LogicCriteria encounterCriteria = service.parseToken(SummaryLogicDataSource.ENCOUNTER_TYPE);
	 * encounterCriteria.in(Arrays.asList(&quot;ADULTRETURN&quot;, &quot;ADULTINITIAL&quot;));
	 * // optionally:
	 * // encounterCriteria.last();
	 * LogicCriteria criteria = conceptCriteria.and(encounterCriteria);
	 * </pre>
	 * 
	 * For Encounter:
	 * 
	 * <pre>
	 * LogicCriteria encounterCriteria = service.parseToken(SummaryLogicDataSource.ENCOUNTER_TYPE);
	 * encounterCriteria.in(Arrays.asList(&quot;ADULTRETURN&quot;, &quot;ADULTINITIAL&quot;));
	 * </pre>
	 * 
	 * @param patientId the patient
	 * @param expression the logic expression
	 * @return result of the logic expression evaluation on the patient
	 */
	private Result handleCriteria(Integer patientId, LogicExpression expression) {
		Result result = new Result();
		// the structure of the logic expression for this data source is:
		// left operand will contains:
		// -- for obs: concept.name (root token), operator: equals, name of the concept
		// -- for encounter: encounter.name (root token), operator: equalTo or in, name of the encounter type (s)
		// right operand will contains:
		// -- for obs only: encounter.name (root token), operator: equalTo or in, name of the encounter type (s)
		Collection<String> typeNames = new HashSet<String>();
		if (StringUtils.equals(expression.getRootToken(), CONCEPT)) {
			
			String conceptName = StringUtils.EMPTY;
			List<Obs> observations = new ArrayList<Obs>();
			
			Operand rightOperand = expression.getRightOperand();
			Operand leftOperand = ((LogicExpressionBinary) expression).getLeftOperand();
			
			// get the concept name from the left operand
			if (leftOperand instanceof LogicExpressionBinary) {
				OperandText operandText = (OperandText) ((LogicExpressionBinary) leftOperand).getRightOperand();
				conceptName = operandText.asString();
			}
			
			// right operand will contains the encounter type names and possibly transform operator
			if (rightOperand instanceof LogicExpressionBinary) {
				Operator operator = ((LogicExpressionBinary) rightOperand).getOperator();
				Operand rightRightOperand = ((LogicExpressionBinary) rightOperand).getRightOperand();
				if (ComparisonOperator.IN.equals(operator))
					for (Object o : ((OperandCollection) rightRightOperand).asCollection())
						typeNames.add(String.valueOf(o));
				else if (ComparisonOperator.EQUALS.equals(operator))
					typeNames.add(((OperandText) rightRightOperand).asString());
				
				LogicTransform transform = ((LogicExpressionBinary) rightOperand).getTransform();
				if (transform != null) {
					// we found a transform, then we are looking for observations for a certain encounter
					Operator transformOperator = transform.getTransformOperator();
					if (TransformOperator.LAST.equals(transformOperator))
						observations.addAll(dataProvider.getEncounterObservations(patientId, conceptName, typeNames, LATEST_ENCOUNTER));
					else if (TransformOperator.FIRST.equals(transformOperator))
						observations.addAll(dataProvider.getEncounterObservations(patientId, conceptName, typeNames, EARLIEST_ENCOUNTER));
				} else {
					// no transformation operation specified, we are evaluating on encounter type names
					observations.addAll(dataProvider.getObservations(patientId, conceptName, typeNames));
				}
				
				for (Obs obs : observations)
					result.add(new Result(obs));
			}
			
		} else if (StringUtils.startsWith(expression.getRootToken(), ENCOUNTER)) {
			
			// for encounter evaluation, we only care about the the type names
			// type names will be on the right operand and it could be a single encounter type name
			// or collection of encounter type names
			
			List<Encounter> encounters = new ArrayList<Encounter>();
			
			Operator operator = expression.getOperator();
			Operand rightOperand = expression.getRightOperand();
			if (ComparisonOperator.IN.equals(operator))
				for (Object o : ((OperandCollection) rightOperand).asCollection())
					typeNames.add(String.valueOf(o));
			else if (ComparisonOperator.EQUALS.equals(operator))
				typeNames.add(((OperandText) rightOperand).asString());
			
			encounters.addAll(dataProvider.getEncounters(patientId, typeNames));
			
			// create result based on the encounter list
			// TODO: maybe we need to enable the transformation here
			String rootToken = expression.getRootToken();
			if (ENCOUNTER_TYPE.equals(rootToken)) {
				for (Encounter encounter : encounters) {
					Date encounterDatetime = encounter.getEncounterDatetime();
					
					String encounterTypeName = StringUtils.EMPTY;
					EncounterType encounterType = encounter.getEncounterType();
					if (encounterType != null)
						encounterTypeName = encounterType.getName();
					
					result.add(new Result(encounterDatetime, encounterTypeName, encounter));
				}
			} else if (ENCOUNTER_LOCATION.equals(rootToken)) {
				for (Encounter encounter : encounters) {
					Date encounterDatetime = encounter.getEncounterDatetime();
					
					String locationName = StringUtils.EMPTY;
					Location location = encounter.getLocation();
					if (location != null)
						locationName = location.getName();
					
					result.add(new Result(encounterDatetime, locationName, encounter));
				}
			} else if (ENCOUNTER_PROVIDER.equals(rootToken)) {
				for (Encounter encounter : encounters) {
					Date encounterDatetime = encounter.getEncounterDatetime();
					
					String providerName = StringUtils.EMPTY;
					Person provider = encounter.getProvider();
					if (provider != null && provider.getPersonName() != null)
						providerName = provider.getPersonName().getFullName();
					
					result.add(new Result(encounterDatetime, providerName, encounter));
				}
			} else if (ENCOUNTER_CREATOR.equals(rootToken)) {
				for (Encounter encounter : encounters) {
					Date encounterDatetime = encounter.getDateCreated();
					
					String creatorName = StringUtils.EMPTY;
					User creator = encounter.getCreator();
					if (creator != null && creator.getPersonName() != null)
						creatorName = creator.getPersonName().getFullName();
					
					result.add(new Result(encounterDatetime, creatorName, encounter));
				}
			}
		}
		
		return result;
	}
	
}
