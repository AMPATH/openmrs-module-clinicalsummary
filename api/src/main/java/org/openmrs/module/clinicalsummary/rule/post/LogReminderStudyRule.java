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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.evaluator.LoggerUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * TODO: Write brief description about the class here.
 */
public class LogReminderStudyRule extends AbstractPostProcessorRule {

    private final Log log = LogFactory.getLog(LogReminderStudyRule.class);

    public static final String TOKEN = "Log Reminder Study";

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

    /**
     * @param context
     * @param patientId
     * @param parameters
     * @return
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

            LoggerUtils.extractLogInformation(document, LoggerUtils.getGeneratingLogFile());

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
     * Get the token name of the rule that can be used to reference the rule from LogicService
     *
     * @return the token name
     */
    @Override
    protected String getEvaluableToken() {
        return TOKEN;
    }
}
