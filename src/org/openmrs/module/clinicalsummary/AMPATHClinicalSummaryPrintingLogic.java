/**
 * 
 */
package org.openmrs.module.clinicalsummary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;

/**
 *
 */
public class AMPATHClinicalSummaryPrintingLogic extends
		ClinicalSummaryPrintingLogic {

	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryPrintingLogic#isPrintWorthy(org.openmrs.Encounter)
	 */
	@Override
	protected Boolean isPrintWorthy(Encounter e) {
		
		List<String> printableFormNames = new ArrayList<String>();
		printableFormNames.add("AMPATH Adult Return Visit Form");
		printableFormNames.add("AMPATH Adult Initial Visit Form");
		
		Form form = e.getForm();
		String formName = form.getName();
		
        // Handle only encounters from MTRH Module 2
		Location location = e.getLocation();
		Integer locationId = location.getLocationId();
		
		return (printableFormNames.contains(formName) && locationId == 13);
	}

	/**
	 * @see org.openmrs.module.clinicalsummary.ClinicalSummaryPrintingLogic#delayPrinting(org.openmrs.Encounter)
	 */
	@Override
	protected Boolean delayPrinting(Encounter e) {
		// if there are tests in this encounters obs, queue it up
		Collection<Obs> obsList = Context.getObsService().getObservations(e);
		
		// loop over all obs in this encounter
		if (obsList != null) {
			for (Obs obs : obsList) {
				Integer conceptId = obs.getConcept().getConceptId();
				
				// if there is a TESTS ORDERED(1271) concept and its answer isn't NONE(1107), delay printing
				if (1271 == conceptId) { 
					Concept value = obs.getValueCoded();
					if (value != null && value.getConceptId() != 1107)
						return true;
				}
				//else if (1234 == conceptId)
				//	return true;
			}
		}
		
		return false;
	}

}
