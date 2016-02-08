package com.freva.masteroppgave.preprocessing.filters;

import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang3.StringEscapeUtils;

import java.text.Normalizer;

public class Filters {
    public static final String USERNAME_PLACEHOLDER = "||U||";
    public static final String HASHTAG_PLACEHOLDER = "||H||";
    public static final String RTTAG_PLACEHOLDER = "||RT||";
    public static final String URL_PLACEHOLDER = "||URL||";


    public static String HTMLUnescape(String text) {
        return StringEscapeUtils.unescapeHtml4(text);
    }

    public static String normalizeForm(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD);
    }

    public static String removeRepeatedWhitespace(String text) {
        return RegexFilters.replaceWhitespace(text, " ");
    }


    public static String parseUnicodeEmoticons(String text) {
        return EmojiParser.parseToAliases(text);
    }

    public static String removeUnicodeEmoticons(String text) {
        return EmojiParser.removeAllEmojis(text);
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


    public static String removePosTags(String text) {
        return RegexFilters.replacePosTag(text, "");
    }

    public static String removeInnerWordCharacters(String text) {
        return RegexFilters.replaceInnerWordCharacters(text, "");
    }

    public static String removeNonSyntacticalText(String text) {
        return RegexFilters.replaceNonSyntacticalText(text, " ");
    }

    public static String removeNonAlphanumericalText(String text) {
        return RegexFilters.replaceNonAlphanumericalText(text, " ");
    }

    public static String removeNonPosTaggedAlphabeticalText(String text) {
        return RegexFilters.replaceNonPosTaggedAlphabeticalText(text, "");
    }

    public static String removeFreeDigits(String text) {
        return RegexFilters.replaceFreeDigits(text, "");
    }


    public static String fixSyntacticalPunctuationGrammar(String text) {
        return RegexFilters.fixSyntacticalPunctuationGrammar(text);
    }
}
