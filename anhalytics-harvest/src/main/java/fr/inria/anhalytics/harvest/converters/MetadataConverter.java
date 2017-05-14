package fr.inria.anhalytics.harvest.converters;

import fr.inria.anhalytics.commons.data.BiblioObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author azhar
 */
public interface MetadataConverter {
    Element convertMetadataToTEIHeader(Document metadata, Document newTEIcorpus, BiblioObject biblio) ;
}
