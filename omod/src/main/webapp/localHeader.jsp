<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Manage Summaries">
		<li <c:if test='<%= request.getRequestURI().contains("summaryList") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/summary/summaryList.list">
				<spring:message code="clinicalsummary.summary"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Summaries">
		<li <c:if test='<%= request.getRequestURI().contains("mappingList") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/summary/mappingList.list">
				<spring:message code="clinicalsummary.mapping"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Generate Summaries">
		<li <c:if test='<%= request.getRequestURI().contains("evaluateCohort") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/evaluator/evaluateCohort.form">
				<spring:message code="clinicalsummary.generate"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Print Summaries">
		<li <c:if test='<%= request.getRequestURI().contains("printSummaries") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/printer/printSummaries.form">
				<spring:message code="clinicalsummary.print"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Summaries">
		<li <c:if test='<%= request.getRequestURI().contains("downloadSummaries") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/download/downloadSummaries.form">
				<spring:message code="clinicalsummary.download"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Summaries">
		<li <c:if test='<%= request.getRequestURI().contains("uploadSummaries") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/upload/uploadSummaries.form">
				<spring:message code="clinicalsummary.upload"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Summaries">
		<li <c:if test='<%= request.getRequestURI().contains("fileSize") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/utils/fileSize.list">
				<spring:message code="clinicalsummary.filesize"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Summaries">
		<li <c:if test='<%= request.getRequestURI().contains("initialSummaries") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/utils/initialSummaries.form">
				<spring:message code="clinicalsummary.initial"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Summaries">
		<li <c:if test='<%= request.getRequestURI().contains("orderedObsList") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/utils/orderedObsList.list">
				<spring:message code="clinicalsummary.ordered"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Summaries">
		<li <c:if test='<%= request.getRequestURI().contains("reminderList") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/reminder/reminderList.list">
				<spring:message code="clinicalsummary.reminder"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Summaries">
		<li <c:if test='<%= request.getRequestURI().contains("medicationResponseList") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/response/medicationResponseList.list">
				<spring:message code="clinicalsummary.response.medication"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Summaries">
		<li <c:if test='<%= request.getRequestURI().contains("reminderResponseList") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/clinicalsummary/response/reminderResponseList.list">
				<spring:message code="clinicalsummary.response.reminder"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
</ul>
