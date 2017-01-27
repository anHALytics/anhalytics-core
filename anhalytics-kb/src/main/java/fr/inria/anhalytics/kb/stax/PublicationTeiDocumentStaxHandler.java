package fr.inria.anhalytics.kb.stax;

import fr.inria.anhalytics.commons.entities.*;
import fr.inria.anhalytics.commons.utilities.Utilities;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.stax2.XMLStreamReader2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * Created by lfoppiano on 29/08/16.
 */
public class PublicationTeiDocumentStaxHandler implements StaxParserContentHandler {
    private static Logger LOGGER = LoggerFactory.getLogger(PublicationTeiDocumentStaxHandler.class);

    private Publication publication;
    private Monograph monogr;
    private Collection collection;
    private Journal journal;
    private Serial_Identifier serialIdentifier;
    private Conference_Event conferenceEvent;
    private Address address;
    private In_Serial inSerial;

    private List<Serial_Identifier> serialIdentifiers = new ArrayList<>();

    private boolean inTitle = false;

    private boolean inSubmissionDate = false;
    private boolean inClassCodeType = false;
    private boolean inClassCodeDomain = false;
    private boolean inLanguage = false;

    //Monogr
    private boolean inMonogr = false;
    private boolean inMonogrIdno = false;
    private boolean inMonogrTitle = false;
    private boolean inMonogrMeeting = false;
    private boolean inMonogrMeetingTitle = false;
    private boolean inMonogrMeetingDate = false;
    private boolean inMonogrMeetingSettlement = false;
    private boolean inMonogrMeetingRegion = false;
    private boolean inMonogrImprint = false;
    private boolean inMonogrImprintPublisher = false;
    private boolean inMonogrImprintDate = false;
    private boolean inMonogrImprintBiblScope = false;


    private String monogrTitleType;
    private String monogrIdnoType;
    private String monographMeetingDateType;
    private String monographImprintBiblScopeUnit;
    private String monographImprintDateType;
    private String monographImprintDateWhen;


    private int indentLevel = 0;

    StackTags stackTags = new StackTags();
    private StaxTag title = new StaxTag("title", "/teiCorpus/teiHeader/fileDesc/titleStmt/title");

    private StaxTag submittedDate = new StaxTag("date", "/teiCorpus/teiHeader/fileDesc/editionStmt/edition/date");
    private StaxTag classCode = new StaxTag("classCode", "/teiCorpus/teiHeader/profileDesc/textClass/classCode");
    private StaxTag language = new StaxTag("language", "/teiCorpus/teiHeader/profileDesc/langUsage/language");
    private StaxTag monograph = new StaxTag("monogr", "/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr");
    private StaxTag monographIdno = new StaxTag("idno", "/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr/idno");
    private StaxTag monographTitle = new StaxTag("title", "/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr/title");
    private StaxTag monographMeeting = new StaxTag("meeting", "/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr/meeting");
    private StaxTag monographMeetingTitle = new StaxTag("title", "/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr/meeting/title");
    private StaxTag monographMeetingDate = new StaxTag("date", "/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr/meeting/date");
    private StaxTag monographMeetingSettlement = new StaxTag("settlement", "/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr/meeting/settlement");
    private StaxTag monographMeetingCountry = new StaxTag("country", "/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr/meeting/country");
    private StaxTag monographMeetingRegion = new StaxTag("region", "/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr/meeting/region");
    private StaxTag monographImprint = new StaxTag("imprint", "/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr/imprint");
    private StaxTag monographImprintPublisher = new StaxTag("publisher", "/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr/imprint/publisher");
    private StaxTag monographImprintDate = new StaxTag("date", "/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr/imprint/date");
    private StaxTag monographImprintBiblScope = new StaxTag("biblScope", "/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr/imprint/biblScope");

    public PublicationTeiDocumentStaxHandler() {
        publication = new Publication();
        collection = new Collection();
        journal = new Journal();
        address = new Address();
        inSerial = new In_Serial();
    }


    public PublicationTeiDocumentStaxHandler(Publication publication) {
        this();
        this.publication = publication;
    }

    @Override
    public void onStartDocument(XMLStreamReader2 reader) {
    }

    @Override
    public void onEndDocument(XMLStreamReader2 reader) {
        if (indentLevel > 0) System.out.println("something is baroken");

        if (conferenceEvent != null) {
            conferenceEvent.setMongoraph(monogr);
        }
    }

    @Override
    public void onStartElement(XMLStreamReader2 reader) {
        final String localName = reader.getName().getLocalPart();
        stackTags.append(localName);
        final StaxTag currentTag = new StaxTag(localName, stackTags.toString());

//        System.out.println(currentTag);

        if (title.equals(currentTag)) {
            inTitle = true;
        } else if (submittedDate.equals(currentTag)) {
            for (int i = 0; i < reader.getAttributeCount(); i++) {
                if ("type".equals(reader.getAttributeLocalName(i)) &&
                        "whenSubmitted".equals(reader.getAttributeValue(i))) {
                    inSubmissionDate = true;
                    break;
                }
            }
        } else if (language.equals(currentTag)) {
            inLanguage = true;
        } else if (classCode.equals(currentTag)) {
            for (int i = 0; i < reader.getAttributeCount(); i++) {
                if ("scheme".equals(reader.getAttributeLocalName(i)) &&
                        "halTypology".equals(reader.getAttributeValue(i))) {
                    inClassCodeType = true;
                    break;
                } else if ("scheme".equals(reader.getAttributeLocalName(i)) &&
                        "halDomain".equals(reader.getAttributeValue(i))) {
                    inClassCodeDomain = true;
                    break;
                }

            }
        } else if (monograph.equals(currentTag)) {
            inMonogr = true;
            monogr = new Monograph();
            publication.setMonograph(monogr);
        } else if (monographIdno.equals(currentTag)) {
            serialIdentifier = new Serial_Identifier();
            inMonogrIdno = true;
            monogrIdnoType = getAttributeValue(reader, "m");
        } else if (monographTitle.equals(currentTag)) {
            inMonogrTitle = true;
            monogrTitleType = getAttributeValue(reader, "level");
        } else if (monographMeeting.equals(currentTag)) {
            inMonogrMeeting = true;
            conferenceEvent = new Conference_Event();
            conferenceEvent.setConference(new Conference());
        } else if (monographMeetingTitle.equals(currentTag)) {
            inMonogrMeetingTitle = true;
        } else if (monographMeetingDate.equals(currentTag)) {
            inMonogrMeetingDate = true;
            monographMeetingDateType = getAttributeValue(reader, "type");
        } else if (monographMeetingSettlement.equals(currentTag)) {
            inMonogrMeetingSettlement = true;
        } else if (monographMeetingCountry.equals(currentTag)) {
            String countryISO = getAttributeValue(reader, "key");
            Country country = new Country();
            if (isNotEmpty(countryISO)) {
                country.setIso(countryISO);
            }
            address.setCountry(country);
        } else if (monographMeetingRegion.equals(currentTag)) {
            inMonogrMeetingRegion = true;
        } else if (monographImprint.equals(currentTag)) {
            inMonogrImprint = true;
        } else if (monographImprintPublisher.equals(currentTag)) {
            inMonogrImprintPublisher = true;
        } else if (monographImprintDate.equals(currentTag)) {
            monographImprintDateType = getAttributeValue(reader, "type");
            monographImprintDateWhen = getAttributeValue(reader, "when");
            inMonogrImprintDate = true;
        } else if (monographImprintBiblScope.equals(currentTag)) {
            monographImprintBiblScopeUnit = getAttributeValue(reader, "unit");
            inMonogrImprintBiblScope = true;
        }

        indentLevel++;

    }

    @Override
    public void onEndElement(XMLStreamReader2 reader) {
        final String localName = reader.getName().getLocalPart();
        final StaxTag currentTag = new StaxTag(localName, stackTags.toString());

        if (title.equals(currentTag)) {
            inTitle = false;
        } else if (submittedDate.equals(currentTag)) {
            inSubmissionDate = false;
        } else if (language.equals(currentTag)) {
            inLanguage = false;
        } else if (classCode.equals(currentTag)) {
            if (inClassCodeDomain)
                inClassCodeDomain = false;
            else if (inClassCodeType)
                inClassCodeType = false;
        } else if (monograph.equals(currentTag)) {
            inMonogr = false;
        } else if (monographIdno.equals(currentTag)) {
            inMonogrIdno = false;
            serialIdentifiers.add(serialIdentifier);
        } else if (monographTitle.equals(currentTag)) {
            inMonogrTitle = false;
        } else if (monographMeeting.equals(currentTag)) {
            inMonogrMeeting = false;
        } else if (monographMeetingTitle.equals(currentTag)) {
            inMonogrMeetingTitle = false;
        } else if (monographMeetingDate.equals(currentTag)) {
            inMonogrMeetingDate = false;
        } else if (monographMeetingSettlement.equals(currentTag)) {
            inMonogrMeetingSettlement = false;
            conferenceEvent.setAddress(address);
        } else if (monographMeetingRegion.equals(currentTag)) {
            inMonogrMeetingRegion = false;
            conferenceEvent.setAddress(address);
        } else if (monographMeetingCountry.equals(currentTag)) {
            conferenceEvent.setAddress(address);

        } else if (monographImprint.equals(currentTag)) {
            inMonogrImprint = false;
        } else if (monographImprintPublisher.equals(currentTag)) {
            inMonogrImprintPublisher = false;
        } else if (monographImprintDate.equals(currentTag)) {
            inMonogrImprintDate = false;
        } else if (monographImprintBiblScope.equals(currentTag)) {
            inMonogrImprintBiblScope = false;
        }

        stackTags.peek();
        indentLevel--;
    }

    @Override
    public void onCharacter(XMLStreamReader2 reader) {
        String text = getText(reader);
        if (isEmpty(text)) {
            return;
        }

        if (inTitle) {
            publication.setDoc_title(text);
        } else if (inSubmissionDate) {
            publication.setDate_eletronic(text);

            try {
                final Date date_printed = Utilities.parseStringDate(text);
                publication.setDate_printed(date_printed);
            } catch (ParseException e) {
                LOGGER.warn("Cannot parse date " + text, e);
            }

        } else if (inLanguage) {
            publication.setLanguage(text);
        } else if (inClassCodeType) {
            publication.setType(text);
        } else if (inMonogr) {
            if (inMonogrIdno) {
                serialIdentifier = new Serial_Identifier();
                serialIdentifier.setId(text);
                serialIdentifier.setType(monogrIdnoType);
                serialIdentifier.setCollection(collection);
                serialIdentifier.setJournal(journal);
            } else if (inMonogrTitle) {
                monogr.setTitle(text);
                monogr.setType(monogrTitleType);
                if ("j".equals(monogrTitleType)) {
                    journal.setTitle(text);
                } else {
                    collection.setTitle(text);
                }

            } else if (inMonogrMeeting) {

                if (inMonogrMeetingTitle) {
                    conferenceEvent.getConference().setTitle(text);

                } else if (inMonogrMeetingDate) {
                    if ("start".equals(monographMeetingDateType)) {
                        conferenceEvent.setStart_date(text);
                    } else if ("end".equals(monographMeetingDateType)) {
                        conferenceEvent.setEnd_date(text);
                    }
                } else if (inMonogrMeetingSettlement) {
                    address.setSettlement(text);
                } else if (inMonogrMeetingRegion) {
                    address.setRegion(text);
                }

            } else if (inMonogrImprint) {

                if (inMonogrImprintBiblScope) {
                    switch (monographImprintBiblScopeUnit) {
                        case "serie":
                            collection.setTitle(text);
                            if (journal.getTitle().isEmpty()) {
                                journal.setTitle(text);
                            }
                            break;
                        case "volume":
                            inSerial.setVolume(text);
                            break;
                        case "issue":
                            inSerial.setIssue(text);
                            break;
                        case "pp":
                            String[] pages = extractPageNumbers(text);

                            if (pages.length > 0) {
                                publication.setStart_page(pages[0]);
                                if (pages.length > 1) {
                                    publication.setEnd_page(pages[1]);
                                }
                            }

                            break;
                    }
                } else if (inMonogrImprintDate) {
                    //TODO: this part is not clear - (1) the date is overwritten!
                    //TODO: moreover if the when is not there, the process crash in the original code.
                    if ("datePub".equals(monographImprintDateType)) {
                        if (isNotEmpty(monographImprintDateWhen)) {
                            publication.setDate_eletronic(monographImprintDateWhen);
                            String completedDate = Utilities.completeDate(monographImprintDateWhen);
                            try {
                                publication.setDate_printed(Utilities.parseStringDate(completedDate));
                            } catch (ParseException e) {
                                LOGGER.warn("Cannot parse date " + completedDate, e);
                            }
                        } //else {
                        //  publication.setDate_eletronic(text);
                        //  String completedDate = Utilities.completeDate(text);
                        //  publication.setDate_printed(Utilities.parseStringDate(completedDate));
                        //}
                    }
                }
            }
        }
    }

    String[] extractPageNumbers(String text) {
        if (text.length() < 10) {
            if (text.contains("-") && text.length() > 3) {
                return text.split("-");

            } else {
                return new String[]{text};
            }
        }

        return new String[0];
    }

    private String getText(XMLStreamReader2 reader) {
        String text = reader.getText();
        text = trim(text);
        return text;
    }

    private String getAttributeValue(XMLStreamReader reader, String attributeName) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            if (attributeName.equals(reader.getAttributeLocalName(i))) {
                return reader.getAttributeValue(i);
            }
        }

        return "";
    }

    private String extractTagContent(XMLEventReader reader, XMLEventWriter writer) throws XMLStreamException {
        XMLEvent event = reader.nextEvent();
        String data = event.asCharacters().getData();
        data = data != null ? data.trim() : "";
        writer.add(event);
        return data;
    }

    public In_Serial getInSerial() {
        return inSerial;
    }

    /**
     * This class require a single parameter which is the input file.
     */
//    public static void main(String[] args) throws IOException, XMLStreamException {
//
//        if (args.length == 0) {
//            System.out.println("Missing input file. First parameter.");
//            System.exit(-1);
//        }
//
//        WstxInputFactory inputFactory = new WstxInputFactory();
//
//        Writer writer = new FileWriter(args[0] + ".output");
//        TeiDocumentStaxHandler teiDocumentStaxHandler = new TeiDocumentStaxHandler(writer);
//
//        InputStream is = new FileInputStream(args[0]);
//        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(is);
//
//        StaxUtils.traverse(reader, teiDocumentStaxHandler);
//
//        writer.close();
//    }


    protected class StaxTag {

        private String tagName;

        private String path;

        StaxTag(String tagName, String path) {
            this.tagName = tagName;
            this.path = path;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StaxTag staxTag = (StaxTag) o;

            if (!path.equals(staxTag.path)) {
                return false;
            }

            if (tagName != null ? !tagName.equals(staxTag.tagName) : staxTag.tagName != null) {
                return false;
            }

            return true;
        }

        @Override
        public String toString() {
            return String.format("%s, %s", tagName, path);
        }

    }

    private class StackTags {

        private List<String> stackTags = new ArrayList<>();

        public void append(String tag) {
            stackTags.add(tag);
        }

        public String peek() {
            return stackTags.remove(stackTags.size() - 1);
        }

        public String toString() {
            return "/" + StringUtils.join(stackTags, "/");
        }

    }

    public Publication getPublication() {
        return publication;
    }

    public Monograph getMonogr() {
        return monogr;
    }

    public Collection getCollection() {
        return collection;
    }

    public Journal getJournal() {
        return journal;
    }

    public List<Serial_Identifier> getSerialIdentifiers() {
        return serialIdentifiers;
    }

    public Conference_Event getConferenceEvent() {
        return conferenceEvent;
    }

    public Address getAddress() {
        return address;
    }
}
