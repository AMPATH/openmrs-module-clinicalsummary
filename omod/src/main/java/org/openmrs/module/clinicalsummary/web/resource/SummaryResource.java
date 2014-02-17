package org.openmrs.module.clinicalsummary.web.resource;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.Summary;
import org.openmrs.module.clinicalsummary.evaluator.EvaluatorUtils;
import org.openmrs.module.clinicalsummary.service.SummaryService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/summary",
        supportedClass = Summary.class,
        supportedOpenmrsVersions = {"1.8.*", "1.9.*"})

public class SummaryResource extends DelegatingCrudResource<Summary> {
    @Override
    public PageableResult doSearch(final RequestContext context) throws ResponseException {
        HttpServletRequest request = context.getRequest();
        Integer summaryId = getInteger(request, "id");
        List<String> summaries = new ArrayList<String>();
        if (summaryId != null) {
            Summary summary = Context.getService(SummaryService.class).getSummary(summaryId);
            File outputDirectory = EvaluatorUtils.getOutputDirectory(summary);
            if (isSummaryDirExists(outputDirectory)) {
                File[] files = outputDirectory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        summaries.add(getJSONObject(file).toString());
                    }
                }
            }
            return new NeedsPaging<String>(summaries, context);
        }
        return new EmptySearchResult();

    }

    private JSONObject getJSONObject(File file) {
        JSONObject jsonObject = new JSONObject();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            jsonObject = XML.toJSONObject(IOUtils.toString(inputStream));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private boolean isSummaryDirExists(File outputDirectory) {
        return outputDirectory.exists() && outputDirectory.isDirectory();
    }

    @Override
    public Summary getByUniqueId(String uniqueId) {
        return null;
    }

    @Override
    protected void delete(Summary summary, String s, RequestContext requestContext) throws ResponseException {

    }

    @Override
    public Summary newDelegate() {
        return null;
    }

    @Override
    public Summary save(Summary summary) {
        return null;
    }

    @Override
    public void purge(Summary summary, RequestContext requestContext) throws ResponseException {

    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        return null;
    }

    private Integer getInteger(HttpServletRequest request, String paramName) {
        try {
            return Integer.valueOf(request.getParameter(paramName));
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
