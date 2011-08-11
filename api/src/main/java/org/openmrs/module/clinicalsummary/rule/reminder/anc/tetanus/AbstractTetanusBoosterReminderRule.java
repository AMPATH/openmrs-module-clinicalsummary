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

package org.openmrs.module.clinicalsummary.rule.reminder.anc.tetanus;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.cache.CacheUtils;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.reminder.ReminderParameters;
import org.openmrs.util.OpenmrsUtil;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;

public abstract class AbstractTetanusBoosterReminderRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(AbstractTetanusBoosterReminderRule.class);

	public static final String FIRST_TETANUS_BOOSTER_REMINDER = "First Tetanus Booster Reminder";

	public static final String SECOND_TETANUS_BOOSTER_REMINDER = "Second Tetanus Booster Reminder";

	public static final String OTHER_TETANUS_BOOSTER_REMINDER = "Other Tetanus Booster Reminder";

	public static final String RECORD_TETANUS_BOOSTER_REMINDER = "Record Tetanus Booster Reminder";

	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		TetanusBoosterReminderCacheInstance cacheInstance = TetanusBoosterReminderCacheInstance.getInstance();

		Patient patient = Context.getPatientService().getPatient(patientId);
		if (!OpenmrsUtil.nullSafeEquals(cacheInstance.getPatient(), patient)) {

			parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 1);
			EncounterWithRestrictionRule encounterWithRestrictionRule = new EncounterWithStringRestrictionRule();
			Result encounterResults = encounterWithRestrictionRule.eval(context, patientId, parameters);
			if (CollectionUtils.isNotEmpty(encounterResults)) {
				Result latestResult = encounterResults.latest();

				if (log.isDebugEnabled())
					log.debug("Patient: " + patient + ", latest encounter id: " + latestResult);

				ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();

				parameters.put(EvaluableConstants.OBS_ENCOUNTER, Arrays.asList(latestResult.getResultObject()));
				parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);

				parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.TETANUS_BOOSTER_COMPLETED));
				parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.YES));
				Result tetanusBoosterCompletedResults = obsWithRestrictionRule.eval(context, patientId, parameters);
				if (CollectionUtils.isEmpty(tetanusBoosterCompletedResults)) {
					parameters.put(EvaluableConstants.OBS_VALUE_CODED, Arrays.asList(EvaluableNameConstants.TETANUS_BOOSTER));
					parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.RECIEVED_ANTENATAL_CARE_SERVICE_THIS_VISIT));
					Result tetanusBoosterReceivedResults = obsWithRestrictionRule.eval(context, patientId, parameters);
					parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.DID_NOT_RECIEVE_ANTENATAL_CARE_SERVICE_THIS_VISIT));
					Result noTetanusBoosterReceivedResults = obsWithRestrictionRule.eval(context, patientId, parameters);
					parameters.remove(EvaluableConstants.OBS_VALUE_CODED);
					if (CollectionUtils.isEmpty(tetanusBoosterReceivedResults) && CollectionUtils.isEmpty(noTetanusBoosterReceivedResults)) {
						cacheInstance.addReminderCache(RECORD_TETANUS_BOOSTER_REMINDER, Boolean.TRUE);
					} else {
						parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.TETANUS_BOOSTER_DETAILED));
						Result tetanusBoosterDetailedResults = obsWithRestrictionRule.eval(context, patientId, parameters);
						// default is display reminder until we find quantity
						if (CollectionUtils.isNotEmpty(tetanusBoosterDetailedResults)) {
							Concept quantityConcept = CacheUtils.getConcept(EvaluableNameConstants.QUANTITY);
							Result tetanusBoosterDetailedResult = tetanusBoosterDetailedResults.latest();
							Obs tetanusBoosterDetailedObs = (Obs) tetanusBoosterDetailedResult.getResultObject();
							Set<Obs> tetanusBoosterDetailedMembers = tetanusBoosterDetailedObs.getGroupMembers();
							for (Obs tetanusBoosterDetailedMember : tetanusBoosterDetailedMembers) {
								Concept tetanusBoosterDetailedMemberConcept = tetanusBoosterDetailedMember.getConcept();
								if (OpenmrsUtil.nullSafeEquals(tetanusBoosterDetailedMemberConcept, quantityConcept)) {
									if (tetanusBoosterDetailedMember.getValueNumeric() == null)
										cacheInstance.addReminderCache(FIRST_TETANUS_BOOSTER_REMINDER, Boolean.TRUE);
									else if (tetanusBoosterDetailedMember.getValueNumeric() == 1) {
										Calendar lastMonth = Calendar.getInstance();
										lastMonth.add(Calendar.MONTH, -1);
										if (tetanusBoosterDetailedMember.getObsDatetime().before(lastMonth.getTime()))
											cacheInstance.addReminderCache(SECOND_TETANUS_BOOSTER_REMINDER, Boolean.TRUE);
									} else if (tetanusBoosterDetailedMember.getValueNumeric() == 2) {
										Calendar sixMonths = Calendar.getInstance();
										sixMonths.add(Calendar.MONTH, -6);
										if (tetanusBoosterDetailedMember.getObsDatetime().before(sixMonths.getTime()))
											cacheInstance.addReminderCache(OTHER_TETANUS_BOOSTER_REMINDER, Boolean.TRUE);
									}
								}
							}
						}
					}
				}
			}
		}

		return processCachedReminder(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT)));
	}

	protected abstract Result processCachedReminder(String displayedReminderText);

	/**
	 * @see org.openmrs.logic.Rule#getDependencies()
	 */
	@Override
	public String[] getDependencies() {
		return new String[]{ObsWithStringRestrictionRule.TOKEN};
	}
}
