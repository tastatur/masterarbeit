public static void main(String[] argc) throws IOException {
    final String scriptsDir = argc[0];
    final String workingDir = argc[1];
    final String targetDir = argc[2];
    final PigServer pig = new PigServer(LOCAL);
    try {
        String dbpediaSentenceseScript = scriptsDir.concat("/01_extract_sentences_with_links.pig");
        String dbpediaArticleScript = scriptsDir.concat("/02_dbpedia_article_types.pig");
        String dbpediaJoin = scriptsDir.concat("/03_join_by_type_and_convert.pig");

        final Map<String, String> sentencesParams = new HashMap<>();
        sentencesParams.put("LANG", "de");
        sentencesParams.put("INPUT", workingDir.concat("/wiki/dewiki-latest-pages-articles1.xml"));
        sentencesParams.put("OUTPUT", workingDir.concat("/wspace"));
        sentencesParams.put("PIGNLPROC_JAR", targetDir.concat("/pignlproc-0.1.0-SNAPSHOT.jar"));
        pig.registerScript(dbpediaSentenceseScript, sentencesParams);

        final Map<String, String> articleParams = new HashMap<>();
        articleParams.put("LANG", "de");
        articleParams.put("INPUT", workingDir.concat("/dbpedia"));
        articleParams.put("OUTPUT", workingDir.concat("/wspace"));
        articleParams.put("PIGNLPROC_JAR", targetDir.concat("/pignlproc-0.1.0-SNAPSHOT.jar"));
        pig.registerScript(dbpediaArticleScript, articleParams);

        final Map<String, String> joinParams = new HashMap<>();
        joinParams.put("LANG", "de");
        joinParams.put("INPUT", workingDir.concat("/wspace"));
        joinParams.put("OUTPUT", workingDir.concat("/wspace"));
        joinParams.put("TYPE_NAMES", scriptsDir.concat("/dbpedia_to_opennlp_types.tsv"));
        joinParams.put("PIGNLPROC_JAR", targetDir.concat("/pignlproc-0.1.0-SNAPSHOT.jar"));
        pig.registerScript(dbpediaJoin, joinParams);
    } catch (IOException e) {
        log.error(e);
        throw e;
    }
}
