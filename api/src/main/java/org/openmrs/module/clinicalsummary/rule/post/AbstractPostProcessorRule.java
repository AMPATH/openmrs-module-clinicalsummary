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

package org.openmrs.module.clinicalsummary.rule.post;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.dv.util.Base64;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.LogicException;
import org.openmrs.logic.result.Result;
import org.openmrs.module.clinicalsummary.evaluator.Evaluator;
import org.openmrs.module.clinicalsummary.rule.EvaluableRule;

import java.util.Map;

/**
 * Eval method in rule class extending this class will get raw xml of the summary sheet that can be pre-processed automatically in the parameters. The
 * key for the raw data is POST_EVALUATION_ARTIFACT. The raw data is encoded, so consumer need to decode the raw data before performing any post
 * processing on the raw data.
 */
public abstract class AbstractPostProcessorRule extends EvaluableRule {

	private static final Log log = LogFactory.getLog(AbstractPostProcessorRule.class);

	protected final String POST_EVALUATION_ARTIFACT = Evaluator.POST_EVALUATION_ARTIFACT;

	protected final String POST_EVALUATION_TEMPLATE = Evaluator.POST_EVALUATION_TEMPLATE;

	/**
	 * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
	 */
	@Override
	public Result eval(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) throws LogicException {
		String artifact = decodeArtifact(parameters.get(POST_EVALUATION_ARTIFACT));
		Result result = new Result(artifact);

		// TODO: redesign this to only pass the above artifact
		if (applicable(parameters))
			result = evaluate(context, patientId, parameters);

		return result;
	}

	/**
	 * TODO: need some more design thought to make this abstract inline with the EvaluableRule
	 * Determine whether the rule should get executed or not
	 *
	 * @param parameters
	 * @return
	 */
	protected abstract Boolean applicable(Map<String, Object> parameters);

	/**
	 * @param object
	 *
	 * @return
	 */
	protected String decodeArtifact(Object object) {
		return new String(Base64.decode(object.toString()));
	}

	/**
	 * @see org.openmrs.logic.Rule#getDefaultDatatype()
	 */
	@Override
	public final Result.Datatype getDefaultDatatype() {
		return Result.Datatype.TEXT;
	}
}
