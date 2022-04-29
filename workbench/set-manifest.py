#!/usr/bin/python3
# -*- coding:utf-8 -*-

import sys
import xml.dom.minidom

AndroidNS = 'http://schemas.android.com/apk/res/android'

if __name__ == '__main__':
    if len(sys.argv) < 3:
        raise Exception('need input file name and output file name')
    inputFileName = sys.argv[1]
    outputFileName = sys.argv[2]

    dom = xml.dom.minidom.parse(
        inputFileName)
    rootNode = dom.documentElement
    if rootNode.nodeName != 'manifest':
        raise Exception('not android xml')

    applicationNodes = rootNode.getElementsByTagName('application')
    if len(applicationNodes) != 1:
        raise Exception('not android xml')

    applicationNode = applicationNodes[0]

    # 期待Miui-Core提供支持

    UsesLibraryNames = [
        'com.miui.system',
        'com.miui.core',
        'com.miui.rom',
        'miuiframework',
    ]

    for usesLibraryNode in applicationNode.getElementsByTagName('uses-library'):
        if usesLibraryNode.getAttributeNS(
                AndroidNS, 'name') in UsesLibraryNames:
            applicationNode.removeChild(usesLibraryNode)

    for usesLibraryName in UsesLibraryNames:
        usesLibraryNode = dom.createElement('uses-library')
        usesLibraryNode.setAttributeNS(AndroidNS, 'name', usesLibraryName)
        usesLibraryNode.setAttributeNS(AndroidNS, 'required', 'false')
        applicationNode.insertBefore(
            usesLibraryNode, applicationNode.firstChild)

    # 允许访问所有的activity

    for activityNode in applicationNode.getElementsByTagName('activity'):
        activityNode.setAttributeNS(
            AndroidNS, 'exported', 'true')
        if activityNode.getAttributeNS(
                AndroidNS, 'name') == 'com.miui.mishare.activity.MiShareSettingsActivity':

            actionNode = dom.createElement('action')
            actionNode.setAttributeNS(
                AndroidNS, 'name', 'android.intent.action.MAIN')

            categoryNode = dom.createElement('category')
            categoryNode.setAttributeNS(
                AndroidNS, 'name', 'android.intent.category.LAUNCHER')

            intentFilterNode = dom.createElement('intent-filter')
            intentFilterNode.appendChild(actionNode)
            intentFilterNode.appendChild(categoryNode)

            activityNode.appendChild(intentFilterNode)

    file = open(outputFileName, "w")
    file.write(dom.toprettyxml())
    file.close()
