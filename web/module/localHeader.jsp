<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Manage Summaries">
		<li <c:if test='<%= request.getRequestURI().contains("summaryList") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/summaryList.list">
				<spring:message code="clinicalsummary.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Generate Summaries">
		<li <c:if test='<%= request.getRequestURI().contains("generateSummaries") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/generateSummaries.form">
				<spring:message code="clinicalsummary.generateSummaries"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Print Summaries">
		<li <c:if test='<%= request.getRequestURI().contains("printSummaries") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/printSummaries.form">
				<spring:message code="clinicalsummary.printSummaries"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Summaries">
		<li <c:if test='<%= request.getRequestURI().contains("threadDownload") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/threadDownload.form">
				<spring:message code="clinicalsummary.threadDownload"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Summaries">
		<li <c:if test='<%= request.getRequestURI().contains("threadUpload") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/threadUpload.form">
				<spring:message code="clinicalsummary.threadUpload"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Print Summaries">
		<li <c:if test='<%= request.getRequestURI().contains("summarySearch") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/summarySearch.form">
				<spring:message code="clinicalsummary.search"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Summaries">
		<li <c:if test='<%= request.getRequestURI().contains("initialSummary") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/initialSummary.form">
				<spring:message code="clinicalsummary.initial"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Summaries">
		<li <c:if test='<%= request.getRequestURI().contains("obsPairList") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/obsPairList.form">
				<spring:message code="clinicalsummary.obs.pair"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
</ul>