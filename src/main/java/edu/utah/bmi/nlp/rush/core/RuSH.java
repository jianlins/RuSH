package edu.utah.bmi.nlp.rush.core;

import edu.utah.bmi.nlp.core.IOUtil;
import edu.utah.bmi.nlp.core.SimpleParser;
import edu.utah.bmi.nlp.core.Span;
import edu.utah.bmi.nlp.core.WildCardChecker;
import edu.utah.bmi.nlp.fastcner.FastRuSHFactory;
import edu.utah.bmi.nlp.fastcner.FastRuSHRule_H;
import edu.utah.bmi.nlp.fastcner.FastRuSHRule_HCN;
import org.apache.uima.examples.tokenizer.SimpleTokenAndSentenceAnnotator;

import java.util.ArrayList;
import java.util.Map;
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
        ArrayList<Marker> markers = result.get("st");
        boolean sentenceStarted = false;
        int stBegin = 0;
        if (markers == null)
            fixGap(text, sentences, 0, text.length());
        else
            for (int i = 0; i < markers.size(); i++) {
                Marker thisMarker = markers.get(i);
                int currentPosition = thisMarker.getPosition();
                if (sentenceStarted) {
                    if (thisMarker.type == Marker.MARKERTYPE.END) {
                        if (autofixGap && sentences.size() > 0) {
                            fixGap(text, sentences, sentences.get(sentences.size() - 1).end, stBegin);
                        }
                        sentences.add(new Span(stBegin, currentPosition));
                        sentenceStarted = false;
                    }
                } else {
                    if (thisMarker.type == MARKERTYPE.BEGIN) {
                        stBegin = currentPosition;
                        sentenceStarted = true;
                    } else if (sentences.size() > 0) {
                        int stEnd = currentPosition;
//                right trim
                        while (stEnd >= 1 && (Character.isWhitespace(text.charAt(stEnd - 1)) || (int) text.charAt(stEnd - 1) == 160)) {
                            stEnd--;
                        }
                        sentences.get(sentences.size() - 1).setEnd(stEnd);
                    }
                }
            }
        if (logger.isLoggable(Level.FINE)) {
            for (Span sentence : sentences) {
                logger.fine("Sentence(" + sentence.begin + "-" + sentence.end + "):\t" + ">"
//                        + text.substring(sentence.fbegin, sentence.fend) + "<"
                );
            }
        }
        return sentences;
    }

    public ArrayList<Span> tokenize(String text) {
        ArrayList<Span> tokens = new ArrayList<>();
        ArrayList<Marker> markers = result.get("to");
        int toBegin = 0;
        boolean tokenStarted = false;
        for (int i = 0; i < markers.size(); i++) {
            Marker thisMarker = markers.get(i);
            if (tokenStarted) {
                if (thisMarker.type == MARKERTYPE.END) {
                    if (fillTextInSpan) {
                        Span span = new Span(toBegin, thisMarker.getPosition());
                        span.setText(text.substring(span.begin, span.end));
                        tokens.add(span);
                    } else
                        tokens.add(new Span(toBegin, thisMarker.getPosition()));
                    tokenStarted = false;
                }
            } else {
                if (thisMarker.type == MARKERTYPE.BEGIN) {
                    toBegin = thisMarker.getPosition();
                    tokenStarted = true;
                } else if (tokens.size() > 0) {
                    if (fillTextInSpan) {
                        Span lastToken = tokens.get(tokens.size() - 1);
                        lastToken.setEnd(thisMarker.getPosition());
                        lastToken.setText(text.substring(lastToken.begin, lastToken.end));

                    } else
                        tokens.get(tokens.size() - 1).setEnd(thisMarker.getPosition());
                }
            }
        }
        return tokens;
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
        if (!result.containsKey("to")) {
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
            tokenss.add(new ArrayList<>());
            ArrayList<Marker> markers = result.get("to");
            int toBegin = 0;
            boolean tokenStarted = false;
            int toEnd = 0, toBeginId, toEndId = 0;
            int sentenceId = 0;
            tokens = tokenss.get(tokenss.size() - 1);
            Span currentSentence = sentences.get(sentenceId);
            for (int i = 0; i < markers.size(); i++) {
                Marker thisMarker = markers.get(i);
                int thisPosition = thisMarker.getPosition();
                if (sentenceId == sentences.size())
                    break;
                if (tokenStarted) {
                    if (thisMarker.type == MARKERTYPE.END) {
                        toEnd = thisPosition;
                        if (tokens.size() > 0) {
                            if (toBegin > currentSentence.end) {
                                sentenceId++;
                                currentSentence = sentences.get(sentenceId);
                                tokenss.add(new ArrayList<>());
                                tokens = tokenss.get(tokenss.size() - 1);
                            } else if (toEnd > currentSentence.end) {
//                            the token is not appropriately tokenized.
                                if (fillTextInSpan)
                                    tokens.add(new Span(toBegin, currentSentence.end, text.substring(toBegin, currentSentence.end)));
                                else
                                    tokens.add(new Span(toBegin, currentSentence.end));
                                sentenceId++;
                                if (sentenceId >= sentences.size())
                                    break;
                                currentSentence = sentences.get(sentenceId);
                                tokenss.add(new ArrayList<>());
                                tokens = tokenss.get(tokenss.size() - 1);
                                toBegin = currentSentence.begin;
                            }

                        }
                        if (fillTextInSpan) {
                            tokens.add(new Span(toBegin, toEnd, text.substring(toBegin, toEnd)));
                        } else
                            tokens.add(new Span(toBegin, toEnd));
                        tokenStarted = false;
                    }
                } else {
                    if (thisMarker.type == MARKERTYPE.BEGIN) {
                        // new token start                    }
                        toBegin = thisPosition;
                        tokenStarted = true;
                    } else if (tokens.size() > 0) {
                        // continued TOKENEND marker
                        if (thisPosition > currentSentence.end) {
//                        if the token is not appropriately tokenized.
                            Span lastToken = tokens.get(tokens.size() - 1);
                            lastToken.setEnd(currentSentence.end);
                            if (fillTextInSpan) {
                                lastToken.setText(text.substring(lastToken.begin, lastToken.end));
                            }
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