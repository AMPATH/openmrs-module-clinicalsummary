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

package org.openmrs.module.clinicalsummary.web.controller.service.xstream;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonName;

/**
 */
public class PersonNameConverter implements Converter {

	private static final Log log = LogFactory.getLog(PersonNameConverter.class);

	/**
	 * Convert an object to textual data.
	 *
	 * @param source
	 * 		The object to be marshalled.
	 * @param writer
	 * 		A stream to write to.
	 * @param context
	 * 		A context that allows nested objects to be processed by XStream.
	 */
	public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
		PersonName personName = (PersonName) source;
		if (personName != null)
			writer.setValue(getName(personName));
	}

	/**
	 * Convert textual data back into an object.
	 *
	 * @param reader
	 * 		The stream to read the text from.
	 * @param context
	 *
	 * @return The resulting object.
	 */
	public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
		PersonName personName = new PersonName();
		List<String> nameFragments = new ArrayList<String>();
		nameFragments.addAll(Arrays.asList(StringUtils.split(reader.getValue(), " ")));
		if (CollectionUtils.isNotEmpty(nameFragments))
			personName.setGivenName(nameFragments.remove(0));
		if (CollectionUtils.isNotEmpty(nameFragments))
			personName.setFamilyName(nameFragments.remove(0));
		if (CollectionUtils.isNotEmpty(nameFragments))
			personName.setMiddleName(StringUtils.join(nameFragments, " "));
		return personName;
	}

	/**
	 * Determines whether the converter can marshall a particular type.
	 *
	 * @param type
	 * 		the Class representing the object type to be converted
	 */
	public boolean canConvert(final Class type) {
		return type.equals(PersonName.class);
	}

	/**
	 * @param personName
	 *
	 * @return
	 */
	private String getName(PersonName personName) {
		List<String> nameFragments = new ArrayList<String>();
		if (StringUtils.isNotEmpty(personName.getGivenName()))
			nameFragments.add(personName.getGivenName());
		if (StringUtils.isNotEmpty(personName.getMiddleName()))
			nameFragments.add(personName.getMiddleName());
		if (StringUtils.isNotEmpty(personName.getFamilyName()))
			nameFragments.add(personName.getFamilyName());
		return StringUtils.join(nameFragments, " ");
	}
}
