#!/usr/bin/python3
# -*- coding:utf-8 -*-

import sys
from tokenize import String
import xml.dom.minidom

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
        if usesLibraryNode.getAttribute('android:name') in UsesLibraryNames:
            applicationNode.removeChild(usesLibraryNode)

    for usesLibraryName in UsesLibraryNames:
        usesLibraryNode = dom.createElement('uses-library')
        usesLibraryNode.setAttribute('android:name', usesLibraryName)
        usesLibraryNode.setAttribute('android:required', 'false')
        applicationNode.insertBefore(
            usesLibraryNode, applicationNode.firstChild)

    # 允许访问所有的activity

    for activityNode in applicationNode.getElementsByTagName('activity'):
        activityNode.setAttribute('android:exported', 'true')

        if activityNode.getAttribute('android:name') == 'com.miui.mishare.activity.MiShareSettingsActivity':
            # 设置主页面
            actionNode = dom.createElement('action')
            actionNode.setAttribute(
                'android:name', 'android.intent.action.MAIN')

            categoryNode = dom.createElement('category')
            categoryNode.setAttribute(
                'android:name', 'android.intent.category.LAUNCHER')

            intentFilterNode = dom.createElement('intent-filter')
            intentFilterNode.appendChild(actionNode)
            intentFilterNode.appendChild(categoryNode)

            activityNode.appendChild(intentFilterNode)
        elif activityNode.getAttribute('android:name') == 'com.miui.mishare.activity.TransActivity':
            def getSendIntentFilter(action: String):
                actionNode = dom.createElement('action')
                actionNode.setAttribute(
                    'android:name', action)

                categoryNode = dom.createElement('category')
                categoryNode.setAttribute(
                    'android:name', 'android.intent.category.DEFAULT')

                dataNode = dom.createElement('data')
                dataNode.setAttribute(
                    'android:mimeType', '*/*')
                dataNode.setAttribute(
                    'android:pathPattern', '.*')

                intentFilterNode = dom.createElement('intent-filter')
                intentFilterNode.appendChild(actionNode)
                intentFilterNode.appendChild(categoryNode)
                intentFilterNode.appendChild(dataNode)
                return intentFilterNode

            # 设置分享页面
            activityNode.appendChild(getSendIntentFilter('android.intent.action.SEND'))
            activityNode.appendChild(getSendIntentFilter('android.intent.action.SEND_MULTIPLE'))

    file = open(outputFileName, "w")
    file.write(dom.toprettyxml(indent=' '))
    file.close()
