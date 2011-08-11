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

package org.openmrs.module.clinicalsummary.evaluator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.clinicalsummary.Constants;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.util.OpenmrsUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class EvaluatorUtils {

	private static final Log log = LogFactory.getLog(EvaluatorUtils.class);

	/**
	 * Get the output location of the evaluation process for a summary template
	 *
	 * @param summary
	 * 		the summary to be evaluated
	 *
	 * @return the output location of the evaluation end product
	 */
	public static File getOutputDirectory(final Summary summary) {
		List<String> paths = new ArrayList<String>();
		paths.add(Constants.GENERATED_PDF_LOCATION);

		if (summary != null)
			paths.add(String.valueOf(summary.getId()));

		return OpenmrsUtil.getDirectoryInApplicationDataDirectory(StringUtils.join(paths, File.separator));
	}
}
