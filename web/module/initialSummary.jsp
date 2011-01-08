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
	
	$j('#locationId').change(function() {
		jQuery.getJSON("initialSummaryDate.form", {"locationId" : $j('#locationId').attr("value")}, function(data) {
			$j('#initialDate').attr("value", data.date);
		});
	});

	$j('input:text').click(function(event) {
		showCalendar(event.target);
	});
	
	// create the accordion pane
	$j('#accordion').accordion({
		autoHeight: false
	});
	
	// init all input to use jquery css
	$j(':input').addClass('ui-state-default');
});
</script>

<spring:message code="clinicalsummary.initialInstruction"/>

<div id="accordion">
	<h3><a href="#"><spring:message code="clinicalsummary.initialHeader" /></a></h3>
	<div class="box">
		<form method="post" action="">
			<table>
				<tr>
					<td><spring:message code="clinicalsummary.location"/></td>
					<td><openmrs:fieldGen type="org.openmrs.Location" formFieldName="locationId" val="" parameters="optionHeader=[blank]" /></td>
				</tr>
				<tr>
					<td>&nbsp;&nbsp;&nbsp;&nbsp;<spring:message code="clinicalsummary.startReturnDate" /></td>
					<td>
						<input type="text" name="initialDate" size="10" id="initialDate" value="${defaultDate}"/>
						<i><spring:message code="clinicalsummary.defaultInitial" /></i>
					</td>
				</tr>
			</table>
			<input type="submit" value="<spring:message code="clinicalsummary.update"/>"/>
		</form>
	</div>
</div>
<%@ include file="/WEB-INF/template/footer.jsp"%>