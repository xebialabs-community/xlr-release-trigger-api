#!flask/bin/python
#
# Copyright 2020 XEBIALABS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#

from flask import Flask
from flask import request, Response
from flask import make_response
from werkzeug.exceptions import HTTPException, BadRequest, NotFound
from functools import wraps
import os, io, json

app = Flask(__name__)

# These are the username and password we expect 
expectedUN = "xlr_test"
expectedPW = "admin"

def getResponse(filename, status="200"):
    ext = 'txt'
    content_type = 'text/*'
    if request.accept_mimetypes['application/json']:
        ext = 'json'
        content_type = 'application/json'
    elif request.accept_mimetypes['application/xml']:
        ext = 'xml'
        content_type = 'application/xml'
    elif request.accept_mimetypes['text/html']:
        ext = 'html'
        content_type = 'text/html'

    filePath = "/mockserver/responses/%s.%s" % (filename, ext)
    if not os.path.isfile(filePath):
        raise NotFound("Unable to load response file '%s'" % filePath)

    f = io.open(filePath, "r", encoding="utf-8")

    resp = make_response( (f.read(), status) )
    resp.headers['Content-Type'] = '%s; charset=utf-8' % content_type

    return resp


def check_auth(username, password):
    """This function is called to check if a username /
    password combination is valid.
    """
    return username == expectedUN and password == expectedPW


def requires_auth(f):
    """
    Determines if the basic auth is valid
    """
    @wraps(f)
    def decorated(*args, **kwargs):
        auth = request.authorization
        if not auth or not check_auth(auth.username, auth.password):
            """Sends a 401 response that enables basic auth"""
            return Response(
                'Could not verify your access level for that URL.\n'
                'You have to login with proper credentials', 401,
                {'WWW-Authenticate': 'Basic realm="Login Required"'})
        return f(*args, **kwargs)
    return decorated


@app.route('/')
def index():
    return "Hello, from the Mockserver!"


@app.route('/<part1>/<endpoint>', methods=['GET'])
@requires_auth
def handleRequest(part1, endpoint):
    return getResponse('%s_%s' % (part1, endpoint))


@app.route('/<part1>/<part2>/<endpoint>', methods=['GET'])
@requires_auth
def handleRequest(part1, part2, endpoint):
    return getResponse('%s_%s_%s' % (part1, part2, endpoint))


@app.route('/<part1>/<part2>/<part3>/<endpoint>', methods=['GET'])
@requires_auth
def handleRequest(part1, part2, part3, endpoint):
    return getResponse('%s_%s_%s_%s' % (part1, part2, part3, endpoint))


if __name__ == '__main__':
    app.run(debug=False)
