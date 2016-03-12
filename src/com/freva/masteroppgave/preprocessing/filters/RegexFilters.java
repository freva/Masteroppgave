package com.freva.masteroppgave.preprocessing.filters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegexFilters {
    //Emoticon definitions.
    private static final String NormalEyes = "[:=8]";
    private static final String WinkEyes = "[;]";
    private static final String NoseArea = "[Ooc^*'-]?";
    private static final String HappyMouths = "[)*>}\\]]";
    private static final String SadMouths = "[c<|@L{/\\(\\[]";
    private static final String conditionalEmotes = "([x=;:]" + NoseArea + "d|[=;:]p)";

    public static final Pattern EMOTICON_NEGATIVE = Pattern.compile("(" + NormalEyes + NoseArea + SadMouths + ")");
    public static final Pattern EMOTICON_POSITIVE = Pattern.compile("(\\^_\\^|<3+|" + "((" + NormalEyes + "|" +
            WinkEyes + ")" + NoseArea + HappyMouths + "))");
    public static final Pattern EMOTICON_CONDITIONAL_LEFT = Pattern.compile("(?:(?:^|\\s)" + conditionalEmotes + ")", Pattern.CASE_INSENSITIVE);
    public static final Pattern EMOTICON_CONDITIONAL_RIGHT = Pattern.compile("(?:" + conditionalEmotes + "(?:$|\\s))", Pattern.CASE_INSENSITIVE);

    //Twitter basic elements
    public static final Pattern TWITTER_USERNAME = Pattern.compile("(@\\w{1,15})");
    public static final Pattern TWITTER_HASHTAG = Pattern.compile("#([a-zA-Z]+\\w*)");
    public static final Pattern TWITTER_RT_TAG = Pattern.compile("(^RT\\s+|\\s+RT\\s+)");
    public static final Pattern TWITTER_URL = Pattern.compile("((https?://|www)\\S+)");
    public static final Pattern TWITTER_EMAIL = Pattern.compile("\\w+@\\S+");

    public static final Pattern WHITESPACE = Pattern.compile("\\s+");
    public static final Pattern POS_TAG = Pattern.compile("_[A-Z$]+");
    public static final Pattern INNER_WORD_CHAR = Pattern.compile("['`´’]");
    public static final Pattern NON_SYNTACTICAL_TEXT = Pattern.compile("[^a-z ?!.,]", Pattern.CASE_INSENSITIVE);
    public static final Pattern NON_SYNTACTICAL_TEXT_PLUS = Pattern.compile("[^a-z ?!.]", Pattern.CASE_INSENSITIVE);
    public static final Pattern SENTENCE_END_PUNCTUATION = Pattern.compile("[!?,.]");

    public static final Pattern NON_ALPHANUMERIC_TEXT = Pattern.compile("[^a-zA-Z0-9 ]");
    public static final Pattern NON_ALPHABETIC_TEXT = Pattern.compile("[^a-zA-Z ]");
    public static final Pattern NON_POS_TAGGED_ALPHABETICAL_TEXT = Pattern.compile("[^a-zA-Z_ ]");
    public static final Pattern NON_ASCII_CHARACTERS = Pattern.compile("[^\\p{ASCII}]");
    public static final Pattern FREE_DIGITS = Pattern.compile("([^\\w]|^)[0-9]+([^\\w]+[0-9]+)*([^\\w]|$)");

    private static final Pattern freeUnderscores = Pattern.compile(" _|_ ");
    private static final Pattern fixSyntacticalGrammar = Pattern.compile("\\s*([!?,.]+(?:\\s+[!?,.]+)*)\\s*");
    private static final Pattern fixQuotationToSentence = Pattern.compile("([\"*(])((?:\\w+ )+\\w+ \\w+)(?:\\1|\\))");



    public static String replaceEmoticons(String text, String replace) {
        text = EMOTICON_POSITIVE.matcher(text).replaceAll(replace);
        text = EMOTICON_NEGATIVE.matcher(text).replaceAll(replace);
        text = EMOTICON_CONDITIONAL_LEFT.matcher(text).replaceAll(replace);
        return EMOTICON_CONDITIONAL_RIGHT.matcher(text).replaceAll(replace);
    }

    public static String replaceUsername(String text, String replace) {
        return TWITTER_USERNAME.matcher(text).replaceAll(replace);
    }

    public static String replaceHashtag(String text, String replace) {
        return TWITTER_HASHTAG.matcher(text).replaceAll(replace);
    }

    public static String replaceRTTag(String text, String replace) {
        return TWITTER_RT_TAG.matcher(text).replaceAll(replace);
    }

    public static String replaceURL(String text, String replace) {
        return TWITTER_URL.matcher(text).replaceAll(replace);
    }

    public static String replaceWhitespace(String text, String replace) {
        return WHITESPACE.matcher(text).replaceAll(replace);
    }

    public static String replacePosTag(String text, String replace) {
        return POS_TAG.matcher(text).replaceAll(replace);
    }

    public static String replaceInnerWordCharacters(String text, String replace) {
        return INNER_WORD_CHAR.matcher(text).replaceAll(replace);
    }

    public static String replaceNonSyntacticalText(String text, String replace) {
        return NON_SYNTACTICAL_TEXT.matcher(text).replaceAll(replace);
    }

    public static String replaceNonSyntacticalTextPlus(String text, String replace) {
        return NON_SYNTACTICAL_TEXT_PLUS.matcher(text).replaceAll(replace);
    }

    public static String replaceNonAlphanumericalText(String text, String replace) {
        return NON_ALPHANUMERIC_TEXT.matcher(text).replaceAll(replace);
    }

    public static String replaceNonAlphabeticText(String text, String replace) {
        return NON_ALPHABETIC_TEXT.matcher(text).replaceAll(replace);
    }

    public static String replaceNonPosTaggedAlphabeticalText(String text, String replace) {
        text = NON_POS_TAGGED_ALPHABETICAL_TEXT.matcher(text).replaceAll(replace);
        return freeUnderscores.matcher(text).replaceAll("");
    }

    public static String replaceFreeDigits(String text, String replace) {
        return FREE_DIGITS.matcher(text).replaceAll(replace);
    }

    public static String replaceNonASCII(String text, String replace) {
        return NON_ASCII_CHARACTERS.matcher(text).replaceAll(replace);
    }


    public static String fixQuotationSentence(String text) {
        return fixQuotationToSentence.matcher(text).replaceAll("$2.");
    }

    public static String fixSyntacticalPunctuationGrammar(String text) {
        StringBuffer resultString = new StringBuffer();
        Matcher matcher = fixSyntacticalGrammar.matcher(text);
        if(matcher.find()) {
            do {
                matcher.appendReplacement(resultString, RegexFilters.replaceWhitespace(matcher.group(1), "") + " ");
            } while (matcher.find());
            matcher.appendTail(resultString);
            return resultString.toString();
        }
        return text;
    }
}
