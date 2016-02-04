package com.freva.masteroppgave.preprocessing.filters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegexFilters {
    //Emoticon definitions.
    private static final String NormalEyes = "[:=8]";
    private static final String HappyEyes = "[xX]";
    private static final String WinkEyes = "[;]";
    private static final String NoseArea = "[Ooc^*'-]?";
    private static final String HappyMouths = "[Dd)*>}\\]]";
    private static final String SadMouths = "[c<|@L{\\/\\(\\[]";
    private static final String TongueMouths = "[pP]";

    public static final Pattern EMOTICON_NEGATIVE = Pattern.compile(NormalEyes + NoseArea + SadMouths);
    public static final Pattern EMOTICON_POSITIVE = Pattern.compile("(^_^|" + "((" + NormalEyes + "|" +
            HappyEyes + "|" + WinkEyes + ")" + NoseArea + HappyMouths + ")|(?:<3+))");

    //Twitter basic elements
    public static final Pattern TWITTER_USERNAME = Pattern.compile("(@\\w{1,15})");
    public static final Pattern TWITTER_HASHTAG = Pattern.compile("#([a-zA-Z]+\\w*)");
    public static final Pattern TWITTER_RT_TAG = Pattern.compile("(^RT\\s+|\\s+RT\\s+)");
    public static final Pattern TWITTER_URL = Pattern.compile("(https?:\\/\\/\\S+)");

    public static final Pattern WHITESPACE = Pattern.compile("\\s+");
    public static final Pattern POS_TAG = Pattern.compile("_[A-Z]+\b");
    public static final Pattern INNER_WORD_CHAR = Pattern.compile("['`´]");
    public static final Pattern NON_SYNTACTICAL_TEXT = Pattern.compile("[^a-zA-Z.,!?]");
    public static final Pattern NON_POS_TAGGED_ALPHABETICAL_TEXT = Pattern.compile("[^a-zA-Z_ ]");

    private static final Pattern freeUnderscores = Pattern.compile(" _|_ ");
    private static final Pattern fixSyntacticalGrammar = Pattern.compile("\\s*([?!.,]+(?:\\s+[?!.,]+)*)\\s*");


    public static String replaceEmoticons(String text, String replace) {
        text = EMOTICON_POSITIVE.matcher(text).replaceAll(replace);
        return EMOTICON_NEGATIVE.matcher(text).replaceAll(replace);
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

    public static String replaceNonPosTaggedAlphabeticalText(String text, String replace) {
        text = NON_POS_TAGGED_ALPHABETICAL_TEXT.matcher(text).replaceAll(replace);
        return freeUnderscores.matcher(text).replaceAll("");
    }


    public static String fixSyntacticalPunctuationGrammar(String text) {
        StringBuffer resultString = new StringBuffer();
        Matcher matcher = fixSyntacticalGrammar.matcher(text);
        if(matcher.find()) {
            do {
                matcher.appendReplacement(resultString, RegexFilters.replaceWhitespace(matcher.group(1), "") + " ");
            } while (matcher.find());
            return resultString.toString();
        }
        return text;
    }
}
