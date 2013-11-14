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
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element10ARule;
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element10BRule;
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element10CRule;
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element10DRule;
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element10ERule;
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element10FRule;
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element10GRule;

import java.util.Map;

/**
 * TODO: Write brief description about the class here.
 */
public class Reminder10Rule extends EvaluableRule {

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

        Element10ARule element10ARule = new Element10ARule();
        Result element10AResult = element10ARule.eval(context, patientId, parameters);
        if (element10AResult.toBoolean()) {
            return result;
        }

        Element10BRule element10BRule = new Element10BRule();
        Result element10BResult = element10BRule.eval(context, patientId, parameters);
        if (element10BResult.toBoolean()) {
            return result;
        }

        Element10CRule element10CRule = new Element10CRule();
        Result element10CResult = element10CRule.eval(context, patientId, parameters);
        if (element10CResult.toBoolean()) {
            return result;
        }

        Element10DRule element10DRule = new Element10DRule();
        Result element10DResult = element10DRule.eval(context, patientId, parameters);
        if (element10DResult.toBoolean()) {
            return result;
        }

        Element10ERule element10ERule = new Element10ERule();
        Result element10EResult = element10ERule.eval(context, patientId, parameters);
        if (element10EResult.toBoolean()) {
            return result;
        }

        Element10FRule element10FRule = new Element10FRule();
        Result element10FResult = element10FRule.eval(context, patientId, parameters);
        if (element10FResult.toBoolean()) {
            return result;
        }

        Element10GRule element10GRule = new Element10GRule();
        Result element10GResult = element10GRule.eval(context, patientId, parameters);
        if (element10GResult.toBoolean()) {
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
