import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Set;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class SimilarPairWriter {

    public static void save(String outputFile, Set<SimilarPair> pairs)
            throws FileNotFoundException, XMLStreamException {
        // create an XMLOutputFactory
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        // create XMLEventWriter
        XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(
                new FileOutputStream(outputFile));
        // create an EventFactory
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        XMLEvent end = eventFactory.createDTD("\n");
        // create and write Start Tag
        StartDocument startDocument = eventFactory.createStartDocument();
        eventWriter.add(startDocument);

        // create open tag
        StartElement startElement = eventFactory.createStartElement("", "", "postlinks");
        eventWriter.add(startElement);
        eventWriter.add(end);
        // Write the different nodes
        for (SimilarPair pair : pairs)
            createNode(eventWriter, pair);

        eventWriter.add(eventFactory.createEndElement("", "", "postlinks"));
        eventWriter.add(end);
        eventWriter.add(eventFactory.createEndDocument());
        eventWriter.close();
    }

    private static void createNode(XMLEventWriter eventWriter, SimilarPair pair)
            throws XMLStreamException {

        XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        XMLEvent end = eventFactory.createDTD("\n");
        XMLEvent tab = eventFactory.createDTD("\t");
        // create Start node
        StartElement sElement = eventFactory.createStartElement("", "", "row");
        eventWriter.add(tab);
        eventWriter.add(sElement);
        // create atrributes
        eventWriter.add(eventFactory.createAttribute("PostId", Integer.toString(pair.id1)));
        eventWriter.add(eventFactory.createAttribute("RelatedPostId", Integer.toString(pair.id2)));
        eventWriter.add(eventFactory.createAttribute("Similarity", Double.toString(pair.sim)));
        // create End node
        EndElement eElement = eventFactory.createEndElement("", "", "row");
        eventWriter.add(eElement);
        eventWriter.add(end);
    }

}
