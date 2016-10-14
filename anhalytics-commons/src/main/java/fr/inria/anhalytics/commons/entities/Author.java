package fr.inria.anhalytics.commons.entities;

/**
 *
 * @author azhar
 */
public class Author {

    private Document document;
    private Person person;
    private int rank;
    private int correp;

    public Author() {
    }

    public Author(Document document, Person person, int rank, int correp) {
        this.document = document;
        this.person = person;
        this.rank = rank;
        this.correp = correp;
    }

    /**
     * @return the document
     */
    public Document getDocument() {
        return document;
    }

    /**
     * @param document the document to set
     */
    public void setDocument(Document document) {
        this.document = document;
    }

    /**
     * @return the person
     */
    public Person getPerson() {
        return person;
    }

    /**
     * @param person the person to set
     */
    public void setPerson(Person person) {
        this.person = person;
    }

    /**
     * @return the rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * @param rank the rank to set
     */
    public void setRank(int rank) {
        this.rank = rank;
    }

    /**
     * @return the correp
     */
    public int getCorrep() {
        return correp;
    }

    /**
     * @param correp the correp to set
     */
    public void setCorrep(int correp) {
        this.correp = correp;
    }
}
