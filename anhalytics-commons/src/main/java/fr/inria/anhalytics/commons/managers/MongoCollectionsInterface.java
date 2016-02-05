package fr.inria.anhalytics.commons.managers;

/**
 * All MongoDb collections are listed here.
 * 
 * @author Achraf
 */
public interface MongoCollectionsInterface {
    //mainly for grobid process to analyze extraction performance
    public static final String HARVEST_DIAGNOSTIC = "diagnostic";
    //files that can't be donwloaded and will be processed later
    public static final String TO_REQUEST_LATER = "to_request_later";
    //metadata teis
    public static final String ADDITIONAL_TEIS = "metadata_teis";
    //binary files , pdf
    public static final String BINARIES = "binaries";
    //publications annexes
    public static final String PUB_ANNEXES = "pub_annexes";
    //tei generated using metadata and fulltext
    public static final String FINAL_TEIS = "final_teis";
    //tei extracted using grobid
    public static final String GROBID_TEIS = "grobid_teis";
    //grobid provided by grobid after tei extraction
    public static final String GROBID_ASSETS = "grobid_assets";
    //annotation recognized usin nerd
    public static final String ANNOTATIONS = "nerd_annotations";
    //metadata harvested frim istex
    public static final String ISTEX_TEIS = "istex_teis";
    public static final String ARXIV_METADATA = "arxiv_metadata";
}
