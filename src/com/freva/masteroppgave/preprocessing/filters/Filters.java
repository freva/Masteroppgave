package com.freva.masteroppgave.preprocessing.filters;

import com.freva.masteroppgave.classifier.ClassifierOptions;
import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang3.StringEscapeUtils;

import java.text.Normalizer;
import java.util.List;
import java.util.function.Function;

public class Filters {
    public static final String USERNAME_PLACEHOLDER = " ||username|| ";
    public static final String HASHTAG_PLACEHOLDER = " ||hashtag|| ";
    public static final String RTTAG_PLACEHOLDER = " ||rt|| ";
    public static final String URL_PLACEHOLDER = " ||url|| ";

    private static final EmojiParser.EmojiTransformer EMOJI_ALIAS_TRANSFORMER = e -> " ||" + e.getEmoji().getAliases().get(0) + "|| ";
    private final List<Function<String, String>> stringFilters;
    private final List<Function<String, String>> tokenFilters;

    public Filters(List<Function<String, String>> stringFilters, List<Function<String, String>> tokenFilters) {
        this.stringFilters = stringFilters;
        this.tokenFilters = tokenFilters;
    }

    public String apply(String text) {
        text = stringChain(text, stringFilters);
        return tokenChain(text, tokenFilters).trim();
    }

    /**
     * Chain several filters after each other, applies the filter on the entire string
     *
     * @param text    String to format
     * @param filters Sequence of filters to apply on String
     * @return The formatted String
     */
    public static String stringChain(String text, Iterable<Function<String, String>> filters) {
        if (filters == null) return text;

        for (Function<String, String> filter : filters) {
            text = filter.apply(text);
        }

        return text;
    }

    /**
     * Chain several filters after each other, applying filters only on non special class tokens
     *
     * @param text    String to format
     * @param filters Sequence of filters to apply to tokens
     * @return The formatted String
     */
    public static String tokenChain(String text, Iterable<Function<String, String>> filters) {
        if (filters == null) return text;

        StringBuilder sb = new StringBuilder();
        for (String token : RegexFilters.WHITESPACE.split(text)) {
            if (!ClassifierOptions.isSpecialClassWord(token)) {
                token = Filters.stringChain(token, filters);
            }

            sb.append(token).append(" ");
        }

        return sb.toString();
    }

    /**
     * Returns HTML unescaped string
     *
     * @param text String to format (f.ex. "&lt;3")
     * @return The formatted String (f.ex. "<3")
     */
    public static String HTMLUnescape(String text) {
        return StringEscapeUtils.unescapeHtml4(text);
    }


    /**
     * Normalizes String to Latin characters if possible
     *
     * @param text String to format (f.ex. "A strîng wìth fúnny chäracters")
     * @return The formatted String (f.ex. "A string with funny characters")
     */
    public static String normalizeForm(String text) {
        return RegexFilters.replaceNonASCII(Normalizer.normalize(text, Normalizer.Form.NFD), "");
    }

    /**
     * Removes repeated whitespace
     *
     * @param text String to format (f.ex. "A string    with maany   spaces  ")
     * @return The formatted String (f.ex. "A string with many spaces ")
     */
    public static String removeRepeatedWhitespace(String text) {
        return RegexFilters.replaceWhitespace(text, " ");
    }


    public static String parseUnicodeEmojisToAlias(String text) {
        return EmojiParser.parseFromUnicode(text, EMOJI_ALIAS_TRANSFORMER);
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


    public static String removeEMail(String text) {
        return RegexFilters.TWITTER_EMAIL.matcher(text).replaceAll("");
    }


    public static String removeHashtag(String text) {
        return RegexFilters.replaceHashtag(text, "");
    }

    public static String placeholderHashtag(String text) {
        return RegexFilters.replaceHashtag(text, HASHTAG_PLACEHOLDER);
    }

    public static String hashtagToWord(String text) {
        return RegexFilters.replaceHashtag(text, "$1");
    }

    public static String protectHashtag(String text) {
        return RegexFilters.replaceHashtag(text, " ||#$1|| ");
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


    /**
     * Removes characters which are often part of a word (mostly apostrophes)
     *
     * @param text String to format (f.ex. "Here's a sentence!")
     * @return The formatted String (f.ex. "Heres a sentence!")
     */
    public static String removeInnerWordCharacters(String text) {
        return RegexFilters.replaceInnerWordCharacters(text, "");
    }


    /**
     * Removes all non-alphabetic or basic punctuation characters (!?,. )
     *
     * @param text String to format (f.ex. "This is' a #crazy tæst")
     * @return The formatted String (f.ex. "This is a crazy tst")
     */
    public static String removeNonSyntacticalText(String text) {
        return RegexFilters.replaceNonSyntacticalText(text, " ");
    }

    public static String removeNonSyntacticalTextPlus(String text) {
        return RegexFilters.replaceNonSyntacticalTextPlus(text, " ");
    }

    /**
     * Removes non-alphanumerical characters
     *
     * @param text String to format (f.ex "It's very nice!")
     * @return The formatted String (f.ex "It s very nice ")
     */
    public static String removeNonAlphanumericalText(String text) {
        return RegexFilters.replaceNonAlphanumericalText(text, " ");
    }


    /**
     * Removes non alphabetic characters
     *
     * @param text String to format (f.ex "Hey, m8!")
     * @return The formatted String (f.ex. "Hey m")
     */
    public static String removeNonAlphabeticText(String text) {
        return RegexFilters.replaceNonAlphabeticText(text, " ");
    }


    /**
     * Removes free standing digits (digits not part of a word)
     *
     * @param text String to format (f.ex. "Only 90s kids will get this 1337 m8")
     * @return The formatted String (f.ex. "Only 90s kids will get this m8")
     */
    public static String removeFreeDigits(String text) {
        return RegexFilters.replaceFreeDigits(text, " ");
    }


    public static String replaceEmoticons(String text) {
        return RegexFilters.replaceEmoticons(text, " ||$1|| ");
    }
}
