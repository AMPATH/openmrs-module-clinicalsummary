<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/summary/summaryForm.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="../localHeader.jsp" %>

<openmrs:htmlInclude file="/moduleResources/clinicalsummary/css/form.css"/>

<div id="container">
	<h3 id="header"><spring:message code="clinicalsummary.summary.editing"/></h3>

	<div id="main">
		<form method="post" action="">
			<div id="sidebar">
				<fieldset>
					<legend><spring:message code="clinicalsummary.summary.basic"/></legend>
					<ol>
						<li>
							<label for="revision"><spring:message code="clinicalsummary.summary.revision"/></label>
							<spring:bind path="summary.revision">
								<input id="revision" type="text" name="${status.expression}" value="${status.value}" size="20" readonly="readonly"/>
							</spring:bind>
						</li>
						<li>
							<label for="name"><spring:message code="general.name"/></label>
							<spring:bind path="summary.name">
								<input id="name" type="text" name="${status.expression}" value="${status.value}" size="20"/>
							</spring:bind>
						</li>
						<li>
							<label for="description"><spring:message code="general.description"/></label>
							<spring:bind path="summary.description">
								<textarea id="description" name="${status.expression}" cols="35" rows="3">${status.value}</textarea>
							</spring:bind>
						</li>
					</ol>
				</fieldset>
			</div>
			<div id="maincontent">
				<fieldset>
					<legend><spring:message code="clinicalsummary.summary.transformation"/></legend>
					<ol>
						<li>
							<label for="template"><spring:message code="clinicalsummary.summary.template"/></label>
							<spring:bind path="summary.xml">
								<textarea id="template" name="${status.expression}" cols="75" rows="12"><c:out value="${status.value}"
								                                                                               escapeXml="true"/></textarea>
							</spring:bind>
						</li>
						<li>
							<label for="xslt"><spring:message code="clinicalsummary.summary.xslt"/></label>
							<spring:bind path="summary.xslt">
								<textarea id="xslt" name="${status.expression}" cols="75" rows="12"><c:out value="${status.value}"
								                                                                           escapeXml="true"/></textarea>
							</spring:bind>
						</li>
					</ol>
				</fieldset>
			</div>
			<div id="clear">
				<fieldset class="checkbox">
					<ul>
						<li>
							<spring:bind path="summary.retired">
								<input type="hidden" name="_${status.expression}">
								<input id="retired" type="checkbox" name="${status.expression}" value="true" <c:if
									test="${status.value == true}">checked</c:if>/>
							</spring:bind>
							<label for="retired"><i><spring:message code="clinicalsummary.summary.retired.help"/></i></label>
						</li>
						<li>
							<spring:bind path="summary.autoGenerate">
								<input type="hidden" name="_${status.expression}">
								<input id="preferred" type="checkbox" name="${status.expression}" value="true" <c:if
									test="${status.value == true}">checked</c:if>
								/>
							</spring:bind>
							<label for="autoGenerate"><i><spring:message code="clinicalsummary.summary.autogenerate.help"/></i></label>
						</li>
					</ul>
				</fieldset>
				<fieldset class="submit">
					<ul>
						<c:if test="${not empty summary.creator.person.personName}">
							<li>
								<spring:message code="general.createdBy"/>
								${summary.creator.person.personName} - <openmrs:formatDate date="${summary.dateCreated}" type="long"/>
							</li>
						</c:if>
						<li>
							<input type="submit" value='<spring:message code="clinicalsummary.summary.save"/>'>
						</li>
					</ul>
				</fieldset>
			</div>
		</form>
	</div>

	<%@ include file="/WEB-INF/template/footer.jsp" %>
