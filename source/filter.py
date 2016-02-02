import filters as f
from unicodefixer import fix_bad_unicode
import io
import re
import os
import time

pos_tags_RE = re.compile(ur'_([A-Z]+)\s')
gate_tagger_path = "gate_pos_tagger/"


def filter_tweets(input_name, output_name):
    filt = [f.no_username, f.no_url, f.no_emoticons, f.no_hash, f.regular_text_only]

    with io.open(input_name, "r", encoding="utf-8") as in_file, open(output_name, "w") as out_file:
        for tweet in in_file:
            for fil in filt:
                tweet = fil(tweet)
            out_file.write(tweet + "\n")


def run_tagger(input_name, output_name):
    old_dir = os.getcwd()
    os.chdir(gate_tagger_path)
    os.system('java -Xmx1024m -jar twitie_tag.jar models/gate-EN-twitter-fast.model ' +
              input_name + ' > ' + output_name)
    os.chdir(old_dir)


def main():
    start_time = time.time()

    filter_tweets(gate_tagger_path + "200k.txt", gate_tagger_path + "filtered.txt")
    run_tagger("filtered.txt", "tagged.txt")

    print "Finished in", "%.2f" % (time.time()-start_time), "sec"

main()
