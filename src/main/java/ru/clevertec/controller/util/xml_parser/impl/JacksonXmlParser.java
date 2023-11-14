package ru.clevertec.controller.util.xml_parser.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator.Feature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.clevertec.controller.util.xml_parser.XmlParser;

/**
 * Implementation XML parser using Jackson library
 */
public class JacksonXmlParser implements XmlParser {

    @Override
    public String getXmlString(Object object) {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.enable(Feature.WRITE_XML_DECLARATION);
        try {
            return xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
