package edu.utah.bmi.nlp.rush.core;

import edu.utah.bmi.nlp.core.*;
import edu.utah.bmi.nlp.fastcner.FastCRule;
import edu.utah.bmi.nlp.fastcner.FastCRuleCN;
import edu.utah.bmi.nlp.rush.core.Marker.MARKERTYPE;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Character.isAlphabetic;
import static java.lang.Character.isDigit;

public class RuSH3 implements RuSHInf {
    protected static Logger logger = IOUtil.getLogger(RuSH3.class);
    protected static FastCRule fcrp;
    protected static final String STBEGIN = "stbegin", STEND = "stend";
    protected HashMap<String, ArrayList<Span>> result;
    public boolean autofixGap = true;
    protected static final String TOKENBEGIN = "tobegin", TOKENEND = "toend";
    public boolean tokenRuleEnabled = false;
    public boolean fillTextInSpan = false;

    @Deprecated
    protected boolean debug = false;

    protected String mLanguage;
    protected boolean includePunctuation = false;


    public RuSH3(String rule) {
        initiate(rule);
    }

    public void initiate(String rule) {
        Object[] values = RuSHFactory.createFastRuSHRule(rule);
        if (values[0] instanceof FastCRuleCN)
            fcrp = (FastCRuleCN) values[0];
        else
            fcrp = (FastCRule) values[0];
        tokenRuleEnabled = (boolean) values[1];
        mLanguage = (String) values[2];

    }

    protected void fixGap(String text, int previousEnd, int thisBegin) {
        int counter = 0, begin = 0, end = 0;
        char[] gapChars = text.substring(previousEnd, thisBegin).toCharArray();
        for (int i = 0; i < thisBegin - previousEnd; i++) {
            char thisChar = gapChars[i];
            if (isAlphabetic(thisChar) || isDigit(thisChar)) {
                end = i;
                counter++;
                if (begin == 0)
                    begin = i;
            } else if (WildCardChecker.isPunctuation(thisChar)) {
                end = i;
            }
        }
//      An arbitrary number to decide whether the gap is likely to be a sentence or not
        if (counter > 5) {
            begin += previousEnd;
            end = end + previousEnd + 1;
            Span sentence = new Span(begin, end);
        }
    }


    public ArrayList<Span> segToSentenceSpans(String text) {
        ArrayList<Span> sentences = new ArrayList<>();
        result = fcrp.processString(text);
        if (logger.isLoggable(Level.FINE)) {
            text = text.replaceAll("\n", " ");
            for (Map.Entry<String, ArrayList<Span>> ent : result.entrySet()) {
                logger.finer(ent.getKey());
                for (Span span : ent.getValue()) {
                    Rule rule = fcrp.getRule(span.ruleId);
                    logger.finer("\t" + span.begin + "-" + span.end + ":" + span.score + "\t" +
                            text.substring(0, span.begin) + "<" + text.substring(span.begin, span.begin + 1)
                            + ">\t[Rule " + rule.id + ":\t" + rule.rule + "\t" + rule.ruleName + "\t" + rule.score + "\t" + rule.type + "]");
                }

            }

        }
//        align begins and ends
        ArrayList<Span> stbegins = result.get(STBEGIN);
        ArrayList<Span> stends = result.get(STEND);
        ArrayList<Marker> markers = createMarkers(stbegins, stends, text);

        boolean sentenceStarted = false;
        int stBegin = 0;
        for (int i = 0; i < markers.size(); i++) {
            Marker thisMarker = markers.get(i);
            if (sentenceStarted) {
                if (thisMarker.type == MARKERTYPE.END) {
                    if (fillTextInSpan) {
                        int stend = thisMarker.getPosition();
                        sentences.add(new Span(stBegin, stend, text.substring(stBegin, stend)));
                    } else
                        sentences.add(new Span(stBegin, thisMarker.getPosition()));
                    sentenceStarted = false;
                }
            } else {
                if (thisMarker.type == MARKERTYPE.BEGIN) {
                    stBegin = thisMarker.getPosition();
                    sentenceStarted = true;
                } else if (sentences.size() > 0) {
                    int stEnd = thisMarker.getPosition();
//                right trim
                    while (stEnd >= 1 && (Character.isWhitespace(text.charAt(stEnd - 1)) || (int) text.charAt(stEnd - 1) == 160)) {
                        stEnd--;
                    }
                    Span lastSentence = sentences.get(sentences.size() - 1);
                    lastSentence.setEnd(thisMarker.getPosition());
                    lastSentence.setText(text.substring(lastSentence.begin, lastSentence.end));
                }
            }
        }


        if (logger.isLoggable(Level.FINE)) {
            for (Span sentence : sentences) {
                logger.fine("Sentence(" + sentence.begin + "-" + sentence.end + "):\t" + ">" + text.substring(sentence.begin, sentence.end) + "<");
            }
        }
        return sentences;
    }


    private ArrayList<Marker> createMarkers(ArrayList<Span> begins, ArrayList<Span> ends, String text) {
        ArrayList<Marker> markers = new ArrayList<>();
        if (begins == null) {
            markers.add(new Marker(0, MARKERTYPE.BEGIN));
            if (ends != null)
                addEndMarkers(ends, markers);
            else
                markers.add(new Marker(text.length() - 0.4f, MARKERTYPE.END));
        } else {
            if (ends == null) {
                addBeginMarkers(begins, markers);
                markers.add(new Marker(text.length() - 0.4f, MARKERTYPE.END));
            } else {
                Iterator<Span> bIter = begins.iterator();
                Iterator<Span> eIter = ends.iterator();
//                create a relatively sorted list to reduce the sorting time.
                while (bIter.hasNext() || eIter.hasNext()) {
                    if (bIter.hasNext()) {
                        Span beginSpan = bIter.next();
                        markers.add(new Marker(beginSpan.getBegin(), MARKERTYPE.BEGIN));
                    } else {
                        while (eIter.hasNext()) {
                            Span endSpan = eIter.next();
                            markers.add(new Marker(endSpan.getBegin() + 0.6f, MARKERTYPE.END));
                        }
                    }
                    if (eIter.hasNext()) {
                        Span endSpan = eIter.next();
                        markers.add(new Marker(endSpan.getBegin() + 0.6f, MARKERTYPE.END));
                    } else {
                        while (bIter.hasNext()) {
                            Span beginSpan = bIter.next();
                            markers.add(new Marker(beginSpan.getBegin(), MARKERTYPE.BEGIN));
                        }
                    }
                }
            }
        }
        Collections.sort(markers);
        return markers;
    }

    private void addBeginMarkers(ArrayList<Span> spans, ArrayList<Marker> markers) {
        for (Span stend : spans) {
            if (fcrp.getRule(stend.ruleId).type == DeterminantValueSet.Determinants.ACTUAL)
                markers.add(new Marker(stend.begin, MARKERTYPE.BEGIN));
        }
    }

    private void addEndMarkers(ArrayList<Span> spans, ArrayList<Marker> markers) {
        for (Span stend : spans) {
            if (fcrp.getRule(stend.ruleId).type == DeterminantValueSet.Determinants.ACTUAL)
                markers.add(new Marker(stend.end - 0.4f, MARKERTYPE.END));
        }
    }

    private int addToken(int tobegin, int toend, ArrayList<Span> sentences, int sentenceId,
                         ArrayList<ArrayList<Span>> tokenss, ArrayList<Span> tokens, String text) {
        Span currentSentence = sentences.get(sentenceId);
        if (tobegin >= currentSentence.begin) {
            if (toend <= currentSentence.end) {
                if (fillTextInSpan)
                    tokens.add(new Span(tobegin, toend, text.substring(tobegin, toend)));
                else
                    tokens.add(new Span(tobegin, toend));
            } else {
                if (tobegin < currentSentence.end) {
                    if (fillTextInSpan)
                        tokens.add(new Span(tobegin, currentSentence.end, text.substring(tobegin, currentSentence.end)));
                    else
                        tokens.add(new Span(tobegin, currentSentence.end));
                    tokens = new ArrayList<>();
                    if (fillTextInSpan)
                        tokens.add(new Span(currentSentence.end, toend, text.substring(tobegin, toend)));
                    else
                        tokens.add(new Span(currentSentence.end, toend));
                    tokenss.add(tokens);
                    sentenceId++;
                } else {
                    sentenceId++;
                    currentSentence = sentences.get(sentenceId);
                    if (tobegin <= currentSentence.end && tobegin >= currentSentence.begin) {
                        tokens = new ArrayList<>();
                        if (fillTextInSpan)
                            tokens.add(new Span(tobegin, toend, text.substring(tobegin, toend)));
                        else
                            tokens.add(new Span(tobegin, toend));
                        tokenss.add(tokens);
                    } else {
                        if (logger.isLoggable(Level.INFO) && text.substring(tobegin, toend).trim().length() > 0) {
                            logger.info("Skipped token:\n\t" + text.substring(tobegin, toend));
                        }
                    }

                }
            }
        }
        return sentenceId;
    }


    public ArrayList<ArrayList<Span>> tokenize
            (ArrayList<Span> sentences, ArrayList<Span> toBegins, ArrayList<Span> toEnds, String text) {
        ArrayList<ArrayList<Span>> tokenss = new ArrayList<>();
        ArrayList<Marker> markers = createMarkers(toBegins, toEnds, text);
        tokenss.add(new ArrayList<>());
        ArrayList<Span> tokens = tokenss.get(tokenss.size() - 1);
        boolean tokenStarted = false;
        int toBegin = 0;
        int sentenceId = 0;
        for (int i = 0; i < markers.size(); i++) {
            Marker thisMarker = markers.get(i);
            if (tokenStarted) {
                if (thisMarker.type == MARKERTYPE.END) {
                    sentenceId = addToken(toBegin, thisMarker.getPosition(), sentences, sentenceId, tokenss, tokens, text);
                    tokenStarted = false;
                }
            } else {
                if (thisMarker.type == MARKERTYPE.BEGIN) {
                    toBegin = thisMarker.getPosition();
                    tokenStarted = true;
                } else {
                    int toEnd = thisMarker.getPosition();
                    if (tokens.size() == 0) {
                        ArrayList<Span> lastTokens = tokenss.get(tokenss.size() - 1);
                        lastTokens.get(lastTokens.size() - 1).setEnd(toEnd);
                    } else {
                        tokens.get(tokens.size() - 1).setEnd(toEnd);
                    }
                }
            }
        }
        Collections.sort(markers);

//        int lastSentence = tokenss.size() - 1;
//        if (tokenss.get(lastSentence).size() == 0) {
//            tokenss.remove(lastSentence);
//        }
        return tokenss;
    }


    public ArrayList<ArrayList<Span>> simpleTokenize(ArrayList<Span> sentences, String text) {
        ArrayList<ArrayList<Span>> tokenss = new ArrayList<>();
        for (Span sentence : sentences) {
            ArrayList<Span> tokens;
            int stBegin = sentence.begin;
            if (mLanguage.equals("cn")) {
                tokens = SimpleParser.tokenizeDecimalSmart(text.substring(sentence.begin, sentence.end), includePunctuation, stBegin);
            } else {
                tokens = SmartChineseCharacterSplitter.tokenizeDecimalSmart(text.substring(sentence.begin, sentence.end), includePunctuation, stBegin);
            }
            tokenss.add(tokens);
        }
        return tokenss;
    }

    public ArrayList<ArrayList<Span>> tokenize(ArrayList<Span> sentences, String text) {

        ArrayList<Span> tobegins = result.get(TOKENBEGIN);
        ArrayList<Span> toends = result.get(TOKENEND);
        if (tokenRuleEnabled) {
            return tokenize(sentences, tobegins, toends, text);
        } else {
            return simpleTokenize(sentences, text);
        }


    }

    @Deprecated
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

}
