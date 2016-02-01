import re
import operator
import io

start_tags = ['_JJ', '_RB', '_NN']
phrases = {}

descriptive_phrases = {
    '_JJ': [['_NN'], ['_JJ', '_NN']],
    '_RB': [['_JJ', '_NN'], ['_VB']],
    '_NN': [['_JJ', '_NN']]
}

regex = re.compile('[^a-zA-Z]')


def main():
    file = open('gate_pos_tagger/tagged.txt', 'r').read().split("\n")
    for j in range(len(file)):
        list = file[j].split(" ")
        for i in range(len(list)):
            for tag in start_tags:
                if list[i].find(tag) > 0 and len(list)-i > 1:
                    check_consecutive_words(list, i, tag, j)

    sorted_phrases = sorted(phrases.items(), key=operator.itemgetter(1), reverse=True)
    for sorted_phrase in sorted_phrases:
        if len(sorted_phrase[1]) >= 50:
            print sorted_phrase[0]


def check_consecutive_words(sentence, index, tag, tweet_nr):
    possible_tags = descriptive_phrases.get(tag)
    if len(sentence) - index >= 3:
        for i in range(len(possible_tags)):
            if sentence[index+1].find(possible_tags[i][0]) > 0:
                if len(possible_tags[i]) > 1:
                    if sentence[index+2].find(possible_tags[i][1]) > 0:
                        continue
                create_and_add_phrase(sentence, index, tweet_nr)
    else:
        for i in range(len(possible_tags)):
            if sentence[index+1].find(possible_tags[i][0]) > 0:
                create_and_add_phrase(sentence, index, tweet_nr)
                break


def create_and_add_phrase(sentence, index, tweet_nr):
    phrase = regex.sub('',sentence[index].split('_')[0]) + " " + regex.sub('', sentence[index+1].split('_')[0])
    phrase = re.sub('[!?.]', '', phrase)
    phrase = phrase.lower()
    if phrase in phrases:
        phrases[phrase].append(tweet_nr)
    else:
        phrases[phrase] = []
        phrases[phrase].append(tweet_nr)


if __name__ == '__main__':
    main()
