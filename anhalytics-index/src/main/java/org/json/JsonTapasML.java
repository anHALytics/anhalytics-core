package org.json;



/*
Derived from JSON.org reference implementation:

Copyright (c) 2008 JSON.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

/* 
By reusing and modifying this software, we assumed that we are not using it
for Evil, at least from the point of view of the European standards.
*/

import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONML;
import org.json.XMLTokener;


/**
 * This provides static methods to convert an XML text into a JSONArray or
 * JSONObject, and to covert a JSONArray or JSONObject into an XML text using
 * the JsonML transform.
 * @author JSON.org
 * @version 2011-11-24
 */
public class JsonTapasML extends JSONML {

    /**
     * Parse XML values and store them in a JSONArray.
     * @param x       The XMLTokener containing the source string.
     * @param arrayForm true if array form, false if object form.
     * @param ja      The JSONArray that is containing the current tag or null
     *     if we are at the outermost level.
     * @return A JSONArray if the value is the outermost tag, otherwise null.
     * @throws JSONException
     */
    private static Object parse(
        XMLTokener x,
        boolean    arrayForm,
        JSONArray  ja
    ) throws JSONException {
        String     attribute;
        char       c;
        String       closeTag = null;
        int        i;
        JSONArray  newja = null;
        JSONObject newjo = null;
        Object     token;
        String     tagName = null;
//JPR   variable to store the name of the parent element for introducing in the childNodes element
//      this is made in order to avoid that childNodes from diferent parent elements can be interpreted
//      by the ES as the same childNodes, which would raise an error when indexing in case of being
//      of different types
        String      tagName_tapas = null;  //JPR
// Test for and skip past these forms:
//      <!-- ... -->
//      <![  ... ]]>
//      <!   ...   >
//      <?   ...  ?>

        while (true) {
            if (!x.more()) {
                throw x.syntaxError("Bad XML");
            }
            token = x.nextContent();
            if (token == XML.LT) {
                token = x.nextToken();
                if (token instanceof Character) {
                    if (token == XML.SLASH) {

// Close tag </

                        token = x.nextToken();
                        if (!(token instanceof String)) {
                            throw new JSONException(
                                    "Expected a closing name instead of '" +
                                    token + "'.");
                        }
                        if (x.nextToken() != XML.GT) {
                            throw x.syntaxError("Misshaped close tag");
                        }
                        return token;
                    } else if (token == XML.BANG) {

// <!

                        c = x.next();
                        if (c == '-') {
                            if (x.next() == '-') {
                                x.skipPast("-->");
                            }
                            x.back();
                        } else if (c == '[') {
                            token = x.nextToken();
                            if (token.equals("CDATA") && x.next() == '[') {
                                if (ja != null) {
                                    ja.put(x.nextCDATA());
                                }
                            } else {
                                throw x.syntaxError("Expected 'CDATA['");
                            }
                        } else {
                            i = 1;
                            do {
                                token = x.nextMeta();
                                if (token == null) {
                                    throw x.syntaxError("Missing '>' after '<!'.");
                                } else if (token == XML.LT) {
                                    i += 1;
                                } else if (token == XML.GT) {
                                    i -= 1;
                                }
                            } while (i > 0);
                        }
                    } else if (token == XML.QUEST) {

// <?

                        x.skipPast("?>");
                    } else {
                        throw x.syntaxError("Misshaped tag");
                    }

// Open tag <

                } else {
                    if (!(token instanceof String)) {
                        throw x.syntaxError("Bad tagName '" + token + "'.");
                    }
                    tagName = (String)token;
                    newja = new JSONArray();
                    newjo = new JSONObject();
                    if (arrayForm) {
                        newja.put(tagName); // we will remove it if there is at least one child
                        if (ja != null) {
                            ja.put(newja); 
                        }
                    } else {
                        newjo.put("tagName", tagName); // we will remove it if there is at least one child
                        if (ja != null) {
                            ja.put(newjo); 
                        }
                    }
                    tagName_tapas = tagName;    //JPR
                    token = null;
                    for (;;) {
                        if (token == null) {
                            token = x.nextToken();
                        }
                        if (token == null) {
                            throw x.syntaxError("Misshaped tag");
                        }
                        if (!(token instanceof String)) {
                            break;
                        }

// attribute = value

                        attribute = (String)token;
//JPR - because the childNodes field has changed to childNodes_parentElementName, I am checking 
//      only if the XML element begins with "childNodes_" 
//PL childNodes_parentElementName is simplified to $parentElementName
//JPR                        if (!arrayForm && ("tagName".equals(attribute) || "childNode".equals(attribute))) {
                        if (!arrayForm && ("tagName".equals(attribute) || attribute.startsWith("$"))) {  //JPR // PL

                            throw x.syntaxError("Reserved attribute.");
                        }
                        token = x.nextToken();
                        if (token == XML.EQ) {
                            token = x.nextToken();
                            if (!(token instanceof String)) {
                                throw x.syntaxError("Missing value");
                            }
                        //newjo.accumulate(attribute, XML.stringToValue((String)token));
						newjo.accumulate(attribute, (String)token); // PL only string please!
                        token = null;
                        } else {
                            newjo.accumulate(attribute, "");
                        }
                    }
                    if (arrayForm && newjo.length() > 0) {
                        newja.put(newjo);
                    }

// Empty tag <.../>

                    if (token == XML.SLASH) {
                        if (x.nextToken() != XML.GT) {
                            throw x.syntaxError("Misshaped tag");
                        }
                        if (ja == null) {
                            if (arrayForm) {
                                return newja;
                            } else {
                                return newjo;
                            }
                        }

// Content, between <...> and </...>

                    } else {
                        if (token != XML.GT) {
                            throw x.syntaxError("Misshaped tag");
                        }
                        closeTag = (String)parse(x, arrayForm, newja);
                        if (closeTag != null) {
                            if (!closeTag.equals(tagName)) {
                                throw x.syntaxError("Mismatched '" + tagName +
                                        "' and '" + closeTag + "'");
                            }
                            //tagName = null; //PL
                            if (!arrayForm && newja.length() > 0) {
//JPR                            newjo.put("childNodes", newja);
//JPR - line changed in order to diferentiate the multiple childNodes in order to allow indexing by ElasticSearch
//      now the childNodes name carries also the name of the parent element   
//PL childNodes string is also removed with simply $parentElementName
								newjo.remove("tagName"); //PL
                                newjo.put("$" + tagName_tapas, newja);  //JPR
								
                            }
							tagName = null; //PL
                            if (ja == null) {
                                if (arrayForm) {
                                    return newja;
                                } else {
                                    return newjo;
                                }
                            }
                        }
                    }
                }
            } else {
                if (ja != null) {
                    ja.put(token instanceof String
//JPR - forcing to store ALL the elements of the XML in String format in order to avoid errors
//      in ES, because if one time the element is "2345" and another time is "BA2345", ES would raise an error                    
//JPR               ? XML.stringToValue((String)token)
                    ? (String)token //JPR
                    : token);
                }
            }
        }
    }


    /**
     * Convert a well-formed (but not necessarily valid) XML string into a
     * JSONArray using the JsonML transform. Each XML tag is represented as
     * a JSONArray in which the first element is the tag name. If the tag has
     * attributes, then the second element will be JSONObject containing the
     * name/value pairs. If the tag contains children, then strings and
     * JSONArrays will represent the child tags.
     * Comments, prologs, DTDs, and <code>&lt;[ [ ]]></code> are ignored.
     * @param string The source string.
     * @return A JSONArray containing the structured data from the XML string.
     * @throws JSONException
     */
    public static org.json.JSONArray toJSONArray(String string) throws JSONException {
        return toJSONArray(new XMLTokener(string));
    }


    /**
     * Convert a well-formed (but not necessarily valid) XML string into a
     * JSONArray using the JsonML transform. Each XML tag is represented as
     * a JSONArray in which the first element is the tag name. If the tag has
     * attributes, then the second element will be JSONObject containing the
     * name/value pairs. If the tag contains children, then strings and
     * JSONArrays will represent the child content and tags.
     * Comments, prologs, DTDs, and <code>&lt;[ [ ]]></code> are ignored.
     * @param x An XMLTokener.
     * @return A JSONArray containing the structured data from the XML string.
     * @throws JSONException
     */
    public static org.json.JSONArray toJSONArray(XMLTokener x) throws JSONException {
        return (org.json.JSONArray)parse(x, true, null);
    }


    /**
     * Convert a well-formed (but not necessarily valid) XML string into a
     * JSONObject using the JsonML transform. Each XML tag is represented as
     * a JSONObject with a "tagName" property. If the tag has attributes, then
     * the attributes will be in the JSONObject as properties. If the tag
     * contains children, the object will have a "childNodes" property which
     * will be an array of strings and JsonML JSONObjects.

     * Comments, prologs, DTDs, and <code>&lt;[ [ ]]></code> are ignored.
     * @param x An XMLTokener of the XML source text.
     * @return A JSONObject containing the structured data from the XML string.
     * @throws JSONException
     */
    public static JSONObject toJSONObject(XMLTokener x) throws JSONException {
        return (JSONObject)parse(x, false, null);
    }

	/**
	 *  PL: return 0 if the depth of an element in the array can be 0 (e.g. it contains a text node)
	 */
	private static int expandLangAttributesArray(JSONArray obj) {
		int depth = -1;
		try {
			for(int i = 0; i < obj.length(); i++) {
				Object obj2 = obj.get(i);
				if (obj2 instanceof JSONArray) {
					expandLangAttributesArray(((JSONArray)obj2));
				}
				else if (obj2 instanceof JSONObject) {
					expandLangAttributes(((JSONObject)obj2));
				}
				else if (obj2 instanceof String) {
					depth = 0;
				}
			}
		}
		catch(Exception e) {}
		return depth;
	}
	
	/**
	 *  PL: pre-expand text nodes with the language attribute value
	 *  { $toto = ["text"], xml:lang = "en" }  becomes { $toto = [$lang-en = ["text"]], xml:lang = "en" } 
	 */
	private static JSONObject expandLangAttributes(JSONObject obj) {
		try {
			Object obj1 = null;
			try {
			 	obj1 = obj.get("xml:lang");
			}
			catch(Exception e) {}
			if (obj1 != null) {
				// test if we are on a leaf
				boolean isLeaf = false;
				String toMove = null;
				JSONArray keys = obj.names();
				for(int i = 0; i < keys.length(); i++) {
					String key = null;
					try {
						key = (String)keys.get(i);
						if ( (key != null) && (key.startsWith("$")) ) {
							//isLeaf = false;
							Object obj2 = null;
							try { 
								int depth = -1;
							 	obj2 = obj.get(key);
								if (obj2 != null) {
									if (obj2 instanceof JSONArray) {
										depth = expandLangAttributesArray(((JSONArray)obj2));
									}
									else if (obj2 instanceof JSONObject) {
										expandLangAttributes(((JSONObject)obj2));
									}
									if (depth == 0) {
										isLeaf = true;
										toMove = key;
									}
								}
							}
							catch(Exception e) {
								System.out.println("issue0 with key: " + key);
							}
						}
					}
					catch(Exception e) {
						System.out.println("issue1 with key: " + key);
					}
				} 
			
				if (isLeaf) {
					// we expand with the language code
					String langValue = ((String)obj1);
					if (toMove != null) {
						JSONArray arr = new JSONArray();
						JSONArray children = (JSONArray)obj.get(toMove);
						for(int i = 0; i <children.length(); i++) {
							Object obb = children.get(i);
							if (obb instanceof String) {
								JSONObject text = new JSONObject();
								JSONArray container = new JSONArray();
								container.put(obb);
								text.put("$lang-" + langValue, container);
								arr.put(text);
							}
							else {
								arr.put(obb);
							}
						}

						obj.remove(toMove);
						obj.put(toMove, arr);
						//obj.remove("xml:lang");
					}
				}
			}
			else {
				JSONArray keys = obj.names();
				for(int i = 0; i < keys.length(); i++) {
					String key = null;
					try {
						key = (String)keys.get(i);
						if ( (key != null) && (key.startsWith("$")) ) {
							Object obj2 = null;
							try { 
							 	obj2 = obj.get(key);
								if (obj2 != null) {
									if (obj2 instanceof JSONArray) {
										expandLangAttributesArray(((JSONArray)obj2));
									}
									else if (obj2 instanceof JSONObject) {
										expandLangAttributes(((JSONObject)obj2));
									}
								}
							}
							catch(Exception e) {
								System.out.println("issue3 with key: " + key);
							}
						}
					}
					catch(Exception e) {
						System.out.println("issue2 with key: " + key);
					}
				}
			}
		}
		catch(Exception e) {
			System.out.println("problem...");
		}
		return obj;
	}


    /**
     * Convert a well-formed (but not necessarily valid) XML string into a
     * JSONObject using the JsonML transform. Each XML tag is represented as
     * a JSONObject with a "tagName" property. If the tag has attributes, then
     * the attributes will be in the JSONObject as properties. If the tag
     * contains children, the object will have a "childNodes" property which
     * will be an array of strings and JsonML JSONObjects.

     * Comments, prologs, DTDs, and <code>&lt;[ [ ]]></code> are ignored.
     * @param string The XML source text.
     * @return A JSONObject containing the structured data from the XML string.
     * @throws JSONException
     */
    public static JSONObject toJSONObject(String string) throws JSONException {
		JSONObject result = toJSONObject(new XMLTokener(string));
		// PL expand the leaf nodes with xml:lang attribute value - comment or uncomment to include it 
		//result = expandLangAttributes(result);
        return result;
    }


    /**
     * Reverse the JSONML transformation, making an XML text from a JSONArray.
     * @param ja A JSONArray.
     * @return An XML string.
     * @throws JSONException
     */
    public static String toString(JSONArray ja) throws JSONException {
        int          i;
        JSONObject   jo;
        String       key;
        Iterator     keys;
        int          length;
        Object       object;
        StringBuffer sb = new StringBuffer();
        String       tagName;
        String       value;

// Emit <tagName

        tagName = ja.getString(0);
        XML.noSpace(tagName);
        tagName = XML.escape(tagName);
        sb.append('<');
        sb.append(tagName);

        object = ja.opt(1);
        if (object instanceof JSONObject) {
            i = 2;
            jo = (JSONObject)object;

// Emit the attributes

            keys = jo.keys();
            while (keys.hasNext()) {
                key = keys.next().toString();
                XML.noSpace(key);
                value = jo.optString(key);
                if (value != null) {
                    sb.append(' ');
                    sb.append(XML.escape(key));
                    sb.append('=');
                    sb.append('"');
                    sb.append(XML.escape(value));
                    sb.append('"');
                }
            }
        } else {
            i = 1;
        }

//Emit content in body

        length = ja.length();
        if (i >= length) {
            sb.append('/');
            sb.append('>');
        } else {
            sb.append('>');
            do {
                object = ja.get(i);
                i += 1;
                if (object != null) {
                    if (object instanceof String) {
                        sb.append(XML.escape(object.toString()));
                    } else if (object instanceof JSONObject) {
                        sb.append(toString((JSONObject)object));
                    } else if (object instanceof JSONArray) {
                        sb.append(toString((JSONArray)object));
                    }
                }
            } while (i < length);
            sb.append('<');
            sb.append('/');
            sb.append(tagName);
            sb.append('>');
        }
        return sb.toString();
    }

    /**
     * Reverse the JSONML transformation, making an XML text from a JSONObject.
     * The JSONObject must contain a "tagName" property. If it has children,
     * then it must have a "childNodes" property containing an array of objects.
     * The other properties are attributes with string values.
     * @param jo A JSONObject.
     * @return An XML string.
     * @throws JSONException
     */
    public static String toString(JSONObject jo) throws JSONException {
        StringBuffer sb = new StringBuffer();
		StringBuffer sb2 = null; // PL
        int          i;
        JSONArray    ja;
        String       key;
        Iterator     keys;
        int          length;
        Object       object;
        String       tagName;
        String       value;
		String       tagName_tapas = null;  //PL
		boolean	 	 langExpansion = false; // PL 
//Emit <tagName

        tagName = jo.optString("tagName");
        //if (tagName == null) { //PL 
            //return XML.escape(jo.toString()); //PL no tagname is normal
			//PL we will get the tag name as "name" of the childNodes	
        //} //PL 

		if (tagName != null) { //PL
			if (tagName.length()>0) { //PL
				//PL we have an element with no child
				XML.noSpace(tagName); //PL
		        tagName = XML.escape(tagName); //PL
		        sb.append('<'); //PL
		        sb.append(tagName); //PL
			} //PL
			else { //PL
				tagName = null; //PL
			}
		}
		
		if (tagName == null)  {
			sb2 = new StringBuffer();
		}
        //XML.noSpace(tagName); //PL
        //tagName = XML.escape(tagName); //PL
        //sb.append('<'); //PL
        //sb.append(tagName); //PL

//Emit the attributes
        keys = jo.keys();
        ja = null; //JPR
        while (keys.hasNext()) {
            key = keys.next().toString();
//JPR - because the names of the childNodes element has changed to childNodes_namePatentElement
//      we have to check for th field values with keys from "childNodes_parentElementName" instead of "childNodes"  
//JPR       if (!"tagName".equals(key) && !"childNodes".equals(key)) {
            if (!"tagName".equals(key) && !key.startsWith("$")) { //JPR
				// PL: we accumulate here the attribute
                XML.noSpace(key);
                value = jo.optString(key);
                if (value != null) {
					if ( (tagName != null) || (sb2 == null) ) { //PL
						//PL we have an element without childnodes tagName != null
						//PL or we have already written the element tag (sb2 is put to null)
                    	sb.append(' ');
	                    sb.append(XML.escape(key));
	                    sb.append('=');
	                    sb.append('"');
	                    sb.append(XML.escape(value));
	                    sb.append('"');
					}
					else {
						//PL this will be copied to the tagName coming from the $parentElementName
						sb2.append(' '); //PL
	                    sb2.append(XML.escape(key)); //PL
	                    sb2.append('='); //PL
	                    sb2.append('"'); //PL
	                    sb2.append(XML.escape(value)); //PL
	                    sb2.append('"'); //PL
					}
                }
            }
//JPR - because the names of the childNodes element has changed to childNodes_namePatentElement
//      we have to check for th field values with keys from "childNodes_parentElementName" instead of "childNodes"  
//PL - childNodes element has finally changed to _namePatentElement             
            if (key.startsWith("$")){ //PL
				if (key.startsWith("$lang-")) { //PL
					// we have an expanded text node
					// we need to take the text
					//langExpansion = true;//PL uncomment if lang expansion should be done here
				} //PL
	
                ja = jo.optJSONArray(key.toString());
				tagName_tapas = key.substring(1, key.length()); //PL
				
				XML.noSpace(tagName_tapas); //PL
		        tagName_tapas = XML.escape(tagName_tapas); //PL
				if (!langExpansion) {
		        	sb.append('<'); //PL
		        	sb.append(tagName_tapas); //PL
					sb.append(sb2.toString()); //PL
				}
				sb2 = null;
            }
//JPR-end
// but PL continue!
        }
//Emit content in body
//JPR        ja = jo.optJSONArray("-");
        //if (ja == null) {
		if ((tagName != null) && (!langExpansion)) { //PL
            sb.append('/');
            sb.append('>');
        } else {
			if (!langExpansion) { //PL
            	sb.append('>'); //PL
			} //PL
			if (ja != null) { //PL
	            for (i = 0; i < ja.length(); i++) {
	                object = ja.get(i);
	                if (object != null) {
	                    if (object instanceof String) {
	                        sb.append(XML.escape(object.toString()));
	                    } else if (object instanceof JSONObject) {
	                        sb.append(toString((JSONObject)object));
	                    } else if (object instanceof JSONArray) {
	                        sb.append(toString((JSONArray)object));
	                    } else {
	                        sb.append(object.toString());
	                    }
	                }
	            }
			}
			if (!langExpansion) { //PL
            	sb.append('<');
            	sb.append('/');
            	//sb.append(tagName); //PL
				sb.append(tagName_tapas); //PL
            	sb.append('>');
			}
        }
        return sb.toString();
    }
	
}