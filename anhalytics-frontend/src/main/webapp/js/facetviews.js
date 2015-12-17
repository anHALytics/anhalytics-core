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