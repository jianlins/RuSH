package edu.utah.bmi.nlp.rush.core;

import edu.utah.bmi.nlp.core.*;
import edu.utah.bmi.nlp.fastcner.FastCNER;
import edu.utah.bmi.nlp.fastcner.FastCRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Character.isAlphabetic;
import static java.lang.Character.isDigit;

/**
 * Deprecated due to speed concerns, use RuSH3 instead---around 15~20% speed improvement.
 */
@Deprecated
public class RuSH implements RuSHInf {
    protected static Logger logger = IOUtil.getLogger(RuSH.class);
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


    public RuSH(String rule) {
        initiate(rule);
    }

    public void initiate(String rule) {
        Object[] values = RuSHFactory.createFastRuSHRule(rule);
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

        ArrayList<Span> stbegins = result.get(STBEGIN);
        ArrayList<Span> stends = result.get(STEND);


//        if(begins==null)
//        System.out.println(text);
        if (stbegins == null || stbegins.size() == 0) {
            stbegins = new ArrayList<>();
            stbegins.add(new Span(0, 1, -1, -1));
        }
        if (stends == null || stends.size() == 0) {
            stends = new ArrayList<>();
            stends.add(new Span(text.length() - 1, text.length(), -1, -1));
        }


        int stBegin = 0;
        boolean sentenceStarted = false;
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
                        fixGap(text, sentences.get(sentences.size() - 1).end, stBegin);
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

    public ArrayList<ArrayList<Span>> tokenize(ArrayList<Span> sentences, ArrayList<Span> tobegins, ArrayList<Span> toends, String text) {
        ArrayList<ArrayList<Span>> tokenss = new ArrayList<>();
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

    public void setSpecialCharacterSupport(Boolean scSupport) {
        fcrp.setSpecialCharacterSupport(scSupport);
    }

    public void setmLanguage(String mLanguage) {
        this.mLanguage = mLanguage;
    }

    public void setIncludePunctuation(boolean includePunctuation) {
        this.includePunctuation = includePunctuation;
    }
}
