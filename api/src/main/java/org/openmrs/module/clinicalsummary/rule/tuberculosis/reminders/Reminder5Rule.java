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
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element5ARule;
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element5BRule;
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element5CRule;

import java.util.Map;

/**
 * TODO: Write brief description about the class here.
 */
public class Reminder5Rule extends EvaluableRule {

    public static final String TOKEN = "Tuberculosis: Reminder 5";

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

        Element5ARule element5ARule = new Element5ARule();
        Result element5AResult = element5ARule.eval(context, patientId, parameters);
        if (element5AResult.toBoolean()) {
            return result;
        }

        Element5BRule element5BRule = new Element5BRule();
        Result element5BResult = element5BRule.eval(context, patientId, parameters);
        if (element5BResult.toBoolean()) {
            return result;
        }

        Element5CRule element5CRule = new Element5CRule();
        Result element5CResult = element5CRule.eval(context, patientId, parameters);
        if (element5CResult.toBoolean()) {
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
