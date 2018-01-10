package org.aurinkopg.testingtools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public interface JsonResourceUser {
    default ObjectMapper objectMapper() {
        return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    default String jsonResource(String resourceName) throws IOException {
        ObjectMapper objectMapper = objectMapper();
        String resource = IOUtils.resourceToString(resourceName, UTF_8);
        JsonNode jsonNode = objectMapper.readTree(resource);
        return objectMapper.writeValueAsString(jsonNode);
    }
}
