<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Generate Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/generateSummaries.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/scripts/jquery/jquery-1.4.2.min.js" />
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/scripts/jquery-ui/js/jquery-ui-1.8.2.custom.min.js" />
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/scripts/jquery-ui/css/redmond/jquery-ui-1.8.2.custom.css" />

<script type="text/javascript" charset="utf-8">

$j = jQuery.noConflict();

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
	
});
</script>

<h2><spring:message code="clinicalsummary.title" /></h2>

<spring:message code="clinicalsummary.generateSummaries.instructions"/>

<div id="accordion">
	<h3><a href="#"><spring:message code="clinicalsummary.byLocation" arguments="Generate" /></a></h3>
	<div class="box">
		<form method="post" action="">
			<table>
				<tr>
					<td><spring:message code="clinicalsummary.location"/></td>
					<td>
						<openmrs:fieldGen type="org.openmrs.Location" formFieldName="locationId" val="" parameters="optionHeader=[blank]" />
					</td>
				</tr>
				<tr>
					<td>&nbsp;&nbsp;&nbsp;&nbsp;<spring:message code="clinicalsummary.startObsDate" /></td>
					<td>
						<input type="text" name="startReturnDate" size="10" id="returnDate" onClick="showCalendar(this)" />
					</td>
				</tr>
				<tr>
					<td>&nbsp;&nbsp;&nbsp;&nbsp;<spring:message code="clinicalsummary.endObsDate" /></td>
					<td>
						<input type="text" name="endReturnDate" size="10" id="returnDate" onClick="showCalendar(this)" />
					</td>
				</tr>
				<tr><td colspan="2">&nbsp;</td></tr>
			</table>
			<input type="submit" value="<spring:message code="clinicalsummary.generate"/>"/>
		</form>
	</div>
	
	<h3><a href="#"><spring:message code="clinicalsummary.byIdentifiers" arguments="Generate" /></a></h3>
	<div class="box">
		<form method="post" action="">
			<table>
				<tr>
					<td valign="top"><spring:message code="clinicalsummary.patientIdentifiers" /></td>
					<td valign="top">
						<textarea name="patientIdentifiers"></textarea>
						<i><spring:message code="clinicalsummary.patientIdentifiers.help" /></i>
					</td>
				</tr>
				<tr><td colspan="2">&nbsp;</td></tr>
			</table>
			<input type="submit" value="<spring:message code="clinicalsummary.generate"/>"/>
		</form>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>
