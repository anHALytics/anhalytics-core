// read the result object and return useful vals based on ES results
// returns an object that contains things like ["data"] and ["facets"]
var parseresultsElasticSearch = function (dataobj) {
    var resultobj = new Object();
    resultobj["records"] = new Array();
    resultobj["highlights"] = new Array();
    resultobj["scores"] = new Array();
    resultobj["ids"] = new Array();
    resultobj["start"] = "";
    resultobj["found"] = "";
    resultobj["took"] = "";
    resultobj["facets"] = new Object();
    resultobj["facets2"] = new Object();
    resultobj["facets3"] = new Object();
    resultobj["aggregations"] = new Object();
    for (var item in dataobj.hits.hits) {
        resultobj["records"].push(dataobj.hits.hits[item].fields);
        resultobj["highlights"].push(dataobj.hits.hits[item].highlight);
        resultobj["scores"].push(dataobj.hits.hits[item]._score);
        resultobj["ids"].push(dataobj.hits.hits[item]._id);
        resultobj["start"] = "";
        resultobj["found"] = dataobj.hits.total;
        resultobj["took"] = dataobj.took;
    }
    for (var item in dataobj.facets) {
        var facetsobj = new Object();
        for (var thing in dataobj.facets[item]["terms"]) {
            facetsobj[ dataobj.facets[item]["terms"][thing]["term"] ] =
                    dataobj.facets[item]["terms"][thing]["count"];
        }
        for (var thing in dataobj.facets[item]["entries"]) {
            facetsobj[ dataobj.facets[item]["entries"][thing]["time"] ] =
                    dataobj.facets[item]["entries"][thing]["count"];
        }
        resultobj["facets"][item] = facetsobj;
    }
    for (var item in dataobj.facets) {
        resultobj["facets2"][item] = dataobj.facets[item]["terms"];
    }
    for (var item in dataobj.facets) {
        resultobj["facets3"][item] = dataobj.facets[item]["entries"];
    }
    for (var item in dataobj.aggregations) {
        resultobj["aggregations"][item] = dataobj.aggregations[item]["buckets"];
    }
    return resultobj;
};
