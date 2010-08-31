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
package org.openmrs.module.clinicalsummary.rule;

import java.util.Comparator;

import org.openmrs.logic.result.Result;


/**
 *
 */
public class ResultComparator implements Comparator<Result> {
	
	public enum CompareProperty {
		DATETIME
	}
	
	private final CompareProperty compareProperty;
	
	public ResultComparator(CompareProperty compareProperty) {
		this.compareProperty = compareProperty;
	}

	public int compare(Result firstResult, Result secondResult) {
		
		if (CompareProperty.DATETIME.equals(compareProperty)) {
			
			// just need to compare the first result
			
			// not single result
			if (!(firstResult.size() < 1))
				firstResult = firstResult.get(0);
			
			if (!(secondResult.size() < 1))
				secondResult = secondResult.get(0);
			
			return secondResult.getResultDate().compareTo(firstResult.getResultDate());
		}
		
		// dirty way to do it, *runnnn*
		return 0;
    }

}
