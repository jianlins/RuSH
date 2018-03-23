package edu.utah.bmi.nlp.fastcner;

import edu.utah.bmi.nlp.rush.core.Marker;

import java.util.ArrayList;
import java.util.HashMap;

public interface FastRuSHRule {

    public HashMap<String, ArrayList<Marker>> processText(String text);
}
