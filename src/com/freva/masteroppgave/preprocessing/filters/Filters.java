package com.freva.masteroppgave.preprocessing.filters;

import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang3.StringEscapeUtils;

import java.text.Normalizer;
import java.util.function.Function;

public class Filters {
    public static final String USERNAME_PLACEHOLDER = "||U||";
    public static final String HASHTAG_PLACEHOLDER = "||H||";
    public static final String RTTAG_PLACEHOLDER = "||RT||";
    public static final String URL_PLACEHOLDER = "||URL||";


    /**
     * Chain several filters after each other
     * @param text String to format
     * @param filters Sequence of filters to apply on String
     * @return The formatted String
     */
    @SafeVarargs
    public static String chain(String text, Function<String, String>... filters) {
        for (Function<String, String> filter : filters)
            text = filter.apply(text);
        return text;
    }


    /**
     * Returns HTML unescaped string
     * @param text String to format (f.ex. "&lt;3")
     * @return The formatted String (f.ex. "<3")
     */
    public static String HTMLUnescape(String text) {
        return StringEscapeUtils.unescapeHtml4(text);
    }


    /**
     * Normalizes String to Latin characters if possible
     * @param text String to format (f.ex. "A strîng wìth fúnny chäracters")
     * @return The formatted String (f.ex. "A string with funny characters")
     */
    public static String normalizeForm(String text) {
        return RegexFilters.replaceNonASCII(Normalizer.normalize(text, Normalizer.Form.NFD), "");
    }

    /**
     * Removes repeated whitespace
     * @param text String to format (f.ex. "A string    with maany   spaces  ")
     * @return The formatted String (f.ex. "A string with many spaces ")
     */
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
        return RegexFilters.replaceHashtag(text, "$1");
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
     * Removes PoS tags (Assumed immediately after a word connected by an underscore)
     * @param text String to format (f.ex "A_TAG PoS_TAG tagged_TAG sentence_TAG")
     * @return The formatted String (f.ex. "A PoS tagged sentence")
     */
    public static String removePosTags(String text) {
        return RegexFilters.replacePosTag(text, "");
    }


    /**
     * Removes characters which are often part of a word (mostly apostrophes)
     * @param text String to format (f.ex. "Here's a sentence!")
     * @return The formatted String (f.ex. "Heres a sentence!")
     */
    public static String removeInnerWordCharacters(String text) {
        return RegexFilters.replaceInnerWordCharacters(text, "");
    }


    /**
     * Removes all non-alphabetic or basic punctuation characters (!?,. )
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
     * @param text String to format (f.ex "It's very nice!")
     * @return The formatted String (f.ex "It s very nice ")
     */
    public static String removeNonAlphanumericalText(String text) {
        return RegexFilters.replaceNonAlphanumericalText(text, " ");
    }


    /**
     * Removes non alphabetic characters
     * @param text String to format (f.ex "Hey, m8!")
     * @return The formatted String (f.ex. "Hey m")
     */
    public static String removeNonAlphabeticText(String text) {
        return RegexFilters.replaceNonAlphabeticText(text, " ");
    }

    /**
     * Removes non-alphabetical symbols from PoS tagged sentence
     * @param text String to format (f.ex. "That_TAG was_TAG ***_TAG ._. cool_TAG m8_TAG")
     * @return The formatted String (f.ex "That_TAG was_TAG cool_TAG m_TAG")
     */
    public static String removeNonPosTaggedAlphabeticalText(String text) {
        return RegexFilters.replaceNonPosTaggedAlphabeticalText(text, "");
    }


    /**
     * Removes free standing digits (digits not part of a word)
     * @param text String to format (f.ex. "Only 90s kids will get this 1337 m8")
     * @return The formatted String (f.ex. "Only 90s kids will get this m8")
     */
    public static String removeFreeDigits(String text) {
        return RegexFilters.replaceFreeDigits(text, " ");
    }


    /**
     * Formats punctuation signs to be syntactically correctly placed (immediately following a word, followed by a space)
     * @param text String to format (f.ex. "This sentence ,has oddly.placed signs ! ! !"
     * @return The formatted String (f.ex. "This sentence, has oddly. placed signs!!!")
     */
    public static String fixSyntacticalPunctuationGrammar(String text) {
        return RegexFilters.fixSyntacticalPunctuationGrammar(text);
    }


    /**
     * Removes English stop words, replacing them with "_"
     * @param text String to format (f.ex. "This is a sentence with many stop words")
     * @return The formatted String (f.ex. "_ _ _ sentence _ many stop words")
     */
    public static String removeStopWords(String text) {
        return WordFilters.replaceStopWords(text, "_");
    }
}
