package com.freva.masteroppgave.utils.tools;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.freva.masteroppgave.preprocessing.filters.RegexFilters;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;


public class GatePosTagger {
    private static HashMap<String, String> corrections = new HashMap<>();
    private static ArrayList<Pattern> interjections = new ArrayList<>();
    private static HashSet<String> entities = new HashSet<>();

    private MaxentTagger tagger;
    private boolean do_correction;
    private boolean do_entities;
    private boolean do_interjections;
    private boolean do_twitter_labels;

    public GatePosTagger(String model) throws Exception {
        this(model, true, true, true, false);
    }


    /**
     * Gate Part-of-Speech tagger initializer
     * @param model File path to Gate model file
     * @param do_correction Perform basic internet speak correction to English (f.ex. da => the, l8r => later)
     * @param do_entities Use entities.txt to classify person-names, city-names and company-names
     * @param do_interjections Use set of regex defined interjections
     * @param do_twitter_labels Tag basic twitter elements like hashtags, usernames and links
     * @throws Exception
     */
    public GatePosTagger(String model, boolean do_correction, boolean do_entities, boolean do_interjections, boolean do_twitter_labels) throws Exception {
        this.tagger = new MaxentTagger(model);
        this.do_correction = do_correction;
        this.do_entities = do_entities;
        this.do_interjections = do_interjections;
        this.do_twitter_labels = do_twitter_labels;

        if (do_correction) {
            for (String line : Files.readAllLines(Paths.get("res/gate_pos_tagger/orth.en.csv"))) {
                String[] br = line.trim().split(",");
                corrections.put(br[0].toLowerCase(), br[1]);
            }
        }

        if (do_entities) {
            entities = new HashSet<>(Files.readAllLines(Paths.get("res/gate_pos_tagger/entities.txt"), Charset.forName("windows-1252")));
        }

        if (do_interjections) {
            interjections.addAll(Files.readAllLines(Paths.get("res/gate_pos_tagger/interjections.regex")).stream()
                    .map(line -> Pattern.compile("^" + line.trim() + "$"))
                    .collect(Collectors.toList()));
        }
    }


    /**
     * Runs Gate Part-of-Speech tagger on a sentence
     * @param sentence Sentence to PoS tag
     * @return PoS tagged string where PoS tags are attached to the end of each word by an underscore
     */
    public String tagSentence(String sentence) {
        String[] input_tokens = RegexFilters.WHITESPACE.split(sentence);
        List<String> tokens = Arrays.asList(input_tokens);

        List<TaggedWord> taggedWords = tagSentence(tokens);
        StringBuilder taggedSentence = new StringBuilder();
        for (int i = 0; i < taggedWords.size(); i++) {
            taggedSentence.append(input_tokens[i]).append("_").append(taggedWords.get(i).tag()).append(" ");
        }

        return taggedSentence.toString().trim();
    }

    private List<TaggedWord> tagSentence(List<String> tokens) {
        ArrayList<TaggedWord> untagged_string = new ArrayList<>();

        TaggedWord to_label;
        for(Iterator<String> var4 = tokens.iterator(); var4.hasNext(); untagged_string.add(to_label)) {
            String token = var4.next();
            to_label = new TaggedWord(token);
            if(do_twitter_labels) {
                if(token.indexOf("#") == 0) {
                    to_label.setTag("HT");
                }

                if(token.indexOf("@") == 0) {
                    to_label.setTag("USR");
                }

                if(token.contains(".com") || token.indexOf("http:") == 0 || token.indexOf("www.") == 0) {
                    to_label.setTag("URL");
                }

                if(token.toLowerCase().equals("rt") || token.substring(0, 1).equals("R") && token.toLowerCase().equals("retweet")) {
                    to_label.setTag("RT");
                }
            }

            String token_lc = token.toLowerCase();
            if(do_correction) {
                if(corrections.containsKey(token_lc)) {
                    String replacement = corrections.get(token_lc);

                    token = replacement;
                    to_label = new TaggedWord(replacement);
                }
            }

            if(do_interjections) {
                for (Pattern token_lc1 : interjections) {
                    Matcher m = token_lc1.matcher(token.toLowerCase());
                    if (m.find()) {
                        to_label.setTag("UH");
                        break;
                    }
                }
            }

            if(do_entities) {
                token_lc = token.toLowerCase();
                if(entities.contains(token_lc)) {
                    to_label.setTag("NNP");
                }
            }
        }

        return tagger.tagSentence(untagged_string, true);
    }
}
