package de.unidue.training;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public interface Train {

    default Logger getLogger() {
        return LoggerFactory.getLogger(Train.class);
    }

    void train() throws IOException;
}
