package edu.utah.bmi.nlp.rush.core;

import edu.utah.bmi.nlp.core.Span;

import java.util.ArrayList;

public interface RuSHInf {
    public ArrayList<Span> segToSentenceSpans(String text);

    public ArrayList<ArrayList<Span>> tokenize(ArrayList<Span> sentences, String text);
}
