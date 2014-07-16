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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.service.UtilService;
import org.openmrs.module.clinicalsummary.util.response.ReminderResponse;
import org.openmrs.module.clinicalsummary.web.controller.WebUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/module/clinicalsummary/utils/reminderResponse")
public class ReminderResponseController {

    private static final Log log = LogFactory.getLog(ReminderResponseController.class);

    @RequestMapping(method = RequestMethod.GET)
    public void prepare(final ModelMap mode) {
    }

    @RequestMapping(method = RequestMethod.POST)
    public
    @ResponseBody
    List<String[]> process(final @RequestParam(required = false, value = "locationId") String locationId,
                           final @RequestParam(required = false, value = "endTime") Date startTime,
                           final @RequestParam(required = false, value = "startTime") Date endTime) {
        UtilService utilService = Context.getService(UtilService.class);

        List<String[]> serialized = new ArrayList<String[]>();

        Location location = Context.getLocationService().getLocation(locationId);
        List<ReminderResponse> reminderResponses = utilService.getResponses(ReminderResponse.class, location, startTime, endTime);
        for (ReminderResponse reminderResponse : reminderResponses) {
            List<String> strings = new ArrayList<String>();
            Patient patient = reminderResponse.getPatient();
            strings.add(patient.getPersonName().getFullName());

            Person provider = reminderResponse.getProvider();
            strings.add(provider.getPersonName().getFullName());

            Date datetime = reminderResponse.getDatetime();
            strings.add(Context.getDateFormat().format(datetime));

            strings.add(reminderResponse.getToken());
            strings.add(String.valueOf(reminderResponse.getResponse()));
            strings.add(reminderResponse.getComment());

            serialized.add(strings.toArray(new String[6]));
        }
        return serialized;
    }

}
