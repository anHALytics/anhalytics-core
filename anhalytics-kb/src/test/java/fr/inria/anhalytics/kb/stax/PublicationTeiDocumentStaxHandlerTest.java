package fr.inria.anhalytics.kb.stax;

import com.ctc.wstx.stax.WstxInputFactory;
import fr.inria.anhalytics.commons.entities.*;
import fr.inria.anhalytics.commons.utilities.Utilities;
import org.codehaus.stax2.XMLStreamReader2;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by lfoppiano on 26/01/17.
 */
public class PublicationTeiDocumentStaxHandlerTest {

    PublicationTeiDocumentStaxHandler target;

    WstxInputFactory inputFactory = new WstxInputFactory();
    Publication publication;

    @Before
    public void setUp() {
        publication = new Publication();
        target = new PublicationTeiDocumentStaxHandler(publication);
    }

    @Test
    public void testParsingPublication_shouldWork() throws Exception {
        InputStream inputStream = this.getClass().getResourceAsStream("/hal-00576900.corpus.tei.xml");
        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(inputStream);

        StaxUtils.traverse(reader, target);

        assertThat(publication.getDoc_title(), is("Uncertainties of cultivated landscape drainage network mapping and its consequences on hydrological fluxes estimations"));
        assertThat(publication.getDate_eletronic(), is("2011-03-15 15:54:30"));
        assertThat(publication.getDate_printed(), is(Utilities.parseStringDate(publication.getDate_eletronic())));
        assertThat(publication.getLanguage(), is("English"));
        assertThat(publication.getType(), is("Conference papers"));
    }

    @Test
    public void testParsingMonograph_idno_shouldWork() throws Exception {
        InputStream inputStream = this.getClass().getResourceAsStream("/hal-00576900.corpus.tei.xml");
        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(inputStream);

        StaxUtils.traverse(reader, target);

        assertThat(publication.getMonograph(), notNullValue());

        Monograph monograph = publication.getMonograph();
        assertThat(monograph.getType(), is("m"));
        assertThat(monograph.getTitle(), is("Accuracy2010"));
        assertThat(target.getCollection().getTitle(), is("Accuracy2010"));
        assertThat(target.getJournal().getTitle(), is(""));

    }

    @Test
    public void testParsingMonogram_meeting_shouldWork() throws Exception {
        InputStream inputStream = this.getClass().getResourceAsStream("/hal-00576900.corpus.tei.xml");
        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(inputStream);

        StaxUtils.traverse(reader, target);

        final Conference_Event conferenceEvent = target.getConferenceEvent();
        assertThat(conferenceEvent, notNullValue());
        assertThat(conferenceEvent.getMonograph(), notNullValue());
        assertThat(conferenceEvent.getStart_date(), is("2010-07-20"));
        assertThat(conferenceEvent.getEnd_date(), is(""));
        assertThat(conferenceEvent.getConference().getTitle(), is("Accuracy2010"));

        final Address address = conferenceEvent.getAddress();
        assertThat(address.getSettlement(), is("Leicester"));
        assertThat(address, notNullValue());

        final Country country = address.getCountry();
        assertThat(country, notNullValue());
        assertThat(country.getIso(), is("GB"));
    }

    @Test
    public void testParsingMonogram_imprint_shouldWork() throws Exception {
        InputStream inputStream = this.getClass().getResourceAsStream("/hal-00576900.corpus.tei.xml");
        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(inputStream);

        StaxUtils.traverse(reader, target);
        //TODO: fix the test below, then uncomment this:
        //assertThat(target.getPublication().getFirst_page(), is("153"));
//        assertThat(target.getPublication().getLast_page(), is("157"));

        //TODO: the imprint is overwriting the publication date from the /editionStmt
//        assertThat(target.getPublication().getDate_eletronic(), is(""));
    }

    @Test
    public void testParsingMonogram_meeting2_shouldWork() throws Exception {
        InputStream inputStream = this.getClass().getResourceAsStream("/inria-00510267.corpus.tei.xml");
        XMLStreamReader2 reader = (XMLStreamReader2) inputFactory.createXMLStreamReader(inputStream);

        StaxUtils.traverse(reader, target);

        final Conference_Event conferenceEvent = target.getConferenceEvent();
        assertThat(conferenceEvent, notNullValue());
        assertThat(conferenceEvent.getMonograph(), notNullValue());
        assertThat(conferenceEvent.getStart_date(), is("2010-09-20"));
        assertThat(conferenceEvent.getEnd_date(), is("2010-09-23"));
        assertThat(conferenceEvent.getConference().getTitle(), is("CLEF 2010 - Conference on Multilingual and Multimodal Information Access Evaluation"));

        final Address address = conferenceEvent.getAddress();
        assertThat(address.getSettlement(), is("Padua"));
        assertThat(address, notNullValue());

        final Country country = address.getCountry();
        assertThat(country, notNullValue());
        assertThat(country.getIso(), is("IT"));

    }

    @Test
    public void testExtractPageNumbers_standardCase() throws Exception {
        final String[] input = target.extractPageNumbers("123-124");

        assertThat(input.length, is(2));
        assertThat(input[0], is("123"));
        assertThat(input[1], is("124"));
    }

    @Ignore("needs to fix the code first")
    @Test
    public void testExtractPageNumbers_standardCaseWithSpaces() throws Exception {
        final String[] input = target.extractPageNumbers(" 123 - 124 ");

        assertThat(input.length, is(2));
        assertThat(input[0], is("123"));
        assertThat(input[1], is("124"));
    }

    @Ignore("needs to fix the code first")
    @Test
    public void testExtractPageNumbers_caseWithText() throws Exception {
        final String[] input = target.extractPageNumbers("p. 123 - p. 124 ");

        assertThat(input.length, is(2));
        assertThat(input[0], is("123"));
        assertThat(input[1], is("124"));
    }


    @Test
    public void testStaxTag_equals_shouldReturnTrue() throws Exception {

        PublicationTeiDocumentStaxHandler.StaxTag a = new PublicationTeiDocumentStaxHandler().new StaxTag("title", "/a/b/c/d");
        PublicationTeiDocumentStaxHandler.StaxTag b = new PublicationTeiDocumentStaxHandler().new StaxTag("title", "/a/b/c/d");

        assertThat(a.equals(b), is(true));
    }

    @Test
    public void testStaxTag_Notequals1_shouldReturnFalse() throws Exception {

        PublicationTeiDocumentStaxHandler.StaxTag a = new PublicationTeiDocumentStaxHandler().new StaxTag("title", "/a/b/c");
        PublicationTeiDocumentStaxHandler.StaxTag b = new PublicationTeiDocumentStaxHandler().new StaxTag("title", "/a/b/c/d");

        assertThat(a.equals(b), is(false));
    }

    @Test
    public void testStaxTag_Notequals2shouldReturnFalse() throws Exception {

        PublicationTeiDocumentStaxHandler.StaxTag a = new PublicationTeiDocumentStaxHandler().new StaxTag("title", "/a/b/c/d");
        PublicationTeiDocumentStaxHandler.StaxTag b = new PublicationTeiDocumentStaxHandler().new StaxTag("title1", "/a/b/c/d");

        assertThat(a.equals(b), is(false));
    }


}