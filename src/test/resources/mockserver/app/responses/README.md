# Purpose

Place files in this directory that represent the responses for mock API calls.

Note that response files should have the name \<partN\>_..._\<endpoint\>.\<mimetype\> where 'partN' is a part of the URI path, 'endpoint' is the last element of the URI and 'mimetype' corresponds to the http 'Accept' header.

For example:

```bash
http://mockserver:5000/api/v1/myservice
```

with an 'Accept' header of 'application/json', would look for a response file named...

```bash
api_v1_myservice.json
```
