package edu.utah.bmi.nlp.rush.core;

import edu.utah.bmi.nlp.core.*;
import edu.utah.bmi.nlp.fastcner.FastCNER;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Character.isAlphabetic;
import static java.lang.Character.isDigit;

import edu.utah.bmi.nlp.rush.core.Marker.MARKERTYPE;

public class RuSH2 implements RuSHInf{
    protected static Logger logger = IOUtil.getLogger(RuSH2.class);
    protected static FastCNER fcrp;
    protected static final String STBEGIN = "stbegin", STEND = "stend";
    protected HashMap<String, ArrayList<Span>> result;
    public boolean autofixGap = true;
    protected static final String TOKENBEGIN = "tobegin", TOKENEND = "toend";
    public boolean tokenRuleEnabled = false;
    public boolean fillTextInSpan = false;

    @Deprecated
    protected boolean debug = false;


    public RuSH2(String rule) {
        initiate(rule);
    }

    public void initiate(String rule) {
        fcrp = new FastCNER(rule);
        IOUtil ioUtil = new IOUtil(rule);
        ArrayList<String> row = ioUtil.getRuleCells().get(0);
        String lastCell = row.get(row.size() - 1);
        if (!lastCell.equals("ACTUAL") && !lastCell.equals("PSEUDO"))
//      compatible to old version rules
            for (Map.Entry<Integer, Rule> entry : fcrp.fastRule.ruleStore.entrySet()) {
                int id = entry.getKey();
                Rule singleRule = entry.getValue();
                if (!tokenRuleEnabled && singleRule.ruleName.equals(TOKENBEGIN))
                    tokenRuleEnabled = true;
                if (singleRule.score % 2 != 0)
                    singleRule.type = DeterminantValueSet.Determinants.PSEUDO;
                fcrp.fastRule.ruleStore.put(id, singleRule);
                fcrp.fastRule.initiate(fcrp.fastRule.ruleStore);
            }

        fcrp.setReplicationSupport(true);

        fcrp.setCompareMethod("scorewidth");

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
        ArrayList<Marker> markers = new ArrayList<>();
        ArrayList<Span> stbegins = result.get(STBEGIN);
        if (stbegins == null) {
            markers.add(new Marker(0, MARKERTYPE.BEGIN));
        } else {
            for (Span stbegin : stbegins) {
                if (fcrp.getRule(stbegin.ruleId).type == DeterminantValueSet.Determinants.ACTUAL)
                    markers.add(new Marker(stbegin.begin, MARKERTYPE.BEGIN));
            }
        }
        ArrayList<Span> stends = result.get(STEND);
        if (stends == null) {
            markers.add(new Marker(text.length() - 0.4f, MARKERTYPE.END));
        } else {
            for (Span stend : stends) {
                if (fcrp.getRule(stend.ruleId).type == DeterminantValueSet.Determinants.ACTUAL)
                    markers.add(new Marker(stend.end - 0.4f, MARKERTYPE.END));
            }
        }
        Collections.sort(markers);
        boolean sentenceStarted = false;
        ArrayList<Span> sentenceSpans = new ArrayList<>();
        int stBegin = 0;
        for (int i = 0; i < markers.size(); i++) {
            Marker thisMarker = markers.get(i);
            if (sentenceStarted) {
                if (thisMarker.type == MARKERTYPE.END) {
                    sentenceSpans.add(new Span(stBegin, thisMarker.getPosition()));
                }
            } else {
                if (thisMarker.type == MARKERTYPE.BEGIN) {
                    stBegin = thisMarker.getPosition();
                } else if (sentenceSpans.size() > 0) {
                    int stEnd = thisMarker.getPosition();
//                right trim
                    while (stEnd >= 1 && (Character.isWhitespace(text.charAt(stEnd - 1)) || (int) text.charAt(stEnd - 1) == 160)) {
                        stEnd--;
                    }
                    sentenceSpans.get(sentenceSpans.size() - 1).setEnd(thisMarker.getPosition());
                }
            }
        }



        int stEnd = 0, stBeginId, stEndId = 0;
        for (stBeginId = 0; stBeginId < stbegins.size(); stBeginId++) {
            if (!sentenceStarted) {
                stBegin = stbegins.get(stBeginId).begin;
                if (stbegins.get(stBeginId).ruleId != -1 && (fcrp.getRule(stbegins.get(stBeginId).ruleId).type == DeterminantValueSet.Determinants.PSEUDO || stBegin < stEnd))
                    continue;
                sentenceStarted = true;
            } else if (stbegins.get(stBeginId).begin < stEnd) {
                continue;
            }
            for (int k = stEndId; k < stends.size(); k++) {
                if (stends.get(k).ruleId != -1 && (fcrp.getRule(stends.get(k).ruleId).type == DeterminantValueSet.Determinants.PSEUDO))
                    continue;
                if (stBeginId < stbegins.size() - 1 && k < stends.size() - 1
                        && stbegins.get(stBeginId + 1).getBegin() < stends.get(k).begin + 1) {
                    break;
                }
                stEnd = stends.get(k).begin + 1;
                stEndId = k;
//                right trim
                while (stEnd >= 1 && (Character.isWhitespace(text.charAt(stEnd - 1)) || (int) text.charAt(stEnd - 1) == 160)) {
                    stEnd--;
                }

                if (stEnd < stBegin)
//                   if this STEND is for previous sentence, move the pointer to the next
                    continue;
                else if (sentenceStarted) {
//                   if current status is after a sentence STBEGIN marker
                    if (autofixGap && sentences.size() > 0) {
                        fixGap(text,sentences, sentences.get(sentences.size() - 1).end, stBegin);
                    }
                    if (fillTextInSpan)
                        sentences.add(new Span(stBegin, stEnd, text.substring(stBegin, stEnd)));
                    else
                        sentences.add(new Span(stBegin, stEnd));
                    sentenceStarted = false;
                    if (stBeginId == stbegins.size() - 1 ||
                            (k < stends.size() - 1
                                    && stbegins.get(stBeginId + 1).getBegin() > stends.get(k + 1).getEnd()))
                        continue;
                    break;
                } else {
//                   if current status is after a sentence STEND marker, then replace the last output
                    if (fillTextInSpan)
                        sentences.set(sentences.size() - 1, new Span(stBegin, stEnd, text.substring(stBegin, stEnd)));
                    else
                        sentences.set(sentences.size() - 1, new Span(stBegin, stEnd));
                    sentenceStarted = false;
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


    public ArrayList<ArrayList<Span>> tokenize(ArrayList<Span> sentences, String text) {
        if (!tokenRuleEnabled)
            return new ArrayList<>();
        ArrayList<ArrayList<Span>> tokenss = new ArrayList<>();
        ArrayList<Span> tobegins = result.get(TOKENBEGIN);
        ArrayList<Span> toends = result.get(TOKENEND);
        Collections.sort(tobegins);
        Collections.sort(toends);
        tokenss.add(new ArrayList<>());
        ArrayList<Span> tokens = tokenss.get(tokenss.size() - 1);

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
        int lastSentence = tokenss.size() - 1;
        if (tokenss.get(lastSentence).size() == 0) {
            tokenss.remove(lastSentence);
        }
        return tokenss;
    }

    @Deprecated
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setSpecialCharacterSupport(Boolean scSupport) {
        fcrp.setSpecialCharacterSupport(scSupport);
    }
}
