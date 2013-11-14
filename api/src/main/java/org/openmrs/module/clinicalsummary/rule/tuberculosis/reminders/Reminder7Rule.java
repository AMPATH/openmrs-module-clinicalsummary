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
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element7ARule;
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element7BRule;
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element7CRule;

import java.util.Map;

/**
 * TODO: Write brief description about the class here.
 */
public class Reminder7Rule extends EvaluableRule {

    public static final String TOKEN = "Tuberculosis: Reminder 7";

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

        Element7ARule element7ARule = new Element7ARule();
        Result element7AResult = element7ARule.eval(context, patientId, parameters);
        if (element7AResult.toBoolean()) {
            return result;
        }

        Element7BRule element7BRule = new Element7BRule();
        Result element7BResult = element7BRule.eval(context, patientId, parameters);
        if (element7BResult.toBoolean()) {
            return result;
        }

        Element7CRule element7CRule = new Element7CRule();
        Result element7CResult = element7CRule.eval(context, patientId, parameters);
        if (element7CResult.toBoolean()) {
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
