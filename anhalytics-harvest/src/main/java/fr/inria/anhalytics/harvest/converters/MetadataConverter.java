package fr.inria.anhalytics.harvest.converters;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author azhar
 */
public interface MetadataConverter {
    public Element convertMetadataToTEIHeader(Document metadata, Document newTEIcorpus) ;
}
