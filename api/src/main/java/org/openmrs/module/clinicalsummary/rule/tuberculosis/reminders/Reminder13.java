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
package org.openmrs.module.clinicalsummary.rule.tuberculosis.reminders;

import org.openmrs.logic.LogicContext;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;
import org.openmrs.module.clinicalsummary.rule.reminder.ReminderParameters;
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element10A;
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element10B;
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element10C;
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element10D;
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element10E;
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element12A;
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element12B;
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element13B;

import java.util.Map;

/**
 * TODO: Write brief description about the class here.
 */
public class Reminder13 extends EvaluableRule {

    public static final String TOKEN = "Tuberculosis: Reminder 1";

    /**
     * @param context
     * @param patientId
     * @param parameters
     * @return
     * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
     */
    @Override
    protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) {
        Result result = new Result();

        Element12A element12A = new Element12A();
        Result element12AResult = element12A.eval(context, patientId, parameters);
        if (element12AResult.toBoolean()) {
            return result;
        }

        Element12B element12B = new Element12B();
        Result element12BResult = element12B.eval(context, patientId, parameters);
        if (element12BResult.toBoolean()) {
            return result;
        }

        Element13B element13B = new Element13B();
        Result element13BResult = element13B.eval(context, patientId, parameters);
        if (element13BResult.toBoolean()) {
            return result;
        }

        Element10A element10A = new Element10A();
        Result element10AResult = element10A.eval(context, patientId, parameters);
        if (element10AResult.toBoolean()) {
            return result;
        }

        Element10B element10B = new Element10B();
        Result element10BResult = element10B.eval(context, patientId, parameters);
        if (element10BResult.toBoolean()) {
            return result;
        }

        Element10C element10C = new Element10C();
        Result element10CResult = element10C.eval(context, patientId, parameters);
        if (element10CResult.toBoolean()) {
            return result;
        }

        Element10D element10D = new Element10D();
        Result element10DResult = element10D.eval(context, patientId, parameters);
        if (element10DResult.toBoolean()) {
            return result;
        }

        Element10E element10E = new Element10E();
        Result element10EResult = element10E.eval(context, patientId, parameters);
        if (element10EResult.toBoolean()) {
            return result;
        }
        result.add(new Result(String.valueOf(parameters.get(ReminderParameters.DISPLAYED_REMINDER_TEXT))));
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
