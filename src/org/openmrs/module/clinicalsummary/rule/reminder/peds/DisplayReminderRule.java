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
package org.openmrs.module.clinicalsummary.rule.reminder.peds;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.Rule;
import org.openmrs.logic.result.Result;
import org.openmrs.logic.result.Result.Datatype;
import org.openmrs.logic.rule.RuleParameterInfo;
import org.openmrs.module.clinicalsummary.encounter.TypeConstants;
import org.openmrs.module.clinicalsummary.rule.CompleteEncounterRule;
import org.openmrs.module.clinicalsummary.task.PrepareStudyConceptTask;
import org.openmrs.util.OpenmrsUtil;

/**
 * 
 */
public class DisplayReminderRule implements Rule {
	
	private static final Log log = LogFactory.getLog(DisplayReminderRule.class);
	
	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, org.openmrs.Patient,
	 *      java.util.Map)
	 */
	public Result eval(LogicContext context, Patient patient, Map<String, Object> parameters) throws LogicException {
		Result result = new Result(Boolean.TRUE);
		
		List<String> typeNames = Arrays.asList(TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN,
		    TypeConstants.ADULT_NONCLINICAL, TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN,
		    TypeConstants.PEDS_NONCLINICAL);
		
		parameters.put("evaluated.encounter", "encounter.type");
		parameters.put("included.encounter.types", typeNames);
		
		CompleteEncounterRule rule = new CompleteEncounterRule();
		Result encounterResults = rule.eval(context, patient, parameters);
		if (!encounterResults.isEmpty()) {
			Result latestEncounterResult = encounterResults.latest();
			Encounter encounter = (Encounter) latestEncounterResult.getResultObject();
			Location pediatricModule = Context.getLocationService().getLocation("MTRH Module 4");
			if (OpenmrsUtil.nullSafeEquals(encounter.getLocation(), pediatricModule)) {
				
				Concept controlConcept = Context.getConceptService().getConcept(PrepareStudyConceptTask.PEDIATRIC_CONTROL);
				Concept interveneConcept = Context.getConceptService().getConcept(PrepareStudyConceptTask.PEDIATRIC_INTERVENTION);
				
				List<Obs> interventionObs = Context.getObsService().getObservationsByPersonAndConcept(patient, interveneConcept);
				
				// if patient have intervention, then by default patient will get reminder
				if (interventionObs.isEmpty()) {
					//patient doesn't have intervention, now check the control
					List<Obs> controlObs = Context.getObsService().getObservationsByPersonAndConcept(patient, controlConcept);
					
					if (log.isDebugEnabled())
						log.debug("Patient: " + patient.getPatientId() + ", control obs is empty: " + controlObs.isEmpty());
					
					if (controlObs.isEmpty()) {
						// this patient is not in the study yet, give him / her the randomization obs
						
						if (ArrayUtils.isEmpty(randomNumber))
							randomNumber = generateRandomNumber();
						
						writeRandomNumber(randomNumber);
						int randomizedValue = randomNumber[0];
						randomNumber = ArrayUtils.remove(randomNumber, 0);
						
						Obs obs = new Obs();
						obs.setObsDatetime(new Date());
						obs.setPerson(patient);
						obs.setConcept(interveneConcept);
						if (randomizedValue == 1) {
							obs.setConcept(controlConcept);
							result = new Result(Boolean.FALSE);
						}
						obs.setLocation(pediatricModule);
						Context.getObsService().saveObs(obs, "Randomizing patient that's not in the study");
					} else {
						// patient is in control group, hide the reminder
						result = new Result(Boolean.FALSE);
					}
				}
				
				if (log.isDebugEnabled())
					log.debug("Patient: " + patient.getPatientId() + ", control obs is empty: " + interventionObs.isEmpty());
			}
		}
		
		return result;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getDefaultDatatype()
	 */
	public Datatype getDefaultDatatype() {
		return Datatype.TEXT;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getDependencies()
	 */
	public String[] getDependencies() {
		return null;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getParameterList()
	 */
	public Set<RuleParameterInfo> getParameterList() {
		return null;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getTTL()
	 */
	public int getTTL() {
		return 0;
	}
	
	private static final String RANDOM_NUMBER_PROPERTY = "clinicalsummary.randomNumber";
	
	private static final String SPACE_CHARACTER = " ";
	
	public static int[] randomNumber = readRandomNumber();
	
	public static int[] readRandomNumber() {
		int[] randomizedValues = null;
		AdministrationService administrationService = Context.getAdministrationService();
		String randomNumberString = administrationService.getGlobalProperty(RANDOM_NUMBER_PROPERTY);
		if (StringUtils.isNotBlank(randomNumberString)) {
			String[] stringNumbers = StringUtils.split(randomNumberString, SPACE_CHARACTER);
			randomizedValues = new int[stringNumbers.length];
			for (int i = 0; i < stringNumbers.length; i++) {
				int randomizedValue = NumberUtils.toInt(stringNumbers[i]);
				randomizedValues[i] = randomizedValue;
			}
		}
		return randomizedValues;
	}
	
	public static void writeRandomNumber(int[] randomizedValues) {
		StringBuffer buffer = new StringBuffer();
		if (randomizedValues != null)
			for (int i : randomizedValues)
				buffer.append(i).append(SPACE_CHARACTER);
		String randomNumberString = buffer.toString().trim();
		
		if (log.isDebugEnabled())
			log.debug("Saving random number to the global property ...");
		
		AdministrationService administrationService = Context.getAdministrationService();
		GlobalProperty globalProperty = administrationService.getGlobalPropertyObject(RANDOM_NUMBER_PROPERTY);
		if (globalProperty == null)
			globalProperty = new GlobalProperty(RANDOM_NUMBER_PROPERTY, StringUtils.EMPTY, "Auto generated random number for randomizing patients");
		globalProperty.setPropertyValue(randomNumberString);
		administrationService.saveGlobalProperty(globalProperty);
	}
	
	public static int[] generateRandomNumber() {
		
		int maxRandomNumber = 1000;
		
		int controlCounter = 0;
		int interventionCounter = 0;
		
		Random random = new Random();
		
		int counter = 0;
		int[] randomizedValues = new int[4];
		while (controlCounter < 2 && interventionCounter < 2) {
			int randomizedValue = random.nextInt(maxRandomNumber);
			if (randomizedValue < maxRandomNumber / 2) {
				controlCounter++;
				randomizedValues[counter++] = 0;
			} else {
				interventionCounter++;
				randomizedValues[counter++] = 1;
			}
		}
		
		int fillValue = 0;
		if (interventionCounter < controlCounter)
			fillValue = 1;
		
		for (int i = counter; i < randomizedValues.length; i++)
			randomizedValues[i] = fillValue;
		
		return randomizedValues;
	}
}
