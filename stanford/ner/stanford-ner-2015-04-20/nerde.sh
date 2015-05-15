#!/bin/bash

java -Xmx1g -cp stanford-ner.jar edu.stanford.nlp.process.PTBTokenizer test.txt > test.tok
perl -ne 'chomp; print "$_ O O O O\n"' test.tok > test.tok.ready
java -Xmx1g -cp stanford-ner.jar edu.stanford.nlp.ie.crf.CRFClassifier -loadClassifier classifiers/edu/stanford/nlp/models/ner/german.dewac_175m_600.crf.ser.gz -testFile test.tok.ready > out.dewac.txt
java -Xmx1g -cp stanford-ner.jar edu.stanford.nlp.ie.crf.CRFClassifier -loadClassifier classifiers/edu/stanford/nlp/models/ner/german.hgc_175m_600.crf.ser.gz -testFile test.tok.ready > out.hgc.txt
