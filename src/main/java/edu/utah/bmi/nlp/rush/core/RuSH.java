package edu.utah.bmi.nlp.rush.core;

import edu.utah.bmi.nlp.core.DeterminantValueSet;
import edu.utah.bmi.nlp.core.IOUtil;
import edu.utah.bmi.nlp.core.Rule;
import edu.utah.bmi.nlp.core.Span;
import edu.utah.bmi.nlp.fastcner.FastCNER;
import edu.utah.bmi.nlp.rush.uima.RuSH_AE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RuSH {
    private static Logger logger = IOUtil.getLogger(RuSH.class);
    private static FastCNER fcrp;
    private static final String begin = "stbegin", end = "stend";
    @Deprecated
    protected boolean debug = false;


    public RuSH(String rule) {
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
                if (singleRule.score % 2 == 0)
                    singleRule.type = DeterminantValueSet.Determinants.PSEUDO;
                fcrp.fastRule.ruleStore.put(id, singleRule);
                fcrp.fastRule.initiate(fcrp.fastRule.ruleStore);
            }

        fcrp.setReplicationSupport(true);

        fcrp.setCompareMethod("scorewidth");

    }

    public ArrayList<String> segToSentenceStrings(String text) {
        return new ArrayList<>();

    }

    public ArrayList<Span> segToSentenceSpans(String text) {
        ArrayList<Span> output = new ArrayList<>();
        HashMap<String, ArrayList<Span>> result = fcrp.processString(text);

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

        ArrayList<Span> begins = result.get(begin);
        ArrayList<Span> ends = result.get(end);

//        if(begins==null)
//        System.out.println(text);
        if (begins == null || begins.size() == 0) {
            begins = new ArrayList<>();
            begins.add(new Span(0, 1, 1, -1));
        }
        if (ends == null || ends.size() == 0) {
            ends = new ArrayList<>();
            ends.add(new Span(text.length() - 1, text.length(), 1, -1));
        }


        int stBegin = 0;
        boolean sentenceStarted = false;
        int stEnd = 0, i, j = 0;
        for (i = 0; i < begins.size(); i++) {
            if (!sentenceStarted) {
                stBegin = begins.get(i).begin;
                if (begins.get(i).score == 1 || stBegin < stEnd)
                    continue;
                sentenceStarted = true;
            } else if (begins.get(i).begin < stEnd) {
                continue;
            }
            for (int k = j; k < ends.size(); k++) {
                if (ends.get(k).score == 3)
                    continue;
                if (i < begins.size() - 1 && k < ends.size() - 1
                        && begins.get(i + 1).getBegin() < ends.get(k).begin + 1) {
                    break;
                }
                stEnd = ends.get(k).begin + 1;
                j = k;
//                right trim
                while (stEnd >= 1 && (Character.isWhitespace(text.charAt(stEnd - 1)) || (int) text.charAt(stEnd - 1) == 160)) {
                    stEnd--;
                }

                if (stEnd < stBegin)
//                   if this end is for previous sentence, move the pointer to the next
                    continue;
                else if (sentenceStarted) {
//                   if current status is after a sentence begin marker
                    output.add(new Span(stBegin, stEnd));
                    sentenceStarted = false;
                    if (i == begins.size() - 1 ||
                            (k < ends.size() - 1
                                    && begins.get(i + 1).getBegin() > ends.get(k + 1).getEnd()))
                        continue;
                    break;
                } else {
//                   if current status is after a sentence end marker, then replace the last output
                    output.set(output.size() - 1, new Span(stBegin, stEnd));
                    sentenceStarted = false;
                }
            }
        }
        if (logger.isLoggable(Level.FINE)) {
            for (Span sentence : output) {
                logger.fine("Sentence(" + sentence.begin + "-" + sentence.end + "):\t" + ">" + text.substring(sentence.begin, sentence.end) + "<");
            }
        }
        return output;
    }

    @Deprecated
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setSpecialCharacterSupport(Boolean scSupport) {
        fcrp.setSpecialCharacterSupport(scSupport);
    }
}
