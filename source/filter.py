import filters as f
from subprocess import Popen, PIPE, STDOUT
import io
import re
import os
import time

pos_tags_RE = re.compile(ur'_([A-Z]+)\s')
gate_tagger_path = "gate_pos_tagger/"


def filter_tweets(tweets):
    filt = [f.no_username, f.no_url, f.no_emoticons, f.no_hash]
    filtered = []

    for tweet in tweets:
        for fil in filt:
            tweet = fil(tweet)
        filtered.append(tweet)
    return filtered


def run_tagger(raw_tweets):
    temp_file = "filtered.temp"
    f = io.open(gate_tagger_path + temp_file, "w", encoding="utf-8")
    f.write("\n".join(raw_tweets))
    f.close()

    old_dir = os.getcwd()
    os.chdir(gate_tagger_path)
    p = Popen(['java', '-Xmx1024m', '-jar', 'twitie_tag.jar', 'models/gate-EN-twitter-fast.model', temp_file],
              stdout=PIPE, stderr=STDOUT)
    os.chdir(old_dir)

    raw_pos = [' '.join(line.split()) for line in p.stdout if "_" in line]
    os.remove(gate_tagger_path + temp_file)
    return raw_pos


def main():
    start_time = time.time()

    print "Reading...",
    tweets = io.open(gate_tagger_path + "2m.txt", "r", encoding="utf-8").read().split("\n")[:200000]
    print "done"

    print "Filtering...",
    tweets = filter_tweets(tweets)
    print "done"

    print "Tagging...",
    tweets = run_tagger(tweets)
    print "done"

    print "Writing...",
    out = open(gate_tagger_path + "tagged.txt", "w")
    out.write('\n'.join(tweets))
    out.close()
    print "done"

    print "Finished in", "%.2f" % (time.time()-start_time), "sec"

main()