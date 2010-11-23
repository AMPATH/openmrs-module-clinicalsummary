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
package org.openmrs.module.clinicalsummary.rule.reminder;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.Rule;
import org.openmrs.logic.result.Result;
import org.openmrs.logic.result.Result.Datatype;
import org.openmrs.logic.rule.RuleParameterInfo;
import org.openmrs.module.clinicalsummary.SummaryService;
import org.openmrs.module.clinicalsummary.cache.SummaryDataSource;
import org.openmrs.module.clinicalsummary.concept.StandardConceptConstants;
import org.openmrs.module.clinicalsummary.rule.PCPMedicationsRule;

/**
 * 
 */
public class PCPCD4ReminderRule implements Rule {
	
	private static final Log log = LogFactory.getLog(CXRReminderRule.class);
	
	private static final String PCP_CD4_REMINDER = "Consider PCP prophylaxis - CD4 &lt; 200 and not on Septrin or Dapsone";
	
	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, org.openmrs.Patient,
	 *      java.util.Map)
	 */
	public Result eval(LogicContext context, Patient patient, Map<String, Object> parameters) throws LogicException {
		Result reminder = new Result();
		
		PCPMedicationsRule rule = new PCPMedicationsRule();
		Result pcpMedicationsResult = rule.eval(context, patient, parameters);
		
		SummaryService service = Context.getService(SummaryService.class);
		
		LogicCriteria conceptCriteria = service.parseToken(SummaryDataSource.CONCEPT).equalTo(StandardConceptConstants.CD4_NAME);
		LogicCriteria encounterCriteria = service.parseToken(SummaryDataSource.ENCOUNTER_TYPE).in(Collections.emptyList());
		LogicCriteria criteria = conceptCriteria.and(encounterCriteria);
		
		Result obsResult = context.read(patient, service.getLogicDataSource("summary"), criteria);
		
		if (log.isDebugEnabled())
			log.debug("Patient: " + patient.getPatientId() + ", cd4 results " + obsResult);
		
		if (pcpMedicationsResult.isEmpty() && !obsResult.isEmpty()) {
			Result r = obsResult.get(0);
			if (r.toNumber() < 200)
				reminder = new Result(PCP_CD4_REMINDER);
		}
		
		if (log.isDebugEnabled())
			log.debug("Patient: " + patient.getPatientId() + ", cd4 reminder " + reminder);
		
		return reminder;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getDefaultDatatype()
	 */
	public Datatype getDefaultDatatype() {
		return Datatype.TEXT;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getDependencies()
	 */
	public String[] getDependencies() {
		return null;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getParameterList()
	 */
	public Set<RuleParameterInfo> getParameterList() {
		return null;
	}
	
	/**
	 * @see org.openmrs.logic.Rule#getTTL()
	 */
	public int getTTL() {
		return 0;
	}
	
}
