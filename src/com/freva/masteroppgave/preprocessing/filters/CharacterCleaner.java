package com.freva.masteroppgave.preprocessing.filters;

import com.freva.masteroppgave.lexicon.container.PriorPolarityLexicon;
import com.freva.masteroppgave.lexicon.container.TokenTrie;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.Fitzpatrick;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class CharacterCleaner {
    private static final TokenTrie<Character> emojiTrie = new TokenTrie<>();
    private static final TokenTrie<Character> emoticonTrie = new TokenTrie<>();
    private static final Pattern fitzpatrick;
    private static final Map<String, String> unicodeToAlias = new HashMap<>();

    static {
        for(Emoji emoji: EmojiManager.getAll()) {
            Character[] characters = emoji.getUnicode().chars().mapToObj(c -> (char)c).toArray(Character[]::new);
            emojiTrie.addTokenSequence(characters);
            unicodeToAlias.put(emoji.getUnicode(), emoji.getAliases().get(0));
        }

        try {
            for(String emoticon: PriorPolarityLexicon.readLexicon(new File("res/data/EmoticonSentimentLexicon.json")).keySet()) {
                Character[] characters = emoticon.chars().mapToObj(c -> (char)c).toArray(Character[]::new);
                emoticonTrie.addTokenSequence(characters);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] fitzpatrickValues = Arrays.stream(Fitzpatrick.values()).map(i -> i.unicode).toArray(String[]::new);
        fitzpatrick = Pattern.compile("[" + String.join("|", fitzpatrickValues) + "]");
    }

    public static String cleanCharacters(String input) {
        StringBuilder sb = new StringBuilder();
        Character[] charInput = input.chars().mapToObj(c -> (char)c).toArray(Character[]::new);

        for(int i=0; i<input.length(); i++) {
            if((i==0 || Character.isAlphabetic(input.charAt(i-1))) && (i == input.length()-1 || Character.isAlphabetic(input.charAt(i+1)))) {
                sb.append(input.charAt(i));
                continue;
            }

            for(int j=i+1; j<=input.length(); j++) {
                Boolean status = emoticonTrie.hasTokens(Arrays.copyOfRange(charInput, i, j));

                if(status == null) {
                    if(i == input.length()-1) {
                        sb.append(input.substring(i, j));
                    }
                    continue;
                } if(status.equals(true)) {
                    sb.append(" ").append(input.substring(i, j)).append(" ");
                    i = j-1;
                    break;
                } else if(status.equals(false)) {
                    sb.append(input.charAt(i));
                    break;
                }
            }
        }

        return sb.toString();
    }


    public static String unicodeEmotesToAlias(String input) {
        input = fitzpatrick.matcher(input).replaceAll("");

        StringBuilder sb = new StringBuilder();
        Character[] charInput = input.chars().mapToObj(c -> (char)c).toArray(Character[]::new);
        for(int i=0; i<input.length(); i++) {
            for(int j=i+1; j<=input.length(); j++) {
                Boolean status = emojiTrie.hasTokens(Arrays.copyOfRange(charInput, i, j));

                if(status == null) {
                    continue;
                } if(status.equals(true)) {
                    sb.append(" |").append(unicodeToAlias.get(input.substring(i, j))).append("| ");
                    i = j-1;
                    break;
                } else if(status.equals(false)) {
                    sb.append(input.charAt(i));
                    break;
                }
            }
        }

        return sb.toString();
    }
}
