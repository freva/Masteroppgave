package com.freva.masteroppgave.cleaner;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private boolean label_fixed;

    public GatePosTagger(String model) throws Exception {
        this(model, true, true, true, true);
    }

    public GatePosTagger(String model, boolean do_correction, boolean do_entities, boolean do_interjections, boolean label_fixed) throws Exception {
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
            for (String line : Files.readAllLines(Paths.get("res/gate_pos_tagger/interjections.regex"))) {
                interjections.add(Pattern.compile("^" + line.trim() + "$"));
            }
        }

        this.tagger = new MaxentTagger(model);
        this.do_correction = do_correction;
        this.do_entities = do_entities;
        this.do_interjections = do_interjections;
        this.label_fixed = label_fixed;
    }

    public String tagSentence(String sentence) {
        String[] input_tokens = sentence.split("\\s+");
        ArrayList<String> tokens = new ArrayList<>(Arrays.asList(input_tokens));

        List<TaggedWord> taggedWords = tagSentence(tokens);
        String taggedSentence = "";

        for (int i = 0; i < taggedWords.size(); i++) {
            taggedSentence = taggedSentence + input_tokens[i] + "_" + taggedWords.get(i).tag() + " ";
        }

        return taggedSentence.trim();
    }

    private List<TaggedWord> tagSentence(ArrayList<String> tokens) {
        ArrayList<TaggedWord> untagged_string = new ArrayList<>();

        TaggedWord to_label;
        for(Iterator<String> var4 = tokens.iterator(); var4.hasNext(); untagged_string.add(to_label)) {
            String token = var4.next();
            to_label = new TaggedWord(token);
            if(label_fixed) {
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
