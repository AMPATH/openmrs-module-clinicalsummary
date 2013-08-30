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
package org.openmrs.module.clinicalsummary.evaluator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * TODO: Write brief description about the class here.
 */
public class LoggerUtils {

    public static final String VIEWING_LOG_FILE = "viewing_summary.log";

    public static final String GENERATING_LOG_FILE = "generating_summary.log";

    public static File getViewingLogFile() {
        File outputDirectory = OpenmrsUtil.getDirectoryInApplicationDataDirectory("clinicalsummary/logger");
        return new File(outputDirectory, LoggerUtils.VIEWING_LOG_FILE);
    }

    public static File getGeneratingLogFile() {
        File outputDirectory = OpenmrsUtil.getDirectoryInApplicationDataDirectory("clinicalsummary/logger");
        return new File(outputDirectory, LoggerUtils.GENERATING_LOG_FILE);
    }

    public static void extractLogInformation(final Document document, final File file) throws IOException {

        Element element = document.getDocumentElement();

        String identifier = StringUtils.EMPTY;
        List<String> identifiers = extractNodeValues(element, "identifier");
        if (!CollectionUtils.isEmpty(identifiers)) {
            identifier = identifiers.get(0);
        }
        String id = StringUtils.EMPTY;
        List<String> internalIds = extractNodeValues(element, "id");
        if (!CollectionUtils.isEmpty(internalIds)) {
            id = internalIds.get(0);
        }
        String generationDate = StringUtils.EMPTY;
        List<String> generationDates = extractNodeValues(element, "currentDatetime");
        if (!CollectionUtils.isEmpty(generationDates)) {
            generationDate = generationDates.get(0);
        }
        String currentDatetime = Context.getDateFormat().format(new Date());
        String requestedBy = String.valueOf(Context.getAuthenticatedUser());
        List<String> reminders = extractNodeValues(element, "reminder");
        List<String> tbReminders = extractNodeValues(element, "tb-reminder");

        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append(identifier).append(",");
        logBuilder.append(id).append(",");
        logBuilder.append(generationDate).append(",");
        logBuilder.append(currentDatetime).append(",");
        logBuilder.append(requestedBy).append(",");
        logBuilder.append(reminders.size());
        if (!CollectionUtils.isEmpty(tbReminders)) {
            logBuilder.append(StringUtils.join(tbReminders, ","));
        }

        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(file, true));
        outputWriter.write(logBuilder.toString());
        outputWriter.write(System.getProperty("line.separator"));
        outputWriter.close();
    }

    private static List<String> extractNodeValues(final Element parentElement, final String tag) {
        List<String> values = new ArrayList<String>();
        NodeList nodeList = parentElement.getElementsByTagName(tag);
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element nodeElement = (Element) nodeList.item(i);
                if (StringUtils.contains(tag, "reminder")) {
                    NodeList reminderNodeList = nodeElement.getElementsByTagName("token");
                    String reminderToken = StringUtils.EMPTY;
                    if (reminderNodeList != null) {
                        Element reminderElement = (Element) reminderNodeList.item(0);
                        reminderToken = reminderElement.getTextContent();
                    }
                    values.add(reminderToken);
                } else {
                    values.add(nodeElement.getTextContent());
                }
            }
        }
        return values;
    }
}
