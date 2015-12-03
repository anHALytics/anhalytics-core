var textFieldPatents = ['$teiCorpus.$TEI.$teiHeader.$fileDesc.$titleStmt.$title.',
    '$teiCorpus.$TEI.$text.$front.$div.$p.',
    '$teiCorpus.$TEI.$text.$body.$div.$div.',
    '$teiCorpus.$TEI.$text.$body.$div.$p.'
];

var textFieldNPL = ['$teiCorpus.$teiHeader.$titleStmt.$title.$title-first',
    '$teiCorpus.$text.$front.$div.$p.',
    '$teiCorpus.$text.$body.$head.',
    '$teiCorpus.$text.$body.$div.',
    '$teiCorpus.$text.$body.$figure.$head.',
    '$teiCorpus.$text.$body.$p.'
];

var textFieldsPatentReturned = ['$teiCorpus.$TEI.$teiHeader.$fileDesc.$titleStmt.$title.$lang_de',
    '$teiCorpus.$TEI.$teiHeader.$fileDesc.$titleStmt.$title.$lang_en',
    '$teiCorpus.$TEI.$teiHeader.$fileDesc.$titleStmt.$title.$lang_fr',
    '$teiCorpus.$TEI.$teiHeader.$fileDesc.$sourceDesc.$biblStruct.$monogr.$date',
    '_id'
];

var textFieldsNPLReturned = ['$teiCorpus.$teiHeader.$titleStmt.$title.$title-first',
    '$teiCorpus.$teiHeader.$titleStmt.xml:id',
    '$teiCorpus.$teiHeader.$sourceDesc.$biblStruct.$monogr.$imprint.$date',
    '$teiCorpus.$teiHeader.$editionStmt.$edition.$date',
    '$teiCorpus.$teiHeader.$sourceDesc.$biblStruct.$author.$persName.$surname',
    '$teiCorpus.$teiHeader.$sourceDesc.$biblStruct.$author.$persName.$forename',
    '$teiCorpus.$teiHeader.$sourceDesc.target',
//			'$teiCorpus.$teiHeader.$profileDesc.$textClass.$classCode.$scheme_halTypology',
    '_id'
];

// build the search query URL based on current params
var elasticsearchquery = function () {
    var qs = {};
    var bool = false;
    var should = false;
    var must_not = false;
    //var nested = false;
    var filtered = false; // true if a filter at least applies to the query
    var queried_fields = []; // list of queried fields for highlights   

    // fields to be returned
    if (options['collection'] == 'patent')
        qs['fields'] = textFieldsPatentReturned;
    else
        qs['fields'] = textFieldsNPLReturned;

    if (options['mode_query'] == 'complex') {
        var rank = 1;
        // first we build the query based on the search fields
        for (rank = 1; rank <= options['complex_fields']; rank++) {

            // modality is one of must, should and must_not
            var modality = $('#label2_facetview_searchbar' + rank).text();

            if ($('#facetview_freetext' + rank).val() != "") {
                // init bool clause
                if (!bool) {
                    if (modality == 'must')
                        bool = {'must': []};
                    else if (modality == 'should')
                        bool = {'should': []};
                    else
                        bool = {'must_not': []};
                }
                else {
                    if (modality == 'must')
                        bool['must'] = [];
                    else if (modality == 'should')
                        bool['should'] = [];
                    else
                        bool['must_not'] = [];
                }

                if ($('#label1_facetview_searchbar' + rank).text() == "all text") {
                    var obj = {'query_string': {'default_operator': 'AND'}};
                    obj['query_string']['query'] = $('#facetview_freetext' + rank).val();
                    queried_fields.push("_all");
                }
                else if ($('#label1_facetview_searchbar' + rank).text() == "all titles") {
                    var obj = {'query_string': {'default_operator': 'AND'}};
                    var theField;
                    if (options['collection'] == 'npl') {
                        theField = "$teiCorpus.$teiHeader.$fileDesc.$titleStmt.$title.";
                    }
                    else {
                        theField = "$teiCorpus.$teiHeader.$fileDesc.$titleStmt.$title.";
                    }
                    if (($('#label3_facetview_searchbar' + rank).text() == "all") ||
                            ($('#label3_facetview_searchbar' + rank).text() == "lang")) {
                        theField += "\\*";
                    }
                    else if ($('#label3_facetview_searchbar' + rank).text() == "de") {
                        theField += "$lang_de";
                    }
                    else if ($('#label3_facetview_searchbar' + rank).text() == "fr") {
                        theField += "$lang_fr";
                    }
                    else {
                        theField += "$lang_en";
                    }
                    obj['query_string']['query'] =
                            theField + ":" + $('#facetview_freetext' + rank).val();
                    queried_fields.push(theField);
                }
                else if ($('#label1_facetview_searchbar' + rank).text() == "all abstracts") {
                    var obj = {'query_string': {'default_operator': 'AND'}};
                    var theField;
                    if (options['collection'] == 'npl') {
                        theField = "$teiCorpus.$teiHeader.$text.$front.$div.$p.";
                    }
                    else {
                        theField = "$teiCorpus.$teiHeader.$text.$front.$div.$p.";
                    }
                    if (($('#label3_facetview_searchbar' + rank).text() == "all") ||
                            ($('#label3_facetview_searchbar' + rank).text() == "lang")) {
                        theField += "\\*";
                    }
                    else if ($('#label3_facetview_searchbar' + rank).text() == "de") {
                        theField += "$lang_de";
                    }
                    else if ($('#label3_facetview_searchbar' + rank).text() == "fr") {
                        theField += "$lang_fr";
                    }
                    else {
                        theField += "$lang_en";
                    }
                    obj['query_string']['query'] =
                            theField + ":" + $('#facetview_freetext' + rank).val();
                    obj['query_string']['analyze_wildcard'] = false;
                    queried_fields.push(theField);
                }
                else if ($('#label1_facetview_searchbar' + rank).text() == "claims") {
                    // this one is for patent only
                    var obj = {'query_string': {'default_operator': 'AND'}};
                    var theField = "$teiCorpus.$teiHeader.$text.$body.$div.$div.";
                    if (($('#label3_facetview_searchbar' + rank).text() == "all") ||
                            ($('#label3_facetview_searchbar' + rank).text() == "lang")) {
                        theField += "\\*";
                    }
                    else if ($('#label3_facetview_searchbar' + rank).text() == "de") {
                        theField += "$lang_de";
                    }
                    else if ($('#label3_facetview_searchbar' + rank).text() == "fr") {
                        theField += "$lang_fr";
                    }
                    else {
                        theField += "$lang_en";
                    }
                    obj['query_string']['query'] =
                            theField + ":" + $('#facetview_freetext' + rank).val();
                    queried_fields.push(theField);
                }
                else if ($('#label1_facetview_searchbar' + rank).text() == "description") {
                    // this one is for patent only
                    var obj = {'query_string': {'default_operator': 'AND'}};
                    var theField = "$teiCorpus.$teiHeader.$text.$body.$div.$p.";
                    if (($('#label3_facetview_searchbar' + rank).text() == "all") ||
                            ($('#label3_facetview_searchbar' + rank).text() == "lang")) {
                        theField += "\\*";
                    }
                    else if ($('#label3_facetview_searchbar' + rank).text() == "de") {
                        theField += "$lang_de";
                    }
                    else if ($('#label3_facetview_searchbar' + rank).text() == "fr") {
                        theField += "$lang_fr";
                    }
                    else {
                        theField += "$lang_en";
                    }
                    obj['query_string']['query'] =
                            theField + ":" + $('#facetview_freetext' + rank).val();
                    obj['query_string']['analyze_wildcard'] = false;
                    queried_fields.push(theField);
                }
                else if ($('#label1_facetview_searchbar' + rank).text() == "full text") {
                    // this one is for NPL only
                    var obj = {'query_string': {'default_operator': 'AND'}};
                    var theField = "$teiCorpus.$teiHeader.$text.$body.$div.$p.";
                    if (($('#label3_facetview_searchbar' + rank).text() == "all") ||
                            ($('#label3_facetview_searchbar' + rank).text() == "lang")) {
                        theField += "\\*";
                    }
                    else if ($('#label3_facetview_searchbar' + rank).text() == "de") {
                        theField += "$lang_de";
                    }
                    else if ($('#label3_facetview_searchbar' + rank).text() == "fr") {
                        theField += "$lang_fr";
                    }
                    else {
                        theField += "$lang_en";
                    }
                    obj['query_string']['query'] =
                            theField + ":" + $('#facetview_freetext' + rank).val();
                    obj['query_string']['analyze_wildcard'] = false;
                    queried_fields.push(theField);
                }
                else if ($('#label1_facetview_searchbar' + rank).text() == "IPC class") {
                    // this one is for patent only
                    var obj = {'query_string': {'default_operator': 'AND'}};
                    //var theField = "$teiCorpus.$teiCorpus.$teiHeader.$profileDesc.$textClass.$classCode.$term";
                    var theField =
                            "$teiCorpus.$teiHeader.$profileDesc.$textClass.$classCode.$scheme_ipc.$term";
                    obj['query_string']['query'] =
                            theField + ":" + $('#facetview_freetext' + rank).val();
                    queried_fields.push(theField);
                }
                else if ($('#label1_facetview_searchbar' + rank).text() == "ECLA class") {
                    // this one is for patent only
                    var obj = {'query_string': {'default_operator': 'AND'}};
                    var theField = "$teiCorpus.$teiHeader.$profileDesc.$textClass.$classCode.$scheme_patent-classification.$ecla.$path";
                    obj['query_string']['query'] =
                            theField + ":" + $('#facetview_freetext' + rank).val();
                    queried_fields.push(theField);
                }
                else if ($('#label1_facetview_searchbar' + rank).text() == "ap. country") {
                    // this one is for patent only	 
                    var obj = {'query_string': {'default_operator': 'AND'}};
                    var theField = "$teiCorpus.$teiHeader.$fileDesc.$sourceDesc.$list.$item.$list.$item.$country";
                    obj['query_string']['query'] =
                            theField + ":" + $('#facetview_freetext' + rank).val();
                    queried_fields.push(theField);
                }
                else if ($('#label1_facetview_searchbar' + rank).text() == "authors\' country") {
                    // this one is for NPL only
                    var obj = {'query_string': {'default_operator': 'AND'}};
                    var theField = "$teiCorpus.$teiHeader.$fileDesc.$sourceDesc.$biblStruct.$author.$affiliation.$address.key";
                    obj['query_string']['query'] =
                            theField + ":" + $('#facetview_freetext' + rank).val();
                    queried_fields.push(theField);
                }
                else if ($('#label1_facetview_searchbar' + rank).text() == "author") {
                    // this one is for NPL only
                    var obj = {'query_string': {'default_operator': 'AND'}};
                    var theField = "$teiCorpus.$teiHeader.$fileDesc.$sourceDesc.$biblStruct..$author.$persName.$surname";
                    obj['query_string']['query'] =
                            theField + ":" + $('#facetview_freetext' + rank).val();
                    queried_fields.push(theField);
                }
                else if ($('#label1_facetview_searchbar' + rank).text() == "affiliation") {
                    // this one is for NPL only
                    var obj = {'query_string': {'default_operator': 'AND'}};
                    var theField = "$teiCorpus.$teiHeader.$sourceDesc.$biblStruct.$author.$affiliation.$org.$orgName";
                    obj['query_string']['query'] =
                            theField + ":" + $('#facetview_freetext' + rank).val();
                    queried_fields.push(theField);
                }
                else if ($('#label1_facetview_searchbar' + rank).text() == "inventor") {
                    // this one is for NPL only
                    var obj = {'query_string': {'default_operator': 'AND'}};
                    var theField = "$teiCorpus.$teiHeader.$fileDesc.$sourceDesc.$list.$item.$type_parties.$listPerson.$person.$type_docdba.$persName";
                    obj['query_string']['query'] =
                            theField + ":" + $('#facetview_freetext' + rank).val();
                    queried_fields.push(theField);
                }
                else if ($('#label1_facetview_searchbar' + rank).text() == "applicant") {
                    // this one is for NPL only
                    var obj = {'query_string': {'default_operator': 'AND'}};
                    var theField = "$teiCorpus.$teiHeader.$fileDesc.$sourceDesc.$list.$item.$type_parties.$list.$item.$type_docdb.$name";
                    obj['query_string']['query'] =
                            theField + ":" + $('#facetview_freetext' + rank).val();
                    queried_fields.push(theField);
                }

                // complete bool clause
                if (modality == 'must')
                    bool['must'].push(obj);
                else if (modality == 'should')
                    bool['should'].push(obj);
                else
                    bool['must_not'].push(obj);
            }
        }
        // we then create the filters which have been selected
        $('.facetview_filterselected', filtered).each(function () {
            if (!filtered)
                filtered = {'and': []};

            if ($(this).hasClass('facetview_facetrange')) {
                // facet filter for a range of values
                var rel = options.facets[ $(this).attr('rel') ]['field'];
                //var from_ = (parseInt( $('.facetview_lowrangeval', this).html() ) - 1970)* 365*24*60*60*1000;
                //var to_ = (parseInt( $('.facetview_highrangeval', this).html() ) - 1970) * 365*24*60*60*1000 - 1;
                var range = $(this).attr('href');
                var ind = range.indexOf('_');
                if (ind != -1) {
                    var from_ = range.substring(0, ind);
                    var to_ = range.substring(ind + 1, range.length);
                    var rngs = {
                        'from': "" + from_,
                        'to': "" + to_
                    }
                    var obbj = {'range': {}}
                    obbj['range'][ rel ] = rngs;
                    filtered['and'].push(obbj);
                }
            }
            else if (($(this).attr('rel').indexOf("$date") != -1) ||
                    ($(this).attr('rel').indexOf("Date") != -1) ||
                    ($(this).attr('rel').indexOf("when") != -1)) {
                // facet filter for date
                var rel = $(this).attr('rel');
                var obbj = {'range': {}}
                var from_ = $(this).attr('href');
                //var to_ = parseInt(from_) + 365*24*60*60*1000 - 1;
                var to_ = parseInt(from_) + 365 * 24 * 60 * 60 * 1000;
                var rngs = {
                    'from': from_,
                    'to': to_
                }
                obbj['range'][ rel ] = rngs;
                filtered['and'].push(obbj);
            }
            else {
                // other facet filter
                var obbj = {'term': {}};
                obbj['term'][ $(this).attr('rel') ] = $(this).attr('href');
                filtered['and'].push(obbj);
            }
        });
        if (bool) {
            var obj = {'query': {}};
            var obj2 = {'bool': bool};

            if (filtered['and']) {
                obj['query'] = obj2;
                obj['filter'] = filtered;
                var objj = {'filtered': {}};
                objj['filtered'] = obj;
                qs['query'] = objj;
            }
            else {
                qs['query'] = obj2;
            }
        }
        else {
            if (filtered['and']) {
                var obj2 = {'query': {}};
                obj2['filter'] = filtered;
                obj2['query'] = {'match_all': {}};
                var objj = {'filtered': {}};
                objj['filtered'] = obj2;
                qs['query'] = objj;
            }
            else
                qs['query'] = {'match_all': {}};
            if (options['collection'] == 'patent') {
                qs['sort'] = [{"$teiCorpus.$TEI.$teiHeader.$fileDesc.$sourceDesc.$biblStruct.$monogr.$date": {"order": "asc"}}];
            }
            else {
                qs['sort'] = [{"$teiCorpus.$teiHeader.$sourceDesc.$biblStruct.$monogr.$imprint.$date": {"order": "desc"}}];
            }
        }
    }
    else if (options['mode_query'] == 'nl') {
        $('.facetview_filterselected', obj).each(function () {
            //console.log($(this).attr('rel') );
            !bool ? bool = {'must': []} : "";
            if ($(this).hasClass('facetview_facetrange')) {
                var rel = options.facets[ $(this).attr('rel') ]['field'];
                //var from_ = (parseInt( $('.facetview_lowrangeval', this).html() ) - 1970)* 365*24*60*60*1000;
                //var to_ = (parseInt( $('.facetview_highrangeval', this).html() ) - 1970) * 365*24*60*60*1000 - 1;
                var range = $(this).attr('href');
                var ind = range.indexOf('_');
                if (ind != -1) {
                    var from_ = range.substring(0, ind);
                    var to_ = range.substring(ind + 1, range.length);
                    var rngs = {
                        'from': "" + from_,
                        'to': "" + to_
                    }
                    var obj = {'range': {}};
                    obj['range'][ rel ] = rngs;
                    bool['must'].push(obj);
                }
            }
            else if (($(this).attr('rel').indexOf("$date") != -1) ||
                    ($(this).attr('rel').indexOf("Date") != -1) ||
                    ($(this).attr('rel').indexOf("when") != -1)) {
                //var rel = options.facets[ $(this).attr('rel') ]['field'];
                var rel = $(this).attr('rel');
                var obj = {'range': {}}
                var from_ = $(this).attr('href');
                //var to_ = parseInt(from_) + 365*24*60*60*1000 - 1;
                var to_ = parseInt(from_) + 365 * 24 * 60 * 60 * 1000;
                var rngs = {
                    'from': from_,
                    'to': to_
                }
                obj['range'][ rel ] = rngs;
                bool['must'].push(obj);
            }
            else {
                var obj = {'term': {}};
                obj['term'][ $(this).attr('rel') ] = $(this).attr('href');
                bool['must'].push(obj);
            }
        });
        for (var item in options.predefined_filters) {
            // predefined filters to apply to all search and defined in the options
            !bool ? bool = {'must': []} : "";
            var obj = {'term': {}};
            obj['term'][ item ] = options.predefined_filters[item];
            bool['must'].push(obj);
        }
        var theField = "";
        var analys = "";
        if (($('#label_facetview_searchbar').text() == "all") ||
                ($('#label_facetview_searchbar').text() == "lang")) {
            theField += "*";
            analys = "default";
        }
        else if ($('#label_facetview_searchbar').text() == "de") {
            theField += "$lang_de";
            analys = "german";
        }
        else if ($('#label_facetview_searchbar').text() == "fr") {
            theField += "$lang_fr";
            analys = "french";
        }
        else {
            theField += "$lang_en";
            analys = "english";
        }

        if (bool) {
            // $('#facetview_freetext').val() != ""
            //    ? bool['must'].push( {'query_string': { 'query': $('#facetview_freetext').val() } } )
            //    : "";
            var obj = {'query': {}};
            var obj2 = {'bool': bool};

            obj['query'] = obj2;
            var obj4 = {'filter': obj};

            if ($('#facetview_freetext').val() == "") {
                obj4['query'] = {'match_all': {}};
                if (options['collection'] == 'patent') {
                    qs['sort'] = [{"$teiCorpus.$teiHeader.$fileDesc.$sourceDesc.$biblStruct.$monogr.$date": {"order": "asc"}}];
                }
                else {
                    qs['sort'] = [{"$teiCorpus.$teiHeader.$fileDesc.$publicationStmt.$date": {"order": "desc"}}];
                }
            }
            else {
                var textLangFields = [];
                var textFields;
                if (options['collection'] == 'npl') {
                    textFields = textFieldNPL;
                }
                else {
                    textFields = textFieldPatents;
                }

                for (var ii = 0; ii < textFields.length; ii++) {
                    if (ii == 0) {
                        textLangFields[ii] = textFields[ii] + theField;
                    }
                    else {
                        textLangFields[ii] = textFields[ii] + theField;
                    }
                    queried_fields.push(textLangFields[ii]);
                }
                // we don't want numbers here
                var myString = $('#facetview_freetext').val().replace(/\d+/g, '');
                //var myString = $('#facetview_freetext').val();
                // and we must escape all lucene special characters: +-&amp;|!(){}[]^"~*?:\
                var regex = RegExp('[' + lucene_specials.join('\\') + ']', 'g');
                myString = myString.replace(regex, "\\$&");

                obj4['query'] = {'query_string': {'fields': textLangFields,
                        'query': myString, "use_dis_max": true, "analyzer": analys, 'default_operator': 'OR'}};
            }
            qs['query'] = {'filtered': obj4};
            //qs['query'] = {'bool': bool}
        }
        else {
            if ($('#facetview_freetext').val() == "") {
                qs['query'] = {'match_all': {}};
                if (options['collection'] == 'patent') {
                    qs['sort'] = [{"$teiCorpus.$teiHeader.$fileDesc.$sourceDesc.$biblStruct.$monogr.$date": {"order": "asc"}}];
                }
                else {
                    qs['sort'] = [{"$teiCorpus.$teiHeader.$sourceDesc.$biblStruct.$monogr.$imprint.$date": {"order": "desc"}}];
                }
            }
            else {
                var textLangFields = [];
                var textFields;
                if (options['collection'] == 'npl') {
                    textFields = textFieldNPL;
                }
                else {
                    textFields = textFieldPatents;
                }

                for (var ii = 0; ii < textFields.length; ii++) {
                    if (ii == 0) {
                        textLangFields[ii] = textFields[ii] + theField;
                    }
                    else {
                        textLangFields[ii] = textFields[ii] + theField;
                    }
                    queried_fields.push(textLangFields[ii]);
                }

                // we don't want numbers here
                var myString = $('#facetview_freetext').val().replace(/\d+/g, '');
                //var myString = $('#facetview_freetext').val();
                // and we must escape all lucene special characters: +-&amp;|!(){}[]^"~*?:\
                var regex = RegExp('[' + lucene_specials.join('\\') + ']', 'g');
                myString = myString.replace(regex, "\\$&");

                qs['query'] = {'query_string': {'fields': textLangFields,
                        'query': myString, "use_dis_max": true, "analyzer": analys, 'default_operator': 'OR'}};
            }
        }
    }
    else {
        // simple query mode	
        $('.facetview_filterselected', obj).each(function () {
            // facet filter for a range of values
            !bool ? bool = {'must': []} : "";
            if ($(this).hasClass('facetview_facetrange')) {
                var rel = options.facets[ $(this).attr('rel') ]['field'];
                //var from_ = (parseInt( $('.facetview_lowrangeval', this).html() ) - 1970)* 365*24*60*60*1000;
                //var to_ = (parseInt( $('.facetview_highrangeval', this).html() ) - 1970) * 365*24*60*60*1000 - 1;
                var range = $(this).attr('href');
                var ind = range.indexOf('_');
                if (ind != -1) {
                    var from_ = range.substring(0, ind);
                    var to_ = range.substring(ind + 1, range.length);
                    var rngs = {
                        'from': "" + from_,
                        'to': "" + to_
                    }
                    var obj = {'range': {}};
                    obj['range'][ rel ] = rngs;
                    bool['must'].push(obj);
                    filtered = true;
                }
            }
            else if (($(this).attr('rel').indexOf("$date") != -1) ||
                    ($(this).attr('rel').indexOf("Date") != -1) ||
                    ($(this).attr('rel').indexOf("when") != -1)) {
                /// facet filter for a date
                var rel = $(this).attr('rel');
                var obj = {'range': {}}
                var from_ = $(this).attr('href');
                //var to_ = parseInt(from_) + 365*24*60*60*1000 - 1;
                var to_ = parseInt(from_) + 365 * 24 * 60 * 60 * 1000;
                var rngs = {
                    'from': from_,
                    'to': to_
                }
                obj['range'][ rel ] = rngs;
                bool['must'].push(obj);
                filtered = true;
            }
            else {
                // other facet filter 
                var obj = {'term': {}};
                obj['term'][ $(this).attr('rel') ] = $(this).attr('href');
                bool['must'].push(obj);
                filtered = true;
            }
        });
        for (var item in options.predefined_filters) {
            // predefined filters to apply to all search and defined in the options
            !bool ? bool = {'must': []} : "";
            var obj = {'term': {}};
            obj['term'][ item ] = options.predefined_filters[item];
            bool['must'].push(obj);
            filtered = true;
        }
        if (bool) {
            // $('#facetview_freetext').val() != ""
            //    ? bool['must'].push( {'query_string': { 'query': $('#facetview_freetext').val() } } )
            //    : "";
            var obj = {'query': {}};
            var obj2 = {'bool': bool};

            /*if (nested) {
             // case nested documents are queried 
             obj['query'] = obj2;
             // when nested documents are for the classes
             obj['path'] = '$teiCorpus.$teiCorpus.$teiHeader.$profileDesc.$textClass';
             // other cases in the future here...
             
             var obj3 = {'nested': obj};
             var obj4 = {'filter': obj3};
             }
             else*/
            {
                // case no nested documents are queried
                obj['query'] = obj2;
                var obj4 = {'filter': obj};
            }

            if ($('#facetview_freetext').val() == "") {
                obj4['query'] = {'match_all': {}};
                if (options['collection'] == 'patent') {
                    qs['sort'] = [{"$teiCorpus.$teiHeader.$fileDesc.$sourceDesc.$biblStruct.$monogr.$date": {"order": "asc"}}];
                }
                else {
                    qs['sort'] = [{"$teiCorpus.$teiHeader.$sourceDesc.$biblStruct.$monogr.$imprint.$date": {"order": "desc"}}];
                }
            }
            else
                obj4['query'] = {'query_string': {'query': $('#facetview_freetext').val(), 'default_operator': 'AND'}};
            qs['query'] = {'filtered': obj4};
            //qs['query'] = {'bool': bool}
        }
        else {
            if ($('#facetview_freetext').val() != "") {
                qs['query'] = {'query_string': {'query': $('#facetview_freetext').val(), 'default_operator': 'AND'}}
            }
            else {
                if (!filtered) {
                    qs['query'] = {'match_all': {}};
                }
                if (options['collection'] == 'patent') {
                    qs['sort'] = [{"$teiCorpus.$teiHeader.$fileDesc.$sourceDesc.$biblStruct.$monogr.$date": {"order": "asc"}}];
                }
                else {
                    qs['sort'] = [{"$teiCorpus.$teiHeader.$sourceDesc.$biblStruct.$monogr.$imprint.$date": {"order": "desc"}}];
                }
            }
        }
    }
    // set any paging
    options.paging.from != 0 ? qs['from'] = options.paging.from : ""
    options.paging.size != 10 ? qs['size'] = options.paging.size : ""

    // set any facets
    qs['facets'] = {};
    for (var item in options.facets) {
        var obj = jQuery.extend(true, {}, options.facets[item]);
        var nameFacet = obj['display'];
        delete obj['display'];

        if (options.facets[item]['type'] == 'date') {
            obj['interval'] = "year";
            //obj['size'] = 5; 
            qs['facets'][nameFacet] = {"date_histogram": obj};
        }
        else {
            obj['size'] = options.facets[item]['size'] + 50;
            // this 50 is a magic number due to the following ES bug:
            // https://github.com/elasticsearch/elasticsearch/issues/1305
            // hopefully to be fixed in a near future
            if (options.facets[item]['order'])
                obj['order'] = options.facets[item]['order'];
            else
                obj['order'] = 'count';
            // we need to remove type and view fields since ES 1.2
            delete obj['type'];
            delete obj['view'];
            qs['facets'][nameFacet] = {"terms": obj};
        }
    }
    // set snippets/highlight
    if (queried_fields.length == 0) {
        queried_fields.push("_all");
    }
    qs['highlight'] = {};
    qs['highlight']['fields'] = {};
    for (var fie in queried_fields) {
        if (options['snippet_style'] == 'andlauer') {
            qs['highlight']['fields'][queried_fields[fie]] = {'fragment_size': 130, 'number_of_fragments': 100};
        }
        else {
            qs['highlight']['fields'][queried_fields[fie]] = {'fragment_size': 130, 'number_of_fragments': 3};
        }
    }
    qs['highlight']['order'] = 'score';
    qs['highlight']['pre_tags'] = ['<strong>'];
    qs['highlight']['post_tags'] = ['</strong>'];
    qs['highlight']['require_field_match'] = true;
    var theUrl = JSON.stringify(qs);

    //if (window.console != undefined) {
        console.log(theUrl);
    //}
    return theUrl;
}