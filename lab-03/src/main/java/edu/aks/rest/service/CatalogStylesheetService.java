package edu.aks.rest.service;

import org.springframework.stereotype.Service;
import tools.jackson.dataformat.xml.XmlMapper;
import javax.xml.XMLConstants;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

@Service
public class CatalogStylesheetService {
    private final XmlMapper mapper = XmlMapper.builder().build();
    private final Templates stylesheet;

    public CatalogStylesheetService() throws TransformerException, IOException {
        TransformerFactory factory = TransformerFactory.newDefaultInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        try (var source = CatalogStylesheetService.class.getResourceAsStream("/static/catalog.xsl")) {
            if (source == null) {
                throw new IOException("Catalog stylesheet not found");
            }
            stylesheet = factory.newTemplates(new StreamSource(source));
        }
    }

    public String renderHtml(Object document) throws TransformerException {
        var transformer = stylesheet.newTransformer();
        transformer.setParameter("format", "html");
        var html = new StringWriter();
        transformer.transform(new StreamSource(new StringReader(mapper.writeValueAsString(document))),
                new StreamResult(html));
        return html.toString();
    }
}
