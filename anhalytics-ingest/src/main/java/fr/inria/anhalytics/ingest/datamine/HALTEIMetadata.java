
package fr.inria.anhalytics.ingest.datamine;

/**
 *
 * @author achraf
 */
public interface HALTEIMetadata {
    
    public final static String MetadataElement = "/teiCorpus/teiHeader";
    public final static String MonogrElement = "/teiCorpus/teiHeader/sourceDesc/biblStruct/monogr";
    public final static String IdnoElement = "/teiCorpus/teiHeader/sourceDesc/biblStruct/idno";
    public final static String TitleElement = "/teiCorpus/teiHeader/titleStmt/title";
    public final static String LanguageElement = "/teiCorpus/teiHeader/profileDesc/langUsage/language";
    public final static String TypologyElement = "/teiCorpus/teiHeader/profileDesc/textClass/classCode[@scheme=\"halTypology\"]";
    public final static String SubmissionDateElement = "/teiCorpus/teiHeader/editionStmt/edition[@type=\"current\"]/date[@type=\"whenSubmitted\"]";
    public final static String DomainElement = "/teiCorpus/teiHeader/profileDesc/textClass/classCode[@scheme=\"halDomain\"]";
    public final static String EditorElement = "/teiCorpus/teiHeader/sourceDesc/biblStruct/editor";
    public final static String AuthorElement = "/teiCorpus/teiHeader/sourceDesc/biblStruct/author";
    
}
