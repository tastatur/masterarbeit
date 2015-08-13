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
