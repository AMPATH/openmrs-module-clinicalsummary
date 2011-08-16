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

package org.openmrs.module.clinicalsummary.rule.primary;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.cache.CacheUtils;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.medication.AntiRetroViralRule;
import org.openmrs.module.clinicalsummary.rule.medication.CryptococcalRule;
import org.openmrs.module.clinicalsummary.rule.medication.PneumocystisCariniiRule;
import org.openmrs.module.clinicalsummary.rule.medication.TuberculosisRule;
import org.openmrs.module.clinicalsummary.rule.treatment.TuberculosisTreatmentRule;
import org.openmrs.util.OpenmrsUtil;

public class EncounterDiagnosisAndMedicationRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(EncounterDiagnosisAndMedicationRule.class);

	public static final String TOKEN = "Encounter Diagnosis And Medication";

	/**
	 * @param context
	 * @param patientId
	 * @param parameters
	 * @return
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) {

		Result result = new Result();

		List<String> encounterTypeNames = Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_INITIAL,
				EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_RETURN, EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_INITIAL,
				EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_RETURN, EvaluableNameConstants.ENCOUNTER_TYPE_MOSORIOT_PERINATAL_INITIAL,
				EvaluableNameConstants.ENCOUNTER_TYPE_MOSORIOT_PRIMARY_CARE);

		EncounterType adultInitialEncounterType = CacheUtils.getEncounterType(EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_INITIAL);
		EncounterType adultReturnEncounterType = CacheUtils.getEncounterType(EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_RETURN);
		EncounterType pediatricInitialEncounterType = CacheUtils.getEncounterType(EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_INITIAL);
		EncounterType pediatricReturnEncounterType = CacheUtils.getEncounterType(EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_RETURN);
		EncounterType mosoriotPerinatalEncounterType = CacheUtils.getEncounterType(EvaluableNameConstants.ENCOUNTER_TYPE_MOSORIOT_PERINATAL_INITIAL);
		EncounterType mosoriotPrimaryCareEncounterType = CacheUtils.getEncounterType(EvaluableNameConstants.ENCOUNTER_TYPE_MOSORIOT_PRIMARY_CARE);

		parameters.put(EvaluableConstants.ENCOUNTER_TYPE, encounterTypeNames);
		parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 5);

		EncounterWithRestrictionRule encounterWithRestrictionRule = new EncounterWithStringRestrictionRule();
		Result encounterResults = encounterWithRestrictionRule.eval(context, patientId, parameters);
		if (CollectionUtils.isNotEmpty(encounterResults)) {
			for (Result encounterResult : encounterResults) {
				Result perEncounterResult = new Result();
				perEncounterResult.add(encounterResult);

				Encounter encounter = (Encounter) encounterResult.getResultObject();
				EncounterType encounterType = encounter.getEncounterType();

				if (OpenmrsUtil.nullSafeEquals(encounterType, adultInitialEncounterType)
						|| OpenmrsUtil.nullSafeEquals(encounterType, adultReturnEncounterType)
						|| OpenmrsUtil.nullSafeEquals(encounterType, pediatricInitialEncounterType)
						|| OpenmrsUtil.nullSafeEquals(encounterType, pediatricReturnEncounterType)) {

					Result problemResults = new Result();

					parameters.clear();
					parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
					EncounterBasedProblemAddedRule problemAddedRule = new EncounterBasedProblemAddedRule();
					Result problemAddedResults = problemAddedRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(problemAddedResults))
						problemResults.addAll(problemAddedResults);
					perEncounterResult.add(problemResults);

					Result medicationResults = new Result();

					parameters.clear();
					parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
					AntiRetroViralRule antiRetroViralRule = new AntiRetroViralRule();
					Result antiRetroViralResults = antiRetroViralRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(antiRetroViralResults))
						medicationResults.addAll(antiRetroViralResults);

					parameters.clear();
					parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
					CryptococcalRule cryptococcalRule = new CryptococcalRule();
					Result cryptococcalResults = cryptococcalRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(cryptococcalResults))
						medicationResults.addAll(cryptococcalResults);

					parameters.clear();
					parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
					PneumocystisCariniiRule pneumocystisCariniiRule = new PneumocystisCariniiRule();
					Result pneumocystisCariniiResults = pneumocystisCariniiRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(pneumocystisCariniiResults))
						medicationResults.addAll(pneumocystisCariniiResults);

					parameters.clear();
					parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
					TuberculosisRule tuberculosisRule = new TuberculosisRule();
					Result tuberculosisResults = tuberculosisRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(tuberculosisResults))
						medicationResults.addAll(tuberculosisResults);

					parameters.clear();
					parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
					TuberculosisTreatmentRule tuberculosisTreatmentRule = new TuberculosisTreatmentRule();
					Result tuberculosisTreatmentResults = tuberculosisTreatmentRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(tuberculosisTreatmentResults))
						medicationResults.addAll(tuberculosisTreatmentResults);

					perEncounterResult.add(medicationResults);
				}

				if (OpenmrsUtil.nullSafeEquals(encounterType, mosoriotPerinatalEncounterType)) {

					Result problemResults = new Result();

					parameters.clear();
					parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
					EncounterBasedProblemAddedRule problemAddedRule = new EncounterBasedProblemAddedRule();
					Result problemAddedResults = problemAddedRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(problemAddedResults))
						problemResults.addAll(problemAddedResults);

					parameters.clear();
					parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
					EncounterBasedProblemReportedRule problemReportedRule = new EncounterBasedProblemReportedRule();
					Result problemReportedResults = problemReportedRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(problemReportedResults))
						problemResults.addAll(problemReportedResults);

					parameters.clear();
					parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
					EncounterBasedPositiveHivRule positiveHivRule = new EncounterBasedPositiveHivRule();
					Result positiveHivResults = positiveHivRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(positiveHivResults))
						problemResults.addAll(positiveHivResults);

					perEncounterResult.add(problemResults);

					Result medicationResults = new Result();

					EncounterBasedObsCollectionRule obsCollectionRule = new EncounterBasedObsCollectionRule();

					parameters.clear();
					parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
					parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.MEDICATION_ADDED));
					Result medicationAddedResults = obsCollectionRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(medicationAddedResults))
						medicationResults.addAll(medicationAddedResults);

					parameters.clear();
					parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
					parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.RECIEVED_ANTENATAL_CARE_SERVICE_THIS_VISIT));
					Result careVisitResults = obsCollectionRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(careVisitResults))
						medicationResults.addAll(careVisitResults);
					parameters.remove(EvaluableConstants.OBS_CONCEPT);

					parameters.clear();
					parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
					EncounterBasedTetanusBoosterRule tetanusBoosterRule = new EncounterBasedTetanusBoosterRule();
					Result tetanusBoosterResults = tetanusBoosterRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(tetanusBoosterResults))
						medicationResults.addAll(tetanusBoosterResults);
					perEncounterResult.add(medicationResults);
				}

				if (OpenmrsUtil.nullSafeEquals(encounterType, mosoriotPrimaryCareEncounterType)) {

					Result problemResults = new Result();

					parameters.clear();
					parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
					EncounterBasedProblemAddedRule problemAddedRule = new EncounterBasedProblemAddedRule();
					Result problemAddedResults = problemAddedRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(problemAddedResults))
						problemResults.addAll(problemAddedResults);

					parameters.clear();
					parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
					EncounterBasedProblemReportedRule problemReportedRule = new EncounterBasedProblemReportedRule();
					Result problemReportedResults = problemReportedRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(problemReportedResults))
						problemResults.addAll(problemReportedResults);

					parameters.clear();
					parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
					EncounterBasedPositiveHivRule positiveHivRule = new EncounterBasedPositiveHivRule();
					Result positiveHivResults = positiveHivRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(positiveHivResults))
						problemResults.addAll(positiveHivResults);
					perEncounterResult.add(problemResults);

					Result medicationResults = new Result();

					EncounterBasedObsCollectionRule obsCollectionRule = new EncounterBasedObsCollectionRule();

					parameters.clear();
					parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
					parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.MEDICATION_ADDED));
					Result medicationAddedResults = obsCollectionRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(medicationAddedResults))
						medicationResults.addAll(medicationAddedResults);

					parameters.clear();
					parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
					parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.RECIEVED_ANTENATAL_CARE_SERVICE_THIS_VISIT));
					Result careVisitResults = obsCollectionRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(careVisitResults))
						medicationResults.addAll(careVisitResults);
					parameters.remove(EvaluableConstants.OBS_CONCEPT);

					parameters.clear();
					parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
					EncounterBasedImmunizationRule immunizationRule = new EncounterBasedImmunizationRule();
					Result immunizationResults = immunizationRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(immunizationResults))
						medicationResults.addAll(immunizationResults);

					parameters.clear();
					parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
					EncounterBasedTetanusBoosterRule tetanusBoosterRule = new EncounterBasedTetanusBoosterRule();
					Result tetanusBoosterResults = tetanusBoosterRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(tetanusBoosterResults))
						medicationResults.addAll(tetanusBoosterResults);

					parameters.clear();
					parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(encounter));
					EncounterBasedFamilyPlanningRule familyPlanningRule = new EncounterBasedFamilyPlanningRule();
					Result familyPlanningResults = familyPlanningRule.eval(context, patientId, parameters);
					if (CollectionUtils.isNotEmpty(familyPlanningResults))
						medicationResults.addAll(familyPlanningResults);
					perEncounterResult.add(medicationResults);
				}

				result.add(perEncounterResult);
			}
		}

		return result;
	}

	/**
	 * Get the token name of the rule that can be used to reference the rule from LogicService
	 *
	 * @return the token name
	 */
	@Override
	protected String getEvaluableToken() {
		return TOKEN;
	}
}
