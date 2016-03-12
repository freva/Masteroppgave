package com.freva.masteroppgave.preprocessing.filters;

import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.Fitzpatrick;

import java.util.*;
import java.util.regex.Pattern;

public class CharacterCleaner {
    private static final EmojiTrie emojiTrie = new EmojiTrie(EmojiManager.getAll());
    private static final Pattern fitzpatrick;

    static {
        String[] fitzpatrickValues = Arrays.stream(Fitzpatrick.values()).map(i -> i.unicode).toArray(String[]::new);
        fitzpatrick = Pattern.compile("(:?" + String.join("|", fitzpatrickValues) + ")");
    }

    public static String cleanCharacters(String input) {
        StringBuilder sb = new StringBuilder();

        for(String token: RegexFilters.WHITESPACE.split(input)) {
            if(! (token.startsWith("||") && token.endsWith("||") && token.length() > 4)) {
                token = Filters.removeInnerWordCharacters(token);
                token = Filters.removeNonAlphanumericalText(token);
            }

            sb.append(token).append(" ");
        }

        return sb.toString();
    }

    public static String removeAllEmojis(String input) {
        input = fitzpatrick.matcher(input).replaceAll("");

        int prev = 0;
        StringBuilder sb = new StringBuilder();
        char[] charInput = input.toCharArray();
        for(int i=0; i<input.length(); i++) {
            int emojiEnd = getEmojiEndPos(charInput, i);

            if (emojiEnd != -1) {
                sb.append(input.substring(prev, i));
                prev = emojiEnd;
                i = prev-1;
            }
        }

        return sb.append(input.substring(prev)).toString();
    }


    public static String unicodeEmotesToAlias(String input) {
        input = fitzpatrick.matcher(input).replaceAll("");

        int prev = 0;
        StringBuilder sb = new StringBuilder();
        char[] charInput = input.toCharArray();
        for(int i=0; i<input.length(); i++) {
            int emojiEnd = getEmojiEndPos(charInput, i);

            if (emojiEnd != -1) {
                sb.append(input.substring(prev, i));
                sb.append(" ||").append(emojiTrie.getEmoji(input.substring(i, emojiEnd)).getAliases().get(0)).append("|| ");
                prev = emojiEnd;
                i = prev-1;
            }
        }

        return sb.append(input.substring(prev)).toString();
    }

    private static int getEmojiEndPos(char[] text, int startPos) {
        int best = -1;
        for(int j=startPos+1; j<=text.length; j++) {
            EmojiTrie.Matches status = emojiTrie.isEmoji(Arrays.copyOfRange(text, startPos, j));

            if (status.exactMatch()) {
                best = j;
            } else if (status.impossibleMatch()) {
                return best;
            }
        }

        return best;
    }
}
