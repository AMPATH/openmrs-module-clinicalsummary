package org.openmrs.module.clinicalsummary.web.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.module.clinicalsummary.SummaryConstants;
import org.openmrs.module.clinicalsummary.engine.GeneratorEngine;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;

/**
 */
@Controller
public class GenerateSummaryFormController {
	
	private static final Log log = LogFactory.getLog(GenerateSummaryFormController.class);
	
	private static final int BUFFER_SIZE = 4096;
	
	private static final String GENERATOR_DELAY = "clinicalsummary.generator.delay";
	
	private static final String PDF_EXTENSION = ".pdf";
	
	private static final String MIME_TYPE = "application/pdf";
	
	private static final File folder = OpenmrsUtil
	        .getDirectoryInApplicationDataDirectory(SummaryConstants.GENERATED_PDF_LOCATION);
	
	@RequestMapping("/module/clinicalsummary/generate")
	public void generate(final @RequestParam(required = true, value = "patientId") int patientId,
	                     final HttpServletRequest request, final HttpServletResponse response) {
		if (Context.isAuthenticated()) {
			Cohort cohort = new Cohort();
			cohort.addMember(patientId);
			
			String delayString = Context.getAdministrationService().getGlobalProperty(GENERATOR_DELAY);
			Integer delay = NumberUtils.toInt(delayString, 3000);
			
			if (log.isDebugEnabled())
				log.debug("Delay for the times is: " + delay);
			
			// let the timer to prepare the attachement if the generation process is not done within the delay
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					prepareAttachment(response, patientId);
				}
				
			}, delay);
			
			GeneratorEngine.generateSummary(cohort);
			
			// if the timer here doesn't kick in yet, then we need to cancel it and create attachment
			timer.cancel();
			prepareAttachment(response, patientId);
		}
	}
	
	private void prepareAttachment(HttpServletResponse response, Integer patientId) {
		
		try {
			//Prepare response
			// create a temporary file that will hold all copied pdf file
			File summaryCollectionsFile = File.createTempFile("Summary", PDF_EXTENSION);
			summaryCollectionsFile.deleteOnExit();
			
			FileOutputStream outputStream = new FileOutputStream(summaryCollectionsFile);
			
			Document document = new Document();
			PdfCopy copy = new PdfCopy(document, outputStream);
			document.open();
			
			Calendar calendar = Calendar.getInstance();
			
			PdfReader reader = null;
			
			// use file filter to get the patient's files and then iterate.
			// instead of using the applicable templates
			
			FileFilter fileFilter = new WildcardFileFilter(patientId + "_*.pdf");
			
			for (File summaryFile : folder.listFiles(fileFilter)) {
				// if the patient is not generated yet, then just skip ...
				if (!summaryFile.exists())
					continue;
				
				calendar.setTimeInMillis(summaryFile.lastModified());
				
				try {
					// when one pdf fail, then we just need to skip that file
					// instead of failing for the whole pdfs collection
					reader = new PdfReader(summaryFile.getAbsolutePath());
					int pageCount = reader.getNumberOfPages();
					for (int i = 1; i <= pageCount; i++)
						copy.addPage(copy.getImportedPage(reader, i));
				}
				catch (Exception e) {
					log.error("Failed to add summary for patient: " + patientId, e);
				}
			}
			
			document.close();
			outputStream.close();
			
			String time = new SimpleDateFormat("yyyyMMdd_HHmmss_").format(calendar.getTime());
			String downloadFilename = "PreGeneratedSummary_" + time + patientId + PDF_EXTENSION;
			
			response.setHeader("Content-Disposition", "attachment; filename=" + downloadFilename);
			response.setContentType(MIME_TYPE);
			response.setContentLength((int) summaryCollectionsFile.length());
			response.flushBuffer();
			
			BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(summaryCollectionsFile));
			
			ReadableByteChannel input = Channels.newChannel(inputStream);
			WritableByteChannel output = Channels.newChannel(response.getOutputStream());
			ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
			
			while (input.read(buffer) != -1) {
				buffer.flip();
				output.write(buffer);
				buffer.clear();
			}
			
			input.close();
			output.close();
		}
		catch (Exception e) {
			log.error("Failed generating summary for patient " + patientId + " ... ", e);
		}
	}
}
