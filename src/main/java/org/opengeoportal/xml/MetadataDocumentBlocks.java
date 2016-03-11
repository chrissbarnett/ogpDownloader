package org.opengeoportal.xml;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

/**
 * Contains reusable and expensive to create XML processing blocks that can be injected into an XML building
 * instance.
 *
 * Created by cbarne02 on 3/3/16.
 */
@Component
public class MetadataDocumentBlocks {

    private DocumentBuilder builder;
    private Transformer transformer;

    @PostConstruct
    public void init() throws ParserConfigurationException, TransformerConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);  // dtd isn't always available; would be nice to attempt to validate

        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        // Use document builder factory

        builder = factory.newDocumentBuilder();

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    }

    public DocumentBuilder getBuilder(){
        return builder;
    }

    public Transformer getTransformer(){
        return transformer;
    }
}
