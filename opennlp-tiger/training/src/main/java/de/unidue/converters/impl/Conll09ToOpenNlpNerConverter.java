package de.unidue.converters.impl;

import de.unidue.converters.Converter;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Conll09ToOpenNlpNerConverter implements Converter {

    private final String conll09File;
    private final String openNlpFile;

    public Conll09ToOpenNlpNerConverter(String conll09File, String openNlpFile) {
        this.conll09File = conll09File;
        this.openNlpFile = openNlpFile;
    }

    public void convert() throws IOException {
        List<String> sentences = new ArrayList<>();
        List<String> lines = Files.lines(new File(conll09File).toPath()).collect(Collectors.toList());
        try (PrintWriter trainingModelWriter = new PrintWriter(new File(openNlpFile))) {

        } catch (IOException e) {
            getLogger().error("Kann die Datei {} nicht erzeugen", openNlpFile);
            throw e;
        }
    }
}
