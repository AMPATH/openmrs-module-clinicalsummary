<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/summary/mappingForm.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="../localHeader.jsp" %>

<openmrs:htmlInclude file="/moduleResources/clinicalsummary/css/form.css"/>

<div id="container">
	<h3 id="header"><spring:message code="clinicalsummary.mapping"/></h3>

	<div id="main">
		<form method="post" action="">
			<div id="sidebar">
				<fieldset>
					<legend><spring:message code="clinicalsummary.mapping.information"/></legend>
					<ol>
						<li>
							<label for="summary"><spring:message code="clinicalsummary.mapping.summary"/></label>
							<spring:bind path="mapping.summary">
								<select name="summary" id="summary">
									<c:forEach var="summary" items="${summaries}" varStatus="varStatus">
										<option value="${summary.id}"
										<c:if test="${status.value == summary.id}">selected="true"</c:if>>
										${summary.name}
										</option>
									</c:forEach>
								</select>
							</spring:bind>
						</li>
						<li>
							<label for="encounterType"><spring:message code="clinicalsummary.mapping.encounterType"/></label>
							<spring:bind path="mapping.encounterType">
								<select name="encounterType" id="encounterType">
									<c:forEach var="encounterType" items="${encounterTypes}" varStatus="varStatus">
										<option value="${encounterType.id}"
										<c:if test="${status.value == encounterType.id}">selected="true"</c:if>>
										${encounterType.name}
										</option>
									</c:forEach>
								</select>
							</spring:bind>
						</li>
						<li>
							<label for="mappingType"><spring:message code="clinicalsummary.mapping.mappingType"/></label>
							<spring:bind path="mapping.mappingType">
								<select name="mappingType" id="mappingType">
									<c:forEach var="mappingType" items="${mappingTypes}" varStatus="varStatus">
										<option value="${mappingType}"
										<c:if test="${status.value == mappingType}">selected="true"</c:if>>
										${mappingType.value}
										</option>
									</c:forEach>
								</select>
							</spring:bind>
						</li>
					</ol>
				</fieldset>
			</div>
			<div id="clear">
			<fieldset class="submit">
				<ul>
					<li>
						<spring:bind path="mapping.retired">
							<input type="hidden" name="_${status.expression}">
							<input id="retired" type="checkbox" name="${status.expression}" value="true" <c:if
								test="${status.value == true}">checked</c:if>/>
						</spring:bind>
						<label for="retired"><i><spring:message code="clinicalsummary.mapping.retired.help"/></i></label>
					</li>
					<li>
						<input type="submit" value='<spring:message code="clinicalsummary.mapping.save"/>'>
					</li>
				</ul>
			</fieldset>
	</div>
	</form>
</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
