
package fr.inria.anhalytics.kb.datamine;

/**
 *
 * @author achraf
 */
public interface TeiPaths {
    
    public final static String MetadataElement = "/teiCorpus/teiHeader";
    public final static String FulltextTeiHeaderAuthors = "/teiCorpus/TEI/teiHeader/fileDesc/sourceDesc/biblStruct/analytic/author";
    public final static String MonogrElement = "/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/monogr";
    public final static String IdnoElement = "/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/idno";
    public final static String TitleElement = "/teiCorpus/teiHeader/fileDesc/titleStmt/title";
    public final static String LanguageElement = "/teiCorpus/teiHeader/profileDesc/langUsage/language";
    public final static String TypologyElement = "/teiCorpus/teiHeader/profileDesc/textClass/classCode[@scheme=\"typology\"]";
    public final static String SubmissionDateElement = "/teiCorpus/teiHeader/fileDesc/editionStmt/edition[@type=\"current\"]/date[@type=\"whenSubmitted\"]";
    public final static String DomainElement = "/teiCorpus/teiHeader/profileDesc/textClass/classCode[@scheme=\"domain\"]";
    public final static String EditorElement = "/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/analytic/editor";
    public final static String AuthorElement = "/teiCorpus/teiHeader/fileDesc/sourceDesc/biblStruct/analytic/author";
    
}
