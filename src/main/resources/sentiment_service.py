import sys
from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer

analyzer = SentimentIntensityAnalyzer()

text = sys.stdin.read().strip()

if not text:
    print(0.0)
    sys.exit(0)

scores = analyzer.polarity_scores(text)
compound_score = scores['compound']

print(compound_score)