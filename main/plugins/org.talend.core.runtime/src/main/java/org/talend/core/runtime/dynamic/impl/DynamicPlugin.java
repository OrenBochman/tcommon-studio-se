// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.runtime.dynamic.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.core.runtime.dynamic.IDynamicExtension;
import org.talend.core.runtime.dynamic.IDynamicPlugin;
import org.talend.core.runtime.dynamic.IDynamicPluginConfiguration;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class DynamicPlugin extends AbstractDynamicElement implements IDynamicPlugin {

    private Map<String, IDynamicExtension> extensionMap;

    private DynamicPluginConfiguration pluginConfiguration;

    public DynamicPlugin() {
        extensionMap = new HashMap<>();
    }

    @Override
    public IDynamicExtension getExtension(String extensionPoint, String extensionId, boolean createIfNotExist) {
        String key = extensionPoint + "/" + extensionId; //$NON-NLS-1$
        IDynamicExtension extension = extensionMap.get(key);
        if (extension == null && createIfNotExist) {
            DynamicExtension dynamicExtension = new DynamicExtension();
            dynamicExtension.setExtensionPoint(extensionPoint);
            dynamicExtension.setExtensionId(extensionId);
            extension = dynamicExtension;
            super.addChild(dynamicExtension);
            extensionMap.put(key, extension);
        }
        return extension;
    }

    @Override
    public List<IDynamicExtension> getAllExtensions() {
        return new ArrayList<IDynamicExtension>(extensionMap.values());
    }

    @Override
    public IDynamicExtension removeExtension(String extensionPoint) {
        IDynamicExtension extension = extensionMap.remove(extensionPoint);
        if (extension != null) {
            super.getChildren().remove(extension);
        }
        return extension;
    }

    public void addExtension(IDynamicExtension extension) {
        super.addChild((AbstractDynamicElement) extension);
        extensionMap.put(extension.getExtensionPoint(), extension);
    }

    @Override
    public String toXmlString() throws Exception {
        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
        xml.append("<?eclipse version=\"3.2\"?>\n"); //$NON-NLS-1$
        xml.append(toXmlJson().toString());
        return xml.toString();
    }

    @Override
    public IDynamicPluginConfiguration getPluginConfiguration() {
        return pluginConfiguration;
    }

    @Override
    public void setPluginConfiguration(IDynamicPluginConfiguration pluginConfiguration) {
        if (this.pluginConfiguration != null) {
            super.removeChild(this.pluginConfiguration);
        }
        this.pluginConfiguration = (DynamicPluginConfiguration) pluginConfiguration;
        if (this.pluginConfiguration != null) {
            super.addChild(this.pluginConfiguration);
        }
    }

    @Override
    public String getTagName() {
        return TAG_NAME;
    }

    public static DynamicPlugin fromXmlJson(JSONObject json) throws Exception {

        String jsonTagName = getTagNameFrom(json);

        if (jsonTagName != null && !jsonTagName.isEmpty()) {
            if (!TAG_NAME.equals(jsonTagName)) {
                throw new Exception("Current json object is not a plugin");
            }
        }

        DynamicPlugin dynamicPlugin = new DynamicPlugin();

        dynamicPlugin.initAttributesFromXmlJson(json);

        JSONArray children = getChildrenFrom(json);
        if (children != null) {
            int length = children.length();
            for (int i = 0; i < length; ++i) {
                JSONObject jObj = children.getJSONObject(i);
                String tagName = getTagNameFrom(jObj);
                if (tagName == null) {
                    DynamicConfiguration config = DynamicConfiguration.fromXmlJson(jObj);
                    dynamicPlugin.addChild(config);
                } else if (DynamicExtension.TAG_NAME.equals(tagName)) {
                    DynamicExtension extension = DynamicExtension.fromXmlJson(jObj);
                    dynamicPlugin.addExtension(extension);
                } else if (DynamicPluginConfiguration.TAG_NAME.equals(tagName)) {
                    DynamicPluginConfiguration pluginConfiguration = DynamicPluginConfiguration.fromXmlJson(jObj);
                    dynamicPlugin.setPluginConfiguration(pluginConfiguration);
                } else {
                    DynamicConfiguration config = DynamicConfiguration.fromXmlJson(jObj);
                    dynamicPlugin.addChild(config);
                }
            }
        }

        return dynamicPlugin;
    }

}
