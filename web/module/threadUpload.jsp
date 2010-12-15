<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/threadUpload.form" />

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

	// pressing the submit button will post the form to the server
	// block all input and wait for the reply from the server
	$j('#submit').click(function(event) {
		$j(':input').addClass('ui-state-disabled');
	})
	
	// create the accordion pane
	$j('#accordion').accordion({
		autoHeight: false
	});

	progress = $j('#progressbar').progressbar({value: 0});

	// check the status of the server when the page gets loaded
	checkStatus();
});

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
		}
		$j('#status').html(data.status);
		progress.progressbar('value', data.percentage);
	});
}
</script>

<div id="accordion">
	<h3><a href="#"><spring:message code="clinicalsummary.uploadHeader" /></a></h3>
	<div class="box">
		<form method="post" enctype="multipart/form-data" action="">
			<table>
				<tr>
					<td><spring:message code="clinicalsummary.password"/></td>
					<td><input type="password" name="password" /></td>
				</tr>
				<tr>
					<td><spring:message code="clinicalsummary.passphrase"/></td>
					<td><input type="password" name="passphrase" /></td>
				</tr>
				<tr>
					<td><spring:message code="clinicalsummary.secretFile"/></td>
					<td><input type="file" name="secretFile" /></td>
				</tr>
				<tr>
					<td><spring:message code="clinicalsummary.summaryCollectionFile"/></td>
					<td><input type="file" name="summaries" /></td>
				</tr>
				<tr>
					<td colspan="2"><input id="submit" type="submit" value="<spring:message code="clinicalsummary.uploadCollection"/>"/></td>
				</tr>
			</table>
		</form>
		<p><spring:message code="clinicalsummary.serverStatus"/>: <span id="status"></span></p>
		<span id="progress"></span>
		<div id="progressbar"></div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>