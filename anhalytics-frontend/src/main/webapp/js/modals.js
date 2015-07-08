/*var getHarvestModal = function (found) {

    var harvestModal = '<div class="modal" id="facetview_harvestmodal" style="max-width:800px;width:650px;"> \
                <div class="modal-header"> \
                <a class="facetview_removeharvest close">×</a> \
                <h3>Harvest the result records</h3> \
                </div> \
                <div class="modal-body"> \
				<form class="well">';

    harvestModal += '<div class="control-group"> \
				<label class="control-label" for="input"><b>CouchDB instance</b></label> \
		 		<div class="controls"> \
				<input type="text" class="input-xxlarge" id="input_couchdb" value="localhost:5984"/> \
				</div></div>';

    harvestModal += '<div class="control-group"> \
				<label class="control-label" for="input"><b>Database name</b></label> \
		 		<div class="controls"> \
				<input type="text" class="input-xxlarge" id="input_dbname" value="query' + Math.floor((Math.random() * 100000) + 1) + '"/> \
				</div></div>';

    harvestModal += '<div class="control-group"> \
				<label class="control-label" for="input"><b>nb results</b></label> \
		 		<div class="controls"> \
				from <input type="text" class="input-small" id="input_from" value="0"/> to \
				<input type="text" class="input-small" id="input_to" value="' + found + '"/> \
				with window size <input type="text" class="input-small" id="step_size" value="' + 50 + '"/> \
				</div></div>';

    harvestModal += '<div class="control-group"> \
				<label class="control-label" for="input"><b>Interval time</b></label> \
		 		<div class="controls"> \
				<input type="text" class="input-small" id="input_interval" value="3000"/> ms \
				</div></div>';

    harvestModal += '</form> \
				      <div class="progress progress-striped progress-danger"> \
				          <div class="bar" id="harvest_progress" style="width:0%;"></div> \
                      </div> \
					  <div id="info_progress" /> \
			    </div> \
                <div class="modal-footer"> \
                <a id="facetview_doharvest" href="#" class="btn btn-danger" rel="">Launch</a> \
                <a class="facetview_removeharvest btn close">Close</a> \
                </div>';

    return harvestModal;
}*/

var getEditFilterModal = function (which) {
    var editFilterModal = '<div class="modal" id="facetview_editmodal" style="max-width:800px;width:650px;"> \
                <div class="modal-header"> \
                <a class="facetview_removeedit close">×</a> \
                <h3>Edit the facet parameters</h3> \
                </div> \
                <div class="modal-body"> \
				<form class="well">';

    for (truc in options.facets[which]) {
        if (truc == 'type') {
            editFilterModal += '<div class="control-group"> \
					            <label class="control-label" for="select"><b>type</b></label> \
					            <div class="controls"> \
					              <select id="input_type"> \
					                <option';
            if (options.facets[which]['type'] == 'date') {
                editFilterModal += ' selected ';
            }
            editFilterModal += '>date</option> \
					                <option';
            if (options.facets[which]['type'] == 'class') {
                editFilterModal += ' selected ';
            }
            editFilterModal += '>class</option> \
					                <option';
            if (options.facets[which]['type'] == 'entity') {
                editFilterModal += ' selected ';
            }
            editFilterModal += '>entity</option> \
					                <option';
            if (options.facets[which]['type'] == 'taxonomy') {
                editFilterModal += ' selected ';
            }
            editFilterModal += '>taxonomy</option> \
					                <option';
            if (options.facets[which]['type'] == 'country') {
                editFilterModal += ' selected ';
            }
            editFilterModal += '>country</option> \
					              </select> \
					            </div> \
					          </div>';
        }
        else if (truc == 'view') {
            editFilterModal += '<div class="control-group"> \
					            <label class="control-label" for="select"><b>view</b></label> \
					            <div class="controls"> \
					              <select id="input_type"> \
					                <option';
            if (options.facets[which]['view'] == 'hidden') {
                editFilterModal += ' selected ';
            }
            editFilterModal += '>hidden</option> \
					                <option';
            if (options.facets[which]['view'] == 'graphic') {
                editFilterModal += ' selected ';
            }
            editFilterModal += '>graphic</option> \
					                <option';
            if (options.facets[which]['view'] == 'textual') {
                editFilterModal += ' selected ';
            }
            editFilterModal += '>textual</option> \
					                <option';
            if (options.facets[which]['view'] == 'all') {
                editFilterModal += ' selected ';
            }
            editFilterModal += '>all</option> \
					              </select> \
					            </div> \
					          </div>';
        }
        else {
            editFilterModal += '<div class="control-group"> \
						<label class="control-label" for="input"><b>' + truc + '</b></label> \
				 		<div class="controls"> \
						<input type="text" class="input-xxlarge" id="input_' + truc + '" value="'
                    + options.facets[which][truc] + '"/> \
						</div></div>';
        }
    }

    editFilterModal += '</form> \
			    </div> \
                <div class="modal-footer"> \
                <a id="facetview_dofacetedit" href="#" class="btn btn-primary" rel="' + which + '">Apply</a> \
                <a class="facetview_removeedit btn close">Cancel</a> \
                </div> \
                </div>';

    return editFilterModal;


}

var facetrangeModal = '<div class="modal" id="facetview_rangemodal"> \
                <div class="modal-header"> \
                <a class="facetview_removerange close">×</a> \
                <h3>Set a filter range</h3> \
                </div> \
                <div class="modal-body"> \
                <div style=" margin:20px;" id="facetview_slider"></div> \
                <h3 id="facetview_rangechoices" style="text-align:center; margin:10px;"> \
                <span class="facetview_lowrangeval">...</span> \
                <small>to</small> \
                <span class="facetview_highrangeval">...</span></h3> \
                </div> \
                <div class="modal-footer"> \
                <a id="facetview_dofacetrange" href="#" class="btn btn-primary">Apply</a> \
                <a class="facetview_removerange btn close">Cancel</a> \
                </div> \
                </div>';