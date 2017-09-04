// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.librariesmanager.model.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import net.sf.json.JSONObject;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.model.general.Project;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * created by wchen on Aug 18, 2017 Detailled comment
 *
 */
public class CustomUriManager {

    private JSONObject customURIObject;

    private static CustomUriManager manager = new CustomUriManager();;

    private static final String CUSTOM_URI_MAP = "CustomURIMapping.json";

    private CustomUriManager() {
        try {
            customURIObject = loadResources(getResourcePath(), CUSTOM_URI_MAP, true);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    public static CustomUriManager getInstance() {
        return manager;
    }

    private synchronized JSONObject loadResources(String path, String fileName, boolean create) throws IOException {
        BufferedReader br = null;
        JSONObject jsonObj = new JSONObject();
        try {
            File file = new File(path, fileName);
            if (file.exists()) {
                br = new BufferedReader(new FileReader(file));
                StringBuffer buffer = new StringBuffer();
                String line = null;
                while ((line = br.readLine()) != null) {
                    buffer.append(line);
                }
                jsonObj = JSONObject.fromObject(buffer.toString());
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }
        return jsonObj;
    }

    private void saveResource(JSONObject customMap, String filePath, String fileName) {
        try {
            FileWriter fileWriter = new FileWriter(new File(filePath, fileName));
            Writer writer = customMap.write(fileWriter);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
    }

    public void saveCustomURIMap() {
        final RepositoryWorkUnit repositoryWorkUnit = new RepositoryWorkUnit(ProjectManager.getInstance().getCurrentProject(),
                "Save custom maven uri map") {

            @Override
            public void run() throws PersistenceException, LoginException {
                saveResource(customURIObject, getResourcePath(), CUSTOM_URI_MAP);
            }
        };
        IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();
        factory.executeRepositoryWorkUnit(repositoryWorkUnit);

    }

    private String getResourcePath() {
        try {
            Project currentProject = ProjectManager.getInstance().getCurrentProject();
            IProject project = ResourceUtils.getProject(currentProject);
            IFolder settingsFolder = project.getFolder(".settings");
            return settingsFolder.getLocation().toPortableString();
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

    public void put(String key, String value) {
        if (value != null) {
            customURIObject.put(key, value);
        } else {
            customURIObject.remove(key);
        }
    }

    public String get(String key) {
        if (customURIObject.containsKey(key)) {
            return customURIObject.getString(key);
        }
        return null;
    }

    public Set<String> keySet() {
        return customURIObject.keySet();
    }

    public void importSettings(String filePath, String fileName) throws Exception {
        JSONObject loadResources = loadResources(filePath, fileName, false);
        if (loadResources != null) {
            customURIObject.putAll(loadResources);
        }
        saveCustomURIMap();
    }

    public void exportSettings(String filePath, String fileName) {
        saveResource(customURIObject, filePath, fileName);
    }

}
