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
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element2A;
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element2B;
import org.openmrs.module.clinicalsummary.rule.tuberculosis.element.Element2C;

import java.util.Map;

/**
 * TODO: Write brief description about the class here.
 */
public class Reminder2 extends EvaluableRule {

    public static final String TOKEN = "Tuberculosis: Reminder 2";

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

        Element2A element2A = new Element2A();
        Result element2AResult = element2A.eval(context, patientId, parameters);
        if (element2AResult.toBoolean()) {
            return result;
        }

        Element2B element2B = new Element2B();
        Result element2BResult = element2B.eval(context, patientId, parameters);
        if (element2BResult.toBoolean()) {
            return result;
        }

        Element2C element2C = new Element2C();
        Result element2CResult = element2C.eval(context, patientId, parameters);
        if (element2CResult.toBoolean()) {
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
