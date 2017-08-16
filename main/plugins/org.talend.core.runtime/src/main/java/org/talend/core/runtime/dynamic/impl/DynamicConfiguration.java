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

import java.util.List;
import java.util.Map;

import org.talend.core.runtime.dynamic.IDynamicConfiguration;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class DynamicConfiguration extends AbstractDynamicElement implements IDynamicConfiguration {

    private String configurationName;

    public void setConfigurationName(String name) {
        this.configurationName = name;
    }

    @Override
    public String toXmlString() throws Exception {
        return toXmlJson().toString();
    }

    @Override
    public void setAttribute(String key, Object value) {
        super.setAttribute(key, value);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return super.getAttributes();
    }

    @Override
    public void addChildConfiguration(IDynamicConfiguration configuration) {
        super.addChild((AbstractDynamicElement) configuration);
    }

    @Override
    public Object getAttribute(String key) {
        return super.getAttribute(key);
    }

    @Override
    public List<IDynamicConfiguration> getChildConfigurations() {
        List children = super.getChildren();
        return children;
    }

    @Override
    protected String getTagName() {
        return configurationName;
    }

    public static DynamicConfiguration fromXmlJson(JSONObject json) throws Exception {
        DynamicConfiguration dynamicConfiguration = new DynamicConfiguration();

        dynamicConfiguration.initAttributesFromXmlJson(json);
        dynamicConfiguration.setConfigurationName(getTagNameFrom(json));

        JSONArray children = getChildrenFrom(json);
        if (children != null) {
            int length = children.length();
            for (int i = 0; i < length; ++i) {
                JSONObject jObj = children.getJSONObject(i);
                DynamicConfiguration config = fromXmlJson(jObj);
                dynamicConfiguration.addChild(config);
            }
        }

        return dynamicConfiguration;
    }
}
