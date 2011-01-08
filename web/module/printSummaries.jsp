<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Print Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/printSummaries.form" />

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
	$j(':input').addClass('ui-state-default');

	// create the accordion pane
	$j('#accordion').accordion({
		autoHeight: false
	});
	
});
</script>

<h2><spring:message code="clinicalsummary.title" /></h2>

<spring:message code="clinicalsummary.printInstructions"/>

<div id="accordion">
	<h3><a href="#"><spring:message code="clinicalsummary.byLocation" arguments="Print" /></a></h3>
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
					<td valign="top"><spring:message code="clinicalsummary.choose"/></td>
					<td valign="top">
						<select name="templateId">
							<c:forEach var="template" items="${templates}" varStatus="varStatus">
								<option value="${template.templateId}" <c:if test="${template.templateId == preferredTemplate.templateId}">selected='true'</c:if> >
									${template.name}
								</option>
							</c:forEach>
						</select>
					</td>
				</tr>
				<tr>
					<td>
						&nbsp;&nbsp;&nbsp;&nbsp;<spring:message code="clinicalsummary.startReturnDate" />
					</td>
					<td>
						<input type="text" 
							   name="startReturnDate" size="10" id="startReturnDate"
							   onClick="showCalendar(this)" />
					</td>
				</tr>
				<tr>
					<td>
						&nbsp;&nbsp;&nbsp;&nbsp;<spring:message code="clinicalsummary.endReturnDate" />
					</td>
					<td>
						<input type="text" 
							   name="endReturnDate" size="10" id="endReturnDate"
							   onClick="showCalendar(this)" />
					</td>
				</tr>
			</table>
			<input type="submit" value="<spring:message code="clinicalsummary.print"/>"/>
		</form>
	</div>
	
	<h3><a href="#"><spring:message code="clinicalsummary.byIdentifiers" arguments="Print" /></a></h3>
	<div class="box">
		<form method="post" action="">
			<table>
				<tr>
					<td valign="top"><spring:message code="clinicalsummary.patientIdentifiers"/></td>
					<td valign="top">
						<textarea name="patientIdentifiers"></textarea>
						<i><spring:message code="clinicalsummary.patientIdentifiers.help"/></i>
					</td>
				</tr>
				<tr>
					<td valign="top"><spring:message code="clinicalsummary.choose"/></td>
					<td valign="top">
						<select name="templateId">
							<c:forEach var="template" items="${templates}" varStatus="varStatus">
								<option value="${template.templateId}" <c:if test="${template.templateId == preferredTemplate.templateId}">selected='true'</c:if> >
									${template.name}
								</option>
							</c:forEach>
						</select>
					</td>
				</tr>
			</table>
			<input type="submit" value="<spring:message code="clinicalsummary.print"/>"/>
		</form>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>
