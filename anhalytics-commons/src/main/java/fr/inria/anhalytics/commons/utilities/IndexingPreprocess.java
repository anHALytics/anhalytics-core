package fr.inria.anhalytics.commons.utilities;

import fr.inria.anhalytics.commons.managers.MongoFileManager;
import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.Iterator;
import java.util.List;
import java.util.Arrays;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Additional Java pre-processing of the JSON string.
 *
 * @author PL
 */
public class IndexingPreprocess {

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

    public String process(String jsonStr) throws Exception {
        return process(jsonStr, null);
    }

    public String process(String jsonStr, String filename) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonRoot = mapper.readTree(jsonStr);
        // root node is the TEI node, we add as a child the "light" annotations in a 
        // standoff element
        if (filename != null) {
            JsonNode teiRoot = jsonRoot.findPath("$teiCorpus");

            if ((teiRoot != null) && (!teiRoot.isMissingNode())) {
                JsonNode standoffNode = getStandoff(mapper, filename);
                ((ArrayNode) teiRoot).add(standoffNode);
            }
        }

        // here recursive modification of the json document via Jackson
        jsonRoot = process(jsonRoot, mapper, null, false, false, false, filename);

        if (!filterDocuments(jsonRoot)) {
            return null;
        }

        return jsonRoot.toString();
    }

    private JsonNode process(JsonNode subJson,
            ObjectMapper mapper,
            String currentLang,
            boolean fromArray,
            boolean expandLang,
            boolean isDate,
            String filename) throws Exception {
        if (subJson.isContainerNode()) {
            if (subJson.isObject()) {
                Iterator<String> fields = ((ObjectNode) subJson).getFieldNames();
                JsonNode theSchemeNode = null;
                JsonNode theClassCodeNode = null;
                JsonNode theTypeNode = null;
                JsonNode thePersonNode = null;
                JsonNode theItemNode = null;
                JsonNode theKeywordsNode = null;
                JsonNode theDepositorKeywordsNode = null;
                JsonNode theDateNode = null;
                JsonNode theIdnoNode = null;
                JsonNode theBiblScopeNode = null;
                JsonNode theWhenNode = null;
                JsonNode theTermNode = null;
                JsonNode theNNode = null;
                JsonNode theTitleNode = null;
                JsonNode theXmlIdNode = null;
                boolean isDepositorKeywords = false;
                while (fields.hasNext()) {
                    String field = fields.next();
                    if (field.startsWith("$")) {
                        if (expandable.contains(field)) {
                            expandLang = true;
                        } else {
                            expandLang = false;
                        }
                    }

                    // ignoring the possibly present $lang_ nodes (this can appear in old version of JsonTapasML)
                    /*if (field.startsWith("$lang_") || field.startsWith("$lang_")) {
                     // we need to ignore this node, as the expansion is added automatically when the textual element 
                     // is reached
                     JsonNode theChildNode = subJson.path(field);
                     // the child should then always be an array with a unique textual child
                     if (theChildNode.isArray()) {
                     Iterator<JsonNode> ite = theChildNode.getElements();
                     while (ite.hasNext()) {
                     JsonNode temp = ite.next();
                     theChildNode = temp;
                     break;
                     }
                     }
                     return process(theChildNode, mapper, currentLang, false, expandLang, false, filename);
                     }
                     */
                    // we add the full name in order to index it directly without more time consuming
                    // script and concatenation at facet creation time
                    if (field.equals("$persName")) {
                        JsonNode theChild = subJson.path("$persName");
                        // this child is an array
                        addFullName(theChild, mapper);

                        return subJson;
                    } else if (field.equals("$classCode")) {
                        theClassCodeNode = subJson.path("$classCode");
                    } else if (field.equals("$title")) {
                        theTitleNode = subJson.path("$title");
                        // we add a canonical copy of the title under $first, which allows to 
                        // query easily the title of an article without knowing the language
                        JsonNode newNode = mapper.createObjectNode();
                        JsonNode textNode = mapper.createArrayNode();

                        String titleString = null;
                        Iterator<JsonNode> ite2 = theTitleNode.getElements();
                        while (ite2.hasNext()) {
                            JsonNode temp2 = ite2.next();
                            titleString = temp2.getTextValue();
                            break;
                        }

                        JsonNode tnode = new TextNode(titleString);
                        ((ArrayNode) textNode).add(tnode);
                        ((ObjectNode) newNode).put("$title-first", textNode);
                        ((ArrayNode) theTitleNode).add(newNode);
                        //return subJson;
                    } else if (field.equals("n")) {
                        theNNode = subJson.path("n");
                    } else if (field.equals("$person")) {
                        thePersonNode = subJson.path("$person");
                    } else if (field.equals("$item")) {
                        theItemNode = subJson.path("$item");
                    } else if (field.equals("$date")) {
                        theDateNode = subJson.path("$date");
                    } else if (field.equals("$keywords")) {
                        theKeywordsNode = subJson.path("$keywords");
                        String keywords = "";
                        if(isDepositorKeywords)
                            theDepositorKeywordsNode = theKeywordsNode;
                        Iterator<JsonNode> ite2 = theKeywordsNode.getElements();
                        while (ite2.hasNext()) {
                            JsonNode temp2 = ite2.next();
                            if (temp2.isTextual()) {
                                // To avoid ambiguity, we wrap the text directy contained into keywords in a text element (otherwise ES doesnt like it)
                                keywords += temp2.getTextValue();
                                String xml_id = fields.next();
                                JsonNode xml_idNode= subJson.path(xml_id);
                                ((ArrayNode) theKeywordsNode).remove(0);
                                JsonNode newNode = mapper.createObjectNode();
                                JsonNode textNode = mapper.createArrayNode();
                                JsonNode tnode = new TextNode(keywords);
                                ((ArrayNode) textNode).add(tnode);
                                ((ObjectNode) newNode).put("$text", textNode);
                                theKeywordsNode = subJson.path("$keywords");
                                ((ObjectNode) newNode).put(xml_id, xml_idNode.getTextValue());
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
                        
                        if(theSchemeNode.getTextValue().equals("author")){
                            isDepositorKeywords = true;
                        }
                    } else if (field.equals("type")) {
                        theTypeNode = subJson.path("type");
                    } else if (field.equals("when")) {
                        theWhenNode = subJson.path("when");
                    } else if (field.equals("$term")) {
                        theTermNode = subJson.path("$term");
                    } else if (field.equals("xml:lang")) {
                        JsonNode theNode = subJson.path("xml:lang");
                        currentLang = theNode.getTextValue();
                    } else if (field.equals("lang")) {
                        JsonNode theNode = subJson.path("lang");
                        currentLang = theNode.getTextValue();
                    } else if (field.equals("xml:id")) {
                        theXmlIdNode = subJson.path("xml:id");
                    }
                }
                /*if ( (theSchemeNode != null) && (theClassCodeNode != null) ) {
                 JsonNode schemeNode = mapper.createObjectNode();
                 ((ObjectNode) schemeNode).put("$scheme_"+theSchemeNode.getTextValue(),
                 process(theClassCodeNode, mapper, currentLang, false, expandLang, false, filename));
                 JsonNode arrayNode = mapper.createArrayNode();		
                 ((ArrayNode) arrayNode).add(schemeNode);
                 ((ObjectNode) subJson).put("$classCode", arrayNode); // update value
                 return subJson;
                 }*/

                /* 
                 //inject annotations in the document...
                 if ( (filename != null) && (theXmlIdNode != null) ) {
                 if (theTitleNode != null) {
                 String theId = theXmlIdNode.getTextValue();
                 System.out.println("filename: " + filename + ", xml:id: " + theId);
                 String annotation = mm.getAnnotation(filename, theId);
                 //System.out.println(annotation);
                 if ( (annotation != null) && (annotation.trim().length() > 0) ) {
                 JsonNode jsonAnnotation= mapper.readTree(annotation);
                 JsonNode newNode = mapper.createObjectNode(); 
                 ((ObjectNode)newNode).put("$title-nerd", jsonAnnotation);
                 ((ArrayNode)theTitleNode).add(newNode);
                 }
                 }
                 }	
                 */
                if ((theSchemeNode != null) && (theClassCodeNode != null)) {
                    JsonNode schemeNode = mapper.createObjectNode();
                    ((ObjectNode) schemeNode).put("$scheme_" + theSchemeNode.getTextValue(),
                            process(theClassCodeNode, mapper, currentLang, false, expandLang, false, filename));
                    if (theNNode != null) {
                        ((ObjectNode) schemeNode).put("$scheme_" + theSchemeNode.getTextValue() + "_abbrev",
                                process(theNNode, mapper, currentLang, false, expandLang, false, filename));
                    }
                    JsonNode arrayNode = mapper.createArrayNode();
                    ((ArrayNode) arrayNode).add(schemeNode);
                    ((ObjectNode) subJson).put("$classCode", arrayNode); // update value
                    return subJson;
                } else if ((theTypeNode != null) && (thePersonNode != null)) {
                    JsonNode typeNode = mapper.createObjectNode();
                    ((ObjectNode) typeNode).put("$type_" + theTypeNode.getTextValue(),
                            process(thePersonNode, mapper, currentLang, false, expandLang, false, filename));
                    JsonNode arrayNode = mapper.createArrayNode();
                    ((ArrayNode) arrayNode).add(typeNode);
                    ((ObjectNode) subJson).put("$person", arrayNode); // update value
                    return subJson;
                } else if ((theTypeNode != null) && (theItemNode != null)) {
                    JsonNode typeNode = mapper.createObjectNode();
                    ((ObjectNode) typeNode).put("$type_" + theTypeNode.getTextValue(),
                            process(theItemNode, mapper, currentLang, false, expandLang, false, filename));
                    JsonNode arrayNode = mapper.createArrayNode();
                    ((ArrayNode) arrayNode).add(typeNode);
                    ((ObjectNode) subJson).put("$item", arrayNode); // update value
                    return subJson;
                } /*else if ( (theTypeNode != null) && (theDateNode != null) ) {
                 JsonNode typeNode = mapper.createObjectNode();
                 ((ObjectNode) typeNode).put("$type_"+theTypeNode.getTextValue(),
                 process(theDateNode, mapper, currentLang, false, expandLang, true, filename));
                 JsonNode arrayNode = mapper.createArrayNode();	
                 ((ArrayNode) arrayNode).add(typeNode);
                 ((ObjectNode) subJson).put("$date", arrayNode); // update value
                 return subJson;
                 }*/ else if ((theTypeNode != null) && (theKeywordsNode != null)) {
                    JsonNode typeNode = mapper.createObjectNode();
                    ((ObjectNode) typeNode).put("$type_" + theTypeNode.getTextValue(),
                            process(theKeywordsNode, mapper, currentLang, false, expandLang, false, filename));
                    JsonNode arrayNode = mapper.createArrayNode();
                    ((ArrayNode) arrayNode).add(typeNode);
                    ((ObjectNode) subJson).put("$keywords", arrayNode); // update value
                    return subJson;
                } else if (theDepositorKeywordsNode != null) {
                    // we need to set a default "author" type 
                    JsonNode typeNode = mapper.createObjectNode();
                    ((ObjectNode) typeNode).put("$type_author",
                            process(theDepositorKeywordsNode, mapper, currentLang, false, expandLang, false, filename));
                    JsonNode arrayNode = mapper.createArrayNode();
                    ((ArrayNode) arrayNode).add(typeNode);
                    ((ObjectNode) subJson).put("$keywords", arrayNode); // update value
                    return subJson;
                } else if ((theTypeNode != null) && (theIdnoNode != null)) {
                    JsonNode typeNode = mapper.createObjectNode();
                    ((ObjectNode) typeNode).put("$type_" + theTypeNode.getTextValue(),
                            process(theIdnoNode, mapper, currentLang, false, expandLang, false, filename));
                    JsonNode arrayNode = mapper.createArrayNode();
                    ((ArrayNode) arrayNode).add(typeNode);
                    ((ObjectNode) subJson).put("$idno", arrayNode); // update value
                    return subJson;
                } else if ((theTypeNode != null) && (theBiblScopeNode != null)) {
                    JsonNode typeNode = mapper.createObjectNode();
                    ((ObjectNode) typeNode).put("$type_" + theTypeNode.getTextValue(),
                            process(theBiblScopeNode, mapper, currentLang, false, expandLang, false, filename));
                    JsonNode arrayNode = mapper.createArrayNode();
                    ((ArrayNode) arrayNode).add(typeNode);
                    ((ObjectNode) subJson).put("$biblScope", arrayNode); // update value
                    return subJson;
                }

                /*else if ( (theTermNode != null) && (theTypeNode != null) ) {
                 String localVal = theTypeNode.getTextValue();
                 if ((localVal != null) && (localVal.equals("classification-symbol")) ) {
						
                 JsonNode theChildNode = null;
                 if (theTermNode.isArray()) {
                 Iterator<JsonNode> ite = theTermNode.getElements();
                 while (ite.hasNext()) {
                 JsonNode temp = ite.next();
                 theChildNode = temp;
                 break;
                 }
                 }
                 String localClass = null;
						
                 if (theChildNode != null) 
                 localClass = theChildNode.getTextValue();
							
                 if ((localClass != null) && (localClass.indexOf(":") == -1)) {
                 // we have an ECLA class
							
                 // all expansions will be put under a container which can handle more
                 // than one class (this is due to the possible compact representation)
                 JsonNode containerNode = mapper.createObjectNode();
							
                 // handle old-ages compact form
                 boolean trace = false;
                 if (localClass.indexOf("+") != -1) {
                 System.out.print(localClass + ": ");
                 trace = true;
                 }
                 List<String> classes = normalizeECLAClass(localClass);
							
                 for (String localClas : classes) {
                 if (trace) {
                 System.out.print(" " + localClas);
                 } 
								
                 List<String> paths = giveECLARootPath(localClas);
                 String rootPath = "";
                 for (String theStep : paths) {
                 rootPath += " " + theStep;
                 }
                 rootPath = rootPath.trim();
                 JsonNode tnode = new TextNode(rootPath);
                 JsonNode arrayNode = mapper.createArrayNode();	
                 ((ArrayNode) arrayNode).add(tnode);
                 ((ObjectNode) containerNode).put("$path", arrayNode); 
                 ((ObjectNode) containerNode).put("term", localClas); 
                 // we finally also add an expansion for the level specific statistics
                 // here at the indexing level we can simply use the path to specify the level
                 String[] steps = rootPath.split(" ");
                 JsonNode objectNode = mapper.createObjectNode();
								
                 for(int i=0; i<steps.length; i++) {
                 String label = null;
                 if (i == 0) {
                 label = "class";
                 }
                 else if (i == 1) {
                 label = "subclass";
                 }
                 else if (i == 2) {
                 label = "group";
                 }
                 else if (i == 3) {
                 label = "subgroup";
                 }
                 else {
                 label = "ecla-" + (i-4);
                 }
								
                 ((ObjectNode) objectNode).put(label, steps[i]);
                 }
                 ((ObjectNode) containerNode).put("$levels", objectNode); 
                 }
                 if (trace) {
                 System.out.println("");
                 }
                 ((ObjectNode) subJson).put("$ecla", containerNode); 
                 }
                 else if (localClass != null) { 
                 // we have an ICO code
                 ((ObjectNode) subJson).put("ico", localClass); 
                 }						
						
                 }
                 return subJson;
                 }*/
            }
            JsonNode newNode = null;
            if (subJson.isArray()) {
                newNode = mapper.createArrayNode();
                Iterator<JsonNode> ite = subJson.getElements();
                while (ite.hasNext()) {
                    JsonNode temp = ite.next();
                    ((ArrayNode) newNode).add(process(temp, mapper, currentLang, true, expandLang, isDate, filename));
                }
            } else if (subJson.isObject()) {
                newNode = mapper.createObjectNode();
                Iterator<String> fields = subJson.getFieldNames();
                while (fields.hasNext()) {
                    String field = fields.next();
                    if (field.equals("$date") || field.equals("when")) {
                        ((ObjectNode) newNode).put(field, process(subJson.path(field), mapper,
                                currentLang, false, expandLang, true, filename));
                    } else {
                        ((ObjectNode) newNode).put(field, process(subJson.path(field), mapper,
                                currentLang, false, expandLang, false, filename));
                    }
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
            if (subJson.getTextValue().length() == 4) {
                val = subJson.getTextValue() + "-12-31";
            } else if ((subJson.getTextValue().length() == 7) || (subJson.getTextValue().length() == 6)) {
                int ind = subJson.getTextValue().indexOf("-");
                String month = subJson.getTextValue().substring(ind + 1, subJson.getTextValue().length());
                if (month.length() == 1) {
                    month = "0" + month;
                }
                if (month.equals("02")) {
                    val = subJson.getTextValue().substring(0, 4) + "-" + month + "-28";
                } else if ((month.equals("04")) || (month.equals("06")) || (month.equals("09"))
                        || (month.equals("11"))) {
                    val = subJson.getTextValue().substring(0, 4) + "-" + month + "-30";
                } else {
                    val = subJson.getTextValue().substring(0, 4) + "-" + month + "-31";
                }
            } else {
                val = subJson.getTextValue().trim();
                // we have the "lazy programmer" case where the month is 00, e.g. 2012-00-31
                // which means the month is unknown
                val = val.replace("-00-", "-12-");
            }
            val = val.replace(" ", "T"); // this is for the dateOptionalTime elasticSearch format 
            JsonNode tnode = new TextNode(val);
            return tnode;
        } else {
            return subJson;
        }
    }

    static private String queryECLA
            = "{\"fields\":[\"rootPath\", \"_id\"],\"query\":{\"query_string\":{\"query\": \"symbol:%CLASS%\"}}}";

    /**
     * For a given ECLA class, give the full root path, from the root down to
     * the current class
     */
    /*public List<String> giveECLARootPath(String eclaClass) {
        // This is done via the ECLA tree index in ElasticSearch
        String query = queryECLA.replace("%CLASS%", eclaClass);
        List<String> result = new ArrayList<String>();
        HttpURLConnection connection = null;
        InputStream is = null;
        try {
            // we send a post request to elasticsearch to get the root path
            URL url = new URL("http://" + IndexProperties + ":" + elasticsearch_port + "/" + eclaTreeIndex + "/_search");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(query);
            wr.flush();
            wr.close();

            is = connection.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() == 0) {
                    continue;
                }
                // we put here, so we block if there is no space to add
                int ind = line.indexOf("ECLARoot");
                if (ind != -1) {
                    int ind2 = line.indexOf(" ", ind + 1);
                    if (ind2 == -1) {
                        continue;
                    }
                    int ind3 = line.indexOf("\"", ind2 + 1);
                    if (ind3 == -1) {
                        continue;
                    }
                    String subLine = line.substring(ind2, ind3);
                    StringTokenizer st = new StringTokenizer(subLine, " ");
                    while (st.hasMoreTokens()) {
                        result.add(st.nextToken());
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            //Closeables.closeQuietly(is);
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception e1) {
                    e1.printStackTrace();
                } finally {
                    connection = null;
                }
            }
            e.printStackTrace();
        }
        return result;
    }
*/
    /**
     * This method provides a basic handling of the ECLA classes expressed in a
     * compact way with a "+" symbol. This might over-generate the possible ECLA
     * classes, so a filter based on the possible valid ECLA classes is
     * necessary.
     */
   /* public List<String> normalizeECLAClass(String classs) {
        List<String> eclas = new ArrayList<String>();
        if (classs.indexOf('+') != -1) {
            StringTokenizer st = new StringTokenizer(classs, "+");
            String firstClass = null;
            while (st.hasMoreTokens()) {
                String clas = st.nextToken();
                if (firstClass == null) {
                    firstClass = clas;
                    eclas.add(firstClass);
                    //break;
                } else {
                    int length = clas.length();
                    if (length < 7) {
                        if (!eclas.contains(firstClass + clas)) {
                            eclas.add(firstClass + clas);
                        }
                        int index = clas.indexOf('/');
                        if (index != -1) {
                            eclas.add(firstClass.substring(0, index + 1) + clas);
                        }
                        clas = firstClass.substring(0, firstClass.length() - length) + clas;
                    }

                    if (!eclas.contains(clas)) {
                        eclas.add(clas);
                    }
                }
            }
        } else {
            eclas.add(classs);
        }
        return eclas;
    }
*/
    private JsonNode getStandoff(ObjectMapper mapper, String filename) throws Exception {
        JsonNode standoffNode = null;
        //System.out.println("filename: " + filename);
        String annotation = mm.getAnnotations(filename);
        //System.out.println(annotation);
        if ((annotation != null) && (annotation.trim().length() > 0)) {
            JsonNode jsonAnnotation = mapper.readTree(annotation);
//System.out.println(jsonAnnotation.toString());
            Iterator<JsonNode> iter0 = jsonAnnotation.getElements();
            JsonNode annotNode = mapper.createArrayNode();
            int n = 0;
            while (iter0.hasNext() && (n < 3)) {
                JsonNode jsonLocalAnnotation = (JsonNode) iter0.next();

                // we only get the concept IDs and the nerd confidence score
                JsonNode nerd = jsonLocalAnnotation.findPath("nerd");
                JsonNode entities = nerd.findPath("entities");
                if ((entities != null) && (!entities.isMissingNode())) {
                    Iterator<JsonNode> iter = entities.getElements();

                    while (iter.hasNext()) {
                        JsonNode piece = (JsonNode) iter.next();

                        JsonNode nerd_scoreN = piece.findValue("nerd_score");
                        JsonNode preferredTermN = piece.findValue("preferredTerm");
                        JsonNode wikipediaExternalRefN = piece.findValue("wikipediaExternalRef");
                        JsonNode freeBaseExternalRefN = piece.findValue("freeBaseExternalRef");

                        String nerd_score = nerd_scoreN.getTextValue();
                        String wikipediaExternalRef = wikipediaExternalRefN.getTextValue();
                        String preferredTerm = preferredTermN.getTextValue();
                        String freeBaseExternalRef = null;
                        if ((freeBaseExternalRefN != null) && (!freeBaseExternalRefN.isMissingNode())) {
                            freeBaseExternalRef = freeBaseExternalRefN.getTextValue();
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
                    }
                }
                n++;
            }
            standoffNode = mapper.createObjectNode();
            ((ObjectNode) standoffNode).put("$standoff", annotNode);
        } else {
            // if we don't have annotations for the file, we skip it!
            standoffNode = null;
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
                                    Iterator<JsonNode> ite = ((ArrayNode) date).getElements();
                                    if (ite.hasNext()) {
                                        JsonNode dateVal = (JsonNode) ite.next();
                                        String dateStr = dateVal.getTextValue();
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
                                Iterator<JsonNode> ite = ((ArrayNode) date).getElements();
                                if (ite.hasNext()) {
                                    JsonNode dateVal = (JsonNode) ite.next();
                                    String dateStr = dateVal.getTextValue();
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

    private void addFullName(JsonNode theChild, ObjectMapper mapper) {
        String fullName = null;
        if (theChild.isArray()) {
            String forename = null;
            String surname = null;
            Iterator<JsonNode> ite = theChild.getElements();
            while (ite.hasNext()) {
                JsonNode temp = ite.next();
                if (temp.isObject()) {
                    Iterator<String> subfields = ((ObjectNode) temp).getFieldNames();

                    while (subfields.hasNext()) {
                        String subfield = subfields.next();
                        if (subfield.equals("$forename")) {
                            // get the text value of the array
                            Iterator<JsonNode> ite2 = temp.path(subfield).getElements();
                            while (ite2.hasNext()) {
                                JsonNode temp2 = ite2.next();

                                if (forename != null) {
                                    forename += " " + temp2.getTextValue();
                                } else {
                                    forename = temp2.getTextValue();
                                }
                                break;
                            }
                        } else if (subfield.equals("$surname")) {
                            // get the text value of the array
                            Iterator<JsonNode> ite2 = temp.path(subfield).getElements();
                            while (ite2.hasNext()) {
                                JsonNode temp2 = ite2.next();
                                surname = temp2.getTextValue();
                                break;
                            }
                        }
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
    }

}
