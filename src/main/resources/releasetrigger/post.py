#
# Copyright 2020 XEBIALABS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#

import os.path
import sys
import logging
import json
from datetime import datetime, timedelta

import java.text.SimpleDateFormat as SDF
import com.xebialabs.xlrelease.api.v1.forms.CreateRelease as CreateRelease
from com.xebialabs.xlrelease.api.v1 import ReleaseApi
from com.xebialabs.xlrelease.api.v1 import TemplateApi
from com.xebialabs.xlrelease.api.v1 import ConfigurationApi


def getValueFromMap(theKey, theType, configurationApi):
    logging.debug('in getValueFromMap: theKey = {}, theType = "{}"'.format(theKey,theType))
    global_vars = configurationApi.globalVariables
    theValue = None

    tpl_map = None
    for gv in global_vars:
        if gv.key == u'global.releaseTriggerMap':
            tpl_map = gv.value
            break

    if tpl_map is None:
        raise Exception('You must define the global variable "releaseTriggerMap" to map keys to template IDs and/or folder IDs')

    logging.debug('found the global variable releaseTriggerMap')
    for k,v in tpl_map.iteritems():
        if theKey == k:
            theValue = v
            break
    logging.debug('returning value {}'.format(theValue))
    return theValue

def post_release(shouldStart, request, releaseApi, templateApi, configurationApi, response):
    logging.debug("post_release: start")
    logging.debug("request.entity = {}".format(request.entity))
    logging.debug("shouldStart = {}".format(shouldStart))

    # validate request
    if not 'template_id' in request.entity and not 'template_key' in request.entity and not 'template_title' in request.entity:
        raise Exception('Either "template_id" or "template_key" or "template_title" is required in the request body')
    if not 'release_title' in request.entity:
        raise Exception('"release_title" is required in the request body')
    if 'autoStart' in request.entity and request.entity['autoStart'] and not 'scheduledStartDate' in request.entity:
        raise Exception('if autoStart is set to true, "scheduledStartDate" is required in the request body')
    
    # determine template to use
    template_id = None
    templateRefInRequest = None
    if 'template_id' in request.entity:
        templateRefInRequest = "template_id = %s" % request.entity['template_id']
        template_id = request.entity['template_id']
    elif 'template_key' in request.entity:
        templateRefInRequest = "template_key = %s" % request.entity['template_key']
        template_id = getValueFromMap(request.entity['template_key'], "template", configurationApi)
    elif 'template_title' in request.entity:
        # find template_id from template_title
        templateRefInRequest = "template_title = %s" % request.entity['template_title']
        tpl_list = templateApi.getTemplates(request.entity['template_title'], None, 0, 5, 5)
        if len(tpl_list) > 0:
            template_id = tpl_list[0].id

    logging.debug("Template Reference in request = {}".format(templateRefInRequest))

    if template_id is None:
        raise Exception('No template "{}" was found'.format(templateRefInRequest))
        
    logging.info('creating release from template "{}"'.format(template_id))

    tpl = templateApi.getTemplate(template_id)

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
    releaseProperties.releaseTitle = request.entity['release_title']
    releaseProperties.variables = rel_vars
    releaseProperties.autoStart = False
    # optional properties
    if 'scheduledStartDate' in request.entity:
        releaseProperties.scheduledStartDate = SDF("MM/dd/yyyy").parse(request.entity['scheduledStartDate'])

    if 'autoStart' in request.entity and request.entity['autoStart']:
        releaseProperties.autoStart = request.entity['autoStart']

    # determine folder to use
    folder_id = None
    if 'folder_id' in request.entity:
        folder_id = request.entity['folder_id']
    elif 'folder_key' in request.entity:
        folder_id = getValueFromMap(request.entity['folder_key'], "folder", configurationApi)

    if folder_id is not None:
        releaseProperties.folderId = folder_id

    logging.info('will put release in folder "{}"'.format(folder_id))

    # create the release
    logging.debug('creating release ----')
    release = templateApi.create(tpl.id, releaseProperties)
    logging.debug('  '+release.id)

    # after release is created
    if 'description' in request.entity:
        release.description = request.entity['description']
    if 'dueDate' in request.entity:
        release.dueDate = SDF("MM/dd/yyyy").parse(request.entity['dueDate'])
    if 'owner' in request.entity:
        release.owner = request.entity['owner']
    if 'tags' in request.entity:
        release.tags = request.entity['tags']

    releaseApi.updateRelease(release)

    logging.debug("The newly created release id is {} ".format(release.id))

    # determine whether or not to start release immediately
    if shouldStart:
        logging.info('starting release "{}"'.format(release.id))
        releaseApi.start(release.id)

    # form response
    response.statusCode = 201
    response.entity = {"release_id": release.id, "template_id": tpl.id}

    logging.debug("post_release: end")


