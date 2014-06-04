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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableConstants;
import org.openmrs.module.clinicalsummary.rule.EvaluableNameConstants;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithRestrictionRule;
import org.openmrs.module.clinicalsummary.rule.encounter.EncounterWithStringRestrictionRule;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TODO: Write brief description about the class here.
 */
public class TuberculosisReminderStudyRule extends AbstractPostProcessorRule {

    private static final Log log = LogFactory.getLog(PediatricReminderStudyRule.class);

    public static final String TOKEN = "Tuberculosis Reminder Study";

    private static final Set<Integer> TREATMENT_LOCATIONS =
            new HashSet<Integer>(
                    Arrays.asList(31, 20, 8, 64, 74, 75, 76, 3, 60, 2,98,12, 91, 106, 71, 4, 54, 62, 28, 65, 90, 126));


    /**
     * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
     */
    @Override
    protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) {

        String artifact = decodeArtifact(parameters.get(POST_EVALUATION_ARTIFACT));
        Result result = new Result(artifact);

        Boolean disableTextReminder = Boolean.TRUE;

        parameters.put(EvaluableConstants.ENCOUNTER_TYPE, Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_INITIAL));
        parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 1);

        EncounterWithRestrictionRule encounterWithRestrictionRule = new EncounterWithStringRestrictionRule();

        Result initialEncounterResults = encounterWithRestrictionRule.eval(context, patientId, parameters);
        if (CollectionUtils.isNotEmpty(initialEncounterResults)) {
            Result initialEncounterResult = initialEncounterResults.latest();
            Encounter initialEncounter = (Encounter) initialEncounterResult.getResultObject();
            Location initialEncounterLocation = initialEncounter.getLocation();
            if (TREATMENT_LOCATIONS.contains(initialEncounterLocation.getLocationId())) {

                parameters.put(EvaluableConstants.ENCOUNTER_TYPE, Arrays.asList(EvaluableNameConstants.ENCOUNTER_TYPE_ADULT_RETURN));
                parameters.put(EvaluableConstants.ENCOUNTER_FETCH_SIZE, 1);

                Result returnEncounterResults = encounterWithRestrictionRule.eval(context, patientId, parameters);
                if (CollectionUtils.isNotEmpty(returnEncounterResults)) {
                    Result returnEncounterResult = returnEncounterResults.latest();
                    Encounter returnEncounter = (Encounter) returnEncounterResult.getResultObject();
                    Location returnEncounterLocation = returnEncounter.getLocation();
                    if (TREATMENT_LOCATIONS.contains(returnEncounterLocation.getLocationId())) {
                        disableTextReminder = Boolean.FALSE;
                    }
                } else {
                    disableTextReminder = Boolean.FALSE;
                }
            }
        }

        try {
            if (disableTextReminder) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                InputSource is = new InputSource(new StringReader(artifact));
                Document document = db.parse(is);

                Element element = document.getDocumentElement();
                NodeList nodeList = element.getElementsByTagName("tb-reminders");
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

    /**
     * TODO: need some more design thought to make this abstract inline with the EvaluableRule
     * Determine whether the rule should get executed or not
     *
     * @param parameters
     * @return
     */
    @Override
    protected Boolean applicable(final Map<String, Object> parameters) {
        return true;
    }
}
