<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/threadDownload.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/scripts/jquery/jquery-1.4.2.min.js" />
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/scripts/jquery-ui/js/jquery-ui-1.8.2.custom.min.js" />
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/scripts/jquery-ui/css/redmond/jquery-ui-1.8.2.custom.css" />

<script type="text/javascript" charset="utf-8">

$j = jQuery.noConflict();

var progress;
var timeout;

$j(document).ready(function() {

	// add hover state change
	$j(':input:not(.ui-state-disabled)').hover(
		function(){
			$j(this).addClass('ui-state-hover');
		},
		function(){
			$j(this).removeClass('ui-state-hover');
		}
	)
	
	// init all input to use jquery css
	$j(':input').addClass('ui-state-default ui-corner-all');

	// create the accordion pane
	$j('#accordion').accordion({
		autoHeight: false
	});

	progress = $j('#progressbar').progressbar({value: 0});

	// check the status of the server when the page gets loaded
	checkStatus()
});

function download(filename, type) {

	// this function will attache the filename and the type of file to be downloaded
	// two types of file can be downloaded zip file and secret file
	var form = document.createElement("form");
	form.setAttribute("method", "post");
    form.setAttribute("action", "download.form");

    var filenameInput = document.createElement("input");
    filenameInput.setAttribute("type", "hidden");
    filenameInput.setAttribute("name", "filename");
    filenameInput.setAttribute("value", filename);

    form.appendChild(filenameInput);
    
    var filetypeInput = document.createElement("input");
    filetypeInput.setAttribute("type", "hidden");
    filetypeInput.setAttribute("name", "type");
    filetypeInput.setAttribute("value", type);

    form.appendChild(filetypeInput);
    
	document.body.appendChild(form);
	
    form.submit();
}

function checkStatus() {
	jQuery.getJSON('checkStatus.form', function(data) {
		
		if (data.running) {
			// when the task is running, disable the form elements
			$j(':input').addClass('ui-state-disabled');
			// show the processing text
			$j('#progress').html('<spring:message code="clinicalsummary.processing"/> ' + data.processed + ' <spring:message code="clinicalsummary.of"/> ' + data.total);
			// create and show the progress bar
			$j('#progressbar').show();
			// check back the server after 10s
			timeout = setTimeout('checkStatus()', 3000);
		} else {
			// when no task is running, enable the form elements
			$j(':input').removeClass('ui-state-disabled');
			// show the status
			$j('#progress').html('<spring:message code="clinicalsummary.noProcess"/>.');
			// hide the progress bar
			$j('#progressbar').hide();
			// clear the timeout
			clearTimeout(timeout);
			// this is total hack, i don't want the json contains too much data
			if (data.status == 'FINISHED (Download)') {
				$j('#secretFiles').html('<spring:message code="clinicalsummary.secretFile"/>: <a href="#" onclick="javascript:download(\'' + data.filename + '\', \'secret\')">' + data.filename + '</a>');
				$j('#zippedFiles').html('<spring:message code="clinicalsummary.summaryCollectionFile"/>: <a href="#" onclick="javascript:download(\'' + data.filename + '\', \'zip\')">' + data.filename + '.zip</a>');
				download(data.filename, "zip");
			}
		}
		$j('#status').html(data.status);
		progress.progressbar('value', data.percentage);
	});
}
</script>

<div id="accordion">
	<h3><a href="#"><spring:message code="clinicalsummary.downloadHeader" /></a></h3>
	<div class="box">
		<form method="post" enctype="multipart/form-data" action="">
			<table cellspacing="5px">
				<tr>
					<td><spring:message code="clinicalsummary.password"/></td>
					<td><input type="password" name="password" /></td>
				</tr>
				<tr>
					<td><spring:message code="clinicalsummary.passphrase"/></td>
					<td><input type="password" name="passphrase" /></td>
				</tr>
				<tr>
					<td colspan="2"><input id="submit" type="submit" value="<spring:message code="clinicalsummary.downloadCollection"/>"/></td>
				</tr>
			</table>
		</form>
		<p><spring:message code="clinicalsummary.serverStatus"/>: <span id="status"></span></p>
		<span id="progress"></span>
		<div id="progressbar"></div>
	</div class="box">
	<h3><a href="#"><spring:message code="clinicalsummary.availableFiles" /></a></h3>
	<div>
		<div id="secretFiles"><c:if test="${not empty secretFile}"><spring:message code="clinicalsummary.secretFile"/>: <a href="#" onclick="javascript:download('${secretFile}', 'secret')">${secretFile}</a></c:if></div>
		<div id="zippedFiles"><c:if test="${not empty zipFile}"><spring:message code="clinicalsummary.summaryCollectionFile"/>: <a href="#" onclick="javascript:download('${zipFile}', 'zip')">${zipFile}.zip</a></c:if></div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>