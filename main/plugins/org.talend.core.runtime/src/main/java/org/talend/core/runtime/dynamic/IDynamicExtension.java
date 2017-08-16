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
package org.talend.core.runtime.dynamic;

import java.util.List;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public interface IDynamicExtension {

    public String toXmlString() throws Exception;

    public List<IDynamicConfiguration> getConfigurations();

    public void addConfiguration(IDynamicConfiguration config);

    public IDynamicConfiguration createEmptyConfiguration();

    public void setExtensionPoint(String extensionPoint);

    public String getExtensionPoint();

}
