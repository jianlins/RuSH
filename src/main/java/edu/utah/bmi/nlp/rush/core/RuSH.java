package edu.utah.bmi.nlp.rush.core;

import edu.utah.bmi.nlp.core.*;
import edu.utah.bmi.nlp.fastcner.FastRuSHFactory;
import edu.utah.bmi.nlp.fastcner.FastRuSHRule_H;
import edu.utah.bmi.nlp.fastcner.FastRuSHRule_HCN;
import edu.utah.bmi.nlp.type.system.Sentence;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.utah.bmi.nlp.rush.core.Marker.MARKERTYPE;
import static java.lang.Character.isAlphabetic;
import static java.lang.Character.isDigit;


public class RuSH implements RuSHInf {
    protected static Logger logger = IOUtil.getLogger(RuSH.class);
    protected static FastRuSHRule_H fcrp;
    public static final String STBEGIN = "stbegin", STEND = "stend";
    public static final String TOKENBEGIN = "tobegin", TOKENEND = "toend";
    protected boolean autofixGap = true;
    public boolean fillTextInSpan = false;
    protected Map<String, ArrayList<Marker>> result;
    public boolean tokenRuleEnabled;
    public String mLanguage = "en";
    public static LinkedHashSet<Boundary> logs = new LinkedHashSet<>();

    public RuSH(String ruleStr) {
        initiate(ruleStr);
    }

    public void initiate(String ruleStr) {
        fcrp = FastRuSHFactory.createFastRuSHRule(ruleStr);
        if (fcrp instanceof FastRuSHRule_HCN) {
            mLanguage = "cn";
        }
        this.tokenRuleEnabled = fcrp.tokenRuleEnabled;
    }

    protected void fixGap(String text, ArrayList<Span> sentences, int previousEnd, int thisBegin) {
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
            sentences.add(new Span(begin, end));
        }

    }

    public ArrayList<Span> segToSentenceSpans(String text) {
        ArrayList<Span> sentences = new ArrayList<>();
        result = fcrp.processText(text);
        if (logger.isLoggable(Level.FINE)) {
            text = text.replaceAll("\n", " ");
            for (Map.Entry<String, ArrayList<Marker>> ent : result.entrySet()) {
                logger.finer(ent.getKey());
                for (Span span : ent.getValue()) {
                    Rule rule = fcrp.getRule(span.ruleId);
                    if (logger.isLoggable(Level.FINE)) {
                        String rulestr = "Rule " + rule.id + ":\t" + rule.rule + "\t" + rule.ruleName + "\t" + rule.score + "\t" + rule.type;
                        logger.finer("\t" + span.begin + "-" + span.end + ":" + span.score + "\t" +
                                text.substring(0, span.begin) + "<" + text.substring(span.begin, span.begin + 1)
                                + ">\t[" + rulestr + "]");
                        logs.add(new Boundary(span.begin, span.end, rulestr, rule.ruleName, rule.type));
                    }
                }
            }
        }
        ArrayList<Marker> stbegins = result.getOrDefault(STBEGIN, new ArrayList<>());
        ArrayList<Marker> stends = result.getOrDefault(STEND, new ArrayList<>());
        if (stbegins.size() == 0) {
            stbegins.add(new Marker(0, Marker.MARKERTYPE.BEGIN));
        }
        if (stends.size() == 0) {
            stends.add(new Marker(text.length()-1, Marker.MARKERTYPE.END));
        }

        float fpos = 0;
        boolean sentStarted = false;
        for (int b = 0; b < stbegins.size(); b++) {
            if (autofixGap && stbegins.get(b).fbegin >fpos){
                fixGap(text, sentences, (int)fpos, stbegins.get(b).getBegin());
            }
            if (stbegins.get(b).fbegin >= fpos && !sentStarted) {
                fpos = stbegins.get(b).fbegin;
                sentStarted = true;
            }
            if (sentStarted) {
                for (int e = 0; e < stends.size(); e++) {
                    if (stends.get(e).fbegin >= fpos) {
                        sentences.add(new Span((int) fpos, stends.get(e).getBegin()+1));
                        fpos=stends.get(e).getBegin()+1;
                        sentStarted = false;
                        break;
                    }
                }
            }
        }
//
//
//        ArrayList<Marker> markers = createMarkers(stbegins, stends, text);
//
//        boolean sentenceStarted = false;
//        int stBegin = 0;
//        for (int i = 0; i < markers.size(); i++) {
//            Marker thisMarker = markers.get(i);
//            if (sentenceStarted) {
//                if (autofixGap && sentences.size() > 0) {
//                    fixGap(text, sentences, sentences.get(sentences.size() - 1).end, stBegin);
//                }
//                if (thisMarker.type == Marker.MARKERTYPE.END) {
//                    if (fillTextInSpan) {
//                        int stend = thisMarker.getBegin();
//                        sentences.add(new Span(stBegin, stend, text.substring(stBegin, stend)));
//                    } else
//                        sentences.add(new Span(stBegin, thisMarker.getBegin()));
//                    sentenceStarted = false;
//                }
//            } else {
//                if (thisMarker.type == Marker.MARKERTYPE.BEGIN) {
//                    stBegin = thisMarker.getBegin();
//                    sentenceStarted = true;
//                } else if (sentences.size() > 0) {
//                    int stEnd = thisMarker.getBegin();
////                right trim
//                    while (stEnd >= 1 && (Character.isWhitespace(text.charAt(stEnd - 1)) || (int) text.charAt(stEnd - 1) == 160)) {
//                        stEnd--;
//                    }
//                    Span lastSentence = sentences.get(sentences.size() - 1);
//                    lastSentence.setEnd(thisMarker.getEnd());
//                    lastSentence.setText(text.substring(lastSentence.begin, lastSentence.end));
//                }
//            }
//        }


        if (logger.isLoggable(Level.FINE)) {
            for (Span sentence : sentences) {
                logger.fine("Sentence(" + sentence.begin + "-" + sentence.end + "):\t" + ">" + text.substring(sentence.begin, sentence.end) + "<");
            }
        }
        return sentences;
    }

    private ArrayList<Marker> createMarkers(ArrayList<Marker> begins, ArrayList<Marker> ends, String text) {
        ArrayList<Marker> markers = new ArrayList<>();
        if (begins == null || begins.size() == 0) {
            markers.add(new Marker(0, Marker.MARKERTYPE.BEGIN));
        }
        if (ends == null || ends.size() == 0) {
            markers.add(new Marker(text.length() - 0.4f, Marker.MARKERTYPE.END));
        }
        for (int i = 0; i < begins.size(); i++) {

        }

        if (begins == null) {
            markers.add(new Marker(0, Marker.MARKERTYPE.BEGIN));
            if (ends != null)
                addEndMarkers(ends, markers);
            else
                markers.add(new Marker(text.length() - 0.4f, Marker.MARKERTYPE.END));
        } else {
            if (ends == null) {
                addBeginMarkers(begins, markers);
                markers.add(new Marker(text.length() - 0.4f, Marker.MARKERTYPE.END));
            } else {
                Iterator<Marker> bIter = begins.iterator();
                Iterator<Marker> eIter = ends.iterator();
//                create a relatively sorted list to reduce the sorting time.
                while (bIter.hasNext() || eIter.hasNext()) {
                    if (bIter.hasNext()) {
                        Span beginSpan = bIter.next();
                        markers.add(new Marker(beginSpan.getBegin(), Marker.MARKERTYPE.BEGIN));
                    } else {
                        while (eIter.hasNext()) {
                            Span endSpan = eIter.next();
                            markers.add(new Marker(endSpan.getEnd(), Marker.MARKERTYPE.END));
                        }
                    }
                    if (eIter.hasNext()) {
                        Span endSpan = eIter.next();
                        markers.add(new Marker(endSpan.getEnd(), Marker.MARKERTYPE.END));
                    } else {
                        while (bIter.hasNext()) {
                            Span beginSpan = bIter.next();
                            markers.add(new Marker(beginSpan.getBegin(), Marker.MARKERTYPE.BEGIN));
                        }
                    }
                }
            }
        }
        Collections.sort(markers);
        return markers;
    }

    private void addBeginMarkers(ArrayList<Marker> spans, ArrayList<Marker> markers) {
        for (Span stend : spans) {
            if (fcrp.getRule(stend.ruleId).type == DeterminantValueSet.Determinants.ACTUAL)
                markers.add(new Marker(stend.begin, Marker.MARKERTYPE.BEGIN));
        }
    }

    private void addEndMarkers(ArrayList<Marker> spans, ArrayList<Marker> markers) {
        for (Span stend : spans) {
            if (fcrp.getRule(stend.ruleId).type == DeterminantValueSet.Determinants.ACTUAL)
                markers.add(new Marker(stend.end - 0.4f, Marker.MARKERTYPE.END));
        }
    }

    public ArrayList<Span> tokenize(String text) {
        ArrayList<Span>sentence=new ArrayList<>();
        sentence.add(new Span(0, text.length()));
        ArrayList<ArrayList<Span>> tokenss = tokenize(sentence, text);
        return tokenss.get(0);
    }

    /**
     * Tokenizer aligns with sentence splitter
     *
     * @param sentences segmented sentences
     * @param text
     * @return
     */
    public ArrayList<ArrayList<Span>> tokenize(ArrayList<Span> sentences, String text) {
        ArrayList<ArrayList<Span>> tokenss = new ArrayList<>();
        ArrayList<Span> tokens = new ArrayList<>();
        if (!result.containsKey(TOKENBEGIN)) {
            for (Span sentence : sentences) {
                if (mLanguage.equals("en")) {
                    tokens = SimpleParser.tokenizeDecimalSmart(text.substring(sentence.begin, sentence.end), true, 0, true);
                } else {
                    tokens = SmartChineseCharacterSplitter.tokenizeDecimalSmart(text.substring(sentence.begin, sentence.end), true, 0);
                }
                for (Span token : tokens) {
                    token.begin += sentence.begin;
                    token.end += sentence.begin;
                    if (fillTextInSpan) {
                        token.text = text.substring(token.begin, token.end);
                    }
                }
                tokenss.add(tokens);
            }

        } else {
            ArrayList<Marker> tobegins = result.get(TOKENBEGIN);
            ArrayList<Marker> toends = result.get(TOKENEND);
            Collections.sort(tobegins);
            Collections.sort(toends);
            tokenss.add(new ArrayList<>());
            tokens = tokenss.get(tokenss.size() - 1);

            int toBegin = 0;
            boolean tokenStarted = false;
            int toEnd = 0, toBeginId, toEndId = 0;
            int sentenceId = 0;
            Span currentSentence = sentences.get(sentenceId);
            for (toBeginId = 0; toBeginId < tobegins.size(); toBeginId++) {
                if (sentenceId == sentences.size())
                    break;
                if (!tokenStarted) {
                    toBegin = tobegins.get(toBeginId).begin;
                    if (fcrp.getRule(tobegins.get(toBeginId).ruleId).type == DeterminantValueSet.Determinants.PSEUDO
                            || toBegin < toEnd)
                        continue;
                    tokenStarted = true;
                } else if (tobegins.get(toBeginId).begin < toEnd) {
                    continue;
                }
                for (int k = toEndId; k < toends.size(); k++) {
                    if (fcrp.getRule(toends.get(k).ruleId).type == DeterminantValueSet.Determinants.PSEUDO)
                        continue;
                    if (toBeginId < tobegins.size() - 1 && k < toends.size() - 1
                            && tobegins.get(toBeginId + 1).getBegin() < toends.get(k).begin + 1) {
                        break;
                    }
                    toEndId = k;
                    toEnd = toends.get(k).end;


                    if (toEnd < toBegin)
//                   if this TOKENEND is for previous token, move the pointer to the next
                        continue;
                    else if (toEnd == toBegin) {
                        if (tokens.size() > 0) {
                            Span lastToken = tokens.get(tokens.size() - 1);
                            lastToken.setEnd(toEnd);
                            lastToken.setText(text.substring(lastToken.begin, lastToken.end));
                            tokens.set(tokens.size() - 1, lastToken);
                        } else {
                            ArrayList<Span> previousSentence = tokenss.get(tokenss.size() - 2);
                            Span lastToken = previousSentence.get(previousSentence.size() - 1);
                            lastToken.setEnd(toEnd);
                            tokens.set(tokens.size() - 1, lastToken);
                            lastToken.setText(text.substring(lastToken.begin, lastToken.end));
                            previousSentence.set(previousSentence.size() - 1, lastToken);
                        }
                        continue;
                    } else if (tokenStarted) {
//                   if current status is after a token TOKENBEGIN marker
                        if (toBegin > currentSentence.end) {
                            sentenceId++;
                            currentSentence = sentences.get(sentenceId);
                            tokenss.add(new ArrayList<>());
                            tokens = tokenss.get(tokenss.size() - 1);
                        } else if (toEnd > currentSentence.end) {
//                        if the token is not appropriately tokenized.
                            if (fillTextInSpan)
                                tokens.add(new Span(toBegin, currentSentence.end, text.substring(toBegin, currentSentence.end)));
                            else
                                tokens.add(new Span(toBegin, currentSentence.end));
                            sentenceId++;
                            currentSentence = sentences.get(sentenceId);
                            tokenss.add(new ArrayList<>());
                            tokens = tokenss.get(tokenss.size() - 1);
                            toBegin = currentSentence.begin;
                        }
                        if (fillTextInSpan) {
                            tokens.add(new Span(toBegin, toEnd, text.substring(toBegin, toEnd)));
                        } else
                            tokens.add(new Span(toBegin, toEnd));
                        tokenStarted = false;
                        if (toBeginId == tobegins.size() - 1 ||
                                (k < toends.size() - 1
                                        && tobegins.get(toBeginId + 1).getBegin() > toends.get(k + 1).getEnd()))
                            continue;

                        toEndId++;
                        break;
                    } else {
//                   if current status is after a token TOKENEND marker, then replace the last output
                        Span tmp;
                        if (fillTextInSpan)
                            tmp = new Span(toBegin, toEnd, text.substring(toBegin, toEnd));
                        else
                            tmp = new Span(toBegin, toEnd);
                        if (tokens.size() > 0) {
                            tokens.set(tokens.size() - 1, tmp);
                        } else {
                            ArrayList<Span> previousSentence = tokenss.get(tokenss.size() - 2);
                            previousSentence.set(previousSentence.size() - 1, tmp);
                        }
                        tokenStarted = false;
                    }
                }
            }
        }

        int lastSentence = tokenss.size() - 1;
        if (tokenss.get(lastSentence).

                size() == 0) {
            tokenss.remove(lastSentence);
        }
        return tokenss;
    }
}