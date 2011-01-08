<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/summaryForm.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp" %>
<openmrs:htmlInclude file="/dwr/engine.js"/>
<openmrs:htmlInclude file="/dwr/util.js"/>
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/scripts/jquery/jquery-1.4.2.min.js" />
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/scripts/jquery-ui/js/jquery-ui-1.8.2.custom.min.js" />
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/scripts/jquery-ui/css/redmond/jquery-ui-1.8.2.custom.css" />
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/scripts/jquery/autocomplete/jquery.autocomplete.css" />
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/scripts/jquery/autocomplete/jquery.autocomplete.min.js" /> 
<script type="text/javascript">
	var $j = jQuery.noConflict();

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
		
		$j(".encounterTypeNames").autocomplete('summaryMapping.form', {
				multiple: false,
				minChars: 2,
				mustMatch: true,
				selectFirst: false,
				max: 100,
				delay: 400
		});
	});

	function addMoreMapping() {
		element = document.getElementById("baseMapping");
		
		cloneElement = element.cloneNode(true);
		cloneElement.id = "";
		cloneElement.style.display = "block";

		// remove the attribute, so the getElementById won't confuse because of multiple id
		$j(cloneElement).removeAttr("id");

		// bind the autocomplete to the text. we clone the tr
		$j(cloneElement).find("input:text").autocomplete('summaryMapping.form', {
			multiple: false,
			minChars: 2,
			mustMatch: true,
			selectFirst: false,
			max: 100,
			delay: 400
		});

		mappingLocation = document.getElementById("mappingLocation");
		mappingLocation.appendChild(cloneElement);
	}
	
</script>

<style>
	th { text-align: left; }
</style>

<h2><spring:message code="clinicalsummary.editing" /></h2>

<form method="post" action="">
	<table>
		<spring:bind path="summary.retired">
			<tr>
				<th><spring:message code="general.retired" /></th>
				<td>
					<input type="hidden" name="_${status.expression}">
					<input type="checkbox" name="${status.expression}" id="${status.expression}" value="true" 
						<c:if test="${status.value == true}">checked</c:if> />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					<i><spring:message code="clinicalsummary.retired.help"/></i>
				</td>
			</tr>
		</spring:bind>
		<spring:bind path="summary.revision">
			<tr>
				<th><spring:message code="clinicalsummary.revision"/></th>
				<td><input type="text" name="${status.expression}" value="${status.value}" size="43" readonly="readonly" /></td>
			</tr>
		</spring:bind>
		<spring:bind path="summary.name">
			<tr>
				<th><spring:message code="general.name"/></th>
				<td><input type="text" name="${status.expression}" value="${status.value}" size="43" /></td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</tr>
		</spring:bind>
		<spring:bind path="summary.description">
			<tr>
				<th valign="top"><spring:message code="general.description"/></th>
				<td><textarea name="${status.expression}" cols="41" rows="3">${status.value}</textarea></td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</tr>
		</spring:bind>
		<spring:bind path="summary.preferred">
			<tr>
				<th><spring:message code="general.preferred" /></th>
				<td>
					<input type="hidden" name="_${status.expression}">
					<input type="checkbox" name="${status.expression}" id="${status.expression}" value="true" 
						<c:if test="${status.value == true}">checked</c:if> />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					<i><spring:message code="clinicalsummary.preferred.help"/></i>
				</td>
			</tr>
		</spring:bind>
		<spring:bind path="summary.template">
			<tr>
				<th valign="top"><spring:message code="clinicalsummary.template"/></th>
				<td><textarea name="${status.expression}" cols="115" rows="15"><c:out value="${status.value}" escapeXml="true" /></textarea></td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</tr>
		</spring:bind>
		<spring:bind path="summary.xslt">
			<tr>
				<th valign="top"><spring:message code="clinicalsummary.xslt"/></th>
				<td><textarea name="${status.expression}" cols="115" rows="15"><c:out value="${status.value}" escapeXml="true" /></textarea></td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</tr>
		</spring:bind>
		<tr>
			<td>&nbsp;</td>
			<td>
				<input type="button" value="Add More Mapping" onclick="addMoreMapping()"/>
			</td>
		</tr>
		<tr id="baseMapping" style="display:none">
			<td>
				<input type="text" name="encounterTypeNames" size="30" class="encounterTypeNames" />
			</td>
		</tr>
		<c:if test="${summary.creator != null}">
			<tr>
				<td><spring:message code="general.createdBy" /></td>
				<td>
					${summary.creator.firstName} ${summary.creator.lastName} -
					<openmrs:formatDate date="${summary.dateCreated}" type="long" />
				</td>
			</tr>
		</c:if>
		<c:if test="${summary.changedBy != null}">
			<tr>
				<td><spring:message code="general.changedBy" /></td>
				<td>
					${summary.changedBy.firstName} ${summary.changedBy.lastName} -
					<openmrs:formatDate date="${summary.dateChanged}" type="long" />
				</td>
			</tr>
		</c:if>
		<tr>
			<td valign="top"><spring:message code="clinicalsummary.mapping" /></td>
			<td>
				<table>
					<tbody id="mappingLocation">
						<c:forEach var="type" items="${summary.encounterTypes}">
							<tr>
								<td>
									<input type="text" value="${type.name}" size="30" name="encounterTypeNames" class="encounterTypeNames" />
								</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</td>
		</tr>
		<spring:bind path="summary.position">
		<tr>
			<td><spring:message code="clinicalsummary.mappingPosition" /></td>
			<td>
				<select name="${status.expression}">
					<c:forEach var="position" items="${positions}" varStatus="varStatus">
						<option value="${position}" <c:if test="${status.value == position}">selected='true'</c:if> >
							${position.value}
						</option>
					</c:forEach>
				</select>
			</td>
		</tr>
		</spring:bind>
	</table>
	<br />
	<input type="submit" value='<spring:message code="clinicalsummary.save"/>'>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
