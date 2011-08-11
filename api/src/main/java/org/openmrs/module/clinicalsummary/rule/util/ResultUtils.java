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

package org.openmrs.module.clinicalsummary.rule.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.logic.result.Result;
import org.openmrs.util.OpenmrsUtil;

/**
 */
public class ResultUtils {

	private static final Log log = LogFactory.getLog(ResultUtils.class);

	/**
	 * Parse a string and strip the alpha element leaving the numeric part
	 *
	 * @param source the string to be parsed
	 * @return the digit elements of a string
	 */
	public static String stripToDigit(final String source) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < source.length(); i++) {
			if (Character.isDigit(source.charAt(i)))
				builder.append(source.charAt(i));
		}
		return builder.toString();
	}

	/**
	 * Strip duplicates result object
	 *
	 * @param results the results with duplicates
	 * @return results without duplicates
	 */
	public static Result stripDuplicates(final Result results) {

		if (CollectionUtils.isEmpty(results))
			return results;

		Result strippedResults = new Result();

		Integer parentCounter = 0;
		while (parentCounter < CollectionUtils.size(results)) {
			Result referredResult = results.get(parentCounter++);
			// search for duplicates and remove them
			Integer childCounter = parentCounter;
			Boolean duplicateFound = Boolean.FALSE;
			while (childCounter < CollectionUtils.size(results) && !duplicateFound) {
				Result currentResult = results.get(childCounter++);
				if (DateUtils.isSameDay(referredResult.getResultDate(), currentResult.getResultDate())
						&& OpenmrsUtil.nullSafeEquals(referredResult.getDatatype(), currentResult.getDatatype())) {
					if (OpenmrsUtil.nullSafeEquals(currentResult.getDatatype(), Result.Datatype.NUMERIC)
							&& OpenmrsUtil.nullSafeEquals(referredResult.toNumber(), currentResult.toNumber())
							|| (OpenmrsUtil.nullSafeEquals(currentResult.getDatatype(), Result.Datatype.CODED)
							&& OpenmrsUtil.nullSafeEquals(referredResult.toConcept(), currentResult.toConcept()))) {
						duplicateFound = Boolean.TRUE;
					}
				}
			}

			if (!duplicateFound)
				strippedResults.add(referredResult);
		}

		return strippedResults;
	}
}
