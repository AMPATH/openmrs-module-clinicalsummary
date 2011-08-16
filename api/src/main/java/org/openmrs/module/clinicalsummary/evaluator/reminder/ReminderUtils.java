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

package org.openmrs.module.clinicalsummary.evaluator.reminder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.Mapping;
import org.openmrs.module.clinicalsummary.Reminder;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.service.EvaluatorService;
import org.openmrs.module.clinicalsummary.service.ReminderService;
import org.openmrs.module.clinicalsummary.service.SummaryService;

/**
 */
public class ReminderUtils {

	private static final Log log = LogFactory.getLog(ReminderUtils.class);

	private static final String REMINDER_TOKEN_MARKER = "Reminder";

	private final Summary summary;

	/**
	 * @param summary
	 */
	public ReminderUtils(Summary summary) {
		this.summary = summary;
	}

	/**
	 * This evaluation method are meant to be used inside velocity template. The method will return empty result when any exception happens during the
	 * execution of the rule.
	 *
	 * @see org.openmrs.module.clinicalsummary.service.EvaluatorService#evaluate(Patient, String, java.util.Map)
	 */
	public void evaluate(final Patient patient, final String token, final Map<String, Object> parameters) {
		try {
			if (StringUtils.endsWith(token, REMINDER_TOKEN_MARKER)) {
				EvaluatorService evaluatorService = Context.getService(EvaluatorService.class);
				Result result = evaluatorService.evaluate(patient, token, parameters);
				if (StringUtils.isNotEmpty(result.toString())) {
					Reminder reminder = new Reminder(new Date(), result.toString(), token);

					List<Mapping> mappings = Context.getService(SummaryService.class).getMappings(summary, null, null);
					Collection<String> encounterTypes = new ArrayList<String>();
					for (Mapping mapping : mappings) {
						EncounterType encounterType = mapping.getEncounterType();
						encounterTypes.add(encounterType.getName());
					}

					Map<String, Object> encounterParameters = new HashMap<String, Object>();
					parameters.put(EvaluableConstants.ENCOUNTER_TYPE, encounterTypes);
					parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 1);

					Result encounterResults = evaluatorService.evaluate(patient, EncounterWithStringRestrictionRule.TOKEN, encounterParameters);
					if (CollectionUtils.isNotEmpty(encounterResults)) {
						Result encounterResult = encounterResults.latest();
						Encounter encounter = (Encounter) encounterResult.getResultObject();
						reminder.setPatient(encounter.getPatient());
						reminder.setProvider(encounter.getProvider());
						reminder.setEncounter(encounter);
						reminder.setLocation(encounter.getLocation());
					}
					Context.getService(ReminderService.class).saveReminder(reminder);
				}
			}
		} catch (Exception e) {
			log.error("Evaluating token " + token + " on patient " + patient.getPatientId() + " failed ...", e);
		}
	}
}
