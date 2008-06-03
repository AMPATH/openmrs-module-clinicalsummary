<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Manage Clinical Summaries">
		<li <c:if test="<%= request.getRequestURI().contains("summaryList") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/summary.list">
				<spring:message code="clinicalsummary.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="View Clinical Summary">
		<li <c:if test="<%= request.getRequestURI().contains("generateSummaries") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/generateSummaries.form">
				<spring:message code="clinicalsummary.generateSummaries"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Print Clinical Summary">
		<li <c:if test="<%= request.getRequestURI().contains("queue") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/queue.list">
				<spring:message code="clinicalsummary.queue.title"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="View Clinical Summary">
		<li <c:if test="<%= request.getRequestURI().contains("queueDirectory") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/queueDirectory.list">
				<spring:message code="clinicalsummary.directory.title"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:extensionPoint pointId="org.openmrs.admin.module.clinicalsummary.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="${pageContext.request.contextPath}/${link.key}"><spring:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>