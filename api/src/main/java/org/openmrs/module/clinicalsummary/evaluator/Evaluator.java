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

import org.openmrs.Patient;
import org.openmrs.module.clinicalsummary.Summary;

/**
 */
public interface Evaluator {

	String FILE_TYPE_PDF = "pdf";

	String FILE_TYPE_XML = "xml";

	String POST_EVALUATION_ARTIFACT = "post.artifact";

	String POST_EVALUATION_TEMPLATE = "post.template";

	/**
	 * Evaluate a summary template
	 *
	 * @param summary      the summary template
	 * @param patient
	 * @param keepArtifact
	 */
	void evaluate(final Summary summary, final Patient patient, final Boolean keepArtifact);
}
