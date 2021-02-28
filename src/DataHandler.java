import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import java.util.Set;

/**
 * Reads an XML archive with Stack Overflow questions and constructs shingle
 * representations.
 */
public class DataHandler extends Reader {
    static final String POST = "row";
    static final String ID = "Id";
    static final String TITLE = "Title";
    static final String BODY = "Body";

    private String filePath;
    private XMLEventReader eventReader;


    public DataHandler(int maxPosts, Shingler shingler, String filePath){
        super(maxPosts, shingler);

        this.filePath = filePath;
        reset();
    }

    @SuppressWarnings({ "unchecked"})
    private Post parseNextEvent() throws XMLStreamException {
        Post post = null;
        while (eventReader.hasNext()) {
            XMLEvent event = this.eventReader.nextEvent();
            if (event.isStartElement()) {

                StartElement startElement = event.asStartElement();
                // If we have a post element, we create a new post
                if (startElement.getName().getLocalPart().equals(POST)) {
                    post = new Post();
                    // We read the attributes from this tag and add each
                    // useful attribute to our object
                    Iterator<Attribute> attributes = startElement.getAttributes();
                    while (attributes.hasNext()) {
                        Attribute attribute = attributes.next();
                        if (attribute.getName().toString().equals(ID)) {
                            post.setId(Integer.parseInt(attribute.getValue()));
                        }
                        if (attribute.getName().toString().equals(TITLE)) {
                            post.setTitle(attribute.getValue());
                        }
                        if (attribute.getName().toString().equals(BODY)) {
                            post.setBody(attribute.getValue());
                        }
                    }
                }
            }
            // If we reach the end of a post element, we add it to the list
            if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                if (endElement.getName().getLocalPart().equals(POST)) {
                    return post;
                }
            }
        }
        return null;
    }

    @Override
    public Set<Integer> next() {
        if (curDoc % 10000 == 0){
            System.out.println("at doc " + curDoc);
        }

        while (this.curDoc < this.maxDocs && eventReader.hasNext()) {
            Post post;
            try {
                post = parseNextEvent();
                if (post != null) {
                    this.curDoc++;
                    this.idToDoc.add(post.getId());
                    return this.shingler.shingle(post);
                }
            } catch (XMLStreamException e) {
                e.printStackTrace();
                break;
            }
        }
        return null;
    }

    @Override
    public void reset() {
        try {
            // First, create a new XMLInputFactory
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            // Setup a new eventReader
            InputStream in = new FileInputStream(filePath);
            this.eventReader = inputFactory.createXMLEventReader(in);
            // Free memory
            System.gc();
            this.curDoc = -1;
            idToDoc = new ArrayList<>();
            idToShingle = new ArrayList<>();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

}
