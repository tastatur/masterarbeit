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
