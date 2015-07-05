package de.unidue.run;

import de.unidue.converters.Converter;
import de.unidue.converters.impl.Conll09ToOpenNlpNerConverter;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Conll09TestRunner {

    private static final Logger log = LoggerFactory.getLogger(Conll09TestRunner.class);

    @SuppressWarnings("all")
    public static void main(String[] argc) throws IOException {
        final String compressedConll09 = Conll09ToOpenNlpNerConverter.class.getClassLoader().getResource("tiger_release_aug07.corrected.16012013.conll09.gz").getPath();
        final String uncompressedConll09 = "/tmp/tiger.conll";
        final String openNlpModel = "/tmp/tiger.train";

        uncompress(compressedConll09, uncompressedConll09);

        final Converter conllCoNverter = new Conll09ToOpenNlpNerConverter(uncompressedConll09, openNlpModel);
        conllCoNverter.convert();
    }

    private static void uncompress(String compressedConll09, String uncompressedConll09) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(compressedConll09));
             FileOutputStream out = new FileOutputStream(uncompressedConll09);
             GzipCompressorInputStream compressor = new GzipCompressorInputStream(in)) {

            final byte[] buffer = new byte[1024];
            int n;
            while (-1 != (n = compressor.read(buffer))) {
                out.write(buffer, 0, n);
            }

        } catch (IOException e) {
            log.error("Kann die Datei nicht extrahieren");
            throw e;
        }
    }
}
