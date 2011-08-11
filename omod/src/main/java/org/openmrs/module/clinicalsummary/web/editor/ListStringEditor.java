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
import java.util.Collection;
import java.util.TreeSet;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.clinicalsummary.web.controller.WebUtils;

/**
 */
public class ListStringEditor extends PropertyEditorSupport {

	private static final Log log = LogFactory.getLog(ListStringEditor.class);

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
			Collection<String> names = new TreeSet<String>();
			for (String name : WebUtils.parse(text))
				names.add(name);
			setValue(names);
		}
	}
}
