package fr.inria.anhalytics.ingest.entities;

/**
 *
 * @author azhar
 */
public class Editor {

    private int rank;
    private Person person;
    private Publication publication;

    public Editor() {
    }

    public Editor(int rank, Person person, Publication publication) {
        this.rank = rank;
        this.person = person;
        this.publication = publication;
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
     * @return the publication
     */
    public Publication getPublication() {
        return publication;
    }

    /**
     * @param publication the publication to set
     */
    public void setPublication(Publication publication) {
        this.publication = publication;
    }
}
