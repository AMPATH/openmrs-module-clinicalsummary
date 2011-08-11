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

package org.openmrs.module.clinicalsummary.web.editor;

import java.beans.PropertyEditorSupport;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.service.SummaryService;

/**
 */
public class SummaryEditor extends PropertyEditorSupport {

	private static final Log log = LogFactory.getLog(SummaryEditor.class);

	/**
	 * Gets the property value as a string suitable for presentation to a human to edit.
	 *
	 * @return The property value as a string suitable for presentation to a human to edit. <p>   Returns "null" is the value can't be expressed as a
	 *         string. <p>   If a non-null value is returned, then the PropertyEditor should be prepared to parse that string back in setAsText().
	 */
	@Override
	public String getAsText() {
		Summary summary = (Summary) getValue();
		if (summary != null)
			return String.valueOf(summary.getId());
		return StringUtils.EMPTY;
	}

	/**
	 * Sets the property value by parsing a given String.  May raise java.lang.IllegalArgumentException if either the String is badly formatted or if
	 * this kind of property can't be expressed as text.
	 *
	 * @param text
	 * 		The string to be parsed.
	 */
	@Override
	public void setAsText(final String text) throws IllegalArgumentException {
		if (StringUtils.isNotEmpty(text)) {
			Summary summary = Context.getService(SummaryService.class).getSummary(NumberUtils.toInt(text, -1));
			setValue(summary);
		}
	}
}
