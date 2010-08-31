/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you mayt use this file except in
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
package org.openmrs.module.clinicalsummary.engine;


import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicService;
import org.openmrs.module.clinicalsummary.rule.ARVMedicationsRule;
import org.openmrs.module.clinicalsummary.rule.CXRResultRule;
import org.openmrs.module.clinicalsummary.rule.CompleteEncounterRule;
import org.openmrs.module.clinicalsummary.rule.CryptoMedicationsRule;
import org.openmrs.module.clinicalsummary.rule.DatetimeLatestSingleObsRule;
import org.openmrs.module.clinicalsummary.rule.HIVRxAdherenceRule;
import org.openmrs.module.clinicalsummary.rule.NumericFlowsheetRule;
import org.openmrs.module.clinicalsummary.rule.PCPMedicationsRule;
import org.openmrs.module.clinicalsummary.rule.ProblemAddedRule;
import org.openmrs.module.clinicalsummary.rule.TBMedicationsRule;
import org.openmrs.module.clinicalsummary.rule.TBTreatmentsRule;
import org.openmrs.module.clinicalsummary.rule.TestOrderedRule;
import org.openmrs.module.clinicalsummary.rule.adult.AdultWHOStageRule;
import org.openmrs.module.clinicalsummary.rule.anc.HistoricalMultiCodedRule;
import org.openmrs.module.clinicalsummary.rule.anc.HistoricalMultiConceptRule;
import org.openmrs.module.clinicalsummary.rule.anc.HistoricalSingleCodedRule;
import org.openmrs.module.clinicalsummary.rule.anc.InitialMultiCodedRule;
import org.openmrs.module.clinicalsummary.rule.anc.InitialMultiConceptRule;
import org.openmrs.module.clinicalsummary.rule.anc.InitialSingleCodedRule;
import org.openmrs.module.clinicalsummary.rule.anc.InitialSingleDateTimeRule;
import org.openmrs.module.clinicalsummary.rule.anc.InitialSingleNumericRule;
import org.openmrs.module.clinicalsummary.rule.anc.LatestMultiCodedRule;
import org.openmrs.module.clinicalsummary.rule.anc.LatestSingleCodedRule;
import org.openmrs.module.clinicalsummary.rule.anc.LatestSingleDateTimeRule;
import org.openmrs.module.clinicalsummary.rule.anc.LatestSingleNumericRule;
import org.openmrs.module.clinicalsummary.rule.peds.ARVSideEffectRule;
import org.openmrs.module.clinicalsummary.rule.peds.AgeCompleteRule;
import org.openmrs.module.clinicalsummary.rule.peds.DNAPCRRule;
import org.openmrs.module.clinicalsummary.rule.peds.ELISARule;
import org.openmrs.module.clinicalsummary.rule.peds.HIVTestStatusRule;
import org.openmrs.module.clinicalsummary.rule.peds.ImmunizationRecordRule;
import org.openmrs.module.clinicalsummary.rule.peds.LastVisitARVPlanRule;
import org.openmrs.module.clinicalsummary.rule.peds.PMTCTRule;
import org.openmrs.module.clinicalsummary.rule.peds.PedsWHOStageRule;
import org.openmrs.module.clinicalsummary.rule.reminder.BaseCD4ReminderRule;
import org.openmrs.module.clinicalsummary.rule.reminder.CXRReminderRule;
import org.openmrs.module.clinicalsummary.rule.reminder.CreatinineReminderRule;
import org.openmrs.module.clinicalsummary.rule.reminder.HGBReminderRule;
import org.openmrs.module.clinicalsummary.rule.reminder.SGPTReminderRule;
import org.openmrs.module.clinicalsummary.rule.reminder.WHOStageReminderRule;

/**
 *
 */
public class GeneratorUtilities {
	
	private GeneratorUtilities() {
	}
	
	public static void registerDefaultRules() {
		LogicService service = Context.getLogicService();
		service.addRule("ARV Medications", new ARVMedicationsRule());
		service.addRule("Crypto Medications", new CryptoMedicationsRule());
		service.addRule("CXR", new CXRResultRule());
		service.addRule("HIV Adherence", new HIVRxAdherenceRule());
		service.addRule("PCP Medications", new PCPMedicationsRule());
		service.addRule("Problems", new ProblemAddedRule());
		service.addRule("TB Medications", new TBMedicationsRule());
		service.addRule("TB Treatments", new TBTreatmentsRule());
		service.addRule("Test Ordered", new TestOrderedRule());
		service.addRule("Adult WHO Stage", new AdultWHOStageRule());
		
		service.addRule("Creatinine Reminder", new CreatinineReminderRule());
		service.addRule("CXR Reminder", new CXRReminderRule());
		service.addRule("Hemoglobin Reminder", new HGBReminderRule());
		service.addRule("SGPT Reminder", new SGPTReminderRule());
		service.addRule("Base CD4 Reminder", new BaseCD4ReminderRule());
		service.addRule("WHO Reminder", new WHOStageReminderRule());
		
		service.addRule("Numeric Flowsheet", new NumericFlowsheetRule());
		
		service.addRule("Age Complete", new AgeCompleteRule());
		service.addRule("ARV Side Effect", new ARVSideEffectRule());
		service.addRule("DNA PCR", new DNAPCRRule());
		service.addRule("Elisa", new ELISARule());
		service.addRule("HIV Test Status", new HIVTestStatusRule());
		service.addRule("Last Visit ARV Plan", new LastVisitARVPlanRule());
		service.addRule("Immunization", new ImmunizationRecordRule());
		service.addRule("PMTCT", new PMTCTRule());
		service.addRule("Peds WHO Stage", new PedsWHOStageRule());
		
		service.addRule("Initial Single Coded", new InitialSingleCodedRule());
		service.addRule("Initial Single Datetime", new InitialSingleDateTimeRule());
		service.addRule("Initial Single Numeric", new InitialSingleNumericRule());
		service.addRule("Initial Multi Coded", new InitialMultiCodedRule());
		
		service.addRule("Latest Single Coded", new LatestSingleCodedRule());
		service.addRule("Latest Single Datetime", new LatestSingleDateTimeRule());
		service.addRule("Latest Single Numeric", new LatestSingleNumericRule());
		service.addRule("Latest Multi Coded", new LatestMultiCodedRule());
		
		service.addRule("Initial Multi Concept", new InitialMultiConceptRule());
		service.addRule("Initial Multi Coded", new InitialMultiCodedRule());
		service.addRule("Historical Multi Concept", new HistoricalMultiConceptRule());
		service.addRule("Historical Multi Coded", new HistoricalMultiCodedRule());
		service.addRule("Historical Single Coded", new HistoricalSingleCodedRule());

		service.addRule("Datetime Latest Obs", new DatetimeLatestSingleObsRule());
		service.addRule("Complete Encounter", new CompleteEncounterRule());
	}
}
