package de.unidue.run;

import de.unidue.training.Train;
import de.unidue.training.impl.MeTrain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PigDataTrainerRunner {

    private static final Logger log = LoggerFactory.getLogger(PigDataTrainerRunner.class);

    public static void main(String[] argc) {
        String trainFile = argc[0];
        String modelFile = argc[1];

        Train pigTrainer = new MeTrain(trainFile, modelFile);
        try {
            pigTrainer.train();
        } catch (IOException e) {
            log.error("Kann das Modell nicht trainieren", e);
        }
    }
}
