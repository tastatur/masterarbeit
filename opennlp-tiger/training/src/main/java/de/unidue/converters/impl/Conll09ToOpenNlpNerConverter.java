package de.unidue.converters.impl;

import com.sun.org.apache.xpath.internal.operations.Bool;
import de.unidue.converters.Converter;
import opennlp.tools.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Conll09ToOpenNlpNerConverter implements Converter {

    private static final String NE_TAG = "NE";

    private final String conll09File;
    private final String openNlpFile;

    public Conll09ToOpenNlpNerConverter(String conll09File, String openNlpFile) {
        this.conll09File = conll09File;
        this.openNlpFile = openNlpFile;
    }

    public void convert() throws IOException {
        final List<String> sentences = new ArrayList<>();
        List<String> lines = Files.lines(new File(conll09File).toPath()).collect(Collectors.toList());
        try (PrintWriter trainingModelWriter = new PrintWriter(new File(openNlpFile))) {
            Boolean neTagIsNotOpen = true;
            StringBuilder sentenceBuilder = new StringBuilder();
            for (String line : lines) {
                if (StringUtil.isEmpty(line)) {
                    if (!neTagIsNotOpen) {
                        sentenceBuilder.append("<END> ").append(" ");
                        neTagIsNotOpen = true;
                    }

                    String sentence = sentenceBuilder.toString().replaceAll("''", "").replaceAll("``", "").replaceAll("/", ".");
                    sentences.add(sentence);
                    sentenceBuilder = new StringBuilder();
                } else {
                    final String dataColumns[] = line.split("\\t");
                    final String token = dataColumns[1];
                    final String tag = dataColumns[4];

                    if (!NE_TAG.equals(tag) && neTagIsNotOpen) {
                        sentenceBuilder.append(token).append(" ");
                    } else if (NE_TAG.equals(tag) && neTagIsNotOpen) {
                        neTagIsNotOpen = false;
                        sentenceBuilder.append("<START:misc> ").append(token).append(" ");
                    } else if (NE_TAG.equals(tag) && !neTagIsNotOpen) {
                        sentenceBuilder.append(token).append(" ");
                    } else if (!NE_TAG.equals(tag) && !neTagIsNotOpen) {
                        neTagIsNotOpen = true;
                        sentenceBuilder.append("<END> ").append(token).append(" ");
                    }
                }
            }

            sentences.stream().forEachOrdered(trainingModelWriter::println);
        } catch (IOException e) {
            getLogger().error("Kann die Datei {} nicht erzeugen", openNlpFile);
            throw e;
        }
    }
}
