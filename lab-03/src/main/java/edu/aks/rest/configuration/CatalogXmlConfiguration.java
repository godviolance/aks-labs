package edu.aks.rest.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.http.converter.xml.JacksonXmlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UriUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.core.JsonGenerator;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@Configuration(proxyBeanMethods = false)
public class CatalogXmlConfiguration implements WebMvcConfigurer {
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorParameter(true).parameterName("format")
                .mediaType("xml", MediaType.APPLICATION_XML)
                .mediaType("json", MediaType.APPLICATION_JSON)
                .mediaType("html", MediaType.TEXT_HTML);
    }

    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        builder.withXmlConverter(new JacksonXmlHttpMessageConverter(XmlMapper.builder()) {
            @Override
            protected void writePrefix(JsonGenerator generator, Object value) {
                var request = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                String context = UriUtils.encodePath(request.getRequest().getContextPath(), StandardCharsets.UTF_8);
                try {
                    ((ToXmlGenerator) generator).getStaxWriter().writeProcessingInstruction(
                            "xml-stylesheet", "type=\"text/xsl\" href=\"" + context + "/catalog.xsl\"");
                } catch (XMLStreamException exception) {
                    throw new UncheckedIOException(new IOException("Cannot write the XSL stylesheet reference", exception));
                }
            }
        });
    }
}
