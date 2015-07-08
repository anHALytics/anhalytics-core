

var parseDisambNERD = function (sdata) {
    //var resObj = {};

    var jsonObject = JSON.parse(sdata);
    //var entities = jsonObject['entities'];

    return jsonObject;
}


var showexpandpre = function (sdata) {
    showexpand(decodeURIComponent(sdata['content']));
}

var showexpand = function (sdata) {
    if (!sdata) {
        return;
    }
    if (sdata.indexOf("----------") == -1) {
        return;
    }

    var jsonObject = parseDisamb(sdata);
    //console.log(jsonObject);

    $('#disambiguation_panel').empty();
    var piece = '<div class="mini-layout fluid" style="background-color:#F7EDDC;"> \
				   		 <div class="row-fluid"><div class="span11">';
    if (jsonObject['senses']) {
        piece += '<table class="table" style="width:100%;border:1px solid white;">';
        for (var sens in jsonObject['senses']) {

            piece += '<tr><td><strong>' + jsonObject['senses'][sens]['label'] + '&nbsp;</strong></td><td>' +
                    jsonObject['senses'][sens]['desc']
                    + '</td><td>'
            if (jsonObject['senses'][sens]['wiki']) {
                piece += '<a href="http://en.wikipedia.org/wiki?curid=' +
                        jsonObject['senses'][sens]['wiki'] +
                        '" target="_blank"><img style="max-width:28px;max-height:22px;" src="data/images/wikipedia.png"/></a>';
            }
            piece += '</td></tr>';
        }
        piece += '</table>';
    }

    for (var surf in jsonObject['paraphrases']) {
        piece += '<p>' + jsonObject['paraphrases'][surf] + '</p>';
    }


    piece += '</div><div class="span1"><div id="close-disambiguate-panel" \
					  style="position:relative;float:right;" class="icon-remove icon-white"/></div></div></div>';
    $('#disambiguation_panel').append(piece);
    $('#close-disambiguate-panel').bind('click', function () {
        $('#disambiguation_panel').hide();
    })

    $('#disambiguation_panel').show();
}

var getPieceShowexpandNERD = function (jsonObject){

    var piece = '<div class="mini-layout fluid" style="background-color:#F7EDDC;"> \
				   		 <div class="row-fluid"><div class="span11" style="width:95%;">';
    if (jsonObject['entities']) {
        piece += '<table class="table" style="width:100%;border:1px solid white;">';
        for (var sens in jsonObject['entities']) {
            var entity = jsonObject['entities'][sens];
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

            piece += '<tr id="selectLine' + sens + '" href="'
                    + wikipedia + '" rel="$teiCorpus.$standoff.wikipediaExternalRef"><td id="selectArea' + sens + '" href="'
                    + wikipedia + '" rel="$teiCorpus.$standoff.wikipediaExternalRef">';
            piece += '<div class="checkbox checkbox-inline checkbox-danger" id="selectEntityBlock' +
                    sens + '" href="' + wikipedia + '" rel="$teiCorpus.$standoff.wikipediaExternalRef">';
            piece += '<input type="checkbox" id="selectEntity' + sens
                    + '" name="selectEntity' + sens + '" value="0" href="'
                    + wikipedia + '" rel="$TEI.$standoff.wikipediaExternalRef">';
            piece += '<label for="selectEntity' + sens + '" id="label' + sens + '"> <strong>' + entity.rawName + '&nbsp;</strong> </label></div></td>';
            //piece += '<td><strong>' + entity.rawName + '&nbsp;</strong></td><td>'+ 
            if (preferredTerm) {
                piece += '<td><b>' + preferredTerm + ': <b>' +
                        definitions[0]['definition']
                        + '</td><td>';
            }
            else {
                piece += '<td>' +
                        definitions[0]['definition']
                        + '</td><td>';
            }

            if (freebase != null) {
                var urlImage = 'https://usercontent.googleapis.com/freebase/v1/image' + freebase;
                urlImage += '?maxwidth=150';
                urlImage += '&maxheight=150';
                urlImage += '&key=' + options.api_key;
                piece += '<img src="' + urlImage + '" alt="' + freebase + '"/>';
            }

            piece += '</td><td>';

            piece += '<table><tr><td>';

            if (wikipedia) {
                piece += '<a href="http://en.wikipedia.org/wiki?curid=' +
                        wikipedia +
                        '" target="_blank"><img style="max-width:28px;max-height:22px;" src="data/images/wikipedia.png"/></a>';
            }
            piece += '</td></tr><tr><td>';

            if (freebase != null) {
                piece += '<a href="http://www.freebase.com' +
                        freebase +
                        '" target="_blank"><img style="max-width:28px;max-height:22px;margin-top:5px;" src="data/images/freebase_icon.png"/></a>';

            }
            piece += '</td></tr></table>';

            piece += '</td></tr>';
        }
        piece += '</table>';
    }

    /*for (var surf in jsonObject['paraphrases']) {
     piece += '<p>' + jsonObject['paraphrases'][surf] + '</p>';
     }*/

    piece += '</div><div><div id="close-disambiguate-panel" \
					  style="position:relative;float:right;" class="icon-remove icon-white"/></div></div></div>';
    return piece;
}

var activateDisambButton = function () {
    if ($('#facetview_freetext').val()) {
        $('#disambiguate').attr("disabled", false);
    }
    else {
        $('#disambiguate').attr("disabled", true);
    }
}
