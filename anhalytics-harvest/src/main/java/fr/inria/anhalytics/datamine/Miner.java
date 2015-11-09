package fr.inria.anhalytics.datamine;

import fr.inria.anhalytics.commons.managers.MongoFileManager;
import java.net.UnknownHostException;

/**
 *
 * @author achraf
 */
abstract class Miner {
    protected MongoFileManager mm = null;
    public Miner() throws UnknownHostException{
        this.mm = MongoFileManager.getInstance(false);
    }
}
