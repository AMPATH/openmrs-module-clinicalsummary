<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:require privilege="Manage Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/utils/orderedObsList.list"/>


<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="../localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables.css" />
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables_jui.css" />

<openmrs:htmlInclude file="/moduleResources/clinicalsummary/css/form.css"/>


<script>
	$j = jQuery.noConflict();

    $j(document).ready(function() {

		var datatable = $j('#result').dataTable();

		$j("#search").click(function() {
			var data = $j("#form").serialize();
			$j.ajax({
				url: "reminderResponse.form",
				type: "POST",
				dataType: 'json',
				data: data,
				success: function(data) {
					datatable.fnClearTable();
					datatable.fnAddData(data);
				}
			});
		});

	});
</script>

<style type="text/css">
	.header {
        white-space: nowrap;
	}

	.dataTables_filter {
		padding: 1em;
	}

	.dataTables_length {
		padding: 1em;
	}

	td, th {
		color: #333333;
	}

	td {
		font-weight: normal;
	}
</style>

<div id="container">
	<h3 id="header">Reminder Responses</h3>

	<div id="main">
        <div id="sidebar">
            <form id="form" method="post" action="">
                <fieldset>
                    <legend>Response Parameter</legend>
                    <ol>
                        <li>
                            <label for="locationId"><spring:message code="clinicalsummary.print.location"/></label>
                            <openmrs:fieldGen type="org.openmrs.Location" formFieldName="locationId" val="" parameters="optionHeader=[blank]"/>
                        </li>
                        <li>
                            <label for="startTime">Start Time</label>
                            <input type="text" name="startTime" size="10" id="startTime" onClick="showCalendar(this)"/>
                        </li>
                        <li>
                            <label for="endTime">End Time</label>
                            <input type="text" name="endTime" size="10" id="endTime" onClick="showCalendar(this)"/>
                        </li>
                        <li>
                            <input type="button" id="search" value="<spring:message code="clinicalsummary.ordered.search"/>"/>
                        </li>
                    </ol>
                </fieldset>
            </form>
        </div>
		<div id="rightcontent" style="width: 100%">
			<table cellpadding="0" width="100%" cellspacing="0" border="0" class="display" id="result">
				<thead>
					<tr>
						<th class="header">Patient</th>
                        <th class="header">Provider</th>
                        <th class="header">Response Datetime</th>
                        <th class="header">Reminder Token</th>
                        <th class="header">Reminder Response</th>
                        <th class="header">Reminder Comment</th>
					</tr>
				</thead>
			</table>
		</div>
		<div id="clear">
		</div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>
