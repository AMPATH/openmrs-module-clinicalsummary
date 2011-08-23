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

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.Mapping;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.enumeration.MappingType;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.observation.ObsWithStringRestrictionRule;
import org.openmrs.module.clinicalsummary.service.SummaryService;
import org.openmrs.util.OpenmrsUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class AdultReminderStudyRule extends AbstractPostProcessorRule {

	private static final Log log = LogFactory.getLog(AdultReminderStudyRule.class);

	private static final Integer MAXIMUM_RANDOM_VALUE = 10000;

	private static final Integer MAXIMUM_REMINDER_DISPLAYED = 5;

	public static final String TOKEN = "Adult Reminder Study";

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) {
		String artifact = decodeArtifact(parameters.get(POST_EVALUATION_ARTIFACT));
		Result result = new Result(artifact);

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(artifact));
			Document document = db.parse(is);

			Element element = document.getDocumentElement();
			NodeList nodeList = element.getElementsByTagName("reminder");
			if (nodeList != null && nodeList.getLength() > MAXIMUM_REMINDER_DISPLAYED) {
				Random random = new Random();
				while (nodeList.getLength() > MAXIMUM_REMINDER_DISPLAYED) {
					Integer randomizedValue = random.nextInt(MAXIMUM_RANDOM_VALUE);
					Integer moduloValue = randomizedValue % nodeList.getLength();
					// get the element
					Element nodeElement = (Element) nodeList.item(moduloValue);
					// get the parent node and then remove the above node
					Node parentNode = nodeElement.getParentNode();
					parentNode.removeChild(nodeElement);
				}
			}

			OutputFormat format = new OutputFormat();
			format.setIndenting(true);
			format.setLineWidth(150);

			Writer writer = new StringWriter();
			XMLSerializer xmlSerializer = new XMLSerializer(writer, format);
			xmlSerializer.serialize(document);
			result = new Result(writer.toString());
		} catch (Exception e) {
			log.error("Failed parsing xml string ...", e);
		}

		return result;
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
					if (StringUtils.equalsIgnoreCase(encounterType.getName(), EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_INITIAL)
							|| StringUtils.equalsIgnoreCase(encounterType.getName(), EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_RETURN)
							|| StringUtils.equalsIgnoreCase(encounterType.getName(), EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_NONCLINICALMEDICATION))
						return Boolean.TRUE;
				}
			}
		}

		return Boolean.FALSE;
	}
}
