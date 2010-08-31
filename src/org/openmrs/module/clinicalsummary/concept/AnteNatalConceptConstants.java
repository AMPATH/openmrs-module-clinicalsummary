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
 * List of all concept name needed by the Ante Natal Care (ANC) summary template
 */
public interface AnteNatalConceptConstants {
	
	/* *****
	 * Single coded
	 * *****
	 */
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String MEDICATION_ADDED = "MEDICATION ADDED";
	
	@RegisterConcept
	String IRON_SUPPLEMENT = "IRON SUPPLEMENT";
	
	@RegisterConcept
	String COTRIMAZOLE = "COTRIMAZOLE";
	
	@RegisterConcept
	String FOLIC_ACID = "FOLIC ACID";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String CIVIL_STATUS = "CIVIL STATUS";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String OCCUPATION = "OCCUPATION";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String HIGHEST_EDUCATION = "HIGHEST EDUCATION";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String EMERGENCY_TRANSPORTATION_PLAN = "EMERGENCY TRANSPORTATION PLAN";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String OBSTETRIC_ULTRASOUND = "OBSTETRIC ULTRASOUND DONE";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String HISTORY_MENTAL_PROBLEM = "HISTORY OF POSTPARTUM DEPRESSION OR MENTAL HEALTH PROBLEM";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String ANTI_D_RH_IGG = "ANTI-D RH IGG GIVEN AT 28 GESTATION WEEKS ";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String PARTNER_OCCUPATION = "PARTNER'S OCCUPATION";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String HEALTH_INSURANCE = "HEALTH INSURANCE";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String DELIVERY_PLACE_BIRTH_PLAN = "DELIVERY PLACE, BIRTH PLAN";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String MOTHER_DELIVERY_PLACE_BIRTH_PLAN = "MOTHER'S PREFERRED DELIVERY PLACE, BIRTH PLAN";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String REASON_NOT_DELIVERING_AT_HEALTH_FACILITY = "REASON FOR NOT DELIVERING AT HEALTH FACILITY";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String PREFERRED_MODE_OF_DELIVERY = "PREFERRED MODE OF DELIVERY";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String MOTHER_FEEDING_BIRTH_PLAN = "MOTHER'S PREFERRED FEEDING METHOD, BIRTH PLAN";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String REFERRALS = "REFERRALS ORDERED";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String HIV_TESTED_THIS_VISIT = "HIV TESTED THIS VISIT";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String HIV_RAPID_TEST_QUALITATIVE = "HIV RAPID TEST, QUALITATIVE";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String TETANUS_BOOSTER_COMPLETED = "TETANUS BOOSTER COMPLETED";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String TETANUS_BOOSTER_DETAILED = "TETANUS BOOSTER, DETAILED";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String TETANUS_BOOSTER = "TETANUS BOOSTER";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String QUANTITY = "QUANTITY";
	
	/* ****
	 * Numeric concept
	 * ****
	 */
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String GRAVIDA = "GRAVIDA";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String PARITY = "PARITY";
	
	// historical single numeric
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String TEMPERATURE = "TEMPERATURE (C)";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String PULSE = "PULSE";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String SYSTOLIC_BLOOD_PRESSURE = "SYSTOLIC BLOOD PRESSURE";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String DIASTOLIC_BLOOD_PRESSURE = "DIASTOLIC BLOOD PRESSURE";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String WEIGHT = "WEIGHT (KG)";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String HEIGHT = "HEIGHT (CM)";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String FUNDAL_HEIGHT = "FUNDAL HEIGHT";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String FOETAL_HEART_RATE = "FOETAL HEART RATE";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String FOETAL_PRESENTATION = "FOETAL PRESENTATION";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String CARDIAC_EXAMS = "CARDIAC EXAM FINDINGS";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String BREAST_EXAMS = "BREAST EXAM FINDINGS";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String GENERAL_EXAMS = "GENERAL EXAM FINDINGS";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String UROGENITAL_EXAMS = "UROGENITAL EXAM FINDINGS";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String HEAD_NECK = "HEAD OR NECK EXAM FINDINGS";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String RESPIRATORY_EXAMS = "RESPIRATORY EXAM FINDINGS";
	
	/* ****
	 * Datetime concept
	 * ****
	 */
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String RETURN_VISIT_DATE = "RETURN VISIT DATE";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String LAST_MENSTRUAL_PERIOD_DATE = "LAST MENSTRUAL PERIOD DATE";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String ESTIMATED_DATE_OF_CONFINEMENT = "ESTIMATED DATE OF CONFINEMENT";
	
	/* ****
	 * Multi coded with no exclusion inclusion
	 * ****
	 */
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String METHOD_OF_FAMILY_PLANNING = "METHOD OF FAMILY PLANNING";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String PRESENCE_OF_SUBSTANCE_USE = "PRESENCE OF SUBSTANCE USE";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String PATIENT_EDUCATION_RECEIVED_THIS_VISIT = "PATIENT EDUCATION RECEIVED THIS VISIT";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String FOETAL_MOVEMENT = "FOETAL MOVEMENT";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String REVIEW_OF_SYSTEMS_OBSTETRICAL = "REVIEW OF SYSTEMS, OBSTETRICAL";
	
	/* ****
	 * Concept with multiple coded answer with inclusion exclusion
	 * ****
	 */
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String RECIEVED_ANTENATAL_CARE_SERVICE_THIS_VISIT = "RECIEVED ANTENATAL CARE SERVICE THIS VISIT";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String REVIEW_OF_MEDICAL_HISTORY = "REVIEW OF MEDICAL HISTORY";
	
	/* ****
	 * ConceptSet
	 * ****
	 */
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String TYPES_OF_BARRIER_TO_PROPER_HEALTH_CARE = "TYPES OF BARRIER TO PROPER HEALTH CARE";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String PAST_OBSTERICAL_HISTORY = "PAST OBSTERICAL HISTORY";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String DATE_OF_CONFINEMENT = "DATE OF CONFINEMENT";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String GESTATION_MONTH_OR_MISCARRIAGE = "GESTATION MONTH AT DELIVERY OR MISCARRIAGE";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String PREGNANCY_OUTCOME = "PREGNANCY OUTCOME";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String PLACE_OF_DELIVERY = "PLACE OF DELIVERY";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String LENGTH_OF_LABOR_IN_HOURS = "LENGTH OF LABOR IN HOURS";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String METHOD_OF_DELIVERY = "METHOD OF DELIVERY";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String GENDER_OF_THE_CHILD = "GENDER OF THE CHILD";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String BIRTH_WEIGHT = "BIRTH WEIGHT";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String MATERNAL_FETAL_OR_CHILD_COMPLICATIONS = "MATERNAL, FETAL, OR CHILD COMPLICATIONS, FREETEXT";
	
	// lab test
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String RHESUS_FACTOR = "RHESUS FACTOR";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String ANTIBODY_SCREEN = "ANTIBODY SCREEN";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String BLOOD_TYPING = "BLOOD TYPING";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String PRESENCE_OF_PROTEIN = "PRESENCE OF PROTEIN";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String PRESENCE_OF_LEUKOCYTES = "PRESENCE OF LEUKOCYTES";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String PRESENCE_OF_SUGAR = "PRESENCE OF SUGAR";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String HEMOGLOBIN = "HEMOGLOBIN";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String MALARIAL_SMEAR = "MALARIAL SMEAR";
	
	@RegisterConcept
	@BufferObservations(type = { TypeConstants.ANC_INITIAL, TypeConstants.ANC_RETURN })
	String SERUM_GLUCOSE = "SERUM GLUCOSE";
}
