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

package org.openmrs.module.clinicalsummary.rule.post;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.Mapping;
import org.openmrs.module.clinicalsummary.MappingType;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.service.SummaryService;
import org.openmrs.util.OpenmrsUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 */
public class PediatricReminderStudyRule extends AbstractPostProcessorRule {

	private static final Log log = LogFactory.getLog(PediatricReminderStudyRule.class);

	public static final String TOKEN = "Pediatric Reminder Study";

	private static final String RANDOM_NUMBER_PROPERTY = "clinicalsummary.pediatric.study.randomizer";


	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) {

		String artifact = decodeArtifact(parameters.get(POST_EVALUATION_ARTIFACT));
		Result result = new Result(artifact);

		List<String> encounterTypes = Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_INITIAL,
				EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_RETURN, EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_NONCLINICALMEDICATION);

		parameters.put(EvaluableConstants.ENCOUNTER_TYPE, encounterTypes);
		parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 1);

		EncounterWithRestrictionRule encounterWithRestrictionRule = new EncounterWithStringRestrictionRule();
		Result encounterResults = encounterWithRestrictionRule.eval(context, patientId, parameters);
		if (CollectionUtils.isNotEmpty(encounterResults)) {
			Result encounterResult = encounterResults.latest();
			Encounter encounter = (Encounter) encounterResult.getResultObject();
			Location studyLocation = Context.getLocationService().getLocation(EvaluableNameConstants.PEDIATRIC_STUDY_LOCATION);
			if (OpenmrsUtil.nullSafeEquals(encounter.getLocation(), studyLocation)) {
				// create the study concept when they're not in the system yet
				Concept interventionConcept = createConceptIfNeeded(EvaluableNameConstants.PEDIATRIC_STUDY_INTERVENTION_GROUP);
				Concept controlConcept = createConceptIfNeeded(EvaluableNameConstants.PEDIATRIC_STUDY_CONTROL_GROUP);

				// the patient is coming from module 4
				parameters.put(EvaluableConstants.OBS_CONCEPT, Arrays.asList(EvaluableNameConstants.PEDIATRIC_STUDY_INTERVENTION_GROUP, EvaluableNameConstants.PEDIATRIC_STUDY_CONTROL_GROUP));
				parameters.put(EvaluableConstants.OBS_FETCH_SIZE, 1);

				ObsWithRestrictionRule obsWithRestrictionRule = new ObsWithStringRestrictionRule();
				Result obsResults = obsWithRestrictionRule.eval(context, patientId, parameters);

				Boolean disableTextReminder = Boolean.FALSE;
				if (CollectionUtils.isEmpty(obsResults)) {
					Obs obs = new Obs();
					obs.setObsDatetime(new Date());
					obs.setPerson(Context.getPatientService().getPatient(patientId));
					obs.setConcept(interventionConcept);
					if (StudyRandomizer.getRandomizedValue(RANDOM_NUMBER_PROPERTY) == 0) {
						obs.setConcept(controlConcept);
						disableTextReminder = Boolean.TRUE;
					}
					obs.setLocation(studyLocation);
					Context.getObsService().saveObs(obs, "Randomizing patient that's not in the study yet.");
				} else {
					Result obsResult = obsResults.latest();
					Obs obs = (Obs) obsResult.getResultObject();
					if (OpenmrsUtil.nullSafeEquals(obs.getConcept(), controlConcept))
						disableTextReminder = Boolean.TRUE;
				}

				try {
					if (disableTextReminder) {
						DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
						DocumentBuilder db = dbf.newDocumentBuilder();
						InputSource is = new InputSource(new StringReader(artifact));
						Document document = db.parse(is);

						Element element = document.getDocumentElement();
						NodeList nodeList = element.getElementsByTagName("reminders");
						if (nodeList != null && nodeList.getLength() > 0) {
							for (int i = 0; i < nodeList.getLength(); i++) {
								Element nodeElement = (Element) nodeList.item(i);
								nodeElement.setAttribute("displayText", String.valueOf(Boolean.FALSE));
							}
						}

						OutputFormat format = new OutputFormat();
						format.setIndenting(true);
						format.setLineWidth(150);

						Writer writer = new StringWriter();
						XMLSerializer xmlSerializer = new XMLSerializer(writer, format);
						xmlSerializer.serialize(document);
						result = new Result(writer.toString());
					}
				} catch (Exception e) {
					log.error("Failed parsing xml string ...", e);
				}
			}
		}

		return result;
	}

	/**
	 * @param name
	 * @return
	 */
	private Concept createConceptIfNeeded(String name) {
		Concept existingConcept = Context.getConceptService().getConcept(name);
		if (existingConcept == null) {

			Concept concept = new Concept();
			concept.setConceptClass(Context.getConceptService().getConceptClassByName("Misc"));
			concept.setDatatype(Context.getConceptService().getConceptDatatypeByName("N/A"));

			ConceptName conceptName = new ConceptName(name, Context.getLocale());
			conceptName.addTag(new ConceptNameTag("preferred_en", "Preferred name in a english"));
			concept.addName(conceptName);

			existingConcept = Context.getConceptService().saveConcept(concept);
		}
		return existingConcept;
	}

	/**
	 * @see org.openmrs.logic.Rule#getDependencies()
	 */
	@Override
	public String[] getDependencies() {
		return new String[]{ObsWithStringRestrictionRule.TOKEN, EncounterWithStringRestrictionRule.TOKEN};
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

	/**
	 * TODO: need some more design thought to make this abstract inline with the EvaluableRule
	 * Determine whether the rule should get executed or not
	 *
	 * @param parameters
	 * @return
	 */
	@Override
	protected Boolean applicable(final Map<String, Object> parameters) {
		String summaryName = String.valueOf(parameters.get(POST_EVALUATION_TEMPLATE));

		SummaryService summaryService = Context.getService(SummaryService.class);

		Summary evaluatedSummary = null;
		List<Summary> summaries = summaryService.getAllSummaries();
		for (Summary summary : summaries) {
			if (StringUtils.equalsIgnoreCase(summaryName, summary.getName()))
				evaluatedSummary = summary;
		}

		if (evaluatedSummary != null) {
			List<Mapping> mappings = summaryService.getMappings(evaluatedSummary, null, null);
			for (Mapping mapping : mappings) {
				if (OpenmrsUtil.nullSafeEquals(mapping.getMappingType(), MappingType.LATEST_ENCOUNTER)) {
					EncounterType encounterType = mapping.getEncounterType();
					if (StringUtils.equalsIgnoreCase(encounterType.getName(), EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_INITIAL)
							|| StringUtils.equalsIgnoreCase(encounterType.getName(), EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_RETURN)
							|| StringUtils.equalsIgnoreCase(encounterType.getName(), EvaluableNameConstants.ENCOUNTER_TYPE_PEDIATRIC_NONCLINICALMEDICATION))
						return Boolean.TRUE;
				}
			}
		}

		return Boolean.FALSE;
	}
}
