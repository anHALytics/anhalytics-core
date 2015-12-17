
var displayAnnotations = function (data, index, id, origin) {
    var jsonObject = null;
    if (!data) {
        return;
    }
    if (data.hits) {
        if (data.hits.hits) {
            jsonObject = eval(data.hits.hits[0]);
        }
    }
    if (!jsonObject) {
        return;
    }

    // origin is title, abstract or keywords
    if (!options.data['' + origin]) {
        options.data['' + origin] = [];
    }
    if (origin == 'keyword') {
        if (!options.data['' + origin][index]) {
            options.data['' + origin][index] = [];
        }
        options.data['' + origin][index][id] = jsonObject['_source']['annotation']['nerd'];
    }
    else
        options.data['' + origin][index] = jsonObject['_source']['annotation']['nerd'];
    //console.log('annotation for ' + id);
    //console.log(jsonObject);

    //var text = jsonObject['_source']['annotation']['nerd']['text'];		
    var text = $('[rel="' + id + '"]').text();
    var entities = jsonObject['_source']['annotation']['nerd']['entities'];
    var m = 0;
    var lastMaxIndex = text.length;
    //for(var m in entities) {
    for (var m = entities.length - 1; m >= 0; m--) {
        //var entity = entities[entities.length - m - 1];
        var entity = entities[m];
        var chunk = entity.rawName;
        var domains = entity.domains;
        var domain = null;
        if (domains && domains.length > 0) {
            domain = domains[0].toLowerCase();
        }
        var label = null;
        if (entity.type)
            label = NERTypeMapping(entity.type, entity.chunk);
        else if (domain)
            label = domain;
        else
            label = chunk;
        var start = parseInt(entity.offsetStart, 10);
        var end = parseInt(entity.offsetEnd, 10);

        // keeping track of the lastMaxIndex allows to handle nbest results, e.g. possible
        // overlapping annotations to display as infobox, but with only one annotation
        // tagging the text
        if (start > lastMaxIndex) {
            // we have a problem in the initial sort of the entities
            // the server response is not compatible with the client 
            console.log("Sorting of entities as present in the server's response not valid for this client.");
        }
        else if ((start == lastMaxIndex) || (end > lastMaxIndex)) {
            // overlap
            end = lastMaxIndex;
        }
        else {
            // we produce the annotation on the string
            if (origin == "abstract") {
                text = text.substring(0, start) +
                        '<span id="annot-abs-' + index + '-' + (entities.length - m - 1) +
                        '" rel="popover" data-color="' + label + '">' +
                        '<span class="label ' + label +
                        '" style="cursor:hand;cursor:pointer;white-space: normal;" >'
                        + text.substring(start, end) + '</span></span>'
                        + text.substring(end, text.length + 1);
            }
            else if (origin == "keyword") {
                text = text.substring(0, start) +
                        '<span id="annot-key-' + index + '-' + (entities.length - m - 1) + '-' + id
                        + '" rel="popover" data-color="' + label + '">' +
                        '<span class="label ' + label + '" style="cursor:hand;cursor:pointer;" >'
                        + text.substring(start, end) + '</span></span>'
                        + text.substring(end, text.length + 1);
            }
            else {
                text = text.substring(0, start) +
                        '<span id="annot-' + index + '-' + (entities.length - m - 1) +
                        '" rel="popover" data-color="' + label + '">' +
                        '<span class="label ' + label + '" style="cursor:hand;cursor:pointer;" >'
                        + text.substring(start, end) + '</span></span>'
                        + text.substring(end, text.length + 1);
            }
            lastMaxIndex = start;
        }
    }

    //var result = '<strong><span style="font-size:13px">' + text + '<span></strong>';
    if (origin == "abstract")
        $('[rel="' + id + '"]').html('<strong>Abstract: </strong>' + text);
    else
        $('[rel="' + id + '"]').html(text);

    // now set the popovers/view event 
    var m = 0;
    for (var m in entities) {
        // set the info box
        if (origin == "abstract")
            $('#annot-abs-' + index + '-' + m).bind('hover', viewEntity);
        else if (origin == "keyword")
            $('#annot-key-' + index + '-' + m + '-' + id).bind('hover', viewEntity);
        else
            $('#annot-' + index + '-' + m).bind('hover', viewEntity);
    }
}

var displayAbstract = function (data, index) {
    var jsonObject = null;
    if (!data) {
        return;
    }
    if (data.hits) {
        if (data.hits.hits) {
            jsonObject = eval(data.hits.hits[0]);
        }
    }
    if (!jsonObject) {
        return;
    }

    if (options.collection == "npl") {
        var docid = jsonObject._id;
        var piece = "";

        //piece += '<div class="row-fluid">';

        piece += '<div class="span2" style="width:13%;">';
        if (options.subcollection == "hal") {
            piece += '<p><strong> <a href="https://hal.archives-ouvertes.fr/'
                    + docid + '" target="_blank" style="text-decoration:underline;">'
                    + docid + '</a></strong></p>';


            // document type
            var type =
                    jsonObject.fields['$teiCorpus.$teiHeader.$profileDesc.$textClass.$classCode.$scheme_halTypology'];
            if (type) {
                piece += '<p><span class="label pubtype" style="white-space:normal;">' + type + '</span></p>';
                //piece += '<p><strong>' + type + '</strong></p>';
            }
        }

        // authors and affiliation
        var names =
                jsonObject.fields['$teiCorpus.$teiHeader.$sourceDesc.$biblStruct.$author.$persName.$fullName'];

        if (names) {
            for (var aut in names) {
                var name_ = names[aut];
                piece += '<p>' + name_ + '</p>';
            }
        }

        piece += '</div>';

        piece += '<div class="span6" style="margin-left:10px;">';
        // abstract, if any
        var abstract = null;

        var abstractID = null;
        var abstractIDs = jsonObject.fields['$teiCorpus.$teiHeader.$profileDesc.xml:id'];
        if (typeof abstractIDs == 'string') {
            abstractID = abstractIDs;
        }
        else {
            if (abstractIDs && (abstractIDs.length > 0)) {
                abstractID = abstractIDs[0];
                while ((typeof abstractID != 'string') && (typeof abstractID != 'undefined')) {
                    abstractID = abstractID[0];
                }
            }
        }

        var abstracts = jsonObject.fields['$teiCorpus.$teiHeader.$profileDesc.$abstract.$lang_en'];
        if (typeof abstracts == 'string') {
            abstract = abstracts;
        }
        else {
            if (abstracts && (abstracts.length > 0)) {
                abstract = abstracts[0];
                while ((typeof abstract != 'string') && (typeof abstract != 'undefined')) {
                    abstract = abstract[0];
                }
            }
        }

        if (!abstract || (abstract.length == 0)) {
            abstracts = jsonObject.fields['$teiCorpus.$teiHeader.$profileDesc.$abstract.$lang_fr'];

            if (typeof abstracts == 'string') {
                abstract = abstracts;
            }
            else {
                if (abstracts && (abstracts.length > 0)) {
                    abstract = abstracts[0];
                    while ((typeof abstract != 'string') && (typeof abstract != 'undefined')) {
                        abstract = abstract[0];
                    }
                }
            }
        }

        if (!abstract || (abstract.length == 0)) {
            abstracts = jsonObject.fields['$teiCorpus.$teiHeader.$profileDesc.$abstract.$lang_de'];

            if (typeof abstracts == 'string') {
                abstract = abstracts;
            }
            else {
                if (abstracts && (abstracts.length > 0)) {
                    abstract = abstracts[0];
                    while ((typeof abstract != 'string') && (typeof abstract != 'undefined')) {
                        abstract = abstract[0];
                    }
                }
            }
        }

        if (!abstract || (abstract.length == 0)) {
            abstracts = jsonObject.fields['$teiCorpus.$teiHeader.$profileDesc.$abstract.$lang_es'];

            if (typeof abstracts == 'string') {
                abstract = abstracts;
            }
            else {
                if (abstracts && (abstracts.length > 0)) {
                    abstract = abstracts[0];
                    while ((typeof abstract != 'string') && (typeof abstract != 'undefined')) {
                        abstract = abstract[0];
                    }
                }
            }
        }

        if (abstract && (abstract.length > 0) && (abstract.trim().indexOf(" ") != -1)) {
            piece += '<p id="abstractNaked" pos="' + index + '" rel="' + abstractID + '" >' + abstract + '</p>';
        }

        // keywords
        var keyword = null;
        var keywordIDs =
                jsonObject.fields['$teiCorpus.$teiHeader.$profileDesc.$textClass.$keywords.$type_author.xml:id'];
        // we have a list of keyword IDs, each one corresponding to an independent annotation set
        var keywords =
                jsonObject.fields['$teiCorpus.$teiHeader.$profileDesc.$textClass.$keywords.$type_author.$term'];

        if (typeof keywords == 'string') {
            keyword = keywords;
        }
        else {
            var keyArray = keywords;
            if (keyArray) {
                for (var p in keyArray) {
                    var keywordID = keywordIDs[p];
                    if (p == 0) {
                        keyword = '<span id="keywordsNaked"  pos="' + index + '" rel="' + keywordID + '">'
                                + keyArray[p] + '</span>';
                    }
                    else {
                        keyword += ', ' + '<span id="keywordsNaked"  pos="' + index + '" rel="' + keywordID + '">' +
                                keyArray[p] + '</span>';
                    }
                }
            }
        }

        if (keyword && (keyword.length > 0) && (keyword.trim().indexOf(" ") != -1)) {
            piece += ' <p><strong>Keywords: </strong> ' + keyword + '</p>';
        }

        piece += '</div>';

        // info box for the entities
        piece += '<div class="span4" style="margin-left:10px; width:35%;">';
        piece += '<span id="detailed_annot-' + index + '" />';
        piece += "</div>";

        piece += "</div>";

        $('#innen_abstract[rel="' + docid + '"]').append(piece);

        $('#abstractNaked[rel="' + abstractID + '"]', obj).each(function () {
            // annotations for the abstract
            var index = $(this).attr('pos');
            var titleID = $(this).attr('rel');
            var localQuery = {"query": {"filtered": {"query": {"term": {"_id": abstractID}}}}};

            $.ajax({
                type: "get",
                url: options.search_url_annotations,
                contentType: 'application/json',
                dataType: 'jsonp',
                data: {source: JSON.stringify(localQuery)},
                success: function (data) {
                    displayAnnotations(data, index, abstractID, 'abstract');
                }
            });
        });

        for (var p in keywordIDs) {
            $('#keywordsNaked[rel="' + keywordIDs[p] + '"]', obj).each(function () {
                // annotations for the keywords
                var index = $(this).attr('pos');
                var keywordID = $(this).attr('rel');
                var localQuery = {"query": {"filtered": {"query": {"term": {"_id": keywordID}}}}};

                $.ajax({
                    type: "get",
                    url: options.search_url_annotations,
                    contentType: 'application/json',
                    dataType: 'jsonp',
                    data: {source: JSON.stringify(localQuery)},
                    success: function (data) {
                        displayAnnotations(data, index, keywordID, 'keyword');
                    }
                });
            });
        }
    }
};

/** 
 * View the full entity information in the infobox 
 */
function viewEntity(event) {
    event.preventDefault();
    // currently entity can appear in the title, abstract or keywords
    // the origin is visible in the event origin id, as well as the "coordinates" of the entity 

    var localID = $(this).attr('id');
    console.log(localID);

    var resultIndex = -1;
    var abstractSentenceNumber = -1;
    var entityNumber = -1;
    var idNumber = null;

    var inAbstract = false;
    var inKeyword = false;
    if (localID.indexOf("-abs-") != -1) {
        // the entity is located in the abstract
        inAbstract = true;
        var ind1 = localID.indexOf('-');
        ind1 = localID.indexOf('-', ind1 + 1);
        //var ind2 = localID.indexOf('-', ind1+1);
        var ind3 = localID.lastIndexOf('-');
        resultIndex = parseInt(localID.substring(ind1 + 1, ind3));
        //abstractSentenceNumber = parseInt(localID.substring(ind2+1,ind3));
        entityNumber = parseInt(localID.substring(ind3 + 1, localID.length));
    }
    else if (localID.indexOf("-key-") != -1) {
        // the entity is located in the keywords
        inKeyword = true;
        var ind1 = localID.indexOf('-');
        ind1 = localID.indexOf('-', ind1 + 1);
        var ind2 = localID.indexOf('-', ind1 + 1);
        var ind3 = localID.lastIndexOf('-');
        resultIndex = parseInt(localID.substring(ind1 + 1, ind3));
        entityNumber = parseInt(localID.substring(ind2 + 1, ind3));
        idNumber = localID.substring(ind3 + 1, localID.length);
    }
    else {
        // the entity is located in the title
        var ind1 = localID.indexOf('-');
        var ind2 = localID.lastIndexOf('-');
        resultIndex = parseInt(localID.substring(ind1 + 1, ind2));
        entityNumber = parseInt(localID.substring(ind2 + 1, localID.length));

        // and, if not expended, we need to expend the record collapsable to show the info box
        //('#myCollapsible_'+resultIndex).collapse('show');
    }

    var entity = null;
    var localSize = -1;

    if (inAbstract) {
        //console.log(resultIndex + " " + entityNumber);
        //console.log(options.data['abstract'][resultIndex]['entities']);

        if ((options.data['abstract'][resultIndex])
                && (options.data['abstract'][resultIndex])
                && (options.data['abstract'][resultIndex]['entities'])
                ) {
            localSize = options.data['abstract'][resultIndex]
                    ['entities'].length;
            entity = options.data['abstract'][resultIndex]
                    ['entities'][localSize - entityNumber - 1];
        }
    }
    else if (inKeyword) {
        //console.log(resultIndex + " " + entityNumber + " " + idNumber);
        //console.log(options.data['keyword'][resultIndex][idNumber]['entities']);

        if ((options.data['keyword'][resultIndex])
                && (options.data['keyword'][resultIndex][idNumber])
                && (options.data['keyword'][resultIndex][idNumber]['entities'])
                ) {
            localSize = options.data['keyword'][resultIndex][idNumber]
                    ['entities'].length;
            entity = options.data['keyword'][resultIndex][idNumber]
                    ['entities'][localSize - entityNumber - 1];
        }
    }
    else {
        //console.log(resultIndex + " " + " " + entityNumber);
        //console.log(options.data['title'][resultIndex]['entities']);

        if ((options.data['title'])
                && (options.data['title'][resultIndex])
                && (options.data['title'][resultIndex]['entities'])
                ) {
            localSize = options.data['title'][resultIndex]['entities'].length;
            entity = options.data['title'][resultIndex]['entities'][localSize - entityNumber - 1];
        }
    }

    var string = "";
    if (entity != null) {
        //console.log(entity);
        var domains = entity.domains;
        if (domains && domains.length > 0) {
            domain = domains[0].toLowerCase();
        }
        var type = entity.type;

        var colorLabel = null;
        if (type)
            colorLabel = type;
        else if (domains && domains.length > 0) {
            colorLabel = domain;
        }
        else
            colorLabel = entity.rawName;

        var start = parseInt(entity.offsetStart, 10);
        var end = parseInt(entity.offsetEnd, 10);

        var subType = entity.subtype;
        var conf = entity.nerd_score;
        if (conf && conf.length > 4)
            conf = conf.substring(0, 4);
        var definitions = entity.definitions;
        var wikipedia = entity.wikipediaExternalRef;
        var freebase = entity.freeBaseExternalRef;
        var content = entity.rawName; //$(this).text();
        var preferredTerm = entity.preferredTerm;

        var sense = null;
        if (entity.sense)
            sense = entity.sense.fineSense;

        string += "<div class='info-sense-box " + colorLabel +
                "'><h3 style='color:#FFF;padding-left:10px;'>" + content.toUpperCase() +
                "</h3>";
        string += "<div class='container-fluid' style='background-color:#F9F9F9;color:#70695C;border:padding:5px;margin-top:5px;'>" +
                "<table style='width:100%;background-color:#fff;border:0px'><tr style='background-color:#fff;border:0px;'><td style='background-color:#fff;border:0px;'>";

        if (type)
            string += "<p>Type: <b>" + type + "</b></p>";

        if (sense)
            string += "<p>Sense: <b>" + sense + "</b></p>";

        if (domains && domains.length > 0) {
            string += "<p>Domains: <b>";
            for (var i = 0; i < domains.length; i++) {
                if (i != 0)
                    string += ", ";
                string += domains[i].replace("_", " ");
            }
            string += "</b></p>";
        }

        if (preferredTerm) {
            string += "<p>Preferred: <b>" + preferredTerm + "</b></p>";
        }

        string += "<p>conf: <i>" + conf + "</i></p>";

        string += "</td><td style='align:right;background-color:#fff'>";

        if (freebase != null) {
            var urlImage = 'https://usercontent.googleapis.com/freebase/v1/image' + freebase;
            urlImage += '?maxwidth=150';
            urlImage += '&maxheight=150';
            urlImage += '&key=' + options.api_key;
            string += '<img src="' + urlImage + '" alt="' + freebase + '"/>';
        }

        string += "</td></tr></table>";

        if ((definitions != null) && (definitions.length > 0)) {
            string += "<p>" + definitions[0].definition + "</p>";
        }
        if ((wikipedia != null) || (freebase != null)) {
            string += '<p>Reference: '
            if (wikipedia != null) {
                string += '<a href="http://en.wikipedia.org/wiki?curid=' +
                        wikipedia +
                        '" target="_blank"><img style="max-width:28px;max-height:22px;margin-top:5px;" src="data/images/wikipedia.png"/></a>';
            }
            if (freebase != null) {
                string += '<a href="http://www.freebase.com' +
                        freebase +
                        '" target="_blank"><img style="max-width:28px;max-height:22px;margin-top:5px;" src="data/images/freebase_icon.png"/></a>';

            }
            string += '</p>';
        }

        string += "</div></div>";

        $('#detailed_annot-' + resultIndex).html(string);
        $('#detailed_annot-' + resultIndex).show();
    }
}