package com.freva.masteroppgave.preprocessing.filters;

import org.apache.commons.lang3.StringEscapeUtils;

public class Filters {
    public static final String USERNAME_PLACEHOLDER = "||U||";
    public static final String HASHTAG_PLACEHOLDER = "||H||";
    public static final String RTTAG_PLACEHOLDER = "||RT||";
    public static final String URL_PLACEHOLDER = "||URL||";


    public static String HTMLUnescape(String text) {
        return StringEscapeUtils.unescapeHtml4(text);
    }


    public static String removeEmoticons(String text) {
        return RegexFilters.replaceEmoticons(text, "");
    }


    public static String removeUsername(String text) {
        return RegexFilters.replaceUsername(text, "");
    }

    public static String placeholderUsername(String text) {
        return RegexFilters.replaceUsername(text, USERNAME_PLACEHOLDER);
    }


    public static String removeHashtag(String text) {
        return RegexFilters.replaceHashtag(text, "");
    }

    public static String placeholderHashtag(String text) {
        return RegexFilters.replaceHashtag(text, HASHTAG_PLACEHOLDER);
    }

    public static String hashtagToWord(String text) {
        return RegexFilters.replaceHashtag(text, "\1");
    }


    public static String removeRTTag(String text) {
        return RegexFilters.replaceRTTag(text, "");
    }

    public static String placeholderRTTag(String text) {
        return RegexFilters.replaceHashtag(text, RTTAG_PLACEHOLDER);
    }


    public static String removeURL(String text) {
        return RegexFilters.replaceURL(text, "");
    }

    public static String placeholderURL(String text) {
        return RegexFilters.replaceURL(text, URL_PLACEHOLDER);
    }
}
