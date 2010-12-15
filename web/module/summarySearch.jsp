<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Summaries" otherwise="/login.htm" redirect="/module/clinicalsummary/summarySearch.form" />

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

	var oIndexTable;
	var oPrintedTable;

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
		
		oIndexTable = $j('#indexes').dataTable({
			 "iDisplayLength": 25,
			 "bStateSave": true,
			 "bJQueryUI": true,
			 "bProcessing": true,
			 "bServerSide": true,
	         "bAutoWidth": false,
			 "sAjaxSource": 'summarySearchIndex.form',
			 "sPaginationType": "full_numbers",
			 "aaSorting": [[ 9, "desc" ]],
			 "aoColumns": [
			                 { "bVisible": false },
			                 { "bSortable": false, "sWidth": "8%" },
			                 { "bSortable": false, "sWidth": "10%" },
			                 { "bSortable": false, "sWidth": "10%" },
			                 { "bSortable": false, "sWidth": "10%" },
			                 { "bSortable": false, "sWidth": "20%" },
			                 { "bSortable": false, "sWidth": "8%" },
			                 { "bSortable": false, "sWidth": "14%" },
			                 { "bSortable": false, "sWidth": "8%" },
			                 { "bSortable": false, "sWidth": "8%" }
	             ]
		}).fnSetFilteringDelay(1000);

		oPrintedTable = $j('#printed').dataTable({
			"bStateSave": true,
			"bJQueryUI": true,
			"bProcessing": true,
	        "bAutoWidth": false,
			"sPaginationType": "full_numbers",
			"aaSorting": [[ 9, "desc" ]],
			"aoColumns": [
			                 { "bVisible": false },
			                 { "bSortable": false, "sWidth": "8%" },
			                 { "bSortable": false, "sWidth": "10%" },
			                 { "bSortable": false, "sWidth": "10%" },
			                 { "bSortable": false, "sWidth": "10%" },
			                 { "sWidth": "20%" },
			                 { "sWidth": "8%" },
			                 { "sWidth": "14%" },
			                 { "sWidth": "8%" },
			                 { "sWidth": "8%" }
     		]
		});

		
		$j("#indexes tbody").click(function(event) {
			$j(oIndexTable.fnSettings().aoData).each(function (){
				$j(this.nTr).removeClass('row_highlight');
			});
			$j(event.target.parentNode).addClass('row_highlight');

			// find the position of the selected row
			indexPosition = oIndexTable.fnGetPosition(event.target.parentNode);
			// get the data for the selected row
			indexData = oIndexTable.fnGetData(indexPosition);

			// check the data in the printed data to prevent adding the same data
			printedData = oPrintedTable.fnGetData();
			foundData = false;

			oPrintedTable.fnClearTable(false);
			
			for (i = 0; i < printedData.length; i++) {
				if (typeof printedData[i] != 'undefined' && printedData[i] != null) {
					oPrintedTable.fnAddData(printedData[i]);
					if (printedData[i][0] == indexData[0])
						foundData = true;
				}
			}

			if (!foundData)
				oPrintedTable.fnAddData(indexData);
		});


		$j("#printed tbody").click(function(event) {
			indexPosition = oPrintedTable.fnGetPosition(event.target.parentNode);
			deletedData = oPrintedTable.fnDeleteRow(indexPosition, function(){}, true);

			/*
			printedData = oPrintedTable.fnGetData();
			for (i = 0; i < printedData.length; i++) {
				if (printedData[i][0] == deletedData[0]) {
					printedData.splice(i, 1);
					break;
				}
			}
			var oSettings = oPrintedTable.fnSettings();
			oSettings.aoData = printedData;
			*/
		});

		$j('#printSummaries').click(function(event) {
			printedData = oPrintedTable.fnGetData();

			// push all index_id into the server data
			// the server will accept this as array of integer
			
			var form = document.createElement("form");
			form.setAttribute("method", "post");
		    form.setAttribute("action", "summaryIndexPrinter.form");

			for (i = 0; i < printedData.length; i++) {
		        var textInput = document.createElement("input");
				textInput.setAttribute("type", "hidden");
				textInput.setAttribute("name", "printedIndexes");
				textInput.setAttribute("value", printedData[i][0]);

		        form.appendChild(textInput);
		    }
		    
			document.body.appendChild(form);
			
		    form.submit();
		    
			/*
			// this is the post using jQuery but it's not working because ajax can only return text
			serverData = [];
			for (i = 0; i < printedData.length; i++)
				serverData.push({"name": "printedIndexes", "value": printedData[i][0]});
			
			$j.ajax({
				type: 'POST',
				url: "summaryIndexPrinter.form",
				data: serverData,
			});
			*/
			
		});	

		$j("#accordion").accordion({
			autoHeight: false,
			collapsible: true
		});
	});
</script>


<h2><spring:message code="clinicalsummary.search" /></h2>

<spring:message code="clinicalsummary.searchInstructions" />

<div id="accordion">
	<h3><a href="#">Search Summary Files</a></h3>
	<div class="box">
		<table width="100%" id="indexes">
			<thead>
				<tr>
					<th>Index Id</th>
					<th>Identifier</th>
					<th>Given Name</th>
					<th>Middle Name</th>
					<th>Family Name</th>
					<th>Location</th>
					<th>Return Date</th>
					<th>Template</th>
					<th>Initial Date</th>
					<th>Generated</th>
				</tr>
			</thead>
			<tbody>
			</tbody>
		</table>
	</div>
	<h3><a href="#">Selected Summary Files</a></h3>
	<div class="box">
		<table width="100%" id="printed">
			<thead>
				<tr>
					<th>Index Id</th>
					<th>Identifier</th>
					<th>Given Name</th>
					<th>Middle Name</th>
					<th>Family Name</th>
					<th>Location</th>
					<th>Return Date</th>
					<th>Template</th>
					<th>Initial Date</th>
					<th>Generated</th>
				</tr>
			</thead>
			<tbody>
			</tbody>
		</table>
		<p>
			<a id="printSummaries" href="javascript:void(0)">Print Summaries</a>
		</p>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp"%>