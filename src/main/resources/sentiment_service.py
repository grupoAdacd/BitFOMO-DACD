from flask import Flask, request, jsonify
from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer

app = Flask(__name__)
analyzer = SentimentIntensityAnalyzer()

@app.route('/analyze', methods=['POST'])
def analyze_sentiment():
    data = request.get_json()
    text = data.get('text', '')
    if not text:
        return jsonify({'sentimentScore': 0.0})
    # Analiza el texto con VADER
    scores = analyzer.polarity_scores(text)
    compound_score = scores['compound']  # Rango [-1, 1]
    return jsonify({'sentimentScore': compound_score})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)