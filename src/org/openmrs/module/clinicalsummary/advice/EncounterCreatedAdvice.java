package org.openmrs.module.clinicalsummary.advice;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.ClinicalSummaryPrintingLogic;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.aop.AfterReturningAdvice;

/**
 * After an encounter is created, get the ClinicalSummaryPrintingLogic class
 * defined in the global property clinicalsummary.printingLogicClass and
 * instantiate it.
 */
public class EncounterCreatedAdvice implements AfterReturningAdvice {

    private Log log = LogFactory.getLog(this.getClass());

    private static final String PRINTING_LOGIC_GP = "clinicalsummary.printingLogicClass";

    private ClinicalSummaryPrintingLogic printingLogic = null;

    public void afterReturning(Object returnValue, Method method,
            Object[] args, Object target) throws Throwable {
        if (method.getName().equals("encounterCreated")) {
            Encounter encounter = (Encounter) args[0];
            log
                    .debug("EncounterCreatedAdvice created an encounter at this time: "
                            + new java.util.Date());
            try {
                // create the logic class object if there isn't one yet
                if (printingLogic == null) {
                    log.debug("Creating printingLogic class");

                    String gp = Context.getAdministrationService()
                            .getGlobalProperty(PRINTING_LOGIC_GP);

                    if (gp != null && !gp.equals("")) {
                        try {
                            Class logicClass = OpenmrsClassLoader.getInstance()
                                    .loadClass(gp);
                            printingLogic = (ClinicalSummaryPrintingLogic) logicClass
                                    .newInstance();
                        } catch (ClassNotFoundException ex) {
                            log.debug("Logic class not found", ex);
                        }
                    }

                    log.debug("Printing logic class is now: " + printingLogic);
                }

                // now run the encounter through the logic
                if (printingLogic != null) {
                    log.debug("this.class: " + this.getClass());
                    log.debug("this.classloader: "
                            + this.getClass().getClassLoader());
                    printingLogic.queueOrPrintEncounter(encounter);
                }
            } catch (Throwable t) {
                log
                        .error("Unable to queue or print encounter: "
                                + encounter, t);
            }
        }
    }

}