package edu.utah.bmi.nlp.rush.core;

import edu.utah.bmi.nlp.core.SimpleParser;
import edu.utah.bmi.nlp.core.Span;
import edu.utah.bmi.nlp.fastcner.UnicodeChecker;

import java.util.ArrayList;

public class SmartChineseCharacterSplitter extends SimpleParser {

    protected static final int chineseChar = 5;

    public static ArrayList<Span> tokenizeDecimalSmart(String text, boolean includePunctuation) {
        return tokenizeDecimalSmart(text, includePunctuation, 0);
    }

    /**
     * TODO split include/exclude punctuation to two methods, reduce the times of if check
     * Tokenizer will consider float number as one token.
     *
     * @param text               input string for tokenizing
     * @param includePunctuation whether tokenize punctuations
     * @param offset             the snippet (sentence) offset to the beginning of the document
     * @return a sentence
     */
    public static ArrayList<Span> tokenizeDecimalSmart(String text, boolean includePunctuation, int offset) {
        ArrayList<Span> tokens = new ArrayList<Span>();
//        0: punctuation or return, 1: letter, 2: digit, 3: dot
//        whitespace = -1, punctuation = 0, dot = 1, returnc = 2, letter = 3, digit = 4;
        int type_2 = whitespace, type_1 = whitespace, type0 = whitespace;
        int tokenBegin = 0, tokenEnd = 0, sentenceBegin = 0, sentenceEnd = 0;
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char thisChar = text.charAt(i);
            if (thisChar == '.') {
                type0 = dot;
            } else if (UnicodeChecker.isPunctuation(thisChar)) {
                type0 = punctuation;
            } else if (thisChar == '\n' || thisChar == '\r') {
                type0 = returnc;
            } else if (Character.isDigit(thisChar)) {
                type0 = digit;
            } else if (isAlphbetic(thisChar)) {
                type0 = letter;
            } else if (UnicodeChecker.isChinese(thisChar)) {
                type0 = chineseChar;
            } else {
//                consider all other characters are whitespace, not included in tokens
                type0 = whitespace;
            }

            switch (type0) {
                case chineseChar:
                    switch (type_1) {
                        case whitespace:
                            tokenBegin = i;
                            break;
                        case punctuation:
                            if (includePunctuation) {
                                tokens.add(new Span(tokenBegin + offset, i + offset, tmp.toString()));
                                tmp.setLength(0);
                            }
                            tokenBegin = i;
                            break;
                        case dot:
                            if (type_2 == digit || type_2 == whitespace) {
                                tmp.append('.');
//                              char appended later outside switch
                                break;
                            } else {
                                if (tmp.length() > 0) {
                                    tokens.add(new Span(tokenBegin + offset, i - 1 + offset, tmp.toString()));
                                    tmp.setLength(0);
                                }
                                if (includePunctuation) {
                                    tokens.add(new Span(i - 1 + offset, i + offset, "."));
                                }
                                tokenBegin = i;
                            }
                            break;
                        default:
                            tokens.add(new Span(tokenBegin + offset, i + offset, tmp.toString()));
                            tmp.setLength(0);
                            tokenBegin = i;
                            break;
                    }
                    tmp.append(thisChar);
                    break;
                case dot:
                    if (type_1 == whitespace) {
                        tokenBegin = i;
                    }
                    if (i == text.length() - 1) {
                        if (tmp.length() > 0) {
                            tokens.add(new Span(tokenBegin + offset, i + offset, tmp.toString()));
                            tmp.setLength(0);
                        }
                        if (includePunctuation)
                            tokens.add(new Span(i + offset, i + 1 + offset, "."));
                    }
                    break;
                case punctuation:
                    if ((type_1 == letter || type_1 == digit || type_1==chineseChar) && tmp.length() > 0) {
                        tokens.add(new Span(tokenBegin + offset, i + offset, tmp.toString()));
                        tmp.setLength(0);
                        tokenBegin = i;
                    }
                    if (includePunctuation) {
                        tmp.append(thisChar);
                        switch (type_1) {
                            case punctuation:
                                break;
                            case dot:
                                if (tmp.length() > 0)
                                    tokens.add(new Span(tokenBegin + offset, i - 1 + offset, tmp.toString()));
                                tokens.add(new Span(i - 1 + offset, i + offset, "."));
                                tmp.setLength(0);
                                tokenBegin = i;
                                break;
                            case whitespace:
                            case returnc:
                                tokenBegin = i;
                                break;
                        }
                    }
                    break;
                case whitespace:
                case returnc:
                    switch (type_1) {
                        case digit:
                        case letter:
                        case chineseChar:
                            tokens.add(new Span(tokenBegin + offset, i + offset, tmp.toString()));
                            tmp.setLength(0);
                            break;
                        case punctuation:
                            if (includePunctuation) {
                                tokens.add(new Span(tokenBegin + offset, i + offset, tmp.toString()));
                                tmp.setLength(0);
                            }
                            break;
                        case dot:
                            if (tmp.length() > 0) {
                                tokens.add(new Span(tokenBegin + offset, i - 1 + offset, tmp.toString()));
                                tmp.setLength(0);
                            }
                            if (includePunctuation)
                                tokens.add(new Span(i - 1 + offset, i + offset, "."));
                            break;
                    }
                    tokenBegin = i;
                    break;
                case digit:
                    switch (type_1) {
                        case whitespace:
                            tokenBegin = i;
                            break;
                        case punctuation:
                            if (includePunctuation) {
                                tokens.add(new Span(tokenBegin + offset, i + offset, tmp.toString()));
                                tmp.setLength(0);
                            }
                            tokenBegin = i;
                            break;
                        case dot:
                            if (type_2 == digit || type_2 == whitespace) {
                                tmp.append('.');
//                              char appended later outside switch
                                break;
                            } else {
                                if (tmp.length() > 0) {
                                    tokens.add(new Span(tokenBegin + offset, i - 1 + offset, tmp.toString()));
                                    tmp.setLength(0);
                                }
                                if (includePunctuation) {
                                    tokens.add(new Span(i - 1 + offset, i + offset, "."));
                                }
                                tokenBegin = i;
                            }
                            break;
                        case letter:
                        case chineseChar:
                            tokens.add(new Span(tokenBegin + offset, i + offset, tmp.toString()));
                            tmp.setLength(0);
                            tokenBegin = i;
                            break;
                    }
                    tmp.append(thisChar);
                    break;
                case letter:
                    switch (type_1) {
                        case whitespace:
                        case returnc:
                            tokenBegin = i;
                            break;
                        case dot:
                            if (tmp.length() > 0) {
                                tokens.add(new Span(tokenBegin + offset, i - 1 + offset, tmp.toString()));
                                tmp.setLength(0);
                            }
                            if (includePunctuation)
                                tokens.add(new Span(i - 1 + offset, i + offset, "."));
                            tokenBegin = i;
                            break;
                        case punctuation:
                            if (includePunctuation) {
                                tokens.add(new Span(tokenBegin + offset, i + offset, tmp.toString()));
                                tmp.setLength(0);
                            }
                            tokenBegin = i;
                            break;
                        case digit:
                        case chineseChar:
                            tokens.add(new Span(tokenBegin + offset, i + offset, tmp.toString()));
                            tmp.setLength(0);
                            tokenBegin = i;
                            break;
                        case letter:
                            break;
                    }
                    tmp.append(thisChar);
                    break;
            }
            type_2 = type_1;
            type_1 = type0;
        }
        if (tmp.length() > 0)
            tokens.add(new Span(tokenBegin + offset, text.length() + offset, tmp.toString()));
        return tokens;
    }


    private static boolean isAlphbetic(char ch) {
        return (((((1 << Character.UPPERCASE_LETTER) |
                (1 << Character.LOWERCASE_LETTER)
        ) >> Character.getType(ch)) & 1)
                != 0);
    }
}
