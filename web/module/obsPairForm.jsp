<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Datalogging Event" otherwise="/login.htm" redirect="/module/datalogging/eventList.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp" %>

<h2>Problematic Obs Pair</h2>

	<b class="boxHeader">
		Patient Demographic
	</b>
	<div class="box">
		<table  width="80%" id="events" cellpadding="2" cellspacing="0">
			<tr>
				<th>Identifier</th>
				<td>${patient.patientIdentifier.identifier}</td>
				<td colspan="4"></td>
			</tr>
			<tr>
				<th>Person Name</th>
				<td>${patient.personName.fullName}</td>
				<td colspan="4"></td>
			</tr>
			<tr>
				<td colspan="6">
					<div id="obsPairList">
						<b class="boxHeader">
							Problematic Obs Pair
						</b>
						<div class="box">
							<table width="90%">
								<tr>
									<th>Date</th>
									<th>Concept</th>
									<th>Answer</th>
									<th>Value</th>
									<th>Status</th>
								</tr>
							<c:forEach var="obsPair" items="${obsPairs}" varStatus="varStatus">
								<tr>
									<td><openmrs:formatDate date="${obsPair.obsDatetime}" type="medium" /></td>
									<c:set var="conceptId" value="${obsPair.concept.conceptId}" />
									<td><c:out value="${conceptNames[conceptId]}" /></td>
									<c:set var="answerId" value="${obsPair.answer.conceptId}" />
									<td><c:out value="${conceptNames[answerId]}" /></td>
									<td>${obsPair.value}</td>
									<td>${obsPair.status}</td>
								</tr>
							</c:forEach>
							</table>
						</div>
					</div>
				</td>
			</tr>
		</table>
	</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>
