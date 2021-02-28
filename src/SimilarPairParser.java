import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class SimilarPairParser {
    static final String PAIR = "row";
    static final String ID1 = "PostId";
    static final String ID2 = "RelatedPostId";
    static final String SIM = "Similarity";

    @SuppressWarnings({ "unchecked" })
    public static Set<SimilarPair> read(String dataFile) {
        Set<SimilarPair> pairs = new HashSet<SimilarPair>();
        try {
            // First, create a new XMLInputFactory
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            // Setup a new eventReader
            InputStream in = new FileInputStream(dataFile);
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            // read the XML document
            int id1 = -1;
            int id2 = -2;
            double sim = 1;
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    // If we have an item element, we create a new item
                    if (startElement.getName().getLocalPart().equals(PAIR)) {
                        // We read the attributes from this tag and add the date
                        // attribute to our object
                        Iterator<Attribute> attributes = startElement
                                .getAttributes();
                        while (attributes.hasNext()) {
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals(ID1)) {
                                id1 = Integer.parseInt(attribute.getValue());
                            }
                            if (attribute.getName().toString().equals(ID2)) {
                                id2 = Integer.parseInt(attribute.getValue());
                            }
                            if (attribute.getName().toString().equals(SIM)) {
                                sim = Double.parseDouble(attribute.getValue());
                            }
                        }
                    }
                }
                // If we reach the end of an item element, we add it to the list
                if (event.isEndElement()) {
                    EndElement endElement = event.asEndElement();
                    if (endElement.getName().getLocalPart().equals(PAIR)) {
                        pairs.add(new SimilarPair(id1, id2, sim));
                    }
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return pairs;
    }

}
