import filters as f
from subprocess import Popen, PIPE, STDOUT
import io
import re
import os
import time

pos_tags_RE = re.compile(ur'_([A-Z]+)\s')
gate_tagger_path = "gate_pos_tagger/"


def filter_tweets(filename):
    filt = [f.no_username, f.no_url, f.no_emoticons, f.no_hash, f.regular_text_only]
    interim_list = []
    batch_size = 50000

    with io.open(filename, "r", encoding="utf-8") as tweet_file:
        for tweet in tweet_file:
            for fil in filt:
                tweet = fil(tweet)
            interim_list.append(tweet)
            if len(interim_list) >= batch_size:
                yield interim_list
                interim_list = []
        if interim_list:
            yield interim_list


def run_tagger(filename):
    temp_file = "filtered.temp"

    for filtered_tweets in filter_tweets(filename):
        f = io.open(gate_tagger_path + temp_file, "w", encoding="utf-8")
        f.write("\n".join(filtered_tweets))
        f.close()

        old_dir = os.getcwd()
        os.chdir(gate_tagger_path)
        p = Popen(['java', '-Xmx1024m', '-jar', 'twitie_tag.jar', 'models/gate-EN-twitter-fast.model', temp_file],
                  stdout=PIPE, stderr=STDOUT)
        os.chdir(old_dir)

        yield [' '.join(line.split()) for line in p.stdout if "_" in line]
    os.remove(gate_tagger_path + temp_file)


def main():
    start_time = time.time()

    with open(gate_tagger_path + "tagged.txt", "w") as out:
        for tagged_tweets in run_tagger(gate_tagger_path + "2m.txt"):
            out.write('\n'.join(tagged_tweets) + '\n')

    print "Finished in", "%.2f" % (time.time()-start_time), "sec"

main()

