"""
    A collection of different filter methods for the tweets.
"""
import HTMLParser
import re
import tokenizer

# Emoticon definitions.
NormalEyes = r'[:=8]'
HappyEyes = r'[xX]'
WinkEyes = r'[;]'
NoseArea = r'[\-o\Oc\^\*\']?'
HappyMouths = r'[dD\)\]\*\>\}]'
SadMouths = r'[c<|@L\(\[\/\{\\]'
TongueMouths = r'[pP]'

Positive_RE = re.compile('(\^_\^|' + "((" + NormalEyes + "|" + HappyEyes + "|" + WinkEyes + ")" + NoseArea + HappyMouths + ')|(?:\<3+))', re.UNICODE)
Negative_RE = re.compile(NormalEyes + NoseArea + SadMouths, re.UNICODE)

Wink_RE = re.compile(WinkEyes + NoseArea + HappyMouths, re.UNICODE)
Tongue_RE = re.compile(NormalEyes + NoseArea + TongueMouths, re.UNICODE)

Emoticon = (
    "(" + NormalEyes + "|" + HappyEyes + "|" + WinkEyes + ")" + NoseArea +
    "(" + TongueMouths + "|" + SadMouths + "|" + HappyMouths + ")"
)
Emoticon_RE = re.compile(Emoticon, re.UNICODE)

# Tag definitions
username_RE = re.compile(r'(@[a-zA-Z0-9_]{1,15})')
hashtag_RE = re.compile(r'(#[a-zA-Z]+[a-zA-Z0-9_]*)')
rt_tag_RE = re.compile(r'(^RT\s+|\s+RT\s+)')
quote_RE = re.compile(r'".*?"')
url_RE = re.compile(r'(\w+:\/\/\S+)')
characters_RE = re.compile(r"[^a-zA-Z !?,.:()']")
characters_limit_RE = re.compile(r"[^a-zA-Z ]")

punctuation = {',', '.', ':', ';', '!', '?'}
word_finder = re.compile(r'(\S+)')

non_regular_text = re.compile(r'[^a-zA-Z.,!?]')
fix_spaces = re.compile(r'\s*([?!.,]+(?:\s+[?!.,]+)*)\s*')


def html_decode(tweet_text):
    h = HTMLParser.HTMLParser()
    return h.unescape(tweet_text)

def tokenize(tweet):
    return ' '.join(tokenizer.tokenize(tweet))

def limit_chars(tweet):
    return characters_limit_RE.sub("", tweet)

def lower_case(tweet):
    return tweet.lower()


def no_emoticons(tweet_text):
    tweet = re.sub(Positive_RE, "", tweet_text)
    tweet = re.sub(Negative_RE, "", tweet)
    tweet = re.sub(Emoticon_RE, "", tweet)
    return tweet


def no_username(tweet_text):
    return username_RE.sub("", tweet_text)

def username_placeholder(tweet_text):
    return username_RE.sub("||U||", tweet_text)


def no_hash(tweet_text):
    return hashtag_RE.sub("", tweet_text)

def hash_placeholder(tweet_text):
    return hashtag_RE.sub("||H||", tweet_text)


def no_rt_tag(tweet_text):
    return rt_tag_RE.sub("", tweet_text)


def no_url(tweet_text):
    return url_RE.sub("", tweet_text)

def url_placeholder(tweet_text):
    return url_RE.sub("||URL||", tweet_text)


def no_quotations(tweet_text):
    return quote_RE.sub("", tweet_text)

def quote_placeholder(tweet_text):
    return quote_RE.sub("||QUOTE||", tweet_text)


def reduce_letter_duplicates(tweet_text):
    return re.sub(r'(.)\1{3,}', r'\1\1\1', tweet_text, flags=re.IGNORECASE)


def hash_as_normal(tweet_text):
    return re.sub(r'#([a-zA-Z]+[a-zA-Z0-9_]*)', "\\1", tweet_text)


def regular_text_only(tweet_text):
    tweet = non_regular_text.sub(' ', tweet_text)
    tweet = fix_spaces.sub(lambda x: "{} ".format(x.group(1).replace(" ", "")), tweet)
    return ' '.join(tweet.split())
