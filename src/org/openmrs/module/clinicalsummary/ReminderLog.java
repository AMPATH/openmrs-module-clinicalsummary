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

package org.openmrs.module.clinicalsummary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

/**
 * ClinicalSummary Reminders need to use this class in order to be logged 
 * to the clinical summary reminder log file.  The reminderlog-yyyy-MM-dd.csv file
 * is a comma separated file of patient reminders.
 */
public class ReminderLog {

	protected final Log log = LogFactory.getLog(getClass());
    
    protected File reminderLog;
    protected BufferedWriter writer;
    protected Boolean initialized = false;
    protected DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
    protected Date timestamp;
    protected String header;
    
    public ReminderLog() {
        timestamp = new Date();
        setColumnHeader("Date Generated, Encounter Date, Patient ID, Patient Identifier, Provider, Reminder");
    }
    
    /**
     * Set the column header of the reminderlog file.
     * @param header
     */
    public void setColumnHeader(String header) {
        this.header = null;
        this.header = header;
    }
    
    /**
     * Get the column header of the reminderlog file.
     * @return
     */
    public String getColumnHeader() {
        return this.header;
    }

    /**
     * Initialize the log file once for each new day.  Add a column header if it does not exist.
     * 
     * @return
     */
    private boolean initLogFile() {
        if (initialized && ymd.format(timestamp).equals(ymd.format(new Date())) ) {
            if (!hasHeader()) {
                addHeader();
            }
            return initialized;
        }
        File dir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(
            Context.getAdministrationService().getGlobalProperty("clinicalsummary.logDir")
        );
        timestamp = null;
        timestamp = new Date();
        String file = "reminderlog-" + ymd.format(timestamp) + ".csv";
        reminderLog = new File(dir, file);
        if (!hasHeader()) {
            addHeader();
        }
        initialized = true;
        return initialized;
    }
    
    /**
     * Adds a header to the reminderlog file.
     */
    private void addHeader() {
        try {
                writer = null;
                writer = new BufferedWriter(new FileWriter(reminderLog, true));
                writer.write(getColumnHeader());
                writer.newLine();
                writer.close();
        } catch (IOException ioe) {
            log.error("Could not add header to reminderlog.", ioe);
        }        
    }

    /**
     * Checks reminderlog file to see if it has a column header.
     * @return true if the reminderlog has a column header.
     */
    private Boolean hasHeader() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(reminderLog));
            String firstLine = reader.readLine();
            reader.close();
            //log.debug("Testing header: " + firstLine + " contains " + header + " ?..." + firstLine.contains(header));
            if (!firstLine.contains(header)) {
                return false;
            }
        } catch (IOException ioe) {
            return false;
        }
        return true;
    }
  
    /**
     * Logs a message to the Clinical Summary Reminder log file in comma
     * separated format.  The reminder message should be obtained from the 
     * SummaryExportFunctions.  This method should be called from the 
     * ClinicalSummaryServiceImpl class.  If the reminder String is null or 
     * empty, then this does not log the reminder.
     * 
     * @param q
     * @param reminder
     * @return Boolean returns true if reminder is not null and is logged successfully
     */
    public Boolean logReminder(ClinicalSummaryQueueItem q, String reminder) {
        if (null == reminder || "".equals(reminder)) {
            return false;
        }
        initLogFile();
        Date dateGenerated = new Date();
        String msg = new String();
        if (null != dateGenerated) {
            msg += dateGenerated;
        }
        msg += ", ";
        if (null != q.getEncounterDatetime()) {
            msg += q.getEncounterDatetime();
        }
        msg += ", ";
        if (null != q.getPatient().getPatientId()) {
            msg += q.getPatient().getPatientId();
        }
        msg += ", ";
        if (null != q.getPatient().getPatientIdentifier()) {
            msg += q.getPatient().getPatientIdentifier();
        }
        msg += ", ";
        if (null != q.getEncounter() && null != q.getEncounter().getProvider() && null != q.getEncounter().getProvider().getSystemId()) {
            msg += q.getEncounter().getProvider().getSystemId();
        }
        msg += ", ";
        msg += reminder;
        try {
            writer = null;
            writer = new BufferedWriter(new FileWriter(reminderLog, true));
            writer.write(msg);
            writer.newLine();
            writer.close();
        } catch (IOException ioe) {
            log.error("Error logging reminder for: " + msg, ioe);
            return false;
        }
        return true;
    }
       
}
