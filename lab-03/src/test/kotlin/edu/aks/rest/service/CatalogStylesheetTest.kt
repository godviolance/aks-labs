package edu.aks.rest.service

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.StringReader
import java.io.StringWriter
import javax.xml.XMLConstants
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

class CatalogStylesheetTest {
    @Test fun `render XML details and navigation as HTML`() {
        val factory = TransformerFactory.newInstance()
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "")
        val html = StringWriter()
        javaClass.getResourceAsStream("/static/catalog.xsl")!!.use { xsl ->
            factory.newTransformer(StreamSource(xsl)).transform(StreamSource(StringReader(
                "<lesson><id>2</id><courseId>3</courseId><courseTitle>Java</courseTitle>" +
                    "<topic>Collections</topic><minutes>45</minutes></lesson>"
            )), StreamResult(html))
        }
        assertTrue(html.toString().contains("Collections"))
        assertTrue(html.toString().contains("../courses/3?format=xml"))
        assertTrue(html.toString().contains("../lessons?format=xml"))
    }
}
