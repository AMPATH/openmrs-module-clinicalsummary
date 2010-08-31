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
package org.openmrs.module.clinicalsummary.concept;

import org.openmrs.module.clinicalsummary.encounter.TypeConstants;

/**
 * List of all concepts that needed by the adult and peads clinical summary
 */
public interface StandardConceptConstants {
	
	// Concept names for ARV
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String ANTIRETROVIRALS_STARTED = "ANTIRETROVIRALS STARTED";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String ANTIRETROVIRAL_PLAN = "ANTIRETROVIRAL PLAN";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String CURRENT_ANTIRETROVIRAL = "CURRENT ANTIRETROVIRAL DRUGS USED FOR TREATMENT";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String ANTIRETROVIRAL_DRUGS = "ANTIRETROVIRAL DRUGS";
	
	// Concept names for PCP
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String PCP_PROPHYLAXIS_STARTED = "PCP PROPHYLAXIS STARTED";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String PCP_PROPHYLAXIS_PLAN = "PCP PROPHYLAXIS PLAN";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String REPORTED_PCP_PROPHYLAXIS = "PATIENT REPORTED CURRENT PCP PROPHYLAXIS";
	
	@RegisterConcept
	String TRIMETHOPRIM_AND_SULFAMETHOXAZOLE = "TRIMETHOPRIM AND SULFAMETHOXAZOLE";
	
	@RegisterConcept
	String DAPSONE = "DAPSONE";
	
	// Concepts names for Crypto
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String CRYPTO_TREATMENT_PLAN = "CRYPTOCOCCAL TREATMENT PLAN";
	
	@RegisterConcept
	String FLUCONAZOLE = "FLUCONAZOLE";
	
	@RegisterConcept
	String AMPHOTERICIN = "AMPHOTERICIN B";
	
	@RegisterConcept
	String ITRACONAZOLE = "ITRACONAZOLE";
	
	// Concepts names for Tuberculosis
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String TUBERCULOSIS_PROPHYLAXIS_PLAN = "TUBERCULOSIS PROPHYLAXIS PLAN";
	
	@RegisterConcept
	String ISONIAZID = "ISONIAZID";
	
	@RegisterConcept
	String RIFAMPIN = "RIFAMPIN";
	
	@RegisterConcept
	String PYRAZINAMIDE = "PYRAZINAMIDE";
	
	// Concepts names for Tuberculosis treatment
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String TUBERCULOSIS_TREATMENT_STARTED = "TUBERCULOSIS TREATMENT STARTED";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String TUBERCULOSIS_TREATMENT_PLAN = "TUBERCULOSIS TREATMENT PLAN";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String CURRENT_TUBERCULOSIS_TREATMENT = "PATIENT REPORTED CURRENT TUBERCULOSIS TREATMENT";
	
	@RegisterConcept
	String STREPTOMYCIN = "TUBERCULOSIS DEFAULTER REGIMEN BY USING STREPTOMYCIN";
	
	@RegisterConcept
	String MULTIDRUG_RESISTANT_TUBERCULOSIS = "MULTIDRUG-RESISTANT TUBERCULOSIS REGIMEN";
	
	@RegisterConcept
	String TUBERCULOSIS_TREATMENT_DRUGS = "TUBERCULOSIS TREATMENT DRUGS";
	
	// Medications added
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String MEDICATION_ADDED = "MEDICATION ADDED";
	
	// Problem added and resolved
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String PROBLEM_ADDED = "PROBLEM ADDED";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String PROBLEM_RESOLVED = "PROBLEM RESOLVED";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String CLINICAL_SUMMARY_PROBLEMS = "PROBLEM LIST FOR CLINICAL SUMMARY";
	
	// Plan answers
	@RegisterConcept
	String START_DRUGS = "START DRUGS";
	
	@RegisterConcept
	String CHANGE_REGIMEN = "CHANGE REGIMEN";
	
	@RegisterConcept
	String CONTINUE_REGIMEN = "CONTINUE REGIMEN";
	
	@RegisterConcept
	String REFILLED = "REFILLED";
	
	@RegisterConcept
	String DOSING_CHANGE = "DOSING CHANGE";
	
	@RegisterConcept
	String NONE = "NONE";
	
	// Flowsheet
	@RegisterConcept
	@BufferObservations
	String TESTS_ORDERED = "TESTS ORDERED";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String RETURN_VISIT_DATE = "RETURN VISIT DATE";
	
	// WHO stage
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String CURRENT_WHO_STAGE = "CURRENT WHO HIV STAGE";
	
	@RegisterConcept
	String WHO_STAGE_1_ADULT = "WHO STAGE 1 ADULT";
	
	@RegisterConcept
	String WHO_STAGE_2_ADULT = "WHO STAGE 2 ADULT";
	
	@RegisterConcept
	String WHO_STAGE_3_ADULT = "WHO STAGE 3 ADULT";
	
	@RegisterConcept
	String WHO_STAGE_4_ADULT = "WHO STAGE 4 ADULT";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String PEDS_WHO_CATEGORY_QUERY = "PEDS WHO CATEGORY QUERY";
	
	@RegisterConcept
	String WHO_STAGE_1_PEDS = "WHO STAGE 1 PEDS";
	
	@RegisterConcept
	String WHO_STAGE_2_PEDS = "WHO STAGE 2 PEDS";
	
	@RegisterConcept
	String WHO_STAGE_3_PEDS = "WHO STAGE 3 PEDS";
	
	@RegisterConcept
	String WHO_STAGE_4_PEDS = "WHO STAGE 4 PEDS";
	
	// HIV Adherence
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String ARV_ADHERENCE = "ANTIRETROVIRAL ADHERENCE IN LAST 7 DAYS";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String ARV_WEEK_ADHERENCE = "ANTIRETROVIRAL ADHERENCE IN PAST WEEK";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String MONTH_DRUG_ADHERENCE = "OVERALL DRUG ADHERENCE IN LAST MONTH";
	
	@RegisterConcept
	String ADHERENCE_ALL = "ALL";
	
	@RegisterConcept
	String ADHERENCE_ARV_DRUGS = "ANTIRETROVIRAL DRUGS";
	
	@RegisterConcept
	@BufferObservations
	String WEIGHT = "WEIGHT (KG)";
	
	@RegisterConcept
	@BufferObservations
	String HEIGHT = "HEIGHT (CM)";
	
	@RegisterConcept
	@BufferObservations
	String CD4_PERCENT = "CD4 PERCENT";
	
	// CD4
	@RegisterConcept
	@BufferObservations
	String CD4_NAME = "CD4 COUNT";
	
	@RegisterConcept
	String CD4_PANEL_NAME = "CD4 PANEL";
	
	@RegisterConcept
	@BufferObservations
	String VIRAL_LOAD = "HIV VIRAL LOAD, QUANTITATIVE";
	
	// SGPT
	@RegisterConcept
	@BufferObservations
	String SGPT_NAME = "SERUM GLUTAMIC-PYRUVIC TRANSAMINASE";
	
	@RegisterConcept
	String CHEMISTRY_LAB_TESTS_NAME = "CHEMISTRY LAB TESTS";
	
	// HGB
	@RegisterConcept
	@BufferObservations
	String HGB_NAME = "HEMOGLOBIN";
	
	@RegisterConcept
	String COMPLETE_BLOOD_COUNT_NAME = "COMPLETE BLOOD COUNT";
	
	// Creatinine
	@RegisterConcept
	@BufferObservations
	String CREATININE_NAME = "SERUM CREATININE";
	
	@RegisterConcept
	String SERUM_ELECTROLYTES_NAME = "SERUM ELECTROLYTES";
	
	// CXR
	@RegisterConcept
	@BufferObservations
	String CXR_NAME = "X-RAY, CHEST";
	
	// Pediatric
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String ARV_SIDE_EFFECT_NAME = "REASONS FOR ANTIRETROVIRAL DRUG SIDE EFFECT SINCE LAST VISIT";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String ARV_PLAN_LAST_VISIT_NAME = "ANTIRETROVIRAL PLAN";
	
	@RegisterConcept
	String ELISA_NAME = "HIV ENZYME IMMUNOASSAY, QUALITATIVE";
	
	@RegisterConcept
	String DNA_PCR_NAME = "HIV DNA POLYMERASE CHAIN REACTION, QUALITATIVE";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String HIV_TEST_STATUS_NAME = "CHILDS CURRENT HIV STATUS";
	
	// Immunization concept names
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String IMMUNIZATION_HISTORY = "IMMUNIZATION HISTORY";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String PREVIOUS_IMMUNIZATIONS_NAME = "PREVIOUS IMMUNIZATIONS ADMINISTERED";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String PREVIOUS_DOSES_RECEIVED_NAME = "NUMBER OF DOSES RECEIVED BEFORE ENROLLMENT";
	
	@RegisterConcept
	String COMPLETED_IMMUNIZATIONS_NAME = "COMPLETED";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String IMMUNIZATIONS_ORDERED_DETAILED = "IMMUNIZATIONS ORDERED, DETAILED";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String IMMUNIZATIONS_ORDERED_NAME = "IMMUNIZATIONS ORDERED";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String IMMUNIZATIONS_DOSES_NAME = "NUMBER OF DOSES";
	
	@RegisterConcept(substitute = "Hep A")
	String HEPATITIS_A_VACCINATION = "HEPATITIS A VACCINATION";
	
	@RegisterConcept(substitute = "Measles")
	String MEASLES_VACCINATION = "MEASLES VACCINATION";
	
	@RegisterConcept(substitute = "H.Flu B")
	String HEMOPHILUS_FLU_B_VACCINATION = "HEMOPHILUS INFLUENZA B VACCINATION";
	
	@RegisterConcept(substitute = "Yellow Fever")
	String YELLOW_FEVER_VACCINATION = "YELLOW FEVER VACCINATION";
	
	@RegisterConcept(substitute = "DPT")
	String DPT_VACCINATION = "DIPTHERIA TETANUS AND PERTUSSIS VACCINATION";
	
	@RegisterConcept(substitute = "Hep B")
	String HEPATITIS_B_VACCINATION = "HEPATITIS B VACCINATION";
	
	@RegisterConcept(substitute = "Polio")
	String POLIO_VACCINATION = "POLIO VACCINATION";
	
	@RegisterConcept(substitute = "BCG")
	String BCG_VACCINATION = "BACILLE CAMILE-GUERIN VACCINATION";
	
	// PMTCT
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String MATERNAL_ARV_HISTORY_NAME = "MATERNAL PARTUM ANTIRETROVIRAL HISTORY";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String PARTUM_ARV_USED_NAME = "PARTUM ANTIRETROVIRAL USE";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String PARTUM_PERIOD_NAME = "PARTUM DOSING PERIOD";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String ARV_DOSE_NAME = "ANTIRETROVIRAL DOSE QUANTIFICATION";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String TREATMENT_WEEKS_NAME = "NUMBER OF WEEKS ON TREATMENT";
	
	// Dose quantification
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ADULT_INITIAL, TypeConstants.ADULT_RETURN, TypeConstants.ADULT_NONCLINICAL,
	        TypeConstants.PEDS_INITIAL, TypeConstants.PEDS_RETURN, TypeConstants.PEDS_NONCLINICAL })
	String TOTAL_MATERNAL_CHILD_PROPHYLAXIS_NAME = "TOTAL MATERNAL TO CHILD TRANSMISSION PROPHYLAXIS";
	
	@RegisterConcept
	String ONE_DOSE_NAME = "ONE DOSE";
	
	@RegisterConcept
	String TWO_DOSES_NAME = "TWO DOSES";
	
	@RegisterConcept
	String MORE_THAN_TWO_DOSES_NAME = "MORE THAN TWO DOSES";
	
	@RegisterConcept
	String MORE_THAN_OR_EQUAL_TO_TWO_DOSES_NAME = "MORE THAN OR EQUAL TO TWO DOSES";
	
}
