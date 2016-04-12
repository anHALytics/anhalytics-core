package fr.inria.anhalytics.kb.datamine;

import fr.inria.anhalytics.commons.managers.MongoFileManager;
import java.net.UnknownHostException;

/**
 * Abstract classe to be sub-classed by all kind of classes meant to enrich KB
 * data.
 *
 * @author Achraf
 */
abstract class Miner {

    protected MongoFileManager mm = null;

    public Miner() throws UnknownHostException {
        this.mm = MongoFileManager.getInstance(false);
    }
}
