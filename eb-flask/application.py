from flask import render_template
from flask import Flask
import requests
import json
application = Flask(__name__)

@application.route('/')
def index():
    return render_template('index.html');

@application.route('/data/<keyword>')
def data(keyword):
    r=requests.post('http://search-twitter-1-kf5qeriqw5iu6uasbyv6dmwfbq.us-west-2.es.amazonaws.com/_search/',"",{"query": {"match": {'text': keyword}}})
    results = json.loads(r.content);
    results = results['hits'];
    js_results = "eqfeed_callback(" + json.dumps(results) + ");";
    return js_results;

@application.route('/updatedata/<keyword>')
def updatedata(keyword):
    r=requests.post('http://search-twitter-1-kf5qeriqw5iu6uasbyv6dmwfbq.us-west-2.es.amazonaws.com/_search/',"",{"query": {"match": {'text': keyword}}})
    results = json.loads(r.content);
    results = results['hits'];
    js_results = json.dumps(results);
    return js_results;

@application.route('/googlemap/<keyword>')
def google_map(keyword):
    return render_template('googlemap_markers.html', keyword=keyword)

if __name__ == '__main__':
    application.run()