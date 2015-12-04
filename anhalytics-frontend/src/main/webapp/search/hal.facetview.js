/**
 *  Functions for the anHALytics Search front end.
 *
 */

// now the display function
(function ($) {
    $.fn.facetview = function (options) {

        // and add in any overrides from the call
        // these options are also overridable by URL parameters
        // facetview options are declared as a function so they are available externally
        // (see bottom of this file)
        var url_options = $.getUrlVars();
        $.fn.facetview.options = $.extend(options, url_options);
        var options = $.fn.facetview.options;

        //var fillDefaultColor = '#FF8000';
        var fillDefaultColor = '#BC0E0E';
        //var fillDefaultColor = '#FF9900';
        var fillDefaultColorLight = '#FE9A2E';

        // ===============================================
        // functions to do with filters
        // ===============================================

        // show the filter values
        var showfiltervals = function (event) {
            event.preventDefault();
            if ($(this).hasClass('facetview_open')) {
                $(this).children('i').replaceWith('<i class="icon-plus"></i>');
                $(this).removeClass('facetview_open');
                $('#facetview_' + $(this).attr('rel')).children('li').hide();
            }
            else {
                $(this).children('i').replaceWith('<i class="icon-minus"></i>');
                $(this).addClass('facetview_open');
                $('#facetview_' + $(this).attr('rel')).children('li').show();
            }
        };

        // function to perform for sorting of filters
        var sortfilters = function (event) {
            event.preventDefault();
            var sortwhat = $(this).attr('href');
            var which = 0;
            for (item in options.facets) {
                if ('field' in options.facets[item]) {
                    if (options.facets[item]['field'] === sortwhat) {
                        which = item;
                    }
                }
            }
            if ($(this).hasClass('facetview_count')) {
                options.facets[which]['order'] = 'count';
            }
            else if ($(this).hasClass('facetview_term')) {
                options.facets[which]['order'] = 'term';
            }
            else if ($(this).hasClass('facetview_rcount')) {
                options.facets[which]['order'] = 'reverse_count';
            }
            else if ($(this).hasClass('facetview_rterm')) {
                options.facets[which]['order'] = 'reverse_term';
            }
            dosearch();
            if (!$(this).parent().parent().siblings('.facetview_filtershow').hasClass('facetview_open')) {
                $(this).parent().parent().siblings('.facetview_filtershow').trigger('click');
            }
        };

        var editfilter = function (event) {
            event.preventDefault();
            var which = $(this).attr('rel');
            $('#facetview').append(getEditFilterModal(which));
            $('.facetview_removeedit').bind('click', removeedit);
            $('#facetview_dofacetedit').bind('click', dofacetedit);
            $('#facetview_editmodal').modal('show');
        };
              
        // trigger a search when a filter choice is clicked
        var clickfilterchoice = function (event) {
            event.preventDefault();
//console.log(event);	
//console.log($(this));			
            if ($(this).html().trim().length === 0) {
                console.log('checkbox');
                if (!$(this).is(':checked')) {
                    console.log('checked');
                    $('.facetview_filterselected[href="' + $(this).attr("href") + '"]').each(function () {
                        $(this).remove();
                    });
                    options.paging.from = 0;
                    dosearch();
                }
                else {
                    var newobj = '<a class="facetview_filterselected facetview_clear ' +
                            'btn btn-info" rel="' + $(this).attr("rel") +
                            '" alt="remove" title="remove"' +
                            ' href="' + $(this).attr("href") + '">';
                    if ($(this).html().trim().length > 0)
                        newobj += $(this).html().replace(/\(.*\)/, '');
                    else
                        newobj += $(this).attr("href");
                    newobj += ' <i class="icon-remove"></i></a>';
                    $('#facetview_selectedfilters').append(newobj);
                    $('.facetview_filterselected').unbind('click', clearfilter);
                    $('.facetview_filterselected').bind('click', clearfilter);
                    options.paging.from = 0;
                    dosearch();
                }
            }
            else {
                var newobj = '<a class="facetview_filterselected facetview_clear ' +
                        'btn btn-info" rel="' + $(this).attr("rel") +
                        '" alt="remove" title="remove"' +
                        ' href="' + $(this).attr("href") + '">';
                if ($(this).html().trim().length > 0)
                    newobj += $(this).html().replace(/\(.*\)/, '');
                else
                    newobj += $(this).attr("href");
                newobj += ' <i class="icon-remove"></i></a>';
                $('#facetview_selectedfilters').append(newobj);
                $('.facetview_filterselected').unbind('click', clearfilter);
                $('.facetview_filterselected').bind('click', clearfilter);
                options.paging.from = 0;
                dosearch();
            }
        };

        // clear a filter when clear button is pressed, and re-do the search
        var clearfilter = function (event) {
            event.preventDefault();
            $(this).remove();
            options.paging.from = 0;
            dosearch();
        }
        
        // remove the edit modal from page altogether on close (rebuilt for each filter)
        var removeedit = function (event) {
            event.preventDefault();
            $('#facetview_editmodal').modal('hide');
            $('#facetview_editmodal').remove();
        }
        // update parameters and re-run the facet
        var dofacetedit = function (event) {
            event.preventDefault();
            var which = $(this).attr('rel');

            for (truc in options.facets[which]) {
                options.facets[which][truc] = $(this).parent().parent().find('#input_' + truc).val();
            }

            $('#facetview_editmodal').modal('hide');
            $('#facetview_editmodal').remove();
            options.paging.from = 0;
            buildfilters();
            dosearch();
            //if ( !$(this).parent().parent().siblings('.facetview_filtershow').hasClass('facetview_open') ) {
            //    $(this).parent().parent().siblings('.facetview_filtershow').trigger('click')
            //}
        };

        // adjust how many results are shown
        var morefacetvals = function (event) {
            event.preventDefault();
            var morewhat = options.facets[ $(this).attr('rel') ]
            if ('size' in morewhat) {
                var currentval = morewhat['size'];
            } else {
                var currentval = 6;
            }
            var newmore = prompt('Currently showing ' + currentval +
                    '. How many would you like instead?');
            if (newmore) {
                options.facets[ $(this).attr('rel') ]['size'] = parseInt(newmore);
                $(this).html('show up to (' + newmore + ')');
                dosearch();
                if (!$(this).parent().parent().siblings('.facetview_filtershow').hasClass('facetview_open')) {
                    $(this).parent().parent().siblings('.facetview_filtershow').trigger('click')
                }
            }
        };

        // insert a facet range once selected
        var dofacetrange = function (event) {
            event.preventDefault();
            var rel = $('#facetview_rangerel').html();
            var range = $('#facetview_rangechoices').html();
            var newobj = '<a class="facetview_filterselected facetview_facetrange facetview_clear ' +
                    'btn btn-info" rel="' + rel +
                    '" alt="remove" title="remove"' +
                    ' href="' + $(this).attr("href") + '">' +
                    range + ' <i class="icon-remove"></i></a>';
            $('#facetview_selectedfilters').append(newobj);
            $('.facetview_filterselected').unbind('click', clearfilter);
            $('.facetview_filterselected').bind('click', clearfilter);
            $('#facetview_rangemodal').modal('hide');
            $('#facetview_rangemodal').remove();
            options.paging.from = 0;
            dosearch();
        }
        // remove the range modal from page altogether on close (rebuilt for each filter)
        var removerange = function (event) {
            event.preventDefault()
            $('#facetview_rangemodal').modal('hide')
            $('#facetview_rangemodal').remove()
        };
        // build a facet range selector
        var facetrange = function (event) {
            event.preventDefault();
            $('#facetview').append(facetrangeModal);
            $('#facetview_rangemodal').append('<div id="facetview_rangerel" style="display:none;">' +
                    $(this).attr('rel') + '</div>');
            $('#facetview_rangemodal').modal('show');
            $('#facetview_dofacetrange').bind('click', dofacetrange);
            $('.facetview_removerange').bind('click', removerange);
            var values = [];
            //var valsobj = $( '#facetview_' + $(this).attr('href').replace(/\./gi,'_') );
            var valsobj = $('#facetview_' + $(this).attr('href'));
            valsobj.children('li').children('a').each(function () {
                var theDate = new Date(parseInt($(this).attr('href')));
                var years = theDate.getFullYear();
                values.push(years);
            });
            values = values.sort();
            $("#facetview_slider").slider({
                range: true,
                min: 0,
                max: values.length - 1,
                values: [0, values.length - 1],
                slide: function (event, ui) {
                    $('#facetview_rangechoices .facetview_lowrangeval').html(values[ ui.values[0] ]);
                    $('#facetview_rangechoices .facetview_highrangeval').html(values[ ui.values[1] ]);
                }
            });
            $('#facetview_rangechoices .facetview_lowrangeval').html(values[0]);
            $('#facetview_rangechoices .facetview_highrangeval').html(values[ values.length - 1]);
        };

        var setScope = function () {
            if ($('input[name=\"scientific\"]').attr('checked') && (options.scope != 'scientific')) {
                options.scope = 'scientific';
                options.paging.from = 0;
                dosearch();
            }
            else if (!($('input[name=\"scientific\"]').attr('checked')) && (options.scope == 'scientific')) {
                options.scope = null;
                options.paging.from = 0;
                dosearch();
            }
            if (($('input[name=\"fulltext\"]').attr('checked')) && (!options['fulltext'])) {
                options['fulltext'] = true;
                options.paging.from = 0;
                dosearch();
            }
            else if (!($('input[name=\"fulltext\"]').attr('checked')) && (options['fulltext'])) {
                options['fulltext'] = false;
                options.paging.from = 0;
                dosearch();
            }
            if (($('input[name=\"scholarly\"]').attr('checked')) && (!options['scholarly'])) {
                options['scholarly'] = true;
                options.paging.from = 0;
                dosearch();
            }
            else if (!($('input[name=\"scholarly\"]').attr('checked')) && (options['scholarly'])) {
                options['scholarly'] = false;
                options.paging.from = 0;
                dosearch();
            }
        };

        var setDateRange = function () {
            var day_from = 1;
            var month_from = 0;

            var values = [];
            var valsobj = $(this).parent().parent();
            valsobj.children('li').children('a').each(function () {
                var theDate = new Date(parseInt($(this).attr('href')));
                var years = theDate.getFullYear();
                values.push(years);
            });
            //values = values.sort();
            var year_from = values[0];
            if (year_from == 0) {
                year_from = 1;
            }

            var range = "";
            if ($('input[name=\"day_from\"]').val()) {
                day_from = parseInt($('input[name=\"day_from\"]').val());
            }
            if ($('input[name=\"month_from\"]').val()) {
                month_from = parseInt($('input[name=\"month_from\"]').val()) - 1;
            }
            if ($('input[name=\"year_from\"]').val()) {
                year_from = parseInt($('input[name=\"year_from\"]').val());
            }

            var day_to = 1;
            var month_to = 0;
            var year_to = values[values.length - 1];

            if ($('input[name=\"day_to\"]').val()) {
                day_to = parseInt($('input[name=\"day_to\"]').val());
            }
            if ($('input[name=\"month_to\"]').val()) {
                month_to = parseInt($('input[name=\"month_to\"]').val()) - 1;
            }
            if ($('input[name=\"year_to\"]').val()) {
                year_to = parseInt($('input[name=\"year_to\"]').val());
            }

            range += (day_from) + '-' + (month_from + 1) + '-' + year_from;
            range += " to ";
            range += (day_to) + '-' + (month_to + 1) + '-' + year_to;

            var date_from = new Date(year_from, month_from, day_from, 0, 0, 0, 0);
            var date_to = new Date(year_to, month_to, day_to, 0, 0, 0, 0);

            //console.log(date_from.toString('yyyy-MM-dd'));
            //console.log(date_to.toString('yyyy-MM-dd'));

            var rel = $(this).attr('rel');
            var newobj = '<a class="facetview_filterselected facetview_facetrange facetview_clear ' +
                    'btn btn-info" rel="' + rel +
                    '" alt="remove" title="remove"' +
                    ' href="' + date_from.getTime() + '_' + date_to.getTime() + '">' +
                    range + ' <i class="icon-remove"></i></a>';
            $('#facetview_selectedfilters').append(newobj);
            $('.facetview_filterselected').unbind('click', clearfilter);
            $('.facetview_filterselected').bind('click', clearfilter);
            options.paging.from = 0;
            dosearch();
        };

        // set the available filter values based on results
        var putvalsinfilters = function (data) {
            // for each filter setup, find the results for it and append them to the relevant filter
            for (var each in options.facets) {
                $('#facetview_' + options.facets[each]['display']).children('li').remove();

                if (options.facets[each]['type'] == 'date') {
                    //console.log(data["facets"][ options.facets[each]['display'] ]);
                    var records = data["facets"][ options.facets[each]['display'] ];
                    for (var item in records) {
                        var itemint = parseInt(item, "10");
                        var theDate = new Date(itemint);
                        var years = theDate.getFullYear();
                        var append = '<li><a class="facetview_filterchoice' +
                                '" rel="' + options.facets[each]['field'] +
                                '" href="' + item + '">' + years +
                                ' (' + addCommas(records[item]) + ')</a></li>';
                        $('#facetview_' + options.facets[each]['display']).append(append);
                    }
                }
                else {
                    var records = data["facets"][ options.facets[each]['display'] ];
                    var numb = 0;
                    for (var item in records) {
                        if (numb >= options.facets[each]['size']) {
                            break;
                        }

                        var item2 = item;
                        if (options.facets[each]['display'].indexOf('class') != -1)
                            item2 = item.replace(/\s/g, '');
                        var append = '<li><a class="facetview_filterchoice' +
                                '" rel="' + options.facets[each]['field'] + '" href="' + item + '">' + item2 +
                                ' (' + addCommas(records[item]) + ')</a></li>';
                        $('#facetview_' + options.facets[each]['display']).append(append);
                        numb++;
                    }
                }
                if (!$('.facetview_filtershow[rel="' + options.facets[each]['display'] +
                        '"]').hasClass('facetview_open')) {
                    $('#facetview_' + options.facets[each]['display']).children("li").hide();
                }
                if ($('#facetview_visualisation_' + options.facets[each]['display']).length > 0) {
                    $('.facetview_visualise[href=' + options.facets[each]['display'] + ']').trigger('click');
                }
            }
            $('.facetview_filterchoice').bind('click', clickfilterchoice);
        };
    
        var add_facet = function (event) {
            event.preventDefault();
            var truc = {'field': 'undefined', 'display': 'new_facet', 'size': 0, 'type': '', 'view': 'hidden'};
            options.facets.push(truc);
            buildfilters();
            dosearch();
        };

        var add_field = function (event) {
            event.preventDefault();
            var nb_fields = options['complex_fields'] + 1;
            $(this).parent().parent().append(field_complex.replace(/{{NUMBER}}/gi, '' + nb_fields)
                    .replace(/{{HOW_MANY}}/gi, options.paging.size));

            // bind the new thingies in the field
            $('#facetview_partial_match' + nb_fields).bind('click', fixmatch);
            $('#facetview_exact_match' + nb_fields).bind('click', fixmatch);
            $('#facetview_fuzzy_match' + nb_fields).bind('click', fixmatch);
            $('#facetview_match_any' + nb_fields).bind('click', fixmatch);
            $('#facetview_match_all' + nb_fields).bind('click', fixmatch);
            $('#facetview_howmany' + nb_fields).bind('click', howmany);

            $('#field_all_text' + nb_fields).bind('click', set_field);
            $('#field_title' + nb_fields).bind('click', set_field);
            $('#field_abstract' + nb_fields).bind('click', set_field);
            $('#field_claims' + nb_fields).bind('click', set_field);
            $('#field_description' + nb_fields).bind('click', set_field);
            $('#field_class_ipc' + nb_fields).bind('click', set_field);
            $('#field_class_ecla' + nb_fields).bind('click', set_field);
            $('#field_country' + nb_fields).bind('click', set_field);
            $('#field_author' + nb_fields).bind('click', set_field);
            $('#field_applicant' + nb_fields).bind('click', set_field);
            $('#field_inventor' + nb_fields).bind('click', set_field);

            $('#lang_all' + nb_fields).bind('click', set_field);
            $('#lang_en' + nb_fields).bind('click', set_field);
            $('#lang_de' + nb_fields).bind('click', set_field);
            $('#lang_fr' + nb_fields).bind('click', set_field);

            $('#must' + nb_fields).bind('click', set_field);
            $('#should' + nb_fields).bind('click', set_field);
            $('#must_not' + nb_fields).bind('click', set_field);

            options['complex_fields'] = nb_fields;

            // resize the new field
            thewidth = $('#facetview_searchbar' + nb_fields).parent().width();
            $('#facetview_searchbar' + nb_fields).css('width', (thewidth / 2) - 30 + 'px');
            $('#facetview_freetext' + nb_fields).css('width', (thewidth / 2) - 30 + 'px');

            // bind the new input field with the query callback
            $('#facetview_freetext' + nb_fields, obj).bindWithDelay('keyup', dosearch, options.freetext_submit_delay);
        };

        var set_field = function (event) {
            event.preventDefault();
            var theID = $(this).attr("rank");
            var labelID = $(this).attr("label");
            $('#label' + labelID + '_facetview_searchbar' + theID).empty();
            $('#label' + labelID + '_facetview_searchbar' + theID).append($(this).text());
            dosearch();
        };

        // show the add/remove filters options
        var addremovefacet = function (event) {
            event.preventDefault();
            if ($(this).hasClass('facetview_filterexists')) {
                $(this).removeClass('facetview_filterexists');
                delete options.facets[$(this).attr('href')];
            } else {
                $(this).addClass('facetview_filterexists');
                options.facets.push({'field': $(this).attr('title')});
            }
            buildfilters();
            dosearch();
        };
        var showarf = function (event) {
            event.preventDefault()
            $('#facetview_addremovefilters').toggle()
        };
        var addremovefacets = function () {
            $('#facetview_filters').append('<a id="facetview_showarf" href="">' +
                    'add or remove filters</a><div id="facetview_addremovefilters"></div>')
            for (var idx in options.facets) {
                if (options.addremovefacets.indexOf(options.facets[idx].field) == -1) {
                    options.addremovefacets.push(options.facets[idx].field)
                }
            }
            for (var facet in options.addremovefacets) {
                var thisfacet = options.addremovefacets[facet]
                var filter = '<a class="btn '
                var index = 0
                var icon = '<i class="icon-plus"></i>'
                for (var idx in options.facets) {
                    if (options.facets[idx].field == thisfacet) {
                        filter += 'btn-info facetview_filterexists'
                        index = idx
                        icon = '<i class="icon-remove icon-white"></i> '
                    }
                }
                filter += ' facetview_filterchoose" style="margin-top:5px;" href="' + index + '" title="' + thisfacet + '">' + icon + thisfacet + '</a><br />'
                $('#facetview_addremovefilters').append(filter)
            }
            $('#facetview_addremovefilters').hide();
            $('#facetview_showarf').bind('click', showarf);
            $('.facetview_filterchoose').bind('click', addremovefacet);
        };
    
        // pass a list of filters to be displayed
        var buildfilters = function () {
            var filters = options.facets;
            //var thefilters = "<h3>Facets</h3>";
            var thefilters = "";

            for (var idx in filters) {
                var _filterTmpl = ' \
                    <div id="facetview_filterbuttons" class="btn-group"> \
                    <a style="text-align:left; min-width:70%;" class="facetview_filtershow btn" \
                      rel="{{FILTER_NAME}}" href=""> \
                      <!--i class="icon-plus"--></i> \
                      {{FILTER_DISPLAY}}</a> \
                      <a class="btn dropdown-toggle" data-toggle="dropdown" \
                      href="#"><span class="caret"></span></a> \
                      <ul class="dropdown-menu"> \
                        <li><a class="facetview_sort facetview_count" href="{{FILTER_EXACT}}">sort by count</a></li> \
                        <li><a class="facetview_sort facetview_term" href="{{FILTER_EXACT}}">sort by term</a></li> \
                        <li><a class="facetview_sort facetview_rcount" href="{{FILTER_EXACT}}">sort reverse count</a></li> \
                        <li><a class="facetview_sort facetview_rterm" href="{{FILTER_EXACT}}">sort reverse term</a></li> \
                        <li class="divider"></li> \
                        <li><a class="facetview_facetrange" rel="{{FACET_IDX}}" href="{{FILTER_EXACT}}">apply a filter range</a></li>{{FACET_VIS}} \
                        <li><a class="facetview_morefacetvals" rel="{{FACET_IDX}}" href="{{FILTER_EXACT}}">show up to ({{FILTER_HOWMANY}})</a></li> \
                        <li class="divider"></li> \
                        <li><a class="facetview_editfilter" rel="{{FACET_IDX}}" href="{{FILTER_EXACT}}">Edit this filter</a></li> \
                        </ul></div> \
						 <ul id="facetview_{{FILTER_NAME}}" \
                        class="facetview_filters"> \
                    	';
                if (filters[idx]['type'] == 'date') {
                    _filterTmpl +=
                            '<div id="date-input" style="position:relative;margin-top:-15px;margin-bottom:10px;margin-left:-30px;"> \
						   <input type="text" id="day_from" name="day_from" \
						   size="2" style="width: 18px;" placeholder="DD"/> \
						   <input type="text" id="month_from" name="month_from" size="2" \
						   style="width: 22px;" placeholder="MM"/> \
						   <input type="text" id="year_from" name="year_from" size="4" \
						   style="width: 34px;"  placeholder="YYYY"/> \
						   to <input type="text" id="day_to" name="day_to" size="2" \
						   style="width: 18px;" placeholder="DD"" /> \
						   <input type="text" id="month_to" name="month_to" size="2" \
						   style="width: 22px;" placeholder="MM"/> \
					   	   <input type="text" id="year_to" name="year_to" size="4" \
					       style="width: 34px;"  placeholder="YYYY"/> \
					       <div id="validate-date-range" alt="set date range" title="set date range" rel="{{FACET_IDX}}" class="icon-ok" /></div>';
                }
                _filterTmpl += '</ul>';
                if (options.visualise_filters) {
                    var vis = '<li><a class="facetview_visualise" rel="{{FACET_IDX}}" href="{{FILTER_DISPLAY}}">visualise this filter</a></li>';
                    thefilters += _filterTmpl.replace(/{{FACET_VIS}}/g, vis);
                }
                else {
                    thefilters += _filterTmpl.replace(/{{FACET_VIS}}/g, '');
                }
                thefilters = thefilters.replace(/{{FILTER_NAME}}/g, filters[idx]['display'])
                        .replace(/{{FILTER_EXACT}}/g, filters[idx]['display']);

                if ('size' in filters[idx]) {
                    thefilters = thefilters.replace(/{{FILTER_HOWMANY}}/gi, filters[idx]['size']);
                }
                else {
                    // default if size is not indicated in the parameters
                    thefilters = thefilters.replace(/{{FILTER_HOWMANY}}/gi, 6);
                }
                thefilters = thefilters.replace(/{{FACET_IDX}}/gi, idx);
                if ('display' in filters[idx]) {
                    thefilters = thefilters.replace(/{{FILTER_DISPLAY}}/g, filters[idx]['display']);
                } else {
                    thefilters = thefilters.replace(/{{FILTER_DISPLAY}}/g, filters[idx]['field']);
                }
            }

            var temp_intro = '<a style="text-align:left; min-width:20%;margin-bottom:10px;" class="btn" \
             		id="new_facet" href=""> \
             		<i class="icon-plus"></i> add new facet </a> \
			';
            $('#facetview_filters').html("").append(temp_intro);
            $('#new_facet').bind('click', add_facet);
        
            var temp_intro = '<form class="well" id="scope_area"><label class="checkbox">' +
                    '<input type="checkbox" name="scientific" checked>Technical content</label>';
            temp_intro += '<label class="checkbox">' +
                    '<input type="checkbox" name="fulltext" checked>Full text available online</label>';
            temp_intro += '<label class="checkbox">' +
                    '<input type="checkbox" name="scholarly">Scholarly content</label>';
            //temp_intro += '<button type="button" class="btn" data-toggle="button">Custom scope restriction</button>';
            temp_intro += '</form>';

            $('#facetview_filters').html("").append(temp_intro);
            $('#scope_area').bind('click', setScope);
            

            $('#facetview_filters').append(thefilters);
            options.visualise_filters ? $('.facetview_visualise').bind('click', show_vis) : "";
            $('.facetview_morefacetvals').bind('click', morefacetvals);
            $('.facetview_facetrange').bind('click', facetrange);
            $('.facetview_sort').bind('click', sortfilters);
            $('.facetview_editfilter').bind('click', editfilter);
            $('.facetview_filtershow').bind('click', showfiltervals);
            options.addremovefacets ? addremovefacets() : "";
            if (options.description) {
                $('#facetview_filters').append('<div><h3>Meta</h3>' + options.description + '</div>');
            }
            $('#validate-date-range').bind('click', setDateRange);
            $('#date-input').hide();
        };

        // ===============================================
        // filter visualisations
        // ===============================================

        var show_vis = function (event) {
            event.preventDefault();
            var update = false;
            if ($('#facetview_visualisation' + '_' + $(this).attr('href')).length) {
                //$('#facetview_visualisation' + '_'+$(this).attr('href')).remove();
                update = true;
            }

            var vis;
            var indx = null;
            for (var idx in options.facets) {
                if (options.facets[idx]['display'] == $(this).attr('href')) {
                    indx = idx;
                    break;
                }
            }

            if (!update) {
                if ((options.facets[idx]['type'] == 'class') || (options.facets[idx]['type'] == 'country')) {
                    vis = '<div id="facetview_visualisation' + '_' + $(this).attr('href') + '" style="position:relative;top:5px;left:-10px;"> \
	                    <div class="modal-body2" id ="facetview_visualisation' + '_' + $(this).attr('href') + '_chart"> \
	                    </div> \
	                    </div>';
                }
                else if (options.facets[idx]['type'] == 'entity') {
                    vis = '<div id="facetview_visualisation' + '_' + $(this).attr('href') + '" style="position:relative;left:-10px;"> \
	                    <div class="modal-body2" id ="facetview_visualisation' + '_' + $(this).attr('href') + '_chart"> \
	                    </div> \
	                    </div>';
                }
                else if (options.facets[idx]['type'] == 'taxonomy') {
                    vis = '<div id="facetview_visualisation' + '_' + $(this).attr('href') + '" style="position:relative;top:5px;left:-15px"> \
	                    <div class="modal-body2" id ="facetview_visualisation' + '_' + $(this).attr('href') + '_chart"> \
	                    </div> \
	                    </div>';
                }
                else {
                    vis = '<div id="facetview_visualisation' + '_' + $(this).attr('href') + '" style="position:relative;left:-10px;"> \
	                    <div class="modal-body2" id ="facetview_visualisation' + '_' + $(this).attr('href') + '_chart" style="position:relative;left:-18px;"> \
	                    </div> \
	                    </div>';
                }
                vis = vis.replace(/{{VIS_TITLE}}/gi, $(this).attr('href'));
                $('#facetview_' + $(this).attr('href')).prepend(vis);
            }
            var parentWidth = $('#facetview_filters').width();

            if ((options.facets[idx]['type'] == 'class') || (options.facets[idx]['type'] == 'country')) {
                donut2($(this).attr('rel'), $(this).attr('href'),
                        parentWidth * 0.8, 'facetview_visualisation' + '_' + $(this).attr('href') + "_chart", update);
            }
            else if (options.facets[idx]['type'] == 'date') {
                timeline($(this).attr('rel'), parentWidth * 0.75,
                        'facetview_visualisation' + '_' + $(this).attr('href') + "_chart");
                $('#date-input').show();
            }
            else if (options.facets[idx]['type'] == 'taxonomy') {
                wheel($(this).attr('rel'), $(this).attr('href'), parentWidth * 0.8,
                        'facetview_visualisation' + '_' + $(this).attr('href') + "_chart", update);
            }
            else {
                bubble($(this).attr('rel'), parentWidth * 0.8,
                        'facetview_visualisation' + '_' + $(this).attr('href') + "_chart", update);
            }

        };

        var wheel = function (facetidx, facetkey, width, place, update) {
            var w = width,
                    h = w,
                    r = w / 2,
                    x = d3.scale.linear().range([0, 2 * Math.PI]),
                    y = d3.scale.pow().exponent(1.3).domain([0, 1]).range([0, r]),
                    p = 5,
                    duration = 1000;

            var vis = d3.select("#facetview_visualisation_" + facetkey + " > .modal-body2");
            if (update) {
                vis.select("svg").remove();
            }

            vis = d3.select("#facetview_visualisation_" + facetkey + " > .modal-body2").append("svg:svg")
                    .attr("width", w + p * 2)
                    .attr("height", h + p * 2)
                    .append("g")
                    .attr("transform", "translate(" + (r + p) + "," + (r + p) + ")");

            var partition = d3.layout.partition()
                    .sort(null)
                    .value(function (d) {
                        return 5.8 - d.depth;
                    });

            var arc = d3.svg.arc()
                    .startAngle(function (d) {
                        return Math.max(0, Math.min(2 * Math.PI, x(d.x)));
                    })
                    .endAngle(function (d) {
                        return Math.max(0, Math.min(2 * Math.PI, x(d.x + d.dx)));
                    })
                    .innerRadius(function (d) {
                        return Math.max(0, d.y ? y(d.y) : d.y);
                    })
                    .outerRadius(function (d) {
                        return Math.max(0, y(d.y + d.dy));
                    });

            //var fill = d3.scale.log(.1, 1).domain([0.005,0.1]).range(["#FF7700","#FCE6D4"]);
            var fill = d3.scale.log(.1, 1).domain([0.005, 0.1]).range(["#FCE6D4", "#FF7700"]);

            var facetfield = options.facets[facetidx]['field'];
            var records = options.data['facets'][facetkey];
            var datas = [];
            var sum = 0;
            var numb = 0;
            for (var item in records) {
                if (numb >= options.facets[facetidx]['size']) {
                    break;
                }
                var item2 = item.replace(/\s/g, '');
                var count = records[item];
                sum += count;

                var ind = item2.indexOf(".");
                if (ind != -1) {
                    // first level
                    var item3 = item2.substring(0, ind);
                    var found3 = false;
                    for (var p in datas) {
                        if (datas[p].term == item3) {
                            datas[p]['count'] += records[item];
                            found3 = true;
                            break;
                        }
                    }
                    if (!found3) {
                        datas.push({'term': item3, 'count': records[item], 'source': item, 'relCount': 0});
                    }
                    var ind2 = item2.indexOf(".", ind + 1);
                    if (ind2 != -1) {
                        //second level
                        var item4 = item2.substring(0, ind2);
                        var found4 = false;
                        for (var p in datas) {
                            if (datas[p].term == item4) {
                                datas[p]['count'] += records[item];
                                found4 = true;
                                break;
                            }
                        }
                        if (!found4) {
                            datas.push({'term': item4, 'count': records[item], 'source': item, 'relCount': 0});
                        }
                        datas.push({'term': item2, 'count': records[item], 'source': item, 'relCount': 0});
                    }
                    else {
                        var found3 = false;
                        for (var p in datas) {
                            if (datas[p].term == item3) {
                                datas[p]['count'] += records[item];
                                found3 = true;
                                break;
                            }
                        }
                        if (!found3) {
                            datas.push({'term': item3, 'count': records[item], 'source': item, 'relCount': 0});
                        }
                        datas.push({'term': item2, 'count': records[item], 'source': item, 'relCount': 0});
                    }
                }
                else {
                    var found2 = false;
                    for (var p in datas) {
                        if (datas[p].term == item2) {
                            datas[p]['count'] += records[item];
                            found2 = true;
                            break;
                        }
                    }
                    if (!found2) {
                        datas.push({'term': item2, 'count': records[item], 'source': item, 'relCount': 0});
                    }
                }
                numb++;
            }
            //console.log('wheel data:');			
            //console.log(datas);
            for (var item in datas) {
                datas[item]['relCount'] = datas[item]['count'] / sum;
            }

            //var entries = datas.sort( function(a, b) { return a.count > b.count ? -1 : 1; } );
            //var entries = datas.sort( function(a, b) { return a.name > b.name ? -1 : 1; } );
            var entries = datas;
            /*var data0 = [];
             for(var item in entries) {
             data0.push(entries[item]['count']);
             }*/

            var json = [];

            // first level
            for (var item in entries) {
                var symbol = entries[item]['term'];
                var ind = symbol.indexOf(".");
                if (ind == -1) {
                    //first level category
                    json.push({'name': symbol, 'colour': fill(entries[item]['relCount'])});
                }
            }

            //second level
            for (var item in entries) {
                //var ind = entries[item]['term'].indexOf(":");
                var ind = entries[item]['term'].indexOf(".");
                if (ind != -1) {
                    var symbol = entries[item]['term'];
                    var motherCategory = symbol.substring(0, ind);
                    for (item2 in json) {
                        if (json[item2]['name'] == motherCategory) {
                            // second level category
                            var children = [];
                            if (json[item2]['children']) {
                                children = json[item2]['children'];
                            }
                            var newSymbol = symbol.substring(ind + 1, symbol.length);
                            var ind2 = newSymbol.indexOf(".");
                            if (ind2 == -1) {
                                children.push({'name': newSymbol,
                                    'colour': fill(entries[item]['relCount'])});
                                json[item2]['children'] = children;
                            }
                            break;
                        }
                    }
                }
            }

            // third and last level
            for (var item in entries) {
                var ind = entries[item]['term'].indexOf(".");
                if (ind != -1) {
                    var symbol = entries[item]['term'];
                    var motherCategory = symbol.substring(0, ind);
                    for (item2 in json) {
                        if (json[item2]['name'] == motherCategory) {
                            var newSymbol = symbol.substring(ind + 1, symbol.length);
                            //var ind2 = newSymbol.indexOf(":");
                            var ind2 = newSymbol.indexOf(".");
                            if (ind2 != -1) {
                                var motherCategory2 = newSymbol.substring(0, ind2);
                                for (item3 in json[item2]['children']) {
                                    if (json[item2]['children'][item3]['name'] == motherCategory2) {
                                        // third level category (and last one)
                                        var children2 = [];
                                        if (json[item2]['children'][item3]['children']) {
                                            children2 = json[item2]['children'][item3]['children']
                                        }
                                        children2.push({'name': newSymbol.substring(ind2 + 1, newSymbol.length),
                                            'colour': fill(entries[item]['relCount'])});
                                        json[item2]['children'][item3]['children'] = children2;
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }

            //console.log(json);
            //console.log(JSON.stringify(json,null, 2));

            var nodes = partition.nodes({children: json});
            var path = vis.selectAll("path")
                    .data(nodes);
            path.enter().append("path")
                    .attr("id", function (d, i) {
                        return "path-" + i;
                    })
                    .attr("d", arc)
                    .attr("fill-rule", "evenodd")
                    .style("fill", colour)
                    //.style("fill", function(d) { return fill(d.data); })
                    .on("click", click);

            var text = vis.selectAll("text").data(nodes);
            var textEnter = text.enter().append("text")
                    .style("fill-opacity", 1)
                    .style("fill", function (d) {
                        return brightness(d3.rgb(colour(d))) < 125 ? "#eee" : "#000";
                    })
                    .attr("text-anchor", function (d) {
                        return x(d.x + d.dx / 2) > Math.PI ? "end" : "start";
                    })
                    .attr("dy", ".2em")
                    .attr("transform", function (d) {
                        var multiline = (d.name || "").split(" ").length > 1,
                                angle = x(d.x + d.dx / 2) * 180 / Math.PI - 90,
                                rotate = angle + (multiline ? -.5 : 0);
                        return "rotate(" + rotate + ")translate(" + (y(d.y) + p) + ")rotate(" + (angle > 90 ? -180 : 0) + ")";
                    })
                    .on("click", click);
            textEnter.append("tspan")
                    .attr("x", 0)
                    .text(function (d) {
                        return d.depth ? d.name.split(" ")[0] : "";
                    });
            textEnter.append("tspan")
                    .attr("x", 0)
                    .attr("dy", "1em")
                    .text(function (d) {
                        return d.depth ? d.name.split(" ")[1] || "" : "";
                    });


            function click(d) {
                // we need to reconstitute the complete field name
                var theName = d.name;
                if (d.parent && d.parent.name) {
                    //theName = d.parent.name + ":" + theName;
                    theName = d.parent.name + "." + theName;
                    if (d.parent.parent && d.parent.parent.name) {
                        //theName = d.parent.parent.name + ":" + theName;
                        theName = d.parent.parent.name + "." + theName;
                    }
                }

                clickGraph(facetfield, theName, theName);

                path.transition()
                        .duration(duration)
                        .attrTween("d", arcTween(d));

                // Somewhat of a hack as we rely on arcTween updating the scales.
                text
                        .style("visibility", function (e) {
                            return isParentOf(d, e) ? null : d3.select(this).style("visibility");
                        })
                        .transition().duration(duration)
                        .attrTween("text-anchor", function (d) {
                            return function () {
                                return x(d.x + d.dx / 2) > Math.PI ? "end" : "start";
                            };
                        })
                        .attrTween("transform", function (d) {
                            var multiline = (d.name || "").split(" ").length > 1;
                            return function () {
                                var angle = x(d.x + d.dx / 2) * 180 / Math.PI - 90,
                                        rotate = angle + (multiline ? -.5 : 0);
                                return "rotate(" + rotate + ")translate(" + (y(d.y) + p) + ")rotate(" + (angle > 90 ? -180 : 0) + ")";
                            };
                        })
                        .style("fill-opacity", function (e) {
                            return isParentOf(d, e) ? 1 : 1e-6;
                        })
                        .each("end", function (e) {
                            d3.select(this).style("visibility", isParentOf(d, e) ? null : "hidden");
                        });
            }

            function isParentOf(p, c) {
                if (p === c)
                    return true;
                if (p.children) {
                    return p.children.some(function (d) {
                        return isParentOf(d, c);
                    });
                }
                return false;
            }

            function colour(d) {
                if (d.children) {
                    // There is a maximum of two children!
                    var colours = d.children.map(colour),
                            a = d3.hsl(colours[0]),
                            b = d3.hsl(colours[1]);
                    // L*a*b* might be better here...
                    return d3.hsl((a.h + b.h) / 2, a.s * 1.2, a.l / 1.2);
                }
                return d.colour || "#fff";
            }

            // Interpolate the scales!
            function arcTween(d) {
                var my = maxY(d),
                        xd = d3.interpolate(x.domain(), [d.x, d.x + d.dx]),
                        yd = d3.interpolate(y.domain(), [d.y, my]),
                        yr = d3.interpolate(y.range(), [d.y ? 20 : 0, r]);
                return function (d) {
                    return function (t) {
                        x.domain(xd(t));
                        y.domain(yd(t)).range(yr(t));
                        return arc(d);
                    };
                };
            }

            function maxY(d) {
                return d.children ? Math.max.apply(Math, d.children.map(maxY)) : d.y + d.dy;
            }

            // http://www.w3.org/WAI/ER/WD-AERT/#color-contrast
            function brightness(rgb) {
                return rgb.r * .299 + rgb.g * .587 + rgb.b * .114;

            }
        };
/*
        var donut = function (facetidx, facetkey, width, place) {
            var facetfield = options.facets[facetidx]['field'];
            var records = options.data['facets'][facetkey];

            if (records.length == 0) {
                $('#' + place).hide();
                return;
            }
            else {
                var siz = 0;
                for (var item in records) {
                    siz++;
                }
                if (siz == 0) {
                    $('#' + place).hide();
                    return;
                }
            }
            $('#' + place).show();

            options.data.facets2[facetkey] = [];
            var numb = 0;
            for (var item in records) {
                if (numb >= options.facets[facetidx]['size']) {
                    break;
                }
                var item2 = item.replace(/\s/g, '');
                options.data.facets2[facetkey].push({'term': item2, 'count': records[item], 'source': item});
                numb++;
            }

            var data = options.data.facets2[facetkey];

            var entries = data.sort(function (a, b) {
                return a.term < b.term ? -1 : 1;
            }),
                    // Create an array holding just the values (counts)
                    values = pv.map(entries, function (e) {
                        return e.count;
                    });

            // Set-up dimensions and color scheme for the chart
            var w = width,
                    h = width * 0.75;

            // Create the basis panel
            var vis = new pv.Panel()
                    .width(w)
                    .height(h)
                    .margin(5, 0, 0, 0);

            // Create the "wedges" of the chart
            vis.add(pv.Wedge)
                    // Set-up auxiliary variable to hold state (mouse over / out)
                    .def("active", -1)
                    // Pass the normalized data to Protovis
                    .data(pv.normalize(values))
                    // Set-up chart position and dimension
                    .left(w / 2.6)
                    .top(w / 2.6)
                    .outerRadius(w / 2.6)
                    // Create a "donut hole" in the center of the chart
                    .innerRadius(15)
                    // Compute the "width" of the wedge
                    .angle(function (d) {
                        return d * 2 * Math.PI;
                    })
                    .fillStyle(pv.Scale.log(.1, 1).range("#FF7700", "#FCE6D4"))
                    // Add white stroke
                    .strokeStyle("#fff")
                    .event("mousedown", function (d) {
                        var term = entries[this.index].term;
                        var source = entries[this.index].source;
                        if (source)
                            clickGraph(facetfield, term, source);
                        else
                            clickGraph(facetfield, term, term);
                        //return (alert("Filter the results by '"+term+"'"));
                    })
                    .anchor("center")
                    .add(pv.Label)
                    .data(entries)
                    .text(function (d) {
                        return d.term;
                    })
                    .textAngle(0)
                    .textStyle("black")
                    .font("09pt sans-serif")

                    // Bind the chart to DOM element
                    .root.canvas(place)
                    // And render it.
                    .render();
        }
*/

        var donut2 = function (facetidx, facetkey, width, place, update) {
            var vis = d3.select("#facetview_visualisation_" + facetkey + " > .modal-body2");

            var facetfield = options.facets[facetidx]['field'];
            var records = options.data['facets'][facetkey];
            if (records.length === 0) {
                $('#' + place).hide();
                return;
            }
            else {
                var siz = 0;
                for (var item in records) {
                    siz++;
                }
                if (siz === 0) {
                    $('#' + place).hide();
                    return;
                }
            }
            $('#' + place).show();

            var data2 = [];
            var sum = 0;
            var numb = 0;
            for (var item in records) {
                if (records[item] > 0) {

                    if (numb >= options.facets[facetidx]['size']) {
                        break;
                    }

                    var item2 = item.replace(/\s/g, '');
                    var count = records[item];
                    sum += count;
                    data2.push({'term': item2, 'count': records[item], 'source': item, 'relCount': 0});
                    numb++;
                }
            }

            for (var item in data2) {
                if (data2[item]['count'] > 0) {
                    data2[item]['relCount'] = data2[item]['count'] / sum;
                }
            }

            var data = data2;
            var entries = data.sort(function (a, b) {
                return a.count < b.count ? -1 : 1;
            });

            var data0 = [];
            for (var item in entries) {
                data0.push(entries[item]['relCount']);
            }

            var height = width * 0.75,
                    outerRadius = Math.min(width, height) / 2,
                    innerRadius = outerRadius * .2,
                    n = data0.length,
                    q = 0,
                    //color = d3.scale.log(.1, 1).range(["#FF7700","#FCE6D4"]),
                    //color = d3.scale.log(.01, .9).range(["#FF7700","#FCE6D4"]),
                    arc = d3.svg.arc(),
                    //fill = d3.scale.log(.1, 1).domain([0.1,0.9]).range(["#FF7700","#FCE6D4"]);
                    fill = d3.scale.log(.1, 1).domain([0.1, 0.9]).range([fillDefaultColor, fillDefaultColorLight]);

            //fill = d3.scale.log(.1, 1).range(["#FF7700","#FCE6D4"]),
            donute = d3.layout.pie().sort(null);

            if (update) {
                vis.selectAll("svg").remove();
            }

            var data_arc = arcs(data0);
            vis.append("svg:svg")
                    .attr("width", width)
                    .attr("height", height)
                    .selectAll("g.arc")
                    //.data(arcs(data0))
                    .data(data_arc)
                    .enter()
                    .append("g")
                    .attr("class", "arc")
                    .attr("transform", "translate(" + (outerRadius * 1.3) + "," + outerRadius + ")")
                    .attr("index", function (d) {
                        return d.index;
                    })
                    .on("mousedown", function (d) {
                        var index = this.getAttribute("index");
                        var term = entries[index].term;
                        var source = entries[index].source;
                        if (source)
                            clickGraph(facetfield, term, source);
                        else
                            clickGraph(facetfield, term, term);
                    })
                    .append("path")
                    //.attr("fill", function(d, i) { return color(entries[i]['relCount']); })
                    .style("fill", function (d) {
                        return fill(d.data);
                    })
                    .attr("stroke", "#fff")
                    .attr("d", arc);

            // we need to re-create all the arcs for placing the text labels, so that the labels
            // are not covered by the arcs
            // we also enlarge the svg area for the labels so that they are not cut
            var text = vis.select("svg")
                    .append("svg:svg")
                    .attr("width", width * 1.2)
                    .attr("height", height * 1.2)
                    .selectAll("g")
                    .data(data_arc)
                    .enter()
                    .append("g")
                    .attr("class", "arc")
                    .attr("transform", "translate(" + (outerRadius * 1.30) + "," + outerRadius + ")")
                    .append("text")
                    .attr("class", "labels")
                    .attr("transform", function (d) {
                        d.innerRadius = innerRadius;
                        d.outerRadius = outerRadius * 1.30;
                        return "translate(" + arc.centroid(d) + ")";
                    })
                    .attr('text-anchor', 'middle')
                    .text(function (d) {
                        return d.term
                    })
                    .style("textStyle", "black")
                    .style("font", "09pt sans-serif")
                    .attr("index", function (d) {
                        return d.index;
                    })
                    .on("mousedown", function (d) {
                        var index = this.getAttribute("index")
                        var term = entries[index].term;
                        var source = entries[index].source;
                        if (source)
                            clickGraph(facetfield, term, source);
                        else
                            clickGraph(facetfield, term, term);
                    });

            // Store the currently-displayed angles in this._current.
            // Then, interpolate from this._current to the new angles.
            function arcTween(a) {
                var i = d3.interpolate(this._current, a);
                this._current = i(0);
                return function (t) {
                    return arc(i(t));
                };
            }

            function arcs(data0) {
                var arcs0 = donute(data0),
                        i = -1,
                        arc;
                while (++i < n) {
                    arc = arcs0[i];
                    arc.innerRadius = innerRadius;
                    arc.outerRadius = outerRadius;
                    arc.next = arcs0[i];
                    arc.term = entries[i]['term'];
                    arc.index = i;
                }
                return arcs0;
            }

            function swap() {
                d3.selectAll("g.arc > path")
                        .data(++q & 1 ? arcs(data0, data1) : arcs(data1, data0))
                        .each(transitionSplit);
            }

            // 1. Wedges split into two rings.
            function transitionSplit(d, i) {
                d3.select(this)
                        .transition().duration(1000)
                        .attrTween("d", tweenArc({
                            innerRadius: i & 1 ? innerRadius : (innerRadius + outerRadius) / 2,
                            outerRadius: i & 1 ? (innerRadius + outerRadius) / 2 : outerRadius
                        }))
                        .each("end", transitionRotate);
            }

            // 2. Wedges translate to be centered on their final position.
            function transitionRotate(d, i) {
                var a0 = d.next.startAngle + d.next.endAngle,
                        a1 = d.startAngle - d.endAngle;
                d3.select(this)
                        .transition().duration(1000)
                        .attrTween("d", tweenArc({
                            startAngle: (a0 + a1) / 2,
                            endAngle: (a0 - a1) / 2
                        }))
                        .each("end", transitionResize);
            }

            // 3. Wedges then update their values, changing size.
            function transitionResize(d, i) {
                d3.select(this)
                        .transition().duration(1000)
                        .attrTween("d", tweenArc({
                            startAngle: d.next.startAngle,
                            endAngle: d.next.endAngle
                        }))
                        .each("end", transitionUnite);
            }

            // 4. Wedges reunite into a single ring.
            function transitionUnite(d, i) {
                d3.select(this)
                        .transition().duration(1000)
                        .attrTween("d", tweenArc({
                            innerRadius: innerRadius,
                            outerRadius: outerRadius
                        }));
            }

            function tweenArc(b) {
                return function (a) {
                    var i = d3.interpolate(a, b);
                    for (var key in b)
                        a[key] = b[key]; // update data
                    return function (t) {
                        return arc(i(t));
                    };
                };
            }
        };

        var timeline = function (facetidx, width, place) {
            var facetkey = options.facets[facetidx]['display'];
            var facetfield = options.facets[facetidx]['field'];

            // Set-up the data
            var entries = options.data.facets3[facetkey];
            // Add the last "blank" entry for proper timeline ending
            if (entries.length > 0) {
                //if (entries.length == 1) {	
                entries.push({count: entries[entries.length - 1].count});
            }

            // Set-up dimensions and scales for the chart
            var w = 250,
                    h = 80,
                    max = pv.max(entries, function (d) {
                        return d.count;
                    }),
                    x = pv.Scale.linear(0, entries.length - 1).range(0, w),
                    y = pv.Scale.linear(0, max).range(0, h);

            // Create the basis panel
            var vis = new pv.Panel()
                    .width(w)
                    .height(h)
                    .bottom(40)
                    .left(0)
                    .right(0)
                    .top(3);

            // Add the X-ticks
            vis.add(pv.Rule)
                    .data(entries)
                    .visible(function (d) {
                        return d.time;
                    })
                    .left(function () {
                        return x(this.index);
                    })
                    .bottom(-15)
                    .height(15)
                    .strokeStyle("#33A3E1")
                    // Add the tick label
                    .anchor("right").add(pv.Label)
                    .text(function (d) {
                        var date = new Date(parseInt(d.time));
                        var year = date.getYear();
                        if (year >= 100) {
                            year = year - 100;
                        }
                        if (year === 0) {
                            year = '00';
                        }
                        else if (year < 10) {
                            year = '0' + year;
                        }
                        return year;
                    })
                    .textStyle("#333333")
                    .textMargin("2");

            // Add container panel for the chart
            vis.add(pv.Panel)
                    // Add the area segments for each entry
                    .add(pv.Area)
                    // Set-up auxiliary variable to hold state (mouse over / out) 
                    .def("active", -1)
                    // Pass the data to Protovis
                    .data(entries)
                    .bottom(0)
                    // Compute x-axis based on scale
                    .left(function (d) {
                        return x(this.index);
                    })
                    // Compute y-axis based on scale
                    .height(function (d) {
                        return y(d.count);
                    })
                    // Make the chart curve smooth
                    .interpolate('cardinal')
                    // Divide the chart into "segments" (needed for interactivity)
                    .segmented(true)
                    .strokeStyle("#fff")
                    .fillStyle(fillDefaultColorLight)

                    // On "mouse down", perform action, such as filtering the results...
                    .event("mousedown", function (d) {
                        var time = entries[this.index].time;
                        var date = new Date(parseInt(time));
                        clickGraph(facetfield, date.getFullYear(), time);
                    })

                    // Add thick stroke to the chart
                    .anchor("top").add(pv.Line)
                    .lineWidth(3)
                    .strokeStyle(fillDefaultColor)

                    // Bind the chart to DOM element
                    .root.canvas(place)
                    // And render it.
                    .render();
        };

        var bubble = function (facetidx, width, place, update) {
            var facetkey = options.facets[facetidx]['display'];
            var facetfield = options.facets[facetidx]['field'];
            var facets = options.data['facets'][facetkey];
            if (facets.length === 0) {
                $('#' + place).hide();
                return;
            }
            else {
                var siz = 0;
                for (var item in facets) {
                    siz++;
                }
                if (siz === 0) {
                    $('#' + place).hide();
                    return;
                }
            }
            $('#' + place).show();

            var data = {"children": []};
            var count = 0;
            var numb = 0;
            for (var fct in facets) {
                if (numb >= options.facets[facetidx]['size']) {
                    break;
                }

                var arr = {
                    "className": fct,
                    "packageName": count++,
                    "value": facets[fct]
                };
                data["children"].push(arr);
                numb++;
            }

            var r = width,
                    format = d3.format(",d"),
                    fill = d3.scale.linear().domain([0, 5]).range(["#FF7700", "#FCE6D4"]);
            var bubblee = d3.layout.pack()
                    .sort(null)
                    .size([r, r]);

            var vis = d3.select("#facetview_visualisation_" + facetkey + " > .modal-body2");
            if (update) {
                vis.selectAll("svg").remove();
            }

            vis = d3.select("#facetview_visualisation_" + facetkey + " > .modal-body2").append("svg:svg")
                    .attr("width", r * 1.5)
                    .attr("height", r)
                    .attr("class", "bubble");
            var node = vis.selectAll("g.node")
                    .data(bubblee(data)
                            .filter(function (d) {
                                return !d.children;
                            }))
                    .enter().append("svg:g")
                    .attr("class", "node")
                    .attr("transform", function (d) {
                        return "translate(" + d.x + "," + d.y + ")";
                    });
            node.append("svg:title")
                    .text(function (d) {
                        if (d.data)
                            return d.data.className + ": " + format(d.data.value);
                        else
                            return d.className + ": " + format(d.value);
                    });
            node.append("svg:circle")
                    .attr("r", function (d) {
                        return d.r;
                    })
                    .style("fill", function (d) {
                        if (d.data)
                            return fill(d.data.packageName);
                        else
                            return fill(d.packageName);
                    });
            node.on('click', function (d) {
                if (d.data)
                    clickGraph(facetfield, d.data.className, d.data.className);
                else
                    clickGraph(facetfield, d.className, d.className);
            });

            var vis2 = d3.select("#facetview_visualisation_" + facetkey + " > .modal-body2").select("svg")
                    .append("svg")
                    .attr("width", r * 1.5)
                    .attr("height", r)
                    .selectAll("g.node")
                    .data(bubblee(data)
                            .filter(function (d) {
                                return !d.children;
                            }))
                    .enter().append("svg:g")
                    .attr("class", "node")
                    .attr("transform", function (d) {
                        return "translate(" + d.x + "," + d.y + ")";
                    })
                    .append("svg:text")
                    .attr("text-anchor", "middle")
                    .attr("dy", ".3em")
                    .text(function (d) {
                        if (d.data && d.data.className)
                            return d.data.className.substr(0, 10) + ".. (" + d.data.value + ")";
                        else if (d.className)
                            return d.className.substr(0, 10) + ".. (" + d.value + ")";
                    })
                    .on("mousedown", function (d) {
                        if (d.data)
                            clickGraph(facetfield, d.data.className, d.data.className);
                        else
                            clickGraph(facetfield, d.className, d.className);
                    });
        };

        // normal click on a graphical facet
        var clickGraph = function (facetKey, facetValueDisplay, facetValue) {
            var newobj = '<a class="facetview_filterselected facetview_clear ' +
                    'btn btn-info" rel="' + facetKey +
                    '" alt="remove" title="remove"' +
                    ' href="' + facetValue + '">' +
                    facetValueDisplay + ' <i class="icon-remove"></i></a>';
            $('#facetview_selectedfilters').append(newobj);
            $('.facetview_filterselected').unbind('click', clearfilter);
            $('.facetview_filterselected').bind('click', clearfilter);
            options.paging.from = 0;
            dosearch();
            //$('#facetview_visualisation'+"_"+facetkey).remove();
        };


        // ===============================================
        // disambiguation
        // ===============================================

        var disambiguateNERD = function () {
            var queryText = $('#facetview_freetext').val();
            doexpandNERD(queryText);
        };

        // call the NERD service and propose senses to the user for his query
        var doexpandNERD = function (queryText) {
            //var queryString = '{ "text" : "' + encodeURIComponent(queryText) +'", "shortText" : true }';
            var queryString = '{ "text" : "' + queryText + '", "shortText" : true, "language": {"lang": "en"} }';

            var urlNERD = "http://" + options.host_nerd;
            if ((!options.port_nerd) || (options.port_nerd.length == 0))
                urlNERD += options.port_nerd + "processERDSearchQuery";
            else
                urlNERD += ":" + options.port_nerd + "/processERDSearchQuery";
            $.ajax({
                type: "POST",
                url: urlNERD,
//				contentType: 'application/json',
//				contentType: 'charset=UTF-8',
//				dataType: 'jsonp',
                dataType: "text",
//				data: { text : encodeURIComponent(queryText) },
                data: queryString,
//				data: JSON.stringify( { text : encodeURIComponent(queryText) } ),
                success: showexpandNERD
            });
        };

        var showexpandNERD = function (sdata) {
            if (!sdata) {
                return;
            }

            var jsonObject = parseDisambNERD(sdata);

            $('#disambiguation_panel').empty();

            /*for (var surf in jsonObject['paraphrases']) {
             piece += '<p>' + jsonObject['paraphrases'][surf] + '</p>';
             }*/

            piece = getPieceShowexpandNERD(jsonObject);
            $('#disambiguation_panel').append(piece);
            $('#close-disambiguate-panel').bind('click', function () {
                $('#disambiguation_panel').hide();
            })

            // we need to bind the checkbox...
            for (var sens in jsonObject['entities']) {
                $('input#selectEntity' + sens).bind('change', clickfilterchoice);
            }

            $('#disambiguation_panel').show();
        };
/*
        // execute a query expansion
        var doexpand = function (queryText) {
            var header = authenticateIdilia(queryText);

            // query parameters
            var queryString = "text=" + encodeURIComponent(queryText);

            // there are three possible disambiguation recipe for queries: paidListings, search, productSearch
            //queryString += "&paraphrasingRecipe=productSearch";
            queryString += "&paraphrasingRecipe=search";
            queryString += "&maxCount=10";
            queryString += "&minWeight=0.0";
            queryString += "&textMime=" + encodeURIComponent("text/query; charset=utf8");
            queryString += "&timeout=200";
            queryString += "&wsdMime=" + encodeURIComponent("application/x-semdoc+xml");

            if (options.service == 'proxy') {
                // ajax service access via a proxy
                for (var param in header) {
                    var obj = header[param];
                    for (var key in obj) {
                        queryString += '&' + key + '=' + encodeURIComponent(header[param][key]);
                    }
                }

                var proxy = options.proxy_host + "/proxy-disambiguate.jsp?";
                $.ajax({
                    type: "get",
                    url: proxy,
                    contentType: 'application/json',
                    dataType: 'jsonp',
                    data: queryString,
                    success: showexpandpre
                });
            }
            else {
                // ajax service access is local
                $.ajax({
                    type: "get",
                    url: "http://api.idilia.com/1/text/paraphrase.mpjson?",
                    contentType: 'application/json',
//				   	dataType: 'json',
                    beforeSend: function (xhr) {
                        for (var param in header) {
                            var obj = header[param];
                            for (var key in obj) {
                                xhr.setRequestHeader(key, header[param][key]);
                            }
                        }
                    },
                    data: queryString,
                    success: showexpand
                });
            }
        }
*/

        // ===============================================
        // building results
        // ===============================================
        // decrement result set
        var decrement = function (event) {
            event.preventDefault();
            if ($(this).html() != '..') {
                options.paging.from = options.paging.from - options.paging.size;
                options.paging.from < 0 ? options.paging.from = 0 : "";
                dosearch();
            }
        };

        // increment result set
        var increment = function (event) {
            event.preventDefault();
            if ($(this).html() != '..') {
                options.paging.from = parseInt($(this).attr('href'));
                dosearch();
            }
        };

        // write the metadata to the page
        var putmetadata = function (data) {
            $('#results_summary').empty();

            $('#results_summary').append("<p style='color:grey;'>"
                    + addCommas("" + data.found) + " results - in " + Math.floor(data.took)
                    + " ms (server time)</p>");

            if (typeof (options.paging.from) != 'number') {
                options.paging.from = parseInt(options.paging.from);
            }
            if (typeof (options.paging.size) != 'number') {
                options.paging.size = parseInt(options.paging.size);
            }

            var metaTmpl = ' \
              <div class="pagination"> \
                <ul> \
                  <li class="prev"><a id="facetview_decrement" href="{{from}}">&laquo; back</a></li> \
                  <li class="active"><a>{{from}} &ndash; {{to}} of {{total}}</a></li> \
                  <li class="next"><a id="facetview_increment" href="{{to}}">next &raquo;</a></li> \
                </ul> \
              </div> \
              ';

            if (options['mode_query'] == 'nl') {
                metaTmpl += ' <div class="span4">&nbsp;</div> \
			   ';
            }

            $('#facetview_metadata').html("Not found...");
            if (data.found) {
                var from = options.paging.from + 1;
                var size = options.paging.size;
                !size ? size = 10 : "";
                var to = options.paging.from + size;
                data.found < to ? to = data.found : "";
                var meta = metaTmpl.replace(/{{from}}/g, from);
                meta = meta.replace(/{{to}}/g, to);
                meta = meta.replace(/{{total}}/g, addCommas("" + data.found));
                $('#facetview_metadata').html("").append(meta);
                $('#facetview_decrement').bind('click', decrement);
                from < size ? $('#facetview_decrement').html('..') : "";
                $('#facetview_increment').bind('click', increment);
                data.found <= to ? $('#facetview_increment').html('..') : "";
            }
        };

        // put the results on the page
        showresults = function (sdata) {
            // get the data and parse from elasticsearch or other 
            var data = null;
            if (options.search_index == "elasticsearch") {
                // default is elasticsearch
                data = parseresultsElasticSearch(sdata);
            }
            else {
                // nothing to do :(
                return;
            }
            options.data = data;

            // put result metadata on the page
            putmetadata(data);
            // put the filtered results on the page
            $('#facetview_results').html("");
            //var infofiltervals = new Array();
            $.each(data.records, function (index, value) {
                // write them out to the results div
                //$('#facetview_results').append( buildrecord(index) );
                buildrecord(index, $('#facetview_results'));
                $('#facetview_results tr:last-child').linkify();
            });
            MathJax.Hub.Queue(["Typeset", MathJax.Hub]);
            // change filter options
            putvalsinfilters(data);

            // for the first time we visualise the filters as defined in the facet options
            for (var each in options.facets) {
                if (options.facets[each]['view'] == 'graphic') {
                    //if ($('.facetview_filterselected[rel=' + options.facets[each]['field'] + "]" ).length == 0 )
                    if ($('#facetview_visualisation_' + options.facets[each]['display'] + '_chart').length == 0)
                        $('.facetview_visualise[href=' + options.facets[each]['display'] + ']').trigger('click');
                }
                else if ((!$('.facetview_filtershow[rel=' + options.facets[each]['display'] +
                        ']').hasClass('facetview_open'))
                        && (options.facets[each]['view'] == 'textual')) {
                    $('.facetview_filtershow[rel=' + options.facets[each]['display'] + ']').trigger('click');
                }
            }

            //we load now in background the additional record information requiring a user interaction for
            // visualisation
            $('#titleNaked', obj).each(function () {
                if (options.collection == "npl") {
                    // annotations for the title
                    var index = $(this).attr('pos');
                    var titleID = $(this).attr('rel');
                    var localQuery = {"query": {"filtered": {"query": {"term": {"_id": titleID}}}}};

                    $.ajax({
                        type: "get",
                        url: options.search_url_annotations,
                        contentType: 'application/json',
                        dataType: 'jsonp',
                        data: {source: JSON.stringify(localQuery)},
                        success: function (data) {
                            displayAnnotations(data, index, titleID, 'title');
                        }
                    });
                }
            });

            $('#innen_abstract', obj).each(function () {
                // load biblio and abstract info. 
                // pos attribute gives the result index, rel attribute gives the document ID 
                var index = $(this).attr('pos');
                var docID = $(this).attr('rel');
                var localQuery;

                if (options.collection == "npl") {

                    // abstract and further informations
                    localQuery = {"fields": ["$teiCorpus.$teiHeader.$profileDesc.xml:id",
                            "$teiCorpus.$teiHeader.$profileDesc.$abstract.$lang_en",
                            "$teiCorpus.$teiHeader.$profileDesc.$abstract.$lang_fr",
                            "$teiCorpus.$teiHeader.$profileDesc.$abstract.$lang_de",
                            "$teiCorpus.$teiHeader.$sourceDesc.$biblStruct.$monogr.$title.$title-first",
                            "$teiCorpus.$teiHeader.$sourceDesc.$biblStruct.$idno.$type_doi",
                            "$teiCorpus.$teiHeader.$sourceDesc.$biblStruct.$author.$persName.$fullName",
                            '$teiCorpus.$teiHeader.$profileDesc.$textClass.$classCode.$scheme_halTypology',
                            "$teiCorpus.$teiHeader.$profileDesc.$textClass.$keywords.$type_author.$term",
                            '$teiCorpus.$teiHeader.$profileDesc.$textClass.$keywords.$type_author.xml:id'],
                        "query": {"filtered": {"query": {"term": {"_id": docID}}}}};

                    $.ajax({
                        type: "get",
                        url: options.search_url,
                        contentType: 'application/json',
                        dataType: 'jsonp',
                        data: {source: JSON.stringify(localQuery)},
                        success: function (data) {
                            displayAbstract(data, index);
                        }
                    });

                }
                else if (options.collection == "patent") {
                    localQuery = {"fields": ["_id",
                            "$teiCorpus.$TEI.$text.$front.$div.$p.$lang_en",
                            "$teiCorpus.$TEI.$text.$front.$div.$p.$lang_de",
                            "$teiCorpus.$TEI.$text.$front.$div.$p.$lang_fr"],
                        "query": {"filtered": {"query": {"term": {"_id": docID}}}}};

                    /*$.post(options.search_url, 
                     {source : JSON.stringify(localQuery) }, 
                     function(data) { displayAbstract(data, index); }, 
                     "jsonp");*/
                    $.ajax({
                        type: "get",
                        url: options.search_url,
                        contentType: 'application/json',
                        dataType: 'jsonp',
                        data: {source: JSON.stringify(localQuery)},
                        success: function (data) {
                            displayAbstract(data, index);
                        }
                    });
                }
            });
        };
        
        // execute a search
        var dosearch = function () {
            // update the options with the latest q value
            options.q = $('#facetview_freetext').val();
            // make the search query
            if (options.search_index == "elasticsearch") {
                $.ajax({
                    type: "get",
                    url: options.search_url,
                    data: {source: elasticsearchquery()},
                    // processData: false,
                    dataType: "jsonp",
                    success: showresults
                });
            }
        };

        // adjust how many results are shown
        var howmany = function (event) {
            event.preventDefault();
            var newhowmany = prompt('Currently displaying ' + options.paging.size +
                    ' results per page. How many would you like instead?');
            if (newhowmany) {
                options.paging.size = parseInt(newhowmany);
                options.paging.from = 0;
                $('#facetview_howmany').html('results per page (' + options.paging.size + ')');
                dosearch();
            }
        };

        // what to do when ready to go
        var whenready = function () {
            // append the facetview object to this object
            var thefacetview;
            if (options['mode_query'] == 'simple') {
                thefacetview = thefacetview_simple;
            }
            else if (options['mode_query'] == 'epoque') {
                thefacetview = thefacetview_epoque;
            }
            else if (options['mode_query'] == 'nl') {
                thefacetview = thefacetview_nl;
            }
            else {
                thefacetview = thefacetview_complex;
            }

            thefacetview = thefacetview.replace(/{{HOW_MANY}}/gi, options.paging.size);
            $(obj).append(thefacetview);

            if (options['mode_query'] == 'complex') {
                // setup default search option triggers
                $('#facetview_partial_match1').bind('click', fixmatch);
                $('#facetview_exact_match1').bind('click', fixmatch);
                $('#facetview_fuzzy_match1').bind('click', fixmatch);
                $('#facetview_match_any1').bind('click', fixmatch);
                $('#facetview_match_all1').bind('click', fixmatch);
                $('#facetview_howmany1').bind('click', howmany);

                $('#field_all_text1').bind('click', set_field);
                $('#field_title1').bind('click', set_field);
                $('#field_abstract1').bind('click', set_field);
                $('#field_claims1').bind('click', set_field);
                $('#field_description1').bind('click', set_field);
                $('#field_fulltext1').bind('click', set_field);
                $('#field_class_ipc1').bind('click', set_field);
                $('#field_class_ecla1').bind('click', set_field);
                $('#field_country1').bind('click', set_field);
                $('#field_affiliation1').bind('click', set_field);
                $('#field_author1').bind('click', set_field);
                $('#field_inventor1').bind('click', set_field);
                $('#field_applicant1').bind('click', set_field);

                $('#lang_all1').bind('click', set_field);
                $('#lang_en1').bind('click', set_field);
                $('#lang_de1').bind('click', set_field);
                $('#lang_fr1').bind('click', set_field);

                $('#must1').bind('click', set_field);
                $('#should1').bind('click', set_field);
                $('#must_not1').bind('click', set_field);

                $('#new_field').bind('click', add_field);
                options['complex_fields'] = 1;
            }
            else if (options['mode_query'] == 'nl') {
                $('#lang_all').bind('click', set_field);
                $('#lang_en').bind('click', set_field);
                $('#lang_de').bind('click', set_field);
                $('#lang_fr').bind('click', set_field);

                $('#facetview_partial_match').bind('click', fixmatch);
                $('#facetview_exact_match').bind('click', fixmatch);
                $('#facetview_fuzzy_match').bind('click', fixmatch);
                $('#facetview_match_any').bind('click', fixmatch);
                $('#facetview_match_all').bind('click', fixmatch);
                $('#facetview_howmany').bind('click', howmany);
            }
            else {
                // setup search option triggers
                $('#facetview_partial_match').bind('click', fixmatch);
                $('#facetview_exact_match').bind('click', fixmatch);
                $('#facetview_fuzzy_match').bind('click', fixmatch);
                $('#facetview_match_any').bind('click', fixmatch);
                $('#facetview_match_all').bind('click', fixmatch);
                $('#facetview_howmany').bind('click', howmany);
            }

            // resize the searchbar
            if (options['mode_query'] == 'complex') {
                thewidth = $('#facetview_searchbar1').parent().width();
                $('#facetview_searchbar1').css('width', (thewidth / 2) - 30 + 'px');
                $('#facetview_freetext1').css('width', (thewidth / 2) - 30 + 'px');
            }
            if (options['mode_query'] == 'nl') {
                thewidth = $('#facetview_searchbar').parent().width();
                $('#facetview_searchbar').css('width', (thewidth / 2) + 70 + 'px');
                $('#facetview_freetext').css('width', (thewidth / 1.5) - 20 + 'px');

                var theheight = $('#facetview_searchbar').parent().height();
                $('#facetview_searchbar').css('height', (theheight) - 20 + 'px');
                $('#facetview_freetext').css('height', (theheight) - 20 + 'px');
            }
            else {
                var thewidth = $('#facetview_searchbar').parent().width();
                $('#facetview_searchbar').css('width', thewidth / 2 + 70 + 'px'); // -50
                $('#facetview_freetext').css('width', thewidth / 2 + 32 + 'px'); // -88

                $('#disambiguate').bind('click', disambiguateNERD);
                $('#disambiguation_panel').hide();

                //$('#harvest').hide();
            }
            // check paging info is available
            !options.paging.size ? options.paging.size = 10 : "";
            !options.paging.from ? options.paging.from = 0 : "";

            // set any default search values into the search bar
            $('#facetview_freetext').val() == "" && options.q != "" ? $('#facetview_freetext').val(options.q) : ""

            // append the filters to the facetview object
            buildfilters();

            if (options['mode_query'] == 'complex') {
                $('#facetview_freetext1', obj).bindWithDelay('keyup', dosearch, options.freetext_submit_delay);
            }
            else {
                $('#facetview_freetext', obj).bindWithDelay('keyup', dosearch, options.freetext_submit_delay);
                $('#facetview_freetext', obj).bind('keyup', activateDisambButton);
                //$('#facetview_freetext', obj).bind('keyup', activateHarvestButton);
            }

            // trigger the search once on load, to get all results
            dosearch();
        };

        // ===============================================
        // now create the plugin on the page
        return this.each(function () {
            // get this object
            obj = $(this);

            // check for remote config options, then do first search
            if (options.config_file) {
                $.ajax({
                    type: "get",
                    url: options.config_file,
                    dataType: "jsonp",
                    success: function (data) {
                        options = $.extend(options, data);
                        whenready();
                    },
                    error: function () {
                        $.ajax({
                            type: "get",
                            url: options.config_file,
                            success: function (data) {
                                options = $.extend(options, $.parseJSON(data));
                                whenready();
                            },
                            error: function () {
                                whenready();
                            }
                        });
                    }
                });
            }
            else {
                whenready();
            }
        }); // end of the function  

    };

    // facetview options are declared as a function so that they can be retrieved
    // externally (which allows for saving them remotely etc.)
    $.fn.facetview.options = {};

})(jQuery);


