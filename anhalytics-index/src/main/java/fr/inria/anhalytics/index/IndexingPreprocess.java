package fr.inria.anhalytics.index;

import fr.inria.anhalytics.commons.managers.MongoFileManager;
import fr.inria.anhalytics.commons.utilities.Utilities;

/*import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;
import org.codehaus.jackson.map.ObjectMapper;*/

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Arrays;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Additional Java pre-processing of the JSON string.
 *
 * First, the TEI structure in JSON is expanded to put some attributes like @lang and @type 
 * for certain elements in the json path in order to make possible ElasticSearch queries 
 * with these attributes. All the structures of the TEI will be searchable with TEI paths 
 * expressed in JSON. Those TEI/JSON paths can be used to query the ElasticSearch indexes 
 * so that there is no need to learn and define an additional index structure
 * for full text. 
 * 
 * Second, some part of the annotations we want to use for searching documents by combining
 * of the annotations and the TEI full text structures are injected in the standoff node.  
 * 
 */
public class IndexingPreprocess {

    private static final Logger logger = LoggerFactory.getLogger(IndexingPreprocess.class);

    // this is the list of elements for which the text nodes should be expanded with an additional json
    // node capturing the nesting xml:lang attribute name/value pair
    static final public List<String> expandable
            = Arrays.asList("$title", "$p", "$item", "$figDesc", "$head", "$meeting", "$div", "$abstract");

    // this is the list of elements to be locally enriched with annotations for the purpose of 
    // presentation of the annotated text
    static final public List<String> annotated
            = Arrays.asList("$title", "$abstract", "term");

    private MongoFileManager mm = null;

    public IndexingPreprocess(MongoFileManager mm) {
        this.mm = mm;
    }

    // maximum number of annotations to be indexed in combination with the full text TEI structure
    private static final int MAX_INDEXED_KEYTERM = 20;
    private static final int MAX_INDEXED_CONCEPT = 20;
    private static final int MAX_INDEXED_NERD = 50;
    private static int MAX_NERD_INDEXED_PARAGRAPHS = 10;
    private static int MAX_QUANTITIES_INDEXED_PARAGRAPHS = 200;
    private static final int MAX_INDEXED_QUANTITIES = 500;

    /**
     * Format jsonStr to fit with ES structure.
     */
    public String process(String jsonStr, String repositoryDocId, String anhalyticsId) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonRoot = mapper.readTree(jsonStr);
        // root node is the TEI node, we add as a child the "light" annotations in a 
        // standoff element
        ((ObjectNode) jsonRoot).put("repositoryDocId", repositoryDocId);
        JsonNode teiRoot = jsonRoot.findPath("$teiCorpus");
        JsonNode tei = jsonRoot.findPath("$TEI");
        //check if fulltext is there..
        if (tei.isNull()) {
            logger.info(repositoryDocId + ": <tei> element is null -> " + tei.toString());
            return null;
        }
        if ((teiRoot != null) && (!teiRoot.isMissingNode())) {
            JsonNode standoffNode = getStandoffNerd(mapper, anhalyticsId);
            standoffNode = getStandoffKeyTerm(mapper, anhalyticsId, standoffNode);
            standoffNode = getStandoffQuantities(mapper, anhalyticsId, standoffNode);
//            standoffNode = getStandoffPDFQuantities(mapper, anhalyticsId, standoffNode);
            if (standoffNode != null) {
                ((ArrayNode) teiRoot).add(standoffNode);
            }
        }

        // here recursive modification of the json document via Jackson
        jsonRoot = process(jsonRoot, mapper, null, false, false, false, anhalyticsId);

        if (!filterDocuments(jsonRoot)) {
            return null;
        }

        return jsonRoot.toString();
    }

    /**
     * Process subJson Node and iterate through sub-nodes.
     */
    private JsonNode process(JsonNode subJson,
            ObjectMapper mapper,
            String currentLang,
            boolean fromArray,
                boolean expandLang,
            boolean isDate,
            String anhalyticsId) throws Exception {
        if (subJson.isContainerNode()) {
            if (subJson.isObject()) {
                Iterator<String> fields = ((ObjectNode) subJson).fieldNames();
                JsonNode theSchemeNode = null;
                JsonNode theClassCodeNode = null;
                JsonNode theTypeNode = null;
                JsonNode theUnitNode = null;
                JsonNode thePersonNode = null;
                JsonNode theItemNode = null;
                JsonNode theKeywordsNode = null;
                JsonNode theDateNode = null;
                JsonNode theIdnoNode = null;
                JsonNode theBiblScopeNode = null;
                JsonNode theWhenNode = null;
                JsonNode theTermNode = null;
                JsonNode theNNode = null;
                JsonNode divNode = null;
                JsonNode theTitleNode = null;
                JsonNode theXmlIdNode = null;
                JsonNode theLevelNode = null;
                while (fields.hasNext()) {
                    String field = fields.next();
                    if (field.startsWith("$")) {
                        if (expandable.contains(field)) {
                            expandLang = true;
                        } else {
                            expandLang = false;
                        }
                    }
                    // we add the full name in order to index it directly without more time consuming
                    // script and concatenation at facet creation time
                    if (field.equals("$author")) {
                        JsonNode theChild = subJson.path("$author");
                        // this child is an array
                        String content = "";
                        if (theChild.isArray()) {
                            String fullname = "";
                            String authorAnhalyticsId = "";
                            Iterator<JsonNode> ite = theChild.elements();
                            String idnoType = "";
                            while (ite.hasNext()) {
                                JsonNode temp = ite.next();

                                if (temp.isObject()) {
                                    if (temp.fieldNames().next().equals("$persName")) {
                                        JsonNode persName = temp.path("$persName");
                                        // this child is an array
                                        fullname = addFullName(persName, mapper);
                                    } else if (temp.fieldNames().next().equals("type")) {
                                        idnoType = temp.path("type").textValue();

                                        if (idnoType.equals("anhalyticsID")) {
                                            authorAnhalyticsId = temp.path("$idno").elements().next().textValue();
                                        }
                                    }
                                }
                            }
                            if (!authorAnhalyticsId.isEmpty() && !fullname.isEmpty()) {
                                content = authorAnhalyticsId + "_" + fullname;
                            }
                        }
                        if (!content.isEmpty()) {
                            JsonNode newIdNode = mapper.createObjectNode();
                            JsonNode newIdNode1 = mapper.createObjectNode();
                            JsonNode newtextNode = mapper.createArrayNode();
                            JsonNode tcontentnode = new TextNode(content);
                            ((ArrayNode) newtextNode).add(tcontentnode);
                            ((ObjectNode) newIdNode).put("$type_authorAnhalyticsID", tcontentnode);
                            JsonNode arrayNode = mapper.createArrayNode();
                            ((ArrayNode) arrayNode).add(newIdNode);
                            ((ObjectNode) newIdNode1).put("$idno", arrayNode); // update value
                            ((ArrayNode) theChild).add(newIdNode1);
                        }
                    } else if (field.equals("$org")) {
                        JsonNode theChild = subJson.path("$org");
                        // this child is an array
                        String content = "";
                        if (theChild.isArray()) {
                            String orgName = "";
                            String orgAnhalyticsId = "";
                            Iterator<JsonNode> ite = theChild.elements();
                            String idnoType = "";
                            while (ite.hasNext()) {
                                JsonNode temp = ite.next();

                                if (temp.isObject()) {
                                    if (temp.fieldNames().next().equals("$orgName")) {
                                        JsonNode persName = temp.path("$orgName");
                                        // this child is an array
                                        if (!orgName.isEmpty()) {
                                            orgName += "_" + persName.elements().next().textValue();
                                        } else {
                                            orgName += persName.elements().next().textValue();
                                        }
                                    } else if (temp.fieldNames().next().equals("type")) {
                                        idnoType = temp.path("type").textValue();

                                        if (idnoType.equals("anhalyticsID")) {
                                            orgAnhalyticsId = temp.path("$idno").elements().next().textValue();
                                        }
                                    }
                                }
                            }
                            if (!orgAnhalyticsId.isEmpty() && !orgName.isEmpty()) {
                                content = orgAnhalyticsId + "_" + orgName;
                            }
                        }
                        if (!content.isEmpty()) {
                            JsonNode newIdNode = mapper.createObjectNode();
                            JsonNode newIdNode1 = mapper.createObjectNode();
                            JsonNode newtextNode = mapper.createArrayNode();
                            JsonNode tcontentnode = new TextNode(content);
                            ((ArrayNode) newtextNode).add(tcontentnode);
                            ((ObjectNode) newIdNode).put("$type_orgAnhalyticsID", tcontentnode);
                            JsonNode arrayNode = mapper.createArrayNode();
                            ((ArrayNode) arrayNode).add(newIdNode);
                            ((ObjectNode) newIdNode1).put("$idno", arrayNode); // update value
                            ((ArrayNode) theChild).add(newIdNode1);
                        }
                    } else if (field.equals("$classCode")) {
                        theClassCodeNode = subJson.path("$classCode");
                    } else if (field.equals("level")) {
                        theLevelNode = subJson.path("level");
                    } else if (field.equals("$title")) {
                        theTitleNode = subJson.path("$title");
                        // we add a canonical copy of the title under $first, which allows to 
                        // query easily the title of an article without knowing the language
                        JsonNode newNode = mapper.createObjectNode();
                        JsonNode textNode = mapper.createArrayNode();

                        String titleString = null;
                        Iterator<JsonNode> ite2 = theTitleNode.elements();
                        while (ite2.hasNext()) {
                            JsonNode temp2 = ite2.next();
                            titleString = temp2.textValue();
                            break;
                        }

                        JsonNode tnode = new TextNode(titleString);
                        ((ArrayNode) textNode).add(tnode);
                        ((ObjectNode) newNode).put("$title-first", textNode);
                        ((ArrayNode) theTitleNode).add(newNode);
                        //same for abstract
                        //return subJson;
                    } else if (field.equals("n")) {
                        theNNode = subJson.path("n");
                    } else if (field.equals("$person")) {
                        thePersonNode = subJson.path("$person");
                    } else if (field.equals("$div")) {
                        divNode = subJson.path("$div");
                    } else if (field.equals("$item")) {
                        theItemNode = subJson.path("$item");
                    } else if (field.equals("$date")) {
                        theDateNode = subJson.path("$date");
                        //} else if (field.equals("$orgname")) {
                    } else if (field.equals("$keywords")) {
                        theKeywordsNode = subJson.path("$keywords");
                        String keywords = "";

                        //raw
                        Iterator<JsonNode> ite2 = theKeywordsNode.elements();
                        /*if(theKeywordsNode.get(0).isTextual()){
                            System.out.println(theKeywordsNode.toString());
                        }*/
                        while (ite2.hasNext()) {
                            JsonNode temp2 = ite2.next();
                            if (temp2.isTextual()) {
                                // To avoid ambiguity, we wrap the text directy contained into keywords in a text element (otherwise ES doesnt like it)
                                keywords += temp2.textValue();
                                /*String xml_id = fields.next();
                                JsonNode xml_idNode = subJson.path(xml_id);
                                 */
                                ((ArrayNode) theKeywordsNode).remove(0);
                                JsonNode newNode = mapper.createObjectNode();
                                JsonNode textNode = mapper.createArrayNode();
                                JsonNode tnode = new TextNode(keywords);
                                ((ArrayNode) textNode).add(tnode);
                                ((ObjectNode) newNode).put("$raw", textNode);

                                theKeywordsNode = subJson.path("$keywords");
                                ((ArrayNode) theKeywordsNode).add(newNode);
                            }
                            break;
                        }
                    } else if (field.equals("$idno")) {
                        theIdnoNode = subJson.path("$idno");
                    } else if (field.equals("$biblScope")) {
                        theBiblScopeNode = subJson.path("$biblScope");
                    } else if (field.equals("scheme")) {
                        theSchemeNode = subJson.path("scheme");
                    } else if (field.equals("type")) {
                        theTypeNode = subJson.path("type");
                    } else if (field.equals("unit")) {
                        theUnitNode = subJson.path("unit");
                    } else if (field.equals("when")) {
                        theWhenNode = subJson.path("when");
                    } else if (field.equals("$term")) {
                        theTermNode = subJson.path("$term");
                    } else if (field.equals("xml:lang")) {
                        JsonNode theNode = subJson.path("xml:lang");
                        currentLang = theNode.textValue();
                    } else if (field.equals("lang")) {
                        JsonNode theNode = subJson.path("lang");
                        currentLang = theNode.textValue();
                    } else if (field.equals("xml:id")) {
                        theXmlIdNode = subJson.path("xml:id");
                    }
                }
                if ((theSchemeNode != null) && (theClassCodeNode != null)) {
                    JsonNode schemeNode = mapper.createObjectNode();
                    ((ObjectNode) schemeNode).put("$scheme_" + theSchemeNode.textValue(),
                            process(theClassCodeNode, mapper, currentLang, false, expandLang, false, anhalyticsId));
                    if (theNNode != null) {
                        ((ObjectNode) schemeNode).put("$scheme_" + theSchemeNode.textValue() + "_abbrev",
                                process(theNNode, mapper, currentLang, false, expandLang, false, anhalyticsId));
                    }
                    JsonNode arrayNode = mapper.createArrayNode();
                    ((ArrayNode) arrayNode).add(schemeNode);
                    ((ObjectNode) subJson).put("$classCode", arrayNode); // update value
                    return subJson;
                } else if ((theTypeNode != null) && (thePersonNode != null)) {
                    JsonNode typeNode = mapper.createObjectNode();
                    ((ObjectNode) typeNode).put("$type_" + theTypeNode.textValue(),
                            process(thePersonNode, mapper, currentLang, false, expandLang, false, anhalyticsId));
                    JsonNode arrayNode = mapper.createArrayNode();
                    ((ArrayNode) arrayNode).add(typeNode);
                    ((ObjectNode) subJson).put("$person", arrayNode); // update value
                    return subJson;
                } else if ((theTypeNode != null) && (theItemNode != null)) {
                    JsonNode typeNode = mapper.createObjectNode();
                    ((ObjectNode) typeNode).put("$type_" + theTypeNode.textValue(),
                            process(theItemNode, mapper, currentLang, false, expandLang, false, anhalyticsId));
                    JsonNode arrayNode = mapper.createArrayNode();
                    ((ArrayNode) arrayNode).add(typeNode);
                    ((ObjectNode) subJson).put("$item", arrayNode); // update value
                    return subJson;
                } else if ((theTypeNode != null) && (divNode != null)) {
                    JsonNode typeNode = mapper.createObjectNode();
                    ((ObjectNode) typeNode).put("$type_" + theTypeNode.textValue(),
                            process(divNode, mapper, currentLang, false, expandLang, false, anhalyticsId));
                    JsonNode arrayNode = mapper.createArrayNode();
                    ((ArrayNode) arrayNode).add(typeNode);
                    ((ObjectNode) subJson).put("$div", arrayNode); // update value
                    return subJson;
                } else if ((theSchemeNode != null  && theSchemeNode.textValue().equals("author")) && theKeywordsNode != null) {
                    // we need to set a default "author" type 
                    JsonNode typeNode = mapper.createObjectNode();
                    ((ObjectNode) typeNode).put("$type_author",
                            process(theKeywordsNode, mapper, currentLang, false, expandLang, false, anhalyticsId));
                    JsonNode arrayNode = mapper.createArrayNode();
                    ((ArrayNode) arrayNode).add(typeNode);
                    ((ObjectNode) subJson).put("$keywords", arrayNode); // update value
                    
                    return subJson;
                } else if ((theTypeNode != null) && (theIdnoNode != null)) {
                    JsonNode typeNode = mapper.createObjectNode();
                    ((ObjectNode) typeNode).put("$type_" + theTypeNode.textValue(),
                            process(theIdnoNode, mapper, currentLang, false, expandLang, false, anhalyticsId));
                    JsonNode arrayNode = mapper.createArrayNode();
                    ((ArrayNode) arrayNode).add(typeNode);
                    ((ObjectNode) subJson).put("$idno", arrayNode); // update value
                    return subJson;
                } else if ((theTypeNode != null) && (theDateNode != null)) {
                    JsonNode typeNode = mapper.createObjectNode();
                    if (theWhenNode != null) {
                        JsonNode theWhenNode2 = mapper.createArrayNode();

                        ((ArrayNode) theWhenNode2).add(theWhenNode);
                        ((ObjectNode) typeNode).put("$type_" + theTypeNode.textValue(), process(theWhenNode2, mapper, currentLang, false, expandLang, true, anhalyticsId));
                        ((ObjectNode) subJson).remove("when");
                    } else {
                        ((ObjectNode) typeNode).put("$type_" + theTypeNode.textValue(),
                                process(theDateNode, mapper, currentLang, false, expandLang, true, anhalyticsId));
                    }
                    JsonNode arrayNode = mapper.createArrayNode();
                    ((ArrayNode) arrayNode).add(typeNode);
                    ((ObjectNode) subJson).put("$date", arrayNode); // update value
                    return subJson;
                } else if ((theTypeNode == null) && (theDateNode != null)) {
                    JsonNode typeNode = mapper.createObjectNode();
                    if (theWhenNode != null) {

                        JsonNode theWhenNode2 = mapper.createArrayNode();
                        ((ArrayNode) theWhenNode2).add(theWhenNode);
                        ((ObjectNode) typeNode).put("$type_unknown", process(theWhenNode2, mapper, currentLang, false, expandLang, true, anhalyticsId));
                        ((ObjectNode) subJson).remove("when");
                    } else {
                        ((ObjectNode) typeNode).put("$type_unknown",
                                process(theDateNode, mapper, currentLang, false, expandLang, true, anhalyticsId));
                    }
                    JsonNode arrayNode = mapper.createArrayNode();
                    ((ArrayNode) arrayNode).add(typeNode);
                    ((ObjectNode) subJson).put("$date", arrayNode); // update value
                    return subJson;
                } else if ((theUnitNode != null) && (theBiblScopeNode != null)) {
                    JsonNode typeNode = mapper.createObjectNode();
                    ((ObjectNode) typeNode).put("$unit_" + theUnitNode.textValue(),
                            process(theBiblScopeNode, mapper, currentLang, false, expandLang, false, anhalyticsId));
                    JsonNode arrayNode = mapper.createArrayNode();
                    ((ArrayNode) arrayNode).add(typeNode);
                    ((ObjectNode) subJson).put("$biblScope", arrayNode); // update value
                    return subJson;
                }
            }
            JsonNode newNode = null;
            if (subJson.isArray()) {
                newNode = mapper.createArrayNode();
                Iterator<JsonNode> ite = subJson.elements();
                while (ite.hasNext()) {
                    JsonNode temp = ite.next();
                    ((ArrayNode) newNode).add(process(temp, mapper, currentLang, true, expandLang, isDate, anhalyticsId));
                }
            } else if (subJson.isObject()) {
                newNode = mapper.createObjectNode();
                Iterator<String> fields = subJson.fieldNames();
                while (fields.hasNext()) {
                    String field = fields.next();
                    /*if (field.equals("when")) {
                        ((ObjectNode) newNode).put(field, process(subJson.path(field), mapper,
                                currentLang, false, expandLang, true, anhalyticsId));
                        if(anhalyticsId.equals("57140e99a8267aefa7851325"))
                            System.out.println(newNode.toString());
                    } else {
                     */
                    ((ObjectNode) newNode).put(field, process(subJson.path(field), mapper,
                            currentLang, false, expandLang, false, anhalyticsId));
                    //}
                }
            }
            return newNode;
        } else if (subJson.isTextual() && fromArray && expandLang) {
            JsonNode langNode = mapper.createObjectNode();
            String langField = "$lang_";
            if (currentLang == null) {
                langField += "unknown";
            } else {
                langField += currentLang;
            }
            ArrayNode langArrayNode = mapper.createArrayNode();
            langArrayNode.add(subJson);
            ((ObjectNode) langNode).put(langField, langArrayNode);

            return langNode;
        } else if (subJson.isTextual() && isDate) {
            String val = null;
            String date = subJson.textValue();

            if (date.length() == 4) {
                val = date + "-12-31";
            } else if ((date.length() == 7) || (date.length() == 6)) {
                int ind = date.indexOf("-");
                String month = date.substring(ind + 1, date.length());
                if (month.length() == 1) {
                    month = "0" + month;
                }
                if (month.equals("02")) {
                    val = date.substring(0, 4) + "-" + month + "-28";
                } else if ((month.equals("04")) || (month.equals("06")) || (month.equals("09"))
                        || (month.equals("11"))) {
                    val = date.substring(0, 4) + "-" + month + "-30";
                } else {
                    val = date.substring(0, 4) + "-" + month + "-31";
                }
            } else {
                val = date.trim();
                val = Utilities.completeDate(val);
                // we have the "lazy programmer" case where the month is 00, e.g. 2012-00-31
                // which means the month is unknown
                ///val = val.replace("-00-", "-12-");
            }
            val = val.replace(" ", "T"); // this is for the dateOptionalTime elasticSearch format
            JsonNode tnode = new TextNode(val);
            return tnode;
        } else {
            return subJson;
        }
    }

    /**
     * Get the (N)ERD annotations and inject them in the document structure in a standoff node
     */
    public JsonNode getStandoffNerd(ObjectMapper mapper, String anhalyticsId) throws Exception {
        JsonNode standoffNode = null;
        String annotation = mm.getNerdAnnotations(anhalyticsId).getJson();
        if ((annotation != null) && (annotation.trim().length() > 0)) {
            JsonNode jsonAnnotation = mapper.readTree(annotation);
            if ((jsonAnnotation != null) && (!jsonAnnotation.isMissingNode())) {
                Iterator<JsonNode> iter0 = jsonAnnotation.elements();
                JsonNode annotNode = mapper.createArrayNode();
                int n = 0;
                int m = 0;
                while (iter0.hasNext()
                        && (n < MAX_NERD_INDEXED_PARAGRAPHS)
                        && (m < MAX_INDEXED_NERD)) {
                    JsonNode jsonLocalAnnotation = (JsonNode) iter0.next();

                    // we only get the concept IDs and the nerd confidence score
                    JsonNode nerd = jsonLocalAnnotation.findPath("nerd");
                    JsonNode entities = nerd.findPath("entities");
                    if ((entities != null) && (!entities.isMissingNode())) {
                        Iterator<JsonNode> iter = entities.elements();

                        while (iter.hasNext()) {
                            JsonNode piece = (JsonNode) iter.next();

                            JsonNode nerd_scoreN = piece.findValue("nerd_score");
                            JsonNode preferredTermN = piece.findValue("preferredTerm");
                            JsonNode wikipediaExternalRefN = piece.findValue("wikipediaExternalRef");
                            JsonNode freeBaseExternalRefN = piece.findValue("freeBaseExternalRef");

                            String nerd_score = nerd_scoreN.textValue();
                            String wikipediaExternalRef = wikipediaExternalRefN.textValue();
                            String preferredTerm = preferredTermN.textValue();
                            String freeBaseExternalRef = null;
                            if ((freeBaseExternalRefN != null) && (!freeBaseExternalRefN.isMissingNode())) {
                                freeBaseExternalRef = freeBaseExternalRefN.textValue();
                            }

                            JsonNode newNode = mapper.createArrayNode();

                            JsonNode nerdScoreNode = mapper.createObjectNode();
                            ((ObjectNode) nerdScoreNode).put("nerd_score", nerd_score);
                            ((ArrayNode) newNode).add(nerdScoreNode);

                            JsonNode wikiNode = mapper.createObjectNode();
                            ((ObjectNode) wikiNode).put("wikipediaExternalRef", wikipediaExternalRef);
                            ((ArrayNode) newNode).add(wikiNode);

                            JsonNode preferredTermNode = mapper.createObjectNode();
                            ((ObjectNode) preferredTermNode).put("preferredTerm", preferredTerm);
                            ((ArrayNode) newNode).add(preferredTermNode);

                            if (freeBaseExternalRef != null) {
                                JsonNode freeBaseNode = mapper.createObjectNode();
                                ((ObjectNode) freeBaseNode).put("freeBaseExternalRef", freeBaseExternalRef);
                                ((ArrayNode) newNode).add(freeBaseNode);
                            }

                            ((ArrayNode) annotNode).add(newNode);
                            m++;
                        }
                    }
                    n++;
                }
                JsonNode nerdStandoffNode = mapper.createObjectNode();
                ((ObjectNode) nerdStandoffNode).put("$nerd", annotNode);

                JsonNode annotationArrayNode = mapper.createArrayNode();
                ((ArrayNode) annotationArrayNode).add(nerdStandoffNode);

                standoffNode = mapper.createObjectNode();
                ((ObjectNode) standoffNode).put("$standoff", annotationArrayNode);
            }
        }
        return standoffNode;
    }

    /**
     * Get the grobid-keyterm annotations and inject them in the document structure in a standoff node
     */
    public JsonNode getStandoffKeyTerm(ObjectMapper mapper, String anhalyticsId, JsonNode standoffNode) throws Exception {
        String annotation = mm.getKeytermAnnotations(anhalyticsId).getJson();
        if ((annotation != null) && (annotation.trim().length() > 0)) {
            JsonNode jsonAnnotation = mapper.readTree(annotation);

            JsonNode keytermNode = jsonAnnotation.findPath("keyterm");
            if ((keytermNode != null) && (!keytermNode.isMissingNode())) {

                // check language - only english is valid here and indexed
                JsonNode languageNode = keytermNode.findPath("language");
                if ((languageNode != null) && (!languageNode.isMissingNode())) {
                    JsonNode langNode = keytermNode.findPath("lang");
                    if ((langNode != null) && (!langNode.isMissingNode())) {
                        String lang = langNode.textValue();
                        if (!lang.equals("en")) {
                            return standoffNode;
                        }
                    }
                }

                // the keyterms
                JsonNode keytermsNode = keytermNode.findPath("keyterms");
                if ((keytermsNode != null) && (!keytermsNode.isMissingNode())) {

                    Iterator<JsonNode> iter0 = keytermsNode.elements();
                    JsonNode annotNode = mapper.createArrayNode();

                    int n = 0;
                    while (iter0.hasNext() && (n < MAX_INDEXED_KEYTERM)) {
                        JsonNode jsonLocalKeyterm = (JsonNode) iter0.next();
                        JsonNode termNode = jsonLocalKeyterm.findPath("term");
                        JsonNode scoreNode = jsonLocalKeyterm.findPath("score");

                        String term = termNode.textValue();
                        double score = scoreNode.doubleValue();

                        JsonNode newNode = mapper.createArrayNode();

                        JsonNode keytermScoreNode = mapper.createObjectNode();
                        ((ObjectNode) keytermScoreNode).put("keyterm_score", score);
                        ((ArrayNode) newNode).add(keytermScoreNode);

                        JsonNode ktermNode = mapper.createObjectNode();
                        ((ObjectNode) ktermNode).put("keyterm", term);
                        ((ArrayNode) newNode).add(ktermNode);

                        // do we have a concept (wikipedia/freebase id)
                        JsonNode entitiesNode = jsonLocalKeyterm.findPath("entities");
                        if ((entitiesNode != null) && (!entitiesNode.isMissingNode())) {
                            Iterator<JsonNode> iter1 = entitiesNode.elements();
                            if (iter1.hasNext()) {
                                JsonNode entityNode = (JsonNode) iter1.next();

                                JsonNode nerd_scoreN = entityNode.findValue("nerd_score");
                                JsonNode preferredTermN = entityNode.findValue("preferredTerm");
                                JsonNode wikipediaExternalRefN = entityNode.findValue("wikipediaExternalRef");
                                JsonNode freeBaseExternalRefN = entityNode.findValue("freeBaseExternalRef");

                                String nerd_score = nerd_scoreN.textValue();
                                String wikipediaExternalRef = wikipediaExternalRefN.textValue();
                                String preferredTerm = preferredTermN.textValue();
                                String freeBaseExternalRef = null;
                                if ((freeBaseExternalRefN != null) && (!freeBaseExternalRefN.isMissingNode())) {
                                    freeBaseExternalRef = freeBaseExternalRefN.textValue();
                                }

                                JsonNode nerdScoreNode = mapper.createObjectNode();
                                ((ObjectNode) nerdScoreNode).put("nerd_score", nerd_score);
                                ((ArrayNode) newNode).add(nerdScoreNode);

                                JsonNode wikiNode = mapper.createObjectNode();
                                ((ObjectNode) wikiNode).put("wikipediaExternalRef", wikipediaExternalRef);
                                ((ArrayNode) newNode).add(wikiNode);

                                JsonNode preferredTermNode = mapper.createObjectNode();
                                ((ObjectNode) preferredTermNode).put("preferredTerm", preferredTerm);
                                ((ArrayNode) newNode).add(preferredTermNode);

                                if (freeBaseExternalRef != null) {
                                    JsonNode freeBaseNode = mapper.createObjectNode();
                                    ((ObjectNode) freeBaseNode).put("freeBaseExternalRef", freeBaseExternalRef);
                                    ((ArrayNode) newNode).add(freeBaseNode);
                                }
                            }
                        }
                        ((ArrayNode) annotNode).add(newNode);
                        n++;
                    }

                    JsonNode keytermStandoffNode = mapper.createObjectNode();
                    ((ObjectNode) keytermStandoffNode).put("$keyterm", annotNode);

                    if (standoffNode == null) {
                        standoffNode = mapper.createObjectNode();

                        JsonNode annotationArrayNode = mapper.createArrayNode();
                        ((ArrayNode) annotationArrayNode).add(keytermStandoffNode);

                        ((ObjectNode) standoffNode).put("$standoff", annotationArrayNode);
                    } else {
                        JsonNode annotationArrayNode = standoffNode.findValue("$standoff");
                        ((ArrayNode) annotationArrayNode).add(keytermStandoffNode);
                    }
                }

                // document categories - the categorization is based on Wikipedia categories
                JsonNode categoriesNode = keytermNode.findPath("global_categories");
                if ((categoriesNode != null) && (!categoriesNode.isMissingNode())) {
                    Iterator<JsonNode> iter0 = categoriesNode.elements();
                    JsonNode annotNode = mapper.createArrayNode();

                    int n = 0;
                    while (iter0.hasNext()) {
                        JsonNode jsonLocalCategory = (JsonNode) iter0.next();
                        String category = null;
                        int pageId = -1;
                        double weight = 0.0;

                        JsonNode catNode = jsonLocalCategory.findPath("category");
                        if ((catNode != null) && (!catNode.isMissingNode())) {
                            category = catNode.textValue();
                        }

                        JsonNode pageNode = jsonLocalCategory.findPath("page_id");
                        if ((pageNode != null) && (!pageNode.isMissingNode())) {
                            pageId = pageNode.intValue();
                        }

                        JsonNode weightNode = jsonLocalCategory.findPath("weight");
                        if ((weightNode != null) && (!weightNode.isMissingNode())) {
                            weight = weightNode.doubleValue();
                        }

                        if ((category != null) && (pageId != -1) && (weight != 0.0)) {
                            JsonNode newNode = mapper.createArrayNode();

                            JsonNode categoryNode = mapper.createObjectNode();
                            ((ObjectNode) categoryNode).put("category", category);
                            ((ArrayNode) newNode).add(categoryNode);

                            JsonNode wikiNode = mapper.createObjectNode();
                            ((ObjectNode) wikiNode).put("wikipediaExternalRef", pageId);
                            ((ArrayNode) newNode).add(wikiNode);

                            JsonNode scoreNode = mapper.createObjectNode();
                            ((ObjectNode) scoreNode).put("score", weight);
                            ((ArrayNode) newNode).add(scoreNode);

                            ((ArrayNode) annotNode).add(newNode);
                            n++;
                        }
                    }

                    JsonNode categoriesStandoffNode = mapper.createObjectNode();
                    ((ObjectNode) categoriesStandoffNode).put("$category", annotNode);

                    if (standoffNode == null) {
                        standoffNode = mapper.createArrayNode();

                        JsonNode annotationArrayNode = mapper.createArrayNode();
                        ((ArrayNode) annotationArrayNode).add(categoriesStandoffNode);

                        ((ObjectNode) standoffNode).put("$standoff", annotationArrayNode);
                    } else {
                        JsonNode annotationArrayNode = standoffNode.findValue("$standoff");
                        ((ArrayNode) annotationArrayNode).add(categoriesStandoffNode);
                    }
                }

            }
        }
        return standoffNode;
    }

    /**
     * Get the grobid quantities annotations and inject them in the document structure in a standoff node
     */
    public JsonNode getStandoffQuantities(ObjectMapper mapper, String anhalyticsId, JsonNode standoffNode) throws Exception {
        String annotation = mm.getQuantitiesAnnotations(anhalyticsId).getJson();
        if ((annotation != null) && (annotation.trim().length() > 0)) {           
            JsonNode jsonAnnotation = mapper.readTree(annotation);
            if ((jsonAnnotation != null) && (!jsonAnnotation.isMissingNode())) {
                JsonNode annotationNode = jsonAnnotation.findPath("annotation");
                if ((annotationNode == null) || (annotationNode.isMissingNode())) 
                    return standoffNode;

                Iterator<JsonNode> iter0 = annotationNode.elements();
                JsonNode annotNode = mapper.createArrayNode();
                int n = 0;
                int m = 0;
                while (iter0.hasNext()
                        && (n < MAX_QUANTITIES_INDEXED_PARAGRAPHS)
                        && (m < MAX_INDEXED_QUANTITIES)) {
                    JsonNode jsonLocalAnnotation = (JsonNode) iter0.next();

                    // we only get what should be searched together with the document metadata and content
                    // so ignoring coordinates, offsets, ...
                    JsonNode quantities = jsonLocalAnnotation.findPath("quantities");
                    if ((quantities == null) || (quantities.isMissingNode())) 
                        continue;
                    JsonNode measurements = quantities.findPath("measurements");
                    if ((measurements == null) || (measurements.isMissingNode())) 
                        continue;
                    // measurements is an array
                    Iterator<JsonNode> iter = measurements.elements();
                    while (iter.hasNext()) {
                        JsonNode piece = (JsonNode) iter.next();
//logger.info(piece.toString());
                        JsonNode typeNode = piece.findPath("type");
                        if ((typeNode == null) || (typeNode.isMissingNode())) 
                            continue;
                        String type = typeNode.textValue();
//logger.info("type is " + type + " / " + piece.toString());
                        
                        if (type.equals("value")) {
                            JsonNode quantity = piece.findPath("quantity");
                            if ((quantity == null) || (quantity.isMissingNode())) 
                                continue;
                            JsonNode typeMeasure = quantity.findPath("type");
                            if ((typeMeasure == null) || (typeMeasure.isMissingNode()))
                                continue;
                            String valueTypeMeasure = typeMeasure.textValue();
                            
                            JsonNode normalizedQuantity = quantity.findPath("normalizedQuantity");
                            if ((normalizedQuantity == null) || (normalizedQuantity.isMissingNode()))
                                continue;
                            Double val = normalizedQuantity.doubleValue();

                            JsonNode newNode = mapper.createObjectNode();
                            ((ObjectNode) newNode).put(valueTypeMeasure.replace(" ", "_"), val);
                            ((ArrayNode) annotNode).add(newNode);
//logger.info("type is " + type + " / " + annotNode.toString());

                        } else if (type.equals("interval")) {
//logger.info("type is " + type + " / " + piece.toString());
                            JsonNode quantityMost = piece.findPath("quantityMost");
                            JsonNode quantityLeast = piece.findPath("quantityLeast");
                            String valueTypeMeasure = null;
                            JsonNode range = null;
                            Double valLeast;
                            Double valMost;

                            if ((quantityMost != null) && (!quantityMost.isMissingNode())) {
                                JsonNode typeMeasure = quantityMost.findPath("type");
                                if ((typeMeasure == null) || (typeMeasure.isMissingNode()))
                                    continue;
                                valueTypeMeasure = typeMeasure.textValue();

                                JsonNode normalizedQuantityMost = quantityMost.findPath("normalizedQuantity");
                                if ((normalizedQuantityMost != null) && (!normalizedQuantityMost.isMissingNode())) {
                                    if (range == null)
                                        range = mapper.createObjectNode();
                                    valMost = normalizedQuantityMost.doubleValue();    
                                    ((ObjectNode) range).put("lte", valMost);
                                }
                            } 

                            if ((quantityLeast != null) && (!quantityLeast.isMissingNode())) {  
                                JsonNode typeMeasure = quantityLeast.findPath("type");
                                typeMeasure = quantityLeast.findPath("type");
                                if ((typeMeasure == null) || (typeMeasure.isMissingNode()))
                                    continue;
                                valueTypeMeasure = typeMeasure.textValue();

                                JsonNode normalizedQuantityLeast = quantityLeast.findPath("normalizedQuantity");
                                if ((normalizedQuantityLeast != null) && (!normalizedQuantityLeast.isMissingNode())) {
                                    if (range == null)
                                        range = mapper.createObjectNode();
                                    valLeast = normalizedQuantityLeast.doubleValue();
                                    ((ObjectNode) range).put("gte", valLeast);
                                }
                            }

                            if (range != null) {
                                JsonNode newNode = mapper.createObjectNode();
                                ((ObjectNode) newNode).put(valueTypeMeasure.replace(" ", "_")+"_range", range);
                                ((ArrayNode) annotNode).add(newNode);

logger.info("type is " + type + " / " + annotNode.toString());
                            }

                        } else if (type.equals("listc")) {
                            JsonNode quantitiesList = piece.findPath("quantities");
                            if ((quantitiesList == null) || (quantitiesList.isMissingNode())) 
                                continue;
                            // quantitiesList here is a list with a list of quantity values

                        }  
                        m++;
                    }
                    //n++;
                }
                JsonNode quantitiesStandoffNode = mapper.createObjectNode();
                ((ObjectNode) quantitiesStandoffNode).put("$quantities", annotNode);

                //JsonNode annotationArrayNode = mapper.createArrayNode();
                //((ArrayNode) annotationArrayNode).add(quantitiesStandoffNode);

                //((ObjectNode) standoffNode).put("$standoff", annotationArrayNode);
                if (m > 0) {
                    if (standoffNode == null) {
                        standoffNode = mapper.createArrayNode();

                        JsonNode annotationArrayNode = mapper.createArrayNode();
                        ((ArrayNode) annotationArrayNode).add(quantitiesStandoffNode);

                        ((ObjectNode) standoffNode).put("$standoff", annotationArrayNode);
                    } else {
                        JsonNode annotationArrayNode = standoffNode.findValue("$standoff");
                        ((ArrayNode) annotationArrayNode).add(quantitiesStandoffNode);
                    }
                }
            }
        }
        return standoffNode;
    }

    private boolean filterDocuments(JsonNode jsonRoot) {
        // we want to filter out documents in the future...
        // paths are $TEI.$teiHeader.$sourceDesc.$biblStruct.$monogr.$imprint.$date
        // or $TEI.$teiHeader.$editionStmt.$edition.$date
        // now a piece of art of progamming : ;)
        boolean ok = true;
        JsonNode teiHeader = jsonRoot.findPath("$teiCorpus.$teiHeader");
        if ((teiHeader != null) && (!teiHeader.isMissingNode())) {
            JsonNode sourceDesc = teiHeader.findPath("$sourceDesc");
            if ((sourceDesc != null) && (!sourceDesc.isMissingNode())) {
                JsonNode biblStruct = sourceDesc.findPath("$biblStruct");
                if ((biblStruct != null) && (!biblStruct.isMissingNode())) {
                    JsonNode monogr = biblStruct.findPath("$monogr");
                    if ((monogr != null) && (!monogr.isMissingNode())) {
                        JsonNode imprint = monogr.findPath("$imprint");
                        if ((imprint != null) && (!imprint.isMissingNode())) {
                            JsonNode date = imprint.findPath("$date");
                            if ((date != null) && (!date.isMissingNode())) {
                                if (date.isArray()) {
                                    Iterator<JsonNode> ite = ((ArrayNode) date).elements();
                                    if (ite.hasNext()) {
                                        JsonNode dateVal = (JsonNode) ite.next();
                                        String dateStr = dateVal.textValue();
                                        try {
                                            Date theDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
                                            Date today = new Date();
                                            if (theDate.compareTo(today) > 0) {
                                                ok = false;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                JsonNode editionStmt = teiHeader.findPath("$editionStmt");
                if ((editionStmt != null) && (!editionStmt.isMissingNode())) {
                    JsonNode edition = editionStmt.findPath("$edition");
                    if ((edition != null) && (!edition.isMissingNode())) {
                        JsonNode date = edition.findPath("$date");
                        if ((date != null) && (!date.isMissingNode())) {
                            if (date.isArray()) {
                                Iterator<JsonNode> ite = ((ArrayNode) date).elements();
                                if (ite.hasNext()) {
                                    JsonNode dateVal = (JsonNode) ite.next();
                                    String dateStr = dateVal.textValue();
                                    try {
                                        Date theDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
                                        Date today = new Date();
                                        if (theDate.compareTo(today) > 0) {
                                            ok = false;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ok;
    }

    /**
     * adds fullname node.
     */
    private String addFullName(JsonNode theChild, ObjectMapper mapper) {
        String fullName = "";
        if (theChild.isArray()) {
            String forename = "";
            String surname = "";
            Iterator<JsonNode> ite = theChild.elements();
            while (ite.hasNext()) {
                JsonNode temp = ite.next();
                if (temp.isObject()) {
                    // get the text value of the array
                    Iterator<JsonNode> ite2 = temp.path("$forename").elements();
                    while (ite2.hasNext()) {
                        JsonNode temp2 = ite2.next();

                        if (forename != null) {
                            forename += " " + temp2.textValue();
                        } else {
                            forename = temp2.textValue();
                        }
                        break;
                    }

                    // get the text value of the array
                    ite2 = temp.path("$surname").elements();
                    while (ite2.hasNext()) {
                        JsonNode temp2 = ite2.next();
                        surname = temp2.textValue();
                        break;
                    }

                }
            }
            if (forename != null) {
                fullName = forename;
            }
            if (surname != null) {
                fullName += " " + surname;
            }

            if (fullName != null) {
                fullName = fullName.trim();
                JsonNode newNode = mapper.createObjectNode();
                JsonNode textNode = mapper.createArrayNode();
                JsonNode tnode = new TextNode(fullName);
                ((ArrayNode) textNode).add(tnode);
                ((ObjectNode) newNode).put("$fullName", textNode);
                ((ArrayNode) theChild).add(newNode);
            }
        }
        return fullName;
    }

}
