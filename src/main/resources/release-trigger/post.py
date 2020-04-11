#
# Copyright 2020 XEBIALABS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#

# Import common Python modules as needed
import os.path
import sys
import logging
import json
from datetime import datetime, timedelta

import java.text.SimpleDateFormat as SDF
import com.xebialabs.xlrelease.api.v1.forms.CreateRelease as CreateRelease

logging.basicConfig(filename='log/custom-api.log',
                            filemode='a',
                            format='%(asctime)s,%(msecs)d %(name)s %(levelname)s %(message)s',
                            datefmt='%H:%M:%S',
                            level=logging.DEBUG)

logging.debug("main: begin")

# validate request
if not 'template_name' in request.entity and not 'template_key' in request.entity:
    raise Exception('Either "template_name" or "template_key" is required in the request body')
if not 'title' in request.entity:
    raise Exception('Release "title" is required in the request body')

# determine template to use
template_name = None
if 'template_name' in request.entity:
    template_name = request.entity['template_name']
elif 'template_key' in request.entity:
    global_vars = configurationApi.globalVariables

    tpl_map = None
    for gv in global_vars:
        if gv.key == u'global.releaseTriggerMap':
            tpl_map = gv.value
            break

    if tpl_map is None:
        raise Exception('You must define global variable "releaseTriggerMap" to map keys to template names')

    key = request.entity['template_key']
    logging.debug('lookup template for template_key "{}"'.format(key))
    for k,v in tpl_map.iteritems():
        if key == k:
            template_name = v
            break

logging.info('creating release from template "{}"'.format(template_name))

# find templateId from name
tpl_list = templateApi.getTemplates(template_name, None, 0, 5, 5)

if len(tpl_list) == 0:
    raise Exception('No template "{}" was found'.format(template_name))

tpl = tpl_list[0]

# get variables in template
tpl_vars = tpl.variables
rel_vars = {}

# create map of variables defined by post entity
logging.debug('template vars ----')
for v in tpl_vars:
    logging.debug('  '+v.key)
    if v.key in request.entity:
        logging.debug('    value='+request.entity[v.key])
        rel_vars[v.key] = request.entity[v.key]

# do CreateRelease createRelease
releaseProperties = CreateRelease()
# required properties
releaseProperties.releaseTitle = request.entity['title']
releaseProperties.variables = rel_vars
releaseProperties.autoStart = False
# optional properties
if 'scheduledStartDate' in request.entity:
    releaseProperties.scheduledStartDate = SDF("MM/dd/yyyy").parse(request.entity['scheduledStartDate'])
if 'folder_path' in request.entity:
    releaseProperties.folderId = request.entity['folder_path']

# create the release
logging.debug('creating release ----')
release = templateApi.create(tpl.id, releaseProperties)
logging.debug('  '+release.id)

# after release is created
if 'description' in request.entity:
    release.description(request.entity['description'])
if 'dueDate' in request.entity:
    release.dueDate = SDF("MM/dd/yyyy").parse(request.entity['dueDate'])
if 'owner' in request.entity:
    release.owner = request.entity['owner']
if 'tags' in request.entity:
    release.tags = request.entity['tags']

releaseApi.updateRelease(release)

if 'autoStart' in request.entity and request.entity['autoStart']:
    logging.info('starting release "{}"'.format(release.id))
    releaseApi.start(release.id)

# form response
response.statusCode = 201
response.entity = {"release_id": release.id, "template_id": tpl.id}

logging.debug("main: end")
