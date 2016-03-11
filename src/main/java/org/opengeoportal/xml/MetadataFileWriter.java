package org.opengeoportal.xml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes an XML String to an XML file. Does some filtering and very basic validation.
 *
 * Created by cbarne02 on 3/3/16.
 */

public class MetadataFileWriter {

    @Autowired
    private MetadataDocumentBlocks blocks;

    /**
     * takes the XML metadata string from the Solr instance, does some filtering, parses it as XML
     * as a form of simplistic validation, writes to a file
     *
     * @param xmlString
     * @param xmlPath
     * @throws TransformerException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public void write(String xmlString, Path xmlPath)
            throws TransformerException, IOException, SAXException, ParserConfigurationException {
        Document document = buildXMLDocFromString(xmlString);

        Source xmlSource = new DOMSource(document);

        try (OutputStream os = Files.newOutputStream(xmlPath)) {
            StreamResult streamResult = new StreamResult(os);
            blocks.getTransformer().transform(xmlSource, streamResult);
        }

    }

    /**
     * takes an XML metadata string, does some filtering, returns an xml document
     *
     * @param rawXMLString
     * @return the XML String as a Document
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */

    private Document buildXMLDocFromString(String rawXMLString) throws ParserConfigurationException, SAXException, IOException {
        //filter extra spaces from xmlString
        rawXMLString = rawXMLString.replaceAll(">[ \t\n\r\f]+<", "><").replaceAll("[\t\n\r\f]+", "");
        byte[] bytes = rawXMLString.getBytes("UTF-8");

        try (InputStream xmlInputStream = new ByteArrayInputStream(bytes)) {
            //parse the returned XML to make sure it is well-formed & to format

            //Parse the document
            Document document = blocks.getBuilder().parse(xmlInputStream);
            return document;
        }

    }


}
