package fr.inria.anhalytics.commons.data;

/**
 *
 * @author azhar
 */
public enum AnnotatorType {
    
    GROBID("grobid"),
    NERD("nerd"),
    KEYTERM("keyterm"),
    QUANTITIES("quantities"),
    PDFQUANTITIES("PDFQUANTITIES"),
    SUPERCONDUCTORS("superconductors"),
    SUPERCONDUCTORS_PDF("superconductors_pdf");;

    private String name;

    private AnnotatorType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public static boolean contains(String test) {
            for (AnnotatorType c : AnnotatorType.values()) {
                if (c.getName().equals(test)) {
                    return true;
                }
            }
            return false;
        }
}
