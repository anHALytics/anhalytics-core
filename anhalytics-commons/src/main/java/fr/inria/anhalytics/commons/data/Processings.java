package fr.inria.anhalytics.commons.data;

/**
 *
 * @author azhar
 */
public enum Processings {
    
    GROBID("grobid"),
    NERD("nerd"),
    KEYTERM("keyterm"),
    QUANTITIES("quantities"),
    PDFQUANTITIES("PDFQUANTITIES");

    private String name;

    private Processings(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public static boolean contains(String test) {
            for (Processings c : Processings.values()) {
                if (c.name().equals(test)) {
                    return true;
                }
            }
            return false;
        }
}
