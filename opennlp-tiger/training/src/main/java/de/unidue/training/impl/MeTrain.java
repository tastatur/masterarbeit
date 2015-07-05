package de.unidue.training.impl;

import de.unidue.training.Train;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class MeTrain implements Train {

    private final String trainingFile;
    private final String trainedModelFile;

    public MeTrain(String trainingFile, String trainedModelFile) {
        this.trainingFile = trainingFile;
        this.trainedModelFile = trainedModelFile;
    }

    @Override
    public void train() throws IOException {
        final Charset unicode = Charset.forName("UTF-8");
        ObjectStream<String> fileStream;
        ObjectStream<NameSample> nameSamplesStream = null;
        TokenNameFinderModel nerModel;
        try(BufferedOutputStream modelOut = new BufferedOutputStream(new FileOutputStream(trainedModelFile))) {
            fileStream = new PlainTextByLineStream(new FileInputStream(trainingFile), unicode);
            nameSamplesStream = new NameSampleDataStream(fileStream);
            nerModel = NameFinderME.train("de", null, nameSamplesStream, TrainingParameters.defaultParams(), (byte[]) null, null);
            nerModel.serialize(modelOut);
        } catch (IOException e) {
            getLogger().error("Die Datei kann nicht geöffnet werden", e);
            throw e;
        } finally {
            if (nameSamplesStream != null) {
                nameSamplesStream.close();
            }
        }
    }
}
