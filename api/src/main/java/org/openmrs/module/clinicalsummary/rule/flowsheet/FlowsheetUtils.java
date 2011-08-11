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

package org.openmrs.module.clinicalsummary.rule.flowsheet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.logic.result.Result;
import org.openmrs.util.OpenmrsUtil;

import java.util.Collections;

/**
 */
public class FlowsheetUtils {

	private static final Log log = LogFactory.getLog(FlowsheetUtils.class);

	/**
	 * Slice the flow-sheet to match the total number and record configuration needed
	 *
	 * @param results the result
	 * @return results object arranged in a certain way
	 */
	public static Result slice(final Result results) {

		if (CollectionUtils.isEmpty(results))
			return results;

		Result slicedResults = new Result();

		// result is ordered descending, last result is the earliest result
		Result firstResult = results.get(CollectionUtils.size(results) - 1);
		slicedResults.add(firstResult);

		if (results.size() > slicedResults.size()) {
			//  get the last four results
			Integer counter = 0;
			Result otherResults = new Result();
			while (counter < results.size() - 1 && counter < FlowsheetParameters.FLOWSHEET_MAX_SIZE - 1) {
				Result result = results.get(counter++);
				// skip some not not valid results
				if (DateUtils.isSameDay(firstResult.getResultDate(), result.getResultDate())
						&& OpenmrsUtil.nullSafeEquals(firstResult.getDatatype(), result.getDatatype())) {
					if (OpenmrsUtil.nullSafeEquals(result.getDatatype(), Result.Datatype.NUMERIC)
							&& OpenmrsUtil.nullSafeEquals(firstResult.toNumber(), result.toNumber())
							|| (OpenmrsUtil.nullSafeEquals(result.getDatatype(), Result.Datatype.CODED)
							&& OpenmrsUtil.nullSafeEquals(firstResult.toConcept(), result.toConcept())))
						continue;
				}
				// get the latest results
				otherResults.add(result);
			}

			// flip them to make the order from earliest to latest
			Collections.reverse(otherResults);
			// add all of them to the oldest result, we now have results from earliest to latest
			slicedResults.addAll(otherResults);
			// flip them back to make the order from latest to the earliest
			Collections.reverse(slicedResults);
		}

		return slicedResults;
	}
}
