from flask import Flask, request, json, jsonify
import client
from flask_cors import CORS


app = Flask(__name__)
CORS(app)

@app.route('/addrule', methods=['POST'])
def addrule():
    data = json.loads(request.data)
    client.addrulestring(data['target'], data['rule'])
    return jsonify(data['rule'])


@app.route('/removerule', methods=['POST'])
def removerule():
    data = json.loads(request.data)
    count = client.removerule(data['target'], data['rule'])
    return jsonify(count)


@app.route('/listcdrrules', methods=['GET'])
def listcdrrules():
    return jsonify(client.listcdrrules())


@app.route('/listbillrules', methods=['GET'])
def listbillrules():
    return jsonify(client.listbillrules())


@app.route('/bills', methods=['GET'])
def getbills():
    return jsonify(client.listbills())


@app.route('/accounts', methods=['GET'])
def getaccounts():
    return jsonify(client.listaccounts())


@app.route('/cleanup', methods=['POST'])
def cleanup():
    data = json.loads(request.data)
    cleanup_dict = client.cleanup(data['target'], data['rules'])
    return jsonify(cleanup_dict)


@app.route('/singleforecast', methods=['POST'])
def singleforecast():
    data = json.loads(request.data)
    forecast = client.generatesingleforecast(data['account'], data['target'], data['size'])
    return jsonify(forecast)


@app.route('/globalforecast', methods=['POST'])
def globalforecast():
    data = json.loads(request.data)
    forecast = client.generateglobalforecast(data['target'], data['size'])
    return jsonify(forecast)


@app.route('/patternforecast', methods=['POST'])
def patternforecast():
    data = json.loads(request.data)
    forecast = client.generatepatternforecast(data['target'], data['size'])
    return jsonify(forecast)


if __name__ == '__main__':
    app.run()
