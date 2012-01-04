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

package org.openmrs.module.clinicalsummary.web.controller.utils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.OpenmrsObject;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.service.CoreService;
import org.openmrs.module.clinicalsummary.util.FetchRestriction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 */
@Controller
@RequestMapping("/module/clinicalsummary/utils/extendedDataGeneral")
public class ExtendedDataGeneralController {

    private static final Log log = LogFactory.getLog(ExtendedDataGeneralController.class);

    private static final String STUDY_DATA = "patientExtendedData";

    private static final String OUTPUT_PREFIX = "output";

    private static final String INPUT_PREFIX = "input";

    @RequestMapping(method = RequestMethod.GET)
    public void populatePage(final ModelMap map) {
    }

    @RequestMapping(method = RequestMethod.POST)
    public void processRequest(final @RequestParam(required = true, value = "data") MultipartFile data, HttpServletResponse response) throws IOException {

        PatientService patientService = Context.getPatientService();
        PatientSetService patientSetService = Context.getPatientSetService();

        File identifierData = File.createTempFile(STUDY_DATA, INPUT_PREFIX);
        OutputStream identifierDataStream = new BufferedOutputStream(new FileOutputStream(identifierData));
        FileCopyUtils.copy(data.getInputStream(), identifierDataStream);

        File extendedData = File.createTempFile(STUDY_DATA, OUTPUT_PREFIX);
        BufferedWriter writer = new BufferedWriter(new FileWriter(extendedData));

        String line;
        BufferedReader reader = new BufferedReader(new FileReader(identifierData));
        while ((line = reader.readLine()) != null) {
            Patient patient = null;
            if (isDigit(StringUtils.trim(line)))
                patient = patientService.getPatient(NumberUtils.toInt(line));
            else {
                Cohort cohort = patientSetService.convertPatientIdentifier(Arrays.asList(line));
                for (Integer patientId : cohort.getMemberIds())
                    patient = patientService.getPatient(patientId);
            }

            if (patient != null) {
                ExtendedData extended = new ExtendedData(patient, null);
                extended.setEncounters(searchEncounters(patient));
                writer.write(extended.generatePatientData());
                writer.newLine();
            } else {
                writer.write("Unresolved patient id or patient identifier for " + line);
                writer.newLine();
            }
        }
        reader.close();
        writer.close();

        InputStream inputStream = new BufferedInputStream(new FileInputStream(extendedData));

        response.setHeader("Content-Disposition", "attachment; filename=" + data.getName());
        response.setContentType("text/plain");
        FileCopyUtils.copy(inputStream, response.getOutputStream());
    }

    private Boolean isDigit(String string) {
        for (int i = 0; i < string.length(); i++)
            if (!Character.isDigit(string.charAt(i)))
                return Boolean.FALSE;
        return Boolean.TRUE;
    }

    private List<Encounter> searchEncounters(Patient patient) {
        CoreService service = Context.getService(CoreService.class);
        return service.getPatientEncounters(patient.getPatientId(), new HashMap<String, Collection<OpenmrsObject>>(), new FetchRestriction());
    }
}
