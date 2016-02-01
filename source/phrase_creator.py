from collections import defaultdict
import re


descriptive_phrases = {
    '_JJ': [['_NN'], ['_JJ', '_NN']],
    '_RB': [['_JJ', '_NN'], ['_VB']],
    '_NN': [['_JJ', '_NN']]
}

phrases = defaultdict(list)
pos_tag = re.compile(r'_[A-Z]+\b')
start_tags = re.compile(r'\w+_(JJ|NN|RB]).?$')



def main():
    tweets = open('gate_pos_tagger/tagged.txt', 'r').read().split("\n")
    for tweetID, tweet in enumerate(tweets):
        words = tweet.split()
        for wordIndex in range(len(words)-1):
            tag = start_tags.findall(words[wordIndex])
            if tag:
                check_consecutive_words(words[wordIndex: wordIndex+2], "_" + tag[0], tweetID)

    filtered_phrases = {key: len(value) for key, value in phrases.items() if len(value) >= 100}
    sorted_phrases = sorted(filtered_phrases.items(), key=lambda k: k[1], reverse=True)

    for phrase, freq in sorted_phrases:
        print phrase.ljust(30), freq


def check_consecutive_words(sentence, tag, tweet_nr):
    possible_tags = descriptive_phrases.get(tag)
    if len(sentence) == 3:
        for possible_tag in possible_tags:
            if sentence[1].find(possible_tag[0]) > 0:
                if len(possible_tag) > 1:
                    if sentence[2].find(possible_tag[1]) > 0:
                        continue
                create_and_add_phrase(sentence, tweet_nr)
    else:
        for possible_tag in possible_tags:
            if sentence[1].find(possible_tag[0]) > 0:
                create_and_add_phrase(sentence, tweet_nr)
                break


def create_and_add_phrase(sentence, tweet_nr):
    phrase = pos_tag.sub('', ' '.join(sentence[:2]))
    phrase = re.sub('[!?.]', '', phrase)
    phrase = phrase.lower()
    phrases[phrase].append(tweet_nr)


if __name__ == '__main__':
    main()
