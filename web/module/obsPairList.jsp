<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/pairSearch.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/scripts/jquery/jquery-1.4.2.min.js" />
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/scripts/jquery-ui/js/jquery-ui-1.8.2.custom.min.js" />
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/scripts/jquery-ui/css/redmond/jquery-ui-1.8.2.custom.css" />
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/scripts/jquery/dataTables/js/jquery.dataTables.min.js" />
<openmrs:htmlInclude file="/moduleResources/clinicalsummary/scripts/jquery/dataTables/css/demo_table_jui.css" />

<style>

tr.even.row_highlight {
	background-color: #B0BED9;
}
tr.odd.row_highlight  {
	background-color: #9FAFD1;
}

</style>

<script type="text/javascript" charset="utf-8">

	var oPairTable;

	$j = jQuery.noConflict();

	$j.fn.dataTableExt.oApi.fnSetFilteringDelay = function ( oSettings, iDelay ) {
		/*
		 * Type:        Plugin for DataTables (www.datatables.net) JQuery plugin.
		 * Name:        dataTableExt.oApi.fnSetFilteringDelay
		 * Version:     2.2.1
		 * Description: Enables filtration delay for keeping the browser more
		 *              responsive while searching for a longer keyword.
		 * Inputs:      object:oSettings - dataTables settings object
		 *              integer:iDelay - delay in miliseconds
		 * Returns:     JQuery
		 * Usage:       $('#example').dataTable().fnSetFilteringDelay(250);
		 * Requires:	  DataTables 1.6.0+
		 *
		 * Author:      Zygimantas Berziunas (www.zygimantas.com) and Allan Jardine (v2)
		 * Created:     7/3/2009
		 * Language:    Javascript
		 * License:     GPL v2 or BSD 3 point style
		 * Contact:     zygimantas.berziunas /AT\ hotmail.com
		 */
		var
			_that = this,
			iDelay = (typeof iDelay == 'undefined') ? 250 : iDelay;
		
		this.each( function ( i ) {
			$j.fn.dataTableExt.iApiIndex = i;
			var
				$this = this, 
				oTimerId = null, 
				sPreviousSearch = null,
				anControl = $j( 'input', _that.fnSettings().aanFeatures.f );
			
				anControl.unbind( 'keyup' ).bind( 'keyup', function() {
				var $$this = $this;

				if (sPreviousSearch === null || sPreviousSearch != anControl.val()) {
					window.clearTimeout(oTimerId);
					sPreviousSearch = anControl.val();	
					oTimerId = window.setTimeout(function() {
						$j.fn.dataTableExt.iApiIndex = i;
						_that.fnFilter( anControl.val() );
					}, iDelay);
				}
			});
			
			return this;
		} );
		return this;
	}
		
	$j(document).ready(function() {
		
		oPairTable = $j('#pairs').dataTable({
			 "iDisplayLength": 25,
			 "bStateSave": true,
			 "bJQueryUI": true,
			 "bProcessing": true,
			 "bServerSide": true,
	         "bAutoWidth": false,
			 "sAjaxSource": 'obsPairSearch.form',
			 "sPaginationType": "full_numbers",
			 "aaSorting": [[ 1, "desc" ]],
			 "aoColumns": [
			                 { "bVisible": false },
			                 { "bSortable": false, "sWidth": "8%" },
			                 { "bSortable": false, "sWidth": "10%" },
			                 { "bSortable": false, "sWidth": "10%" },
			                 { "bSortable": false, "sWidth": "20%" },
			                 { "bSortable": false, "sWidth": "8%" },
	             ]
		}).fnSetFilteringDelay(1000);

		$j("#accordion").accordion({
			autoHeight: false,
		});
		
		$j("#pairs tbody").click(function(event) {
			$j(oPairTable.fnSettings().aoData).each(function (){
				$j(this.nTr).removeClass('row_highlight');
			});
			$j(event.target.parentNode).addClass('row_highlight');

			// find the position of the selected row
			eventPosition = oPairTable.fnGetPosition(event.target.parentNode);
			// get the data for the selected row
			eventData = oPairTable.fnGetData(eventPosition);

			var form = document.createElement("form");
			form.setAttribute("method", "post");
		    form.setAttribute("action", "obsPairForm.form");
		    
	        var textInput = document.createElement("input");
			textInput.setAttribute("type", "hidden");
			textInput.setAttribute("name", "patientId");
			textInput.setAttribute("value", eventData[0]);

			form.appendChild(textInput);
			document.body.appendChild(form);
			
		    form.submit();
		});
	});
</script>

<div id="accordion">
	<h3><a href="#">Search Obs Pair Files</a></h3>
	<div class="box">
		<table width="100%" id="pairs">
			<thead>
				<tr>
					<th>Internal</th>
					<th>Identifier</th>
					<th>Given Name</th>
					<th>Middle Name</th>
					<th>Family Name</th>
					<th>Total Problem</th>
				</tr>
			</thead>
			<tbody>
			</tbody>
		</table>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>