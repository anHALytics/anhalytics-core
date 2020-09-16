package fr.inria.anhalytics.commons.managers;

/**
 * All MongoDb collections are listed here.
 * 
 * @author Achraf
 */
public interface MongoCollectionsInterface {

    // PL note: could be changed to an enum class

    
    // files that can't be donwloaded and will be processed later
    public static final String BIBLIO_OBJECTS = "biblio_objects";
    
    // where anhalytics identifiers are generated
    public static final String IDENTIFIERS = "identifiers";
    // mainly for grobid process to analyze extraction performance
    public static final String HARVEST_DIAGNOSTIC = "diagnostic";
    // files that can't be donwloaded and will be processed later
    public static final String TO_REQUEST_LATER = "to_request_later";
    // source metadata tei to be renamed to METADATAS_TEIS
    public static final String METADATAS_TEIS = "metadata_teis";
    // binary files , pdf
    public static final String BINARIES = "binaries";
    // publications annexes
    public static final String PUB_ANNEXES = "pub_annexes";
    // tei generated using metadata and fulltext
    public static final String TEI_CORPUS = "tei_corpus";
    // tei generated using metadata and fulltext
    public static final String METADATA_WITHFULLTEXT_TEIS = "metadata_teis_fulltext";
    // tei extracted using grobid
    public static final String GROBID_TEIS = "grobid_teis";
    // grobid provided by grobid after tei extraction
    public static final String GROBID_ASSETS = "grobid_assets";
    // text mining annotation 
    public static final String NERD_ANNOTATIONS = "nerd_annotations";
    public static final String KEYTERM_ANNOTATIONS = "keyterm_annotations";
    public static final String QUANTITIES_ANNOTATIONS = "quantities_annotations";
    public static final String PDF_QUANTITIES_ANNOTATIONS = "pdf_quantities_annotations";
    public static final String SUPERCONDUCTORS_ANNOTATIONS = "superconductors_annotations";
    public static final String SUPERCONDUCTORS_PDF_ANNOTATIONS = "superconductors_pdf_annotations";

    //metadata harvested from other repositorues
    public static final String ISTEX_TEIS = "istex_teis";

    public static final String ISTEX_PDFS = "istex_pdfs";
    public static final String ARXIV_METADATA = "arxiv_metadata";
    public static String CROSSREF_METADATAS = "crossref_metadata";
}
