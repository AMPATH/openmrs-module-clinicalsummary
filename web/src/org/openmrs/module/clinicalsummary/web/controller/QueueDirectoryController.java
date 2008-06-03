package org.openmrs.module.clinicalsummary.web.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.clinicalsummary.ClinicalSummaryUtil;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class QueueDirectoryController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
   
	/**
	 * Returns List<ArrayList<File>>(2) containing all Files under the clinicalsummary.queueItemGenerateDir
	 * and under the clinicalsummary.queueItemPrintDir (defined by global properties).
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		
		String showAll = request.getParameter("showAll");
		
		File generateDir = ClinicalSummaryUtil.getOutDir(null, ClinicalSummaryUtil.DIRECTORY.GENERATED);
		File printDir = ClinicalSummaryUtil.getOutDir(null, ClinicalSummaryUtil.DIRECTORY.TO_PRINT);
		List<ArrayList<File>> fileList = new ArrayList<ArrayList<File>>(2);
		fileList.add(new ArrayList<File>());
		fileList.get(0).add(generateDir);
		for (File f: FileSystemView.getFileSystemView().getFiles(generateDir, false)) {
			fileList.get(0).add(f);
		}
		fileList.add(new ArrayList<File>());
		fileList.get(1).add(printDir);
		for (File f: FileSystemView.getFileSystemView().getFiles(printDir, false)) {
			fileList.get(1).add(f);
		}
		return fileList;
	}

}