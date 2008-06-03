<%@ include file="/WEB-INF/template/include.jsp"%>

<%@page import="org.apache.fop.area.Page"%>
<openmrs:require privilege="Manage Clinical Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/queue.list" />
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp" %>

<style type="text/css">
#content b.boxHeader a {
	color: white;
}
</style>

<script type="text/javascript">
	
function doSort(column) {
	document.forms[0].sortColumn.value=column;
	document.forms[0].submit();
}

function setSubmitMsg(msg) {
	document.forms[0].submitMsg.value=msg;
}

function doConfirm( ) {
	if (window.confirm(document.forms[0].submitMsg.value + " ?")) return true;
       	return false;	
}

function turnPage(nextPage) {
	if (nextPage == 'showStart') {
		document.forms[0].offset.value="0";
		//document.forms[0].currentPage.value="1";
	}
	else if (nextPage == 'showAll') {
		document.forms[0].offset.value="0";
		document.forms[0].limit.value="0";
		//document.forms[0].currentPage.value="1";
	}
	else if (nextPage == 'showPrev') {
		var ofs = parseInt(document.forms[0].offset.value, 10);
		var lmt = parseInt(document.forms[0].limit.value, 10);
		if (isNaN(ofs)) ofs = 0;
		if (isNaN(lmt)) lmt = 50;
		if (ofs - lmt <= 0) {
			document.forms[0].offset.value="0";
			//document.forms[0].currentPage.value="1";
		}
		else {
			document.forms[0].offset.value=(ofs - lmt);
			//document.forms[0].currentPage.value=(Math.ceil(ofs/lmt));
		}
	}
	else if (nextPage == 'showNext') {
		var ofs = parseInt(document.forms[0].offset.value, 10);
		var lmt = parseInt(document.forms[0].limit.value, 10);
		if (isNaN(ofs)) ofs = 0;
		if (isNaN(lmt)) lmt = 50;
		document.forms[0].offset.value=(ofs + lmt);
		//if (ofs <= lmt)
			//document.forms[0].currentPage.value="1";
		//else
			//document.forms[0].currentPage.value=(Math.ceil(ofs/lmt));
	}
	else { }
	document.forms[0].submit();
}

function setLimit(limitNum) {
	if (limitNum == 'All') 
		document.forms[0].limit.value="0";
	else
		document.forms[0].limit.value=limitNum;
	showLimit();
}

function showLimit( ) {
	if (document.forms[0].limit.value == "") {
		document.forms[0].limit.value = "50";
	}
	var pageLinks = document.getElementsByName("limitHref");
	for (var i=0; i<pageLinks.length; i++) {
		if (pageLinks[i].id == document.forms[0].limit.value) {
			pageLinks[i].className = "largerFont";
		}
		else {
			pageLinks[i].className = "";
		}
	}
}

function selectAll(current) {
	if (current.id == "topSelectAll") {
		document.getElementById("bottomSelectAll").checked = current.checked;
	}
	else {
		document.getElementById("topSelectAll").checked = current.checked;
	}
	var items = document.getElementsByName("queueId");
	//document.write(items.length);
	for (var i=0; i < items.length; i++)
		if (items[i].parentNode.parentNode.style.display != "none")
			items[i].checked = current.checked;
}

</script>

<h2><spring:message code="clinicalsummary.queue.title" /></h2>

<form id="filterOptions" action="" method="post" onsubmit="return doConfirm()">
	<b class="boxHeader">
		<spring:message code="clinicalsummary.queue.filter" />
	</b>
	<input type="hidden" name="submitMsg" value="${param.submitMsg}"/>
	<div class="box">
		<table width="90%" cellpadding="2" cellspacing="0">
			<tr>
				<th><spring:message code="clinicalsummary.queue.applyFilter" /></th>
				<th><spring:message code="Location.title" /></th>
				<th><spring:message code="Encounter.datetime" /><input type="hidden" name="datePattern" value="<openmrs:datePattern />"/>
				<i style="font-weight: normal; font-size: 0.8em;">(<spring:message code="general.format"/>: <openmrs:datePattern />)</i>
				</th>
				<th><spring:message code="clinicalsummary.queue.status" /></th>
				<th><spring:message code="clinicalsummary.queue.results" /></th>
			</tr>
			<tr>
				<td>
				<input type="button" name="selectFilter" onClick="turnPage('showStart')" value="<spring:message code='clinicalsummary.queue.applyFilter'/>" />	
				</td>
				<td>
					<select name="locationFilter"  >
						<option value="" > </option>
						<openmrs:forEachRecord name="location">
						<c:choose><c:when test="${record.name == param.locationFilter}"><option value="${record.name}" selected="true">${record.name}</option></c:when><c:otherwise><option value="${record.name}" >${record.name}</option></c:otherwise></c:choose>
						</openmrs:forEachRecord>
					</select>&nbsp;
				</td>
				<td id="startDate">Start
					<input type="text" name="startDate" value="${param.startDate}" onClick="showCalendar(this)" /> &nbsp; End
					<input type="text" name="endDate" value="${param.endDate}" onClick="showCalendar(this)"  />
				</td>
				<td>
					<select name="statusFilter" >
						<option value=""> </option>
						<c:forEach var="s" items="${status}">
						<c:choose><c:when test="${param.statusFilter == s}"><option value="${s}" selected="true">${s}</option></c:when><c:otherwise><option value="${s}">${s}</option></c:otherwise></c:choose>
						</c:forEach>
					</select>
				</td>
				<td>
					<select name="limit" >
						<option value="${param.limit}">${param.limit}</option>
						<c:forEach var="r" items="${queuePage}">
						<c:choose><c:when test="${param.rowFilter == r}"><option value="${r}" selected="true">${r}</option></c:when><c:otherwise><option value="${r}">${r}</option></c:otherwise></c:choose>
						</c:forEach>
					</select>
				</td>
			</tr>
		</table>
	</div>
<br/>
	<input type="checkbox" id="topSelectAll" onClick="selectAll(this)"/><label for="topSelectAll"><spring:message code="clinicalsummary.queue.selectAll"/></label>
	&nbsp;
	<input type="submit" name="action" value='<spring:message code="clinicalsummary.queue.generateSummaries"/>' onclick="setSubmitMsg(this.value)"/>
	&nbsp;
	<input type="submit" name="action" value='<spring:message code="clinicalsummary.queue.printSummaries"/>' onclick="setSubmitMsg(this.value)" />
	&nbsp;
	<input type="submit" name="action" value='<spring:message code="clinicalsummary.queue.removeSummaries"/>' onclick="setSubmitMsg(this.value)" />
	&nbsp;&nbsp;
	<br/>
	<b class="boxHeader" > 
		<spring:message code="clinicalsummary.queue.select" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		<spring:message code="clinicalsummary.queue.view" /><input type="hidden" name="offset" value="${param.offset}"/>
		(<a href="#" name="showAll" onclick="turnPage(this.name)"/><spring:message code="clinicalsummary.queue.all" /></a>)
		<c:if test="${param.offset > 0 || param.limit == 0}">
			(<a href="#" name="showStart" onclick="turnPage(this.name)"/><spring:message code="clinicalsummary.queue.start" /></a>)
			(<a href="#" name="showPrev" onclick="turnPage(this.name)"/><spring:message code="clinicalsummary.queue.previous" /></a>)
		</c:if>
		(<a href="#" name="showNext" onclick="turnPage(this.name)"/><spring:message code="clinicalsummary.queue.next" /></a>)
		<spring:message code="clinicalsummary.queue.results" />.&nbsp;&nbsp;&nbsp;&nbsp;  
		<%-- [<spring:message code="clinicalsummary.queue.page" /><input type="hidden" name="currentPage" value="${param.currentPage}" />
		<c:choose><c:when test="${param.currentPage > 0}">${param.currentPage}</c:when><c:otherwise>1</c:otherwise></c:choose>] --%>
		<%-- (<c:forEach var="qPage" items="${queuePage}"><a id="${qPage}" href="#" name="limitHref" onclick="setLimit(this.id)">${qPage}</a>&nbsp;</c:forEach>) --%>
	</b>
	<div class="box">
		<table width="90%" cellpadding="2" cellspacing="0">
			<tr>
				<th>
					<input type="hidden" name="sortColumn" value="${param.sortColumn}" />
				</th>
				<th><spring:message code="general.name" /></th>
				<th><a href="#" name="<spring:message code='Patient.identifier' />" onclick="doSort(this.name)"><spring:message code='Patient.identifier' /></a></th>
				<th><spring:message code="Location.title" /></th>
				<th><a href="#" name="<spring:message code='Encounter.datetime' />" onclick="doSort(this.name)"><spring:message code="Encounter.datetime" /></a></th>
				<th><spring:message code="clinicalsummary.queue.status" /></th>
				<th><spring:message code="clinicalsummary.queue.filename" /></th>
			</tr>
		<c:set var="prevLocId" value="-1" /><c:set var="firstLocId" value="-1" />
		<c:forEach var="item" items="${queueItems}" varStatus="varStatus">
			<c:set var="itemLocationId" value="${item.location.locationId}" />
			<c:if test="${varStatus.first == true}"><c:set var="firstLocId" value="${item.location.locationId}" /></c:if>
			<c:if test="${item.location.locationId != prevLocId && item.location.locationId != firstLocId}"><tr name="trSpace"><td> <br/> </td></tr><c:set var="prevLocId" value="${item.location.locationId}" /></c:if>
			<tr name="queueItemRow" id="${varStatus.index}" class="<c:choose><c:when test="${varStatus.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose><c:if test="${item.status == 'ERROR'}"> error</c:if>">
				<td><input type="checkbox" id="${item.location.locationId}" name="queueId" value="${item.clinicalSummaryQueueId}" /></td>
				<td>${item.patient.personName}</td>
				<td>${item.patient.patientIdentifier}</td>
				<td name="tdLocation" id="${item.location.name}">${item.location.name}</td>
				<td name="tdDate" id="${item.encounterDatetime}"><openmrs:formatDate date="${item.encounterDatetime}" type="medium" /></td>
				<td name="tdStatus" id="${item.status}">${item.status}</td>
				<td name="tdFileName" id="${item.fileName}">${item.fileName}</td>
			</tr>
		</c:forEach>
		</table>
	</div>
	
	<input type="checkbox" id="bottomSelectAll" onClick="selectAll(this)"/><label for="bottomSelectAll"><spring:message code="clinicalsummary.queue.selectAll"/></label>
	&nbsp;
	<input type="submit" name="action" value='<spring:message code="clinicalsummary.queue.generateSummaries"/>' onclick="setSubmitMsg(this.value)" />
	&nbsp;
	<input type="submit" name="action" value='<spring:message code="clinicalsummary.queue.printSummaries"/>' onclick="setSubmitMsg(this.value)" />
	&nbsp;
	<input type="submit" name="action" value='<spring:message code="clinicalsummary.queue.removeSummaries"/>' onclick="setSubmitMsg(this.value)" />
	
</form>

<script type="text/javascript">
showLimit();	
</script>

<%@ include file="/WEB-INF/template/footer.jsp"%>


