var defaults = {
    host_nerd: "localhost",
    port_nerd: "8080",
    service: "local", // access to service can be local or proxy, depending on security requirements
    proxy_host: "http://localhost:8080/proxy/",    
    search_url: 'http://localhost:9200/anhalytics_teis/_search?', // the URL against which to submit searches
    search_url_annotations: 'http://localhost:9200/anhalytics_annotations/_search?',
    search_index: 'elasticsearch',    
    query_parameter: "q", // the query parameter if required for setting to the search URL
    collection: 'npl',
    subcollection: 'hal',
    snippet_style: 'andlauer',   
    freetext_submit_delay: "200", // delay for auto-update of search results in ms
    display_images: true, // whether or not to display images found in links in search results    
    config_file: false, // a remote config file URL
    addremovefacets: false, // false if no facets can be added at front en
    visualise_filters: true, // whether or not to allow filter vis via d3
    description: "", // a description of the current search to embed in the display
    default_url_params: {}, // any params that the search URL needs by default
    q: "", // default query value
    predefined_filters: {}, // predefined filters to apply to all searches
    paging: {
        from: 0, // where to start the results from
        size: 10                   // how many results to get
    },
    mode_query: "simple", // query input, possible values: simple, complex, nl, semantic, analytics
    complex_fields: 0, // number of fields introduced in the complex query form
}


