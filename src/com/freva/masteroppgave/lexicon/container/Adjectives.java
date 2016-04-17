package com.freva.masteroppgave.lexicon.container;

public class Adjectives {
    private static final String VOWELS = "aeiouy";
    private static final String SEPARATOR = ",";

    public static String[] getAdverbAndAdjectives(String word) {
        if (!consistsOnlyOfAlphabeticalCharacters(word)) return new String[0];

        String adjectives = getComparativeAndSuperlativeAdjectives(word);
        if (adjectives.isEmpty()) {
            return new String[]{formAdverbFromAdjective(word)};
        } else {
            adjectives = formAdverbFromAdjective(word) + SEPARATOR + adjectives;
            return adjectives.split(SEPARATOR);
        }
    }

    /**
     * Returns a comma separated string with the comparative and the superlative forms of the input adjective. F.ex.
     * "good" => "better,best", "happy" => "happier,happiest". If the comparative and superlative forms are
     * "more [word]" and "most [word]", empty string is returned, f.ex. "careful" => "".
     * Forms are generated using rules from: http://www.eflnet.com/tutorials/adjcompsup.php
     *
     * @param word adjective
     * @return comma separated comparative and the superlative forms of the input adjective
     */
    private static String getComparativeAndSuperlativeAdjectives(String word) {
        switch (word) {
            case "good":
                return "better, best";
            case "bad":
                return "worse, worst";
            case "far":
                return "farther, farthest";
            case "little":
                return "less, least";
            case "slow":
                return "slower, slowest";
            default:
                return normalComparativeAndSuperlativeAdjectives(word);
        }
    }

    private static String normalComparativeAndSuperlativeAdjectives(String word) {
        final int numberOfSyllables = getNumberOfSyllables(word);
        StringBuilder sb = new StringBuilder();

        if (numberOfSyllables == 1) { //If one-syllable adjective
            final char lastLetter = word.charAt(word.length() - 1);

            //If the adjective ends with an e, just add –r for the comparative form and –st for the superlative form
            if (word.endsWith("e")) {
                sb.append(word).append("r");
                sb.append(SEPARATOR).append(word).append("st");

                //If the adjective ends with –y, change the y to i and add –er for the comparative form.
                //For the superlative form change the y to i and add –est.
            } else if (word.endsWith("y")) {
                String stub = word.substring(0, word.length() - 1);
                sb.append(stub).append("ier");
                sb.append(SEPARATOR).append(stub).append("iest");

                //If the adjective ends with a single consonant with a vowel before it, double the consonant and add –er
                //for the comparative form; and double the consonant and add –est for the superlative form
            } else if (isVowel(word.charAt(word.length() - 2)) && !isVowel(lastLetter)) {
                sb.append(word).append(lastLetter).append("er");
                sb.append(SEPARATOR).append(word).append(lastLetter).append("est");

                //Otherwise just add -er for the comparative form and -est for the superlative form
            } else {
                sb.append(word).append("er");
                sb.append(SEPARATOR).append(word).append("est");
            }

        } else if (numberOfSyllables == 2) { //If two-syllable adjective
            //If the adjective ends with –y, change the y to i and add –er for the comparative form.
            //For the superlative form change the y to i and add –est.
            if (word.endsWith("y")) {
                String stub = word.substring(0, word.length() - 1);
                sb.append(stub).append("ier");
                sb.append(SEPARATOR).append(stub).append("iest");

                //If the adjective ending in –er, -le, or –ow, add –er and –est to form the comparative and superlative forms
            } else if (word.endsWith("er") || word.endsWith("le") || word.endsWith("ow")) {
                sb.append(word).append("er");
                sb.append(SEPARATOR).append(word).append("est");
            }
        }

        return sb.toString();
    }

    /**
     * Forms an adverb from the input adjective, f.ex. "happy" => "happily".
     * Adverbs are generated using rules from: http://www.edufind.com/english-grammar/forming-adverbs-adjectives/
     *
     * @param adjective adjective
     * @return adverb form of the input adjective
     */
    private static String formAdverbFromAdjective(String adjective) {
        //If the adjective ends in -able, -ible, or -le, replace the -e with -y
        if (adjective.endsWith("able") || adjective.endsWith("ible") || adjective.endsWith("le")) {
            return adjective.substring(0, adjective.length() - 1) + "y";

            //If the adjective ends in -y, replace the y with i and add -ly
        } else if (adjective.endsWith("y")) {
            return adjective.substring(0, adjective.length() - 1) + "ily";

            //If the adjective ends in -ic, add -ally
        } else if (adjective.endsWith("ic")) {
            return adjective.substring(0, adjective.length() - 2) + "ally";
        }

        //In most cases, an adverb is formed by adding -ly to an adjective
        return adjective + "ly";
    }


    private static int getNumberOfSyllables(String word) {
        int count = 0;
        boolean lastIsConsonant = true;

        for (int i = 0; i < word.length() - 1; i++) {
            if (isVowel(word.charAt(i))) {
                if (lastIsConsonant) count++;
                lastIsConsonant = false;
            } else {
                lastIsConsonant = true;
            }
        }
        return count;
    }

    private static boolean isVowel(char c) {
        return VOWELS.indexOf(c) != -1;
    }

    private static boolean consistsOnlyOfAlphabeticalCharacters(String name) {
        for (char c : name.toCharArray()) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }

        return true;
    }
}
