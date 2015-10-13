// the facet view object to be appended to the page
var thefacetview_simple = ' \
           <div id="facetview"> \
             <div class="row-fluid"> \
               <div class="span3"> \
                 <div id="facetview_filters"></div> \
               </div> \
               <div class="span9" id="facetview_rightcol" style="position:relative; left:0px; margin-left:5px; margin-right:0px; "> \
                   <div id="facetview_searchbar" style="display:inline; float:left;" class="input-prepend"> \
                   <span class="add-on"><i class="icon-search"></i></span> \
                   <input class="span4" id="facetview_freetext" name="q" value="" placeholder="search term" autofocus /> \
                   </div> \
                   <div style="display:inline; float:left;margin-left:-2px;" class="btn-group"> \
                    <a style="-moz-border-radius:0px 3px 3px 0px; \
                    -webkit-border-radius:0px 3px 3px 0px; border-radius:0px 3px 3px 0px;" \
                    class="btn dropdown-toggle" data-toggle="dropdown" href="#"> \
                    <i class="icon-cog"></i> <span class="caret"></span></a> \
                    <ul style="margin-left:-110px;" class="dropdown-menu"> \
                    <li><a id="facetview_partial_match" href="">partial match</a></li> \
                    <li><a id="facetview_exact_match" href="">exact match</a></li> \
                    <li><a id="facetview_fuzzy_match" href="">fuzzy match</a></li> \
                    <li><a id="facetview_match_all" href="">match all</a></li> \
                    <li><a id="facetview_match_any" href="">match any</a></li> \
                    <li><a href="#">clear all</a></li> \
                    <li class="divider"></li> \
                    <li><a target="_blank" \
                    href="http://lucene.apache.org/java/2_9_1/queryparsersyntax.html"> \
                    query syntax doc.</a></li> \
                    <li class="divider"></li> \
                    <li><a id="facetview_howmany" href="#">results per page ({{HOW_MANY}})</a></li> \
                    </ul> \
                   </div> \
				   <div class-"span2" id="disambiguate_button"> \
				   <button type="button" id="disambiguate" class="btn" disabled="true" data-toggle="button">Disamb./Expand</button> \
				   </div> \
                   <div style="clear:both;" id="facetview_selectedfilters"></div> \
				   <div class="span5" id="results_summary"></div> \
				   <div class="span9" id="disambiguation_panel" style="margin-left:5px;"></div> \
                 <table class="table table-striped" id="facetview_results"></table> \
                 <div id="facetview_metadata"></div> \
               </div> \
             </div> \
           </div> \
           ';

// the facet view object to be appended to the page
var thefacetview_nl = ' \
           <div id="facetview"> \
             <div class="row-fluid"> \
               <div class="span3"> \
                 <div id="facetview_filters"></div> \
               </div> \
               <div class="span9" id="facetview_rightcol" style="position:relative; left:0px;"> \
                   <div id="facetview_searchbar" style="display:inline; float:left;" class="input-prepend"> \
                   <span class="add-on"><i class="icon-search"></i></span> \
				<div style="display:inline-block;margin-left:-5px;" class="btn-group"> \
				    <a style="-moz-border-radius:0px 3px 3px 0px; \
			       -webkit-border-radius:0px 3px 3px 0px; border-radius:0px 3px 3px 0px;" \
				   class="btn dropdown-toggle" data-toggle="dropdown" href="#"> \
			       <b><span id="label_facetview_searchbar">lang</span></b> <span class="caret"></span></a> \
					<ul style="margin-left:-10px;" class="dropdown-menu"> \
				   <li><a id="lang_all" label="" rank="" href="">all</a></li> \
			       <li><a id="lang_en" label="" rank="" href="">en</a></li> \
			       <li><a id="lang_de" label="" rank="" href="">de</a></li> \
				   <li><a id="lang_fr" label="" rank="" href="">fr</a></li> \
				   </div> \
                   <textarea class="span4" id="facetview_freetext" name="q" value="" placeholder="search text" autofocus /> \
                   </div> \
                   <div style="display:inline; float:left;margin-left:-2px;bottom:-50px;" class="btn-group"> \
                   <div style="clear:both;" id="facetview_selectedfilters"></div> \
				   <div class="span5" id="results_summary"></div> \
                 <table class="table table-striped" id="facetview_results"></table> \
                 <div id="facetview_metadata"></div> \
               </div> \
             </div> \
           </div> \
           ';

var field_complex;
if (defaults['collection'] == 'npl') {
    field_complex = ' \
			<div style="display:inline-block; margin-left:-2px;" class="btn-group"> \
			    <a style="-moz-border-radius:0px 3px 3px 0px; \
		       -webkit-border-radius:0px 3px 3px 0px; border-radius:0px 3px 3px 0px;" \
		       class="btn dropdown-toggle" data-toggle="dropdown" href="#"> \
		       <b><span id="label1_facetview_searchbar{{NUMBER}}">select field</span></b> <span class="caret"></span></a> \
				<ul style="margin-left:-10px;" class="dropdown-menu"> \
		       <li><a id="field_all_text{{NUMBER}}" rank="{{NUMBER}}" label="1" href="">all text</a></li> \
			   <li><a id="field_title{{NUMBER}}" rank="{{NUMBER}}" label="1" href="">all titles</a></li> \
			   <li><a id="field_abstract{{NUMBER}}" rank="{{NUMBER}}" label="1" href="">all abstracts</a></li> \
			   <li><a id="field_fulltext{{NUMBER}}" rank="{{NUMBER}}" label="1" href="">full text</a></li> \
			   <li><a id="field_author{{NUMBER}}" rank="{{NUMBER}}" label="1" href="">author</a></li> \
			   <li><a id="field_affiliation{{NUMBER}}" rank="{{NUMBER}}" label="1" href="">affiliation</a></li> \
			   <li><a id="field_country{{NUMBER}}" rank="{{NUMBER}}" label="1" href="">authors\' country</a></li> \
			   </div>'
}
else {
    field_complex = ' \
			<div style="display:inline-block; margin-left:-2px;" class="btn-group"> \
			    <a style="-moz-border-radius:0px 3px 3px 0px; \
		       -webkit-border-radius:0px 3px 3px 0px; border-radius:0px 3px 3px 0px;" \
		       class="btn dropdown-toggle" data-toggle="dropdown" href="#"> \
		       <b><span id="label1_facetview_searchbar{{NUMBER}}">select field</span></b> <span class="caret"></span></a> \
				<ul style="margin-left:-10px;" class="dropdown-menu"> \
		       <li><a id="field_all_text{{NUMBER}}" rank="{{NUMBER}}" label="1" href="">all text</a></li> \
			   <li><a id="field_title{{NUMBER}}" rank="{{NUMBER}}" label="1" href="">all titles</a></li> \
			   <li><a id="field_abstract{{NUMBER}}" rank="{{NUMBER}}" label="1" href="">all abstracts</a></li> \
			   <li><a id="field_claims{{NUMBER}}" rank="{{NUMBER}}" label="1" href="">claims</a></li> \
			   <li><a id="field_description{{NUMBER}}" rank="{{NUMBER}}" label="1" href="">description</a></li> \
		       <li><a id="field_class_ipc{{NUMBER}}" rank="{{NUMBER}}" label="1" href="">IPC class</a></li> \
			   <li><a id="field_class_ecla{{NUMBER}}" rank="{{NUMBER}}" label="1" href="">ECLA class</a></li> \
			   <li><a id="field_country{{NUMBER}}" rank="{{NUMBER}}" label="1" href="">ap. country</a></li> \
			   <li><a id="field_inventor{{NUMBER}}" rank="{{NUMBER}}" label="1" href="">inventor</a></li> \
			   <li><a id="field_applicant{{NUMBER}}" rank="{{NUMBER}}" label="1" href="">applicant</a></li> \
			   </div>'
}
field_complex += '<div style="display:inline-block;margin-left:-5px;" class="btn-group"> \
			    <a style="-moz-border-radius:0px 3px 3px 0px; \
		       -webkit-border-radius:0px 3px 3px 0px; border-radius:0px 3px 3px 0px;" \
			   class="btn dropdown-toggle" data-toggle="dropdown" href="#"> \
		       <b><span id="label3_facetview_searchbar{{NUMBER}}">lang</span></b> <span class="caret"></span></a> \
				<ul style="margin-left:-10px;" class="dropdown-menu"> \
			   <li><a id="lang_all{{NUMBER}}" rank="{{NUMBER}}" label="3" href="">all</a></li> \
		       <li><a id="lang_en{{NUMBER}}" rank="{{NUMBER}}" label="3" href="">en</a></li> \
		       <li><a id="lang_de{{NUMBER}}" rank="{{NUMBER}}" label="3" href="">de</a></li> \
			   <li><a id="lang_fr{{NUMBER}}" rank="{{NUMBER}}" label="3" href="">fr</a></li> \
			   </div> \
			<div style="display:inline-block;;margin-left:-5px;" class="btn-group"> \
			    <a style="-moz-border-radius:0px 3px 3px 0px; \
		       -webkit-border-radius:0px 3px 3px 0px; border-radius:0px 3px 3px 0px;" \
		       class="btn dropdown-toggle" data-toggle="dropdown" href="#"> \
		       <b><span id="label2_facetview_searchbar{{NUMBER}}">should</span></b> <span class="caret"></span></a> \
				<ul style="margin-left:-10px;" class="dropdown-menu"> \
		       <li><a id="must{{NUMBER}}" rank="{{NUMBER}}" label="2" href="">must</a></li> \
		       <li><a id="should{{NUMBER}}" rank="{{NUMBER}}" label="2" href="">should</a></li> \
			   <li><a id="must_not{{NUMBER}}" rank="{{NUMBER}}" label="2" href="">must_not</a></li> \
			   </div> \
		      <div id="facetview_searchbar{{NUMBER}}" style="margin-left:-5px; position:relative; top:-7px; display:inline-block;"> \
		       <input class="span4" id="facetview_freetext{{NUMBER}}" name="q" value="" placeholder="search term" /> \
		       </div> \
		       <div style="display:inline-block; margin-left:-2px;" class="btn-group"> \
		        <a style="-moz-border-radius:0px 3px 3px 0px; \
		        -webkit-border-radius:0px 3px 3px 0px; border-radius:0px 3px 3px 0px;" \
		        class="btn dropdown-toggle" data-toggle="dropdown" href="#"> \
		        <i class="icon-cog"></i> <span class="caret"></span></a> \
		        <ul style="margin-left:-110px;" class="dropdown-menu"> \
		        <li><a id="facetview_partial_match{{NUMBER}}" href="">partial match</a></li> \
		        <li><a id="facetview_exact_match{{NUMBER}}" href="">exact match</a></li> \
		        <li><a id="facetview_fuzzy_match{{NUMBER}}" href="">fuzzy match</a></li> \
		        <li><a id="facetview_match_all{{NUMBER}}" href="">match all</a></li> \
		        <li><a id="facetview_match_any{{NUMBER}}" href="">match any</a></li> \
		        <li><a href="#">clear all</a></li> \
		        <li class="divider"></li> \
		        <li><a target="_blank" \
		        href="http://lucene.apache.org/java/2_9_1/queryparsersyntax.html"> \
		        query syntax doc.</a></li> \
		        <li class="divider"></li> \
		        <li><a id="facetview_howmany{{NUMBER}}" href="#">results per page ({{HOW_MANY}})</a></li> \
		        </ul> \
		       </div> \
			   <br/> \
			';

var thefacetview_complex = ' \
           <div id="facetview"> \
             <div class="row-fluid"> \
               <div class="span3"> \
                 <div id="facetview_filters"></div> \
               </div> \
               <div class="span9" id="facetview_rightcol" style="position:relative; left:0px;"> \
				 <div id="search_form">	\
					<div style="margin-left:-2px;margin-bottom:10px;" id="facetview_fieldbuttons" class="btn-group"> \
       	        	<a style="text-align:left; min-width:20%;" class="btn" \
	                 		id="new_field" href=""> \
	                 		<i class="icon-plus"></i> add new search field </a> \
					</div> ' +
        field_complex.replace(/{{NUMBER}}/gi, "1") +
        '</div> \
			      <div style="clear:both;" id="facetview_selectedfilters"></div> \
				  <div class="span5" id="results_summary"></div> \
	              <table class="table table-striped" id="facetview_results"></table> \
	              <div id="facetview_metadata"></div> \
				</div> \
             </div> \
           </div> \
           ';

var thefacetview_epoque = ' \
           <div id="facetview"> \
			<span style="position:relative; top:-10px;"><a href="#?mode_query=simple">Simple</a> - <a href="#?mode_query=complex">Complex</a> - <a href="#?mode_query=nl">NL</a> - Epoque</span> \
             <div class="row-fluid"> \
               <div class="span3"> \
                 <div id="facetview_filters"></div> \
               </div> \
               <div class="span9" id="facetview_rightcol" style="position:relative; left:-20px;"> \
                   <div id="facetview_searchbar" style="display:inline; float:left;" class="input-prepend"> \
                   <span class="add-on"><i class="icon-search"></i></span> \
                   <input class="span4" id="facetview_freetext" name="q" value="" placeholder="search term" autofocus /> \
                   </div> \
                   <div style="display:inline; float:left;margin-left:-2px;" class="btn-group"> \
                    <a style="-moz-border-radius:0px 3px 3px 0px; \
                    -webkit-border-radius:0px 3px 3px 0px; border-radius:0px 3px 3px 0px;" \
                    class="btn dropdown-toggle" data-toggle="dropdown" href="#"> \
                    <i class="icon-cog"></i> <span class="caret"></span></a> \
                    <ul style="margin-left:-110px;" class="dropdown-menu"> \
                    <li><a id="facetview_partial_match1" href="">partial match</a></li> \
                    <li><a id="facetview_exact_match1" href="">exact match</a></li> \
                    <li><a id="facetview_fuzzy_match1" href="">fuzzy match</a></li> \
                    <li><a id="facetview_match_all1" href="">match all</a></li> \
                    <li><a id="facetview_match_any1" href="">match any</a></li> \
                    <li><a href="#">clear all</a></li> \
                    <li class="divider"></li> \
                    <li><a target="_blank" \
                    href="http://lucene.apache.org/java/2_9_1/queryparsersyntax.html"> \
                    query syntac doc.</a></li> \
                    <li class="divider"></li> \
                    <li><a id="facetview_howmany" href="#">results per page ({{HOW_MANY}})</a></li> \
                    </ul> \
                   </div> \
                   <div style="clear:both;" id="facetview_selectedfilters"></div> \
                 <table class="table table-striped" id="facetview_results"></table> \
                 <div id="facetview_metadata"></div> \
               </div> \
             </div> \
           </div> \
           ';