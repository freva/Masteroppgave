package com.freva.masteroppgave.preprocessing.filters;

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
    public static final Pattern non_splitting_chars = Pattern.compile("['`´]");
    public static final Pattern non_regular_text = Pattern.compile("[^a-zA-Z.,!?]");
    public static final Pattern fix_spaces = Pattern.compile("\\s*([?!.,]+(?:\\s+[?!.,]+)*)\\s*");


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
}
