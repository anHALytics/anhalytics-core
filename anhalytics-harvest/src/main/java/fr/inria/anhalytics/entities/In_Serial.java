package fr.inria.anhalytics.entities;

/**
 *
 * @author azhar
 */
public class In_Serial {
    private Monograph mg;
    private Journal j;
    private Collection c;
    private String volume="";
    private String number="";
    public In_Serial(){}
    public In_Serial(Monograph mg, Journal j, Collection c, String volume, String number){
        this.mg = mg;
        this.j = j;
        this.c = c;
        this.volume = volume;
        this.number = number;
    }

    /**
     * @return the mg
     */
    public Monograph getMg() {
        return mg;
    }

    /**
     * @param mg the mg to set
     */
    public void setMg(Monograph mg) {
        this.mg = mg;
    }

    /**
     * @return the j
     */
    public Journal getJ() {
        return j;
    }

    /**
     * @param j the j to set
     */
    public void setJ(Journal j) {
        this.j = j;
    }

    /**
     * @return the c
     */
    public Collection getC() {
        return c;
    }

    /**
     * @param c the c to set
     */
    public void setC(Collection c) {
        this.c = c;
    }

    /**
     * @return the volume
     */
    public String getVolume() {
        return volume;
    }

    /**
     * @param volume the volume to set
     */
    public void setVolume(String volume) {
        this.volume = volume;
    }

    /**
     * @return the number
     */
    public String getNumber() {
        return number;
    }

    /**
     * @param number the number to set
     */
    public void setNumber(String number) {
        this.number = number;
    }
}
