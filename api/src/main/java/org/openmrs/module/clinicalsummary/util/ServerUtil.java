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
package org.openmrs.module.clinicalsummary.util;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;

public class ServerUtil {

    public static final String CLINICALSUMMARY_CENTRAL_SERVER = "clinicalsummary.central.server";

    public static boolean isCentral() {
        // by default, disable the upload page.
        boolean isCentral = true;
        AdministrationService administrationService = Context.getAdministrationService();
        String serverType = administrationService.getGlobalProperty(CLINICALSUMMARY_CENTRAL_SERVER);
        if (StringUtils.isNotEmpty(serverType))
            isCentral = Boolean.parseBoolean(serverType);
        return isCentral;
    }

}
