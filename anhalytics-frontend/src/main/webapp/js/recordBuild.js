// given a result record, build how it should look on the page
var buildrecord = function (index, node) {
    var record = options.data['records'][index];
    var highlight = options.data['highlights'][index];
    var score = options.data['scores'][index];

    var result = '';

    var jsonObject = eval(record);
    //var jsonObject = record;

//			result += '<tr><td>';
    result += '<tr style="border-collapse:collapse;"><td>';

    //result += '<table class="table" cellspacing="0" cellpadding="0" style="width:100%;border-collapse:collapse;border:none;">'+
    //	'<tr style="width:100%;border-collapse:collapse;border:none;">';

    result += '<div class="row-fluid">';

    var type = null;
    var id = options.data['ids'][index];

    var family = id;
    // family id
    if (options['collection'] == 'patent') {
        if (family.length > 1) {
            result += '<div class="span10" class="height:100%;" id="myCollapsible_' + index + '" data-toggle="collapse" data-target="#abstract_' + index + '" style="white-space:normal;">';
            result += 'Family: ' + family;
        }
    }
    else {
        result += '<div class="span10" class="height:100%;" id="myCollapsible_' + index + '" data-toggle="collapse" data-target="#abstract_' + index + '" style="white-space:normal;">';
    }

    // date
    var date;
    var dates = null;
    if (options['collection'] == 'patent')
        dates = jsonObject['$teiCorpus.$teiHeader.$fileDesc.$sourceDesc.$biblStruct.$monogr.$date'];
    else {
        dates = jsonObject['$teiCorpus.$teiHeader.$sourceDesc.$biblStruct.$monogr.$imprint.$date'];
        if (!dates) {
            dates = jsonObject['$teiCorpus.$teiHeader.$sourceDesc.$biblStruct.$monogr.$imprint.$when'];
        }
        if (!dates) {
            dates = jsonObject['$teiCorpus.$teiHeader.$editionStmt.$edition.$date'];
        }
    }

    if (options['collection'] == 'patent') {
        var rawDate = JSON.stringify(dates);
        var ind1 = rawDate.indexOf('"');
        var ind2 = rawDate.indexOf('"', ind1 + 1);
        date = rawDate.substring(ind1, ind2 + 1);

        if (date && (date.length > 1)) {
            result += ' - <em>' + date.substring(7, date.length - 1) + '.' + date.substring(5, 7)
                    + '.' + date.substring(1, 5) + '</em>' + '<br />';
        }
    }

    var title;
    var titles = null;
    var titleID = null;
    var titleIDs = null;
    var titleAnnotated = null;
    if (options['collection'] == 'patent')
        titles = jsonObject['$teiCorpus.$teiHeader.$fileDesc.$titleStmt.$title.$lang_en'];
    else {
        // NPL
        titles = jsonObject['$teiCorpus.$teiHeader.$titleStmt.$title.$title-first'];
        titleIDs = jsonObject['$teiCorpus.$teiHeader.$titleStmt.xml:id'];
    }
    if (typeof titles == 'string') {
        title = titles;
    }
    else {
        if (titles) {
            title = titles[0];
            while ((typeof title != 'string') && (typeof title != 'undefined')) {
                title = title[0];
            }
        }
    }
    if (typeof titleIDs == 'string') {
        titleID = titleIDs;
    }
    else {
        if (titleIDs) {
            titleID = titleIDs[0];
            while ((typeof title != 'string') && (typeof title != 'undefined')) {
                titleID = titleID[0];
            }
        }
    }

    if (!title || (title.length == 0)) {
        if (options['collection'] == 'patent') {
            titles = jsonObject['$teiCorpus.$teiHeader.$fileDesc.$titleStmt.$title.$lang_en'];
        }
        else {
            titles = jsonObject['$teiCorpus.$teiHeader.$sourceDesc.$biblStruct.$analytic.$title.$lang_en'];
        }
        if (typeof titles == 'string') {
            title = titles;
        }
        else {
            if (titles) {
                title = titles[0];
                while ((typeof title != 'string') && (typeof title != 'undefined')) {
                    title = title[0];
                }
            }
        }
    }

    if (!title || (title.length == 0)) {
        if (options['collection'] == 'patent') {
            titles = jsonObject['$teiCorpus.$teiHeader.$fileDesc.$titleStmt.$title.$lang_fr'];
        }
        else {
            titles = jsonObject['$teiCorpus.$teiHeader.$sourceDesc.$biblStruct.$analytic.$title.$lang_fr'];
        }
        if (typeof titles == 'string') {
            title = titles;
        }
        else {
            if (titles) {
                title = titles[0];
                while ((typeof title != 'string') && (typeof title != 'undefined')) {
                    title = title[0];
                }
            }
        }
    }

    if (!title || (title.length == 0)) {
        if (options['collection'] == 'patent') {
            titles = jsonObject['$teiCorpus.$teiHeader.$fileDesc.$titleStmt.$title.$lang_de'];
        }
        else {
            titles = jsonObject['$teiCorpus.$teiHeader.$sourceDesc.$biblStruct.$analytic.$title.$lang_de'];
        }
        if (typeof titles == 'string') {
            title = titles;
        }
        else {
            if (titles) {
                title = titles[0];
                while ((typeof title != 'string') && (typeof title != 'undefined')) {
                    title = title[0];
                }
            }
        }
    }

    if (title && (title.length > 1) && !titleAnnotated) {
        if (options['collection'] == 'npl') {
            var docid = id;
            if (docid.indexOf('fulltext')) {
                docid = docid.replace('.fulltext', '');
            }
            result += '<span style="color:grey">' + docid
                    + ' - </span> <strong><span '
            if (titleID) {
                result += ' id="titleNaked" pos="' + index + '" rel="' + titleID + '" ';
            }
            result += ' style="font-size:13px; color:black; white-space:normal;">' + title + '<span></strong>';
        }
        else {
            result += '<strong><span style="font-size:13px">' + title + '<span></strong>';
        }
    }

    result += '<br />';

    if (options['collection'] != 'patent') {
        var authorsLast = null;
        var authorsFirst = null;

        authorsLast = jsonObject['$teiCorpus.$teiHeader.$sourceDesc.$biblStruct.$analytic.$author.$persName.$surname'];
        var tempStr = "" + authorsLast;
        authorsLast = tempStr.split(",");
        authorsFirst = jsonObject['$teiCorpus.$teiHeader.$sourceDesc.$biblStruct.$analytic.$author.$persName.$forename'];
        tempStr = "" + authorsFirst;
        authorsFirst = tempStr.split(",");

        if (authorsLast.length < 4) {
            for (var author in authorsLast) {
                if (author == 0) {
                    if (authorsFirst.length > 0) {
                        result += authorsFirst[0][0] + ". ";
                    }
                    result += authorsLast[0];
                }
                else {
                    result += ", ";
                    if (authorsFirst.length > author) {
                        result += authorsFirst[author][0] + ". ";
                    }
                    result += authorsLast[author];
                }
            }
        }
        else {
            if (authorsFirst.length > 0) {
                result += authorsFirst[0][0] + ". ";
            }
            result += authorsLast[0] + ' et al.';
        }
    }

    // book, proceedings or journal title
    if (options['collection'] == 'npl') {
        var titleBook = null;
        var titlesBook = null;
        //if (options['collection'] == 'npl') {
        titlesBook = jsonObject['$teiCorpus.$teiHeader.$sourceDesc.$biblStruct.$monogr.$title'];
        var titleBookTmp = null;
        if (typeof titlesBook == 'string') {
            titleBook = titlesBook;
        }
        else {
            titleBook = titlesBook;
            while ((typeof titleBook != 'string') && (typeof titleBook != 'undefined')) {
                titleBookTmp = titleBook[0];
                if (typeof titleBookTmp != 'undefined') {
                    titleBook = titleBookTmp;
                }
                else {
                    for (var x in titleBook) {
                        titleBookTmp = titleBook[x];
                    }
                    if (titleBookTmp)
                        titleBook = titleBookTmp[0];
                    else
                        titleBook = null;
                    break;
                }

            }
        }
        //}
        if (titleBook && (titleBook.length > 1)) {
            result += ' - <em>' + titleBook + '</em>';
        }
    }

    if (options['collection'] != 'patent') {
        var rawDate = JSON.stringify(dates);
        if (rawDate != null) {
            var ind1 = rawDate.indexOf('"');
            var ind2 = rawDate.indexOf('"', ind1 + 1);
            date = rawDate.substring(ind1, ind2 + 1);

            if (date && (date.length > 1)) {
                var year = date.substring(1, 5);
                var month = null;
                if (date.length > 6)
                    month = date.substring(6, 8);
                if ((month) && (month.length > 1) && (month[0] == "0")) {
                    month = month.substring(1, month.length)
                }
                var day = null;
                if (date.length > 9)
                    day = date.substring(9, date.length - 1);
                if ((day != undefined) && (day.length > 1) && (day[0] == "0")) {
                    day = day.substring(1, day.length)
                }
                result += ' - <em>';
                if ((day != undefined) && (day.length > 0))
                    result += day + '.';
                if ((month != undefined) && (month.length > 0))
                    result += month + '.';
                if (year != undefined)
                    result += year + '</em>' + '<br />';
            }
        }
    }

    // snippets 
    // Dominique Andlauer's strategy (sort of Google's one), at least one snippet per matched term, then 
    // per relevance, first we check the number of different matched terms
    if (options.snippet_style == "andlauer") {
        if (highlight) {
            var jsonObject2 = eval(highlight);
            var newSnippets = [];
            // we first list all term stems found in the full list of snippets
            var activeTerms = [];
            for (var n in jsonObject2) {
                var snippets = jsonObject2[n];
                for (var i = 0; i < snippets.length; i++) {
                    var indd = 0;
                    while (indd < snippets[i].length) {
                        var inddd = snippets[i].indexOf("<strong>", indd);
                        if (inddd != -1) {
                            var inddd2 = snippets[i].indexOf("</strong>", inddd);
                            if (inddd2 != -1) {
                                var term = stemmer(snippets[i].substring(inddd + 8, inddd2).toLowerCase());
                                if (activeTerms.length == 0) {
                                    activeTerms.push(term);
                                }
                                else {
                                    var present = false;
                                    for (var k in activeTerms) {
                                        if (activeTerms[k] == term) {
                                            present = true;
                                            break;
                                        }
                                    }
                                    if (!present) {
                                        activeTerms.push(term);
                                    }
                                }
                                indd = inddd2 + 1;
                            }
                            else {
                                break;
                            }
                        }
                        else {
                            break;
                            //indd = snippets[i].length;
                        }
                    }
                }
            }

            // we then re-rank to have all the terms present in the highest ranked snippets
            var passiveTerms = [];
            var localTerms = [];
            var remainingSnippets = [];
            for (var n in jsonObject2) {
                var snippets = jsonObject2[n];
                for (var i = 0; i < snippets.length; i++) {
                    if (passiveTerms.length == activeTerms.length) {
                        remainingSnippets.push(snippets[i]);
                    }
                    var indd = 0;
                    while (indd < snippets[i].length) {
                        var inddd = snippets[i].indexOf("<strong>", indd);
                        if (inddd != -1) {
                            var inddd2 = snippets[i].indexOf("</strong>", inddd);
                            if (inddd2 != -1) {
                                var term = stemmer(snippets[i].substring(inddd + 8, inddd2).toLowerCase());
                                if (localTerms.length == 0) {
                                    localTerms.push(term);
                                }
                                else {
                                    var present = false;
                                    for (var k in activeTerms) {
                                        if (localTerms[k] == term) {
                                            present = true;
                                            break;
                                        }
                                    }
                                    if (!present)
                                        localTerms.push(term);
                                }
                                indd = inddd2 + 1;
                            }
                            else {
                                break;
                            }
                        }
                        else {
                            break;
                        }
                    }
                    // shall we include snippets[i] as next snippet?
                    if (passiveTerms.length == 0) {
                        newSnippets.push(snippets[i]);
                        for (var dumm in localTerms) {
                            passiveTerms.push(localTerms[dumm])
                        }
                    }
                    else {
                        var previousState = passiveTerms.length;
                        for (var dumm in localTerms) {
                            var present = false;
                            for (var k in passiveTerms) {
                                if (passiveTerms[k] == localTerms[dumm]) {
                                    present = true;
                                    break;
                                }
                            }

                            if (!present) {
                                newSnippets.push(snippets[i]);
                                for (var dumm2 in localTerms) {
                                    passiveTerms.push(localTerms[dumm2])
                                }
                            }
                        }
                        /*if (previousState == passiveTerms.length) {
                         // no new term
                         remainingSnippets.push(snippets[i]);
                         }*/
                    }
                }
            }
            // we complete the new snippet
            for (var dumm in remainingSnippets) {
                newSnippets.push(remainingSnippets[dumm]);
            }

            // we have the new snippets and can output them
            var totalDisplayed = 0;
            for (var dumm in newSnippets) {
                if (newSnippets[dumm].length > 200) {
                    // we have an issue with the snippet boundaries
                    var indd = newSnippets[dumm].indexOf("<strong>");
                    var max = indd + 100;
                    if (max > newSnippets[dumm].length) {
                        max = newSnippets[dumm].length;
                    }
                    result += '...<span style="font-size:12px"><em>' + newSnippets[dumm].substring(indd, max) + '</em></span>...<br />';
                }
                else {
                    result += '...<span style="font-size:12px"><em>' + newSnippets[dumm] + '</em></span>...<br />';
                }
                totalDisplayed++;
                if (totalDisplayed == 3) {
                    break;
                }
            }
        }
    }
    else {
        // here default strategy, snippet ranking per relevance
        if (highlight) {
            var jsonObject2 = eval(highlight);
            //var snippets = jsonObject2['_all'];
            //console.log(snippets);
            var totalDisplayed = 0;
            for (var n in jsonObject2) {
                var snippets = jsonObject2[n];
                for (var i = 0; i < snippets.length; i++) {
                    if (snippets[i].length > 200) {
                        // we have an issue with the snippet boundaries
                        var indd = snippets[i].indexOf("<strong>");
                        var max = indd + 100;
                        if (max > snippets[i].length) {
                            max = snippets[i].length;
                        }
                        result += '...<span style="font-size:12px"><em>' + snippets[i].substring(indd, max) + '</em></span>...<br />';
                    }
                    else {
                        result += '...<span style="font-size:12px"><em>' + snippets[i] + '</em></span>...<br />';
                    }
                    totalDisplayed++;
                    if (totalDisplayed == 3) {
                        break;
                    }
                }
                if (totalDisplayed == 3) {
                    break;
                }
            }
        }
    }

    result += '</div>';

    // add image where available
    if (options.display_images) {
        if ((options['collection'] == 'cendari')) {
            var img = jsonObject['thumbnail_s'];
            var img2 = jsonObject['thumbnail_l'];
            var img3 = jsonObject['thumbnail_m'];
            var uri = jsonObject['URI'];
            img[0] = 'data/cache/images/cendari_photo/' + family + ".png";
            if (img && img2) {
                result += '<div class="span2"><img alt="bla" src="' + img[0] + '" pbsrc="' + img2[0] +
                        '" class="PopBoxImageSmall" pbRevertText="" onclick="Pop(this,50,\'PopBoxImageLarge\');" /></div>';
            }
            else if (img && img3) {
                result += '<div class="span2"><img alt="bla" src="' + img[0] + '" pbsrc="' + img3[0] +
                        '" class="PopBoxImageSmall" pbRevertText="" onclick="Pop(this,50,\'PopBoxImageLarge\');" /></div>';
            }
            else if (img) {
                if (uri) {
                    result += '<div class="span2"><a href="' + uri[0] + '" target="_blank""><img class="thumbnail" style="float:right; max-width:100px; '
                            + 'max-height:150px;" src="' + img[0] + '" /></a></div>';
                }
                else {
                    result += '<div class="span2"><img class="thumbnail" style="float:right; max-width:100px; '
                            + 'max-height:150px;" src="' + img[0] + '" /></div>';
                }
            }
            else {
                result += '<div class="span2" />';
            }
        }
        else {
            var ind = family.indexOf("-");
            if ((ind != -1) && (family.length > ind)) {
                var pubNum = family.substring(ind + 1, family.length);
                result += '<div class="span2"><a href="https://hal.archives-ouvertes.fr/' + family +
                        '/document" target="_blank"><img class="thumbnail" style="float:right; " src="' +
                        'https://hal.archives-ouvertes.fr/' + family + '/thumb' + '" /></a></div>';
            }
            else {
                result += '<div class="span2" />';
            }
        }
    }

    //result += '</tr></table>';
    result += '</div>';

    result += '<div class="row-fluid"><div id="abstract_' + index +
            '" class="collapse">';  //#f8f8f8
    if (index % 2) {
        result += '<div class="mini-layout fluid" style="background-color:#f8f8f8; padding-right:0px;">';
    }
    else {
        result += '<div class="mini-layout fluid" style="background-color:#ffffff;">';
    }
    //result += '<div class="class="container-fluid" style="border: 1px solid #DDD;">';
    result += '<div class="row-fluid">';

    {
        // we need to retrieve the extra biblio and abstract for this biblo item
        result +=
                '<div class="row-fluid" id="innen_abstract" pos="' + index + '" rel="' + family + '">';
        //'"><div style="background:url(data/images/bar-loader.gif) '+
        //'no-repeat center center; height:13px; "/></div>';
        result += '</div>';
        result += '</div>';
    }

    //result += '</div>';
    result += '</div>';
    result += "</div>";

    result += "</div>";

    if ((options['collection'] == 'npl') && (options['collection'] != 'cendari')) {
        var pdfURL = jsonObject['$teiCorpus.$teiHeader.$sourceDesc.target'];
        var docid = jsonObject._id;
        if (pdfURL || docid) {
            result += '<td>';
            if (pdfURL) {
                result += '<a href="' + pdfURL
                        + '" target="_blank"><img src="data/images/pdf_icon_small.gif"></a>';
            }
            if (docid) {
                if (options['subcollection'] == 'hal') {
                    result += '<a href="http://hal.archives-ouvertes.fr/' + docid + '/en" target="_blank">hal</a>';
                }
                else if ((options['subcollection'] == 'zfn') && pdfURL) {
                    //var urlToHeader = pdfURL.replace("pdf", "header.tei.xml");
                    pdfURL = '' + pdfURL;
                    var urlToHeader = pdfURL.replace(/pdf/g, 'header.tei.xml');
                    result += '<a href="' + urlToHeader + '" target="_blank">tei</a>';
                }
            }
            result += '</td>';
        }
    }
    else {
        result += '<td></td>';
    }

    result += '</td></tr>';

    node.append(result);
}