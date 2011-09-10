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

package org.openmrs.module.clinicalsummary.evaluator.reminder;

import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.Log4JLogChute;
import org.openmrs.Patient;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.evaluator.Evaluator;

/**
 */
public class ReminderEvaluator implements Evaluator {

	private static final Log log = LogFactory.getLog(ReminderEvaluator.class);

	/**
	 * Evaluate a summary template
	 *
	 * @param summary      the summary template
	 * @param patient
	 * @param keepArtifact
	 */
	@Override
	public void evaluate(final Summary summary, final Patient patient, final Boolean keepArtifact) {
		try {
			// http://velocity.apache.org/evaluator/releases/velocity-1.6.2/developer-guide.html#Configuring_Logging
			Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, Log4JLogChute.class.getName());
			Velocity.setProperty("runtime.log.logsystem.log4j.logger", ReminderEvaluator.class.getName());
			Velocity.init();

			VelocityContext context = new VelocityContext();
			context.put("patient", patient);
			context.put("patientId", patient.getPatientId());
			context.put("summary", summary);

			// custom utility functions
			context.put("fn", new ReminderUtils(summary));

			StringWriter writer = new StringWriter();
			Velocity.evaluate(context, writer, ReminderEvaluator.class.getName(), summary.getXml());
		} catch (Exception e) {
			log.error("Evaluating " + summary.getName() + " on patient " + patient.getPatientId() + " failed ...", e);
		}
	}
}
