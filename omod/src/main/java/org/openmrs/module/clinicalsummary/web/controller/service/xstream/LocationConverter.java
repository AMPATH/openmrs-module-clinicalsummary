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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;

/**
 */
public class LocationConverter implements Converter {

	private static final Log log = LogFactory.getLog(LocationConverter.class);

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
		Location location = (Location) source;
		// write the location id node
		writer.startNode("id");
		writer.setValue(location.getId() != null ? String.valueOf(location.getId()) : StringUtils.EMPTY);
		writer.endNode();
		// write the location name
		writer.startNode("name");
		writer.setValue(location.getName());
		writer.endNode();
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
		Location location = new Location();

		reader.moveDown();
		location.setId(NumberUtils.toInt(reader.getValue()));
		reader.moveUp();

		reader.moveDown();
		location.setName(reader.getValue());
		reader.moveUp();

		return location;
	}

	/**
	 * Determines whether the converter can marshall a particular type.
	 *
	 * @param type
	 * 		the Class representing the object type to be converted
	 */
	public boolean canConvert(final Class type) {
		return type.equals(Location.class);
	}
}
