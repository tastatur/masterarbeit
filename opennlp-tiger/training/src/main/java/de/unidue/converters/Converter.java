package de.unidue.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public interface Converter {

    default Logger getLogger() {
        return LoggerFactory.getLogger(Converter.class);
    }

    void convert() throws IOException;
}
