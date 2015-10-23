package de.unidue;

import de.unidue.proxyapi.util.EnhancementEngine;
import opennlp.tools.cmdline.PerformanceMonitor;
import opennlp.tools.namefind.*;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;
import opennlp.tools.util.eval.Evaluator;
import opennlp.tools.util.eval.FMeasure;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class EvalAuto extends Evaluator<NameSample> {

    private final EnhancementEngine engine;
    private FMeasure fmeasure = new FMeasure();

    private TokenNameFinder nameFinder;

    public EvalAuto(final EnhancementEngine engine, TokenNameFinder nameFinder, TokenNameFinderEvaluationMonitor... listeners) {
        super(listeners);
        this.nameFinder = nameFinder;
        this.engine = engine;
    }

    public FMeasure getFMeasure() {
        return fmeasure;
    }

    public static void main(String[] argc) throws IOException {
        final String tigerFilename = "/home/strah/masterarbeit-data/mitte/corpora/tiger.train";
        final String pigFilename = "/home/strah/masterarbeit-data/mitte/corpora/pig.train";

        evaluateTiger(tigerFilename);
        evaluatePig(pigFilename);
    }

    private static void evaluatePig(final String pigFilename) throws IOException {
        evaluate(pigFilename, EnhancementEngine.STANFORD_BOTH);
        evaluate(pigFilename, EnhancementEngine.STANFORD_DEWAC);
        evaluate(pigFilename, EnhancementEngine.STANFORD_HGC);
        evaluate(pigFilename, EnhancementEngine.MITIE_TIGER);
        evaluate(pigFilename, EnhancementEngine.TIGER);
    }

    private static void evaluate(final String filename, final EnhancementEngine engine) throws IOException {
        StanbolTokenNameFinder nameFinder = new StanbolTokenNameFinder("http://localhost:8080/stanbol", engine);
        System.out.println("Performing evaluation ...");
        EvalAuto evaluator = new EvalAuto(engine, nameFinder, new TokenNameFinderEvaluationMonitor[0]);
        final NameSampleDataStream sampleStream = new NameSampleDataStream(new PlainTextByLineStream(new InputStreamReader(new FileInputStream(filename))));
        final PerformanceMonitor monitor = new PerformanceMonitor("sent");
        monitor.startAndPrintThroughput();
        ObjectStream iterator = new ObjectStream() {
            public NameSample read() throws IOException {
                monitor.incrementCounter();
                return sampleStream.read();
            }

            public void reset() throws IOException {
                sampleStream.reset();
            }

            public void close() throws IOException {
                sampleStream.close();
            }
        };
        evaluator.evaluate(iterator);
        monitor.stopAndPrintFinalResult();
        System.out.println("==========="+engine.name()+"============================");
        System.out.println("F-Measure: " + evaluator.getFMeasure().getFMeasure());
        System.out.println("Recall: " + evaluator.getFMeasure().getRecallScore());
        System.out.println("Precision: " + evaluator.getFMeasure().getPrecisionScore());
    }

    private static void evaluateTiger(final String tigerFilename) throws IOException {
        evaluate(tigerFilename, EnhancementEngine.MITIE_PIG);
        evaluate(tigerFilename, EnhancementEngine.PIG);
    }

    @Override
    protected NameSample processSample(NameSample reference) {

        if (reference.isClearAdaptiveDataSet()) {
            nameFinder.clearAdaptiveData();
        }

        Span predictedNames[] = nameFinder.find(reference.getSentence());
        Span references[] = reference.getNames();

        for (int i = 0; i < references.length; i++) {
            references[i] = new Span(references[i].getStart(), references[i].getEnd(), "default");
        }
        for (int i = 0; i < predictedNames.length; i++) {
            predictedNames[i] = new Span(predictedNames[i].getStart(), predictedNames[i].getEnd(), "default");
        }

        fmeasure.updateScores(references, predictedNames);

        return new NameSample(reference.getSentence(), predictedNames, reference.isClearAdaptiveDataSet());
    }
}
