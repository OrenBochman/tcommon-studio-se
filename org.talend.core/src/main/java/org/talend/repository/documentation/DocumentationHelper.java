// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.documentation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.CorePlugin;
import org.talend.core.model.general.Project;
import org.talend.core.model.genhtml.IHTMLDocConstants;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobDocumentationItem;
import org.talend.core.model.properties.JobletDocumentationItem;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryObject;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.repository.ProjectManager;
import org.talend.repository.documentation.generation.DocumentationPathProvider;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode.EProperties;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC ftang class global comment. Detailled comment <br/>
 * 
 */
public class DocumentationHelper {

    /**
     * ftang Comment method "isSyncWithDocumentation".
     * 
     * @return
     */
    public static boolean isSyncWithDocumentation() {
        boolean isSync = CorePlugin.getDefault().getPreferenceStore().getBoolean(ITalendCorePrefConstants.DOC_GENERATION);
        return isSync;
    }

    /**
     * ftang Comment method "isFolderExisting".
     * 
     * @param type
     * @param path
     * @param folderName
     * @return
     */
    public static boolean isPathValid(ERepositoryObjectType type, IPath path, String folderName) {
        IProxyRepositoryFactory repositoryFactory = CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();
        try {
            return repositoryFactory.isPathValid(type, path, folderName);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return false;
        }
    }

    private static Item getBaseItemFromDocItem(Item docItem) throws PersistenceException {
        // if it's not a doc, cancel
        if (((docItem instanceof ProcessItem)) || (docItem instanceof JobletProcessItem)) {
            return docItem;
        }
        IProxyRepositoryFactory proxyFactory = CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();
        List<IRepositoryObject> itemsList;
        if (docItem instanceof JobDocumentationItem) {
            itemsList = proxyFactory.getAll(ERepositoryObjectType.PROCESS);
        } else if (docItem instanceof JobletDocumentationItem) {
            itemsList = proxyFactory.getAll(ERepositoryObjectType.JOBLET);
        } else {
            return null;
        }

        for (IRepositoryObject repositoryObject : itemsList) {
            String label = repositoryObject.getProperty().getLabel();
            String version = repositoryObject.getProperty().getVersion();

            if (label.equals(docItem.getProperty().getLabel()) && version.equals(docItem.getProperty().getVersion())) {
                return repositoryObject.getProperty().getItem();
            }
        }

        return null;
    }

    /**
     * ftang Comment method "getExportFileResources".
     * 
     * @param nodes
     * @return
     */
    public static ExportFileResource[] getExportFileResources(RepositoryNode node, boolean allVersions) {

        List<ExportFileResource> list = new ArrayList<ExportFileResource>();
        if (node.getType() == ENodeType.SYSTEM_FOLDER || node.getType() == ENodeType.SIMPLE_FOLDER
                || node.getType() == ENodeType.STABLE_SYSTEM_FOLDER) {
            String folderName = "";
            boolean isNotProcess = !node.getProperties(EProperties.LABEL).toString().equals(
                    ERepositoryObjectType.PROCESS.toString());
            boolean isNotJoblet = !node.getProperties(EProperties.LABEL).toString().equals(
                    ERepositoryObjectType.JOBLET.toString());
            boolean isNotGenerated = !node.getProperties(EProperties.LABEL).toString().equals(
                    ERepositoryObjectType.GENERATED.toString());
            boolean isNotJobs = !node.getProperties(EProperties.LABEL).toString().equals(ERepositoryObjectType.JOBS.toString());
            boolean isNotJoblets = !node.getProperties(EProperties.LABEL).toString().equals(
                    ERepositoryObjectType.JOBLETS.toString());

            if (isNotProcess && isNotJoblet && isNotGenerated && isNotJobs && isNotJoblets) {
                folderName = node.getProperties(EProperties.LABEL).toString();
            }
            // to improve
            addTreeNode(node, folderName, list, allVersions);
        }
        if (node.getType() == ENodeType.REPOSITORY_ELEMENT) {
            IRepositoryObject repositoryObject = node.getObject();
            if ((repositoryObject.getProperty().getItem() instanceof Item)) {
                Item baseItem = repositoryObject.getProperty().getItem();
                Item jobOrJobletItem = null;
                try {
                    jobOrJobletItem = getBaseItemFromDocItem(baseItem);
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
                List<Item> docsToGenerate = new ArrayList<Item>();
                if (jobOrJobletItem != null) {
                    if (!allVersions) {
                        docsToGenerate.add(jobOrJobletItem);
                    } else {
                        IProxyRepositoryFactory proxyFactory = CorePlugin.getDefault().getRepositoryService()
                                .getProxyRepositoryFactory();
                        try {
                            List<IRepositoryObject> objects = proxyFactory.getAllVersion(jobOrJobletItem.getProperty().getId());
                            for (IRepositoryObject object : objects) {
                                docsToGenerate.add(object.getProperty().getItem());
                            }
                        } catch (PersistenceException e) {
                            ExceptionHandler.process(e);
                        }
                    }
                }
                for (Item toGenerate : docsToGenerate) {
                    ExportFileResource resource = new ExportFileResource(toGenerate, toGenerate.getProperty().getLabel() + "_"
                            + toGenerate.getProperty().getVersion());
                    resource.setNode(node);
                    list.add(resource);
                }
            }
        }

        ExportFileResource[] resourceArray = list.toArray(new ExportFileResource[list.size()]);
        return resourceArray;
    }

    /**
     * DOC Administrator Comment method "addTreeNode".
     * 
     * @param node
     * @param path
     * @param list
     */
    private static void addTreeNode(RepositoryNode node, String path, List<ExportFileResource> list, boolean allVersions) {
        if (node != null && node.getType() == ENodeType.REPOSITORY_ELEMENT) {
            IRepositoryObject repositoryObject = node.getObject();
            if (repositoryObject.getProperty().getItem() instanceof Item) {
                Item processItem = repositoryObject.getProperty().getItem();
                ExportFileResource resource = new ExportFileResource(processItem, path);
                resource.setNode(node);
                list.add(resource);
            }
        }
        Object[] nodes = node.getChildren().toArray();
        if (nodes.length <= 0) {
            return;
        }
        for (int i = 0; i < nodes.length; i++) {

            String label = ((RepositoryNode) nodes[i]).getProperties(EProperties.LABEL).toString();
            String version = ""; //$NON-NLS-1$
            IRepositoryObject object = ((RepositoryNode) nodes[i]).getObject();
            if (((RepositoryNode) nodes[i]).getType() != ENodeType.SIMPLE_FOLDER && object != null) {
                version = object.getProperty().getVersion();
            }
            String nodePath = ""; //$NON-NLS-1$
            if (path != null && !"".equals(path)) {
                nodePath = path + "/"; //$NON-NLS-1$
            }
            if (version.equals("")) { //$NON-NLS-1$
                addTreeNode((RepositoryNode) nodes[i], nodePath + label, list, allVersions);
            } else {
                if (allVersions) {
                    IProxyRepositoryFactory proxyFactory = CorePlugin.getDefault().getRepositoryService()
                            .getProxyRepositoryFactory();
                    try {
                        List<IRepositoryObject> objects = proxyFactory.getAllVersion(object.getProperty().getId());
                        for (IRepositoryObject curObj : objects) {
                            RepositoryNode repNode = new RepositoryNode(curObj, node, ((RepositoryNode) nodes[i]).getType());
                            addTreeNode(repNode, nodePath + curObj.getProperty().getLabel() + "_"
                                    + curObj.getProperty().getVersion(), list, allVersions);
                        }
                    } catch (PersistenceException e) {
                        ExceptionHandler.process(e);
                    }
                } else {
                    addTreeNode((RepositoryNode) nodes[i], nodePath + label + "_" + version, list, allVersions);
                }
            }
        }
    }

    /**
     * ftang Comment method "getHTMLFilePath".
     * 
     * @param currentNode
     * @return
     */
    public static File getHTMLFilePath(RepositoryNode currentNode, String docRootPath) {
        String jobNodeDocRootPath = null;
        if (currentNode.getObject() != null) {
            String jobName = currentNode.getObject().getProperty().getLabel();

            // Gets the related path of current node
            Item item = currentNode.getObject().getProperty().getItem();
            String currentJobPath = item.getState().getPath();
            jobNodeDocRootPath = getJobNodeDocumentationRoot(item, docRootPath);
            currentJobPath = currentJobPath == null ? "" : IPath.SEPARATOR + currentJobPath;

            jobNodeDocRootPath = jobNodeDocRootPath + currentJobPath + IPath.SEPARATOR + jobName;
        } else {
            jobNodeDocRootPath = getJobNodeDocumentationRoot(currentNode.getRoot().getProject(), docRootPath);
        }

        java.io.File folder = new File(jobNodeDocRootPath);
        return folder;
    }

    /**
     * ftang Comment method "getJobNodeDocumentationRoot".
     * 
     * @return
     */
    public static String getJobNodeDocumentationRoot(Item item, String docRootPath) {
        IProject project = ProjectManager.getInstance().getResourceProject(item);
        java.io.File file = project.getLocation().toFile();
        String jobNodeDocRootPath = file.toString() + IPath.SEPARATOR + docRootPath;
        return jobNodeDocRootPath;
    }

    public static String getJobNodeDocumentationRoot(Project p, String docRootPath) {
        org.talend.core.model.properties.Project emfProject = null;
        if (p != null) {
            emfProject = p.getEmfProject();
        }
        IProject project = ProjectManager.getInstance().getResourceProject(emfProject);
        java.io.File file = project.getLocation().toFile();
        String jobNodeDocRootPath = file.toString() + IPath.SEPARATOR + docRootPath;
        return jobNodeDocRootPath;
    }

    // /**
    // * ftang Comment method "getJobletNodeDocumentationRoot".
    // *
    // * @return
    // */
    // public static String getJobletNodeDocumentationRoot() {
    // IProject project =
    // ResourcesPlugin.getWorkspace().getRoot().getProject(getProject().getTechnicalLabel());
    // java.io.File file = project.getLocation().toFile();
    // String jobletNodeDocRootPath = file.toString() + IPath.SEPARATOR +
    // IHTMLDocConstants.JOBLET_NODE_DOCUMENTATION_ROOT_PATH;
    // return jobletNodeDocRootPath;
    // }

    /**
     * Gets the documentation node which in the Recycle bin for current job node.
     * 
     * @param sourceNode
     * @return
     */
    public static RepositoryNode getDocumentationNodeInRecycleBin(RepositoryNode sourceNode) {
        String documentationNodeName = sourceNode.getObject().getProperty().getLabel() + "_"
                + sourceNode.getObject().getProperty().getVersion();
        IRepositoryView repositoryView = (IRepositoryView) (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .findView(IRepositoryView.VIEW_ID));
        RepositoryNode root = repositoryView.getRoot();
        for (RepositoryNode node : root.getChildren()) {
            if (node == null) {
                continue;
            }
            if (node.isBin()) {
                for (RepositoryNode subNode : node.getChildren()) {
                    String nodeName = subNode.getObject().getProperty().getLabel() + "_"
                            + subNode.getObject().getProperty().getVersion();
                    if (nodeName.equals(documentationNodeName)) {
                        return subNode;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Get the documentation node for the job node.
     * 
     * @param selectedJobNode
     * @return
     */
    public static RepositoryNode getCurrentDocumentationNode(final RepositoryNode selectedJobNode) {
        IRepositoryView viewPart = getViewPart();
        RepositoryNode root = viewPart.getRoot();

        RepositoryNode documentationNode = null;
        for (RepositoryNode node : root.getChildren()) {
            if (node.getContentType() == ERepositoryObjectType.DOCUMENTATION) {
                documentationNode = node;
                break;
            }
        }

        if (documentationNode == null) {
            return null;
        }

        for (RepositoryNode node : documentationNode.getChildren()) {
            // Goto the Node "Generated";
            if (node.getContentType() != ERepositoryObjectType.GENERATED) {
                continue;
            }

            // Goto the Node "Jobs";
            for (RepositoryNode subNode : node.getChildren()) {
                if (subNode.getContentType() != ERepositoryObjectType.JOBS) {
                    continue;
                }

                for (RepositoryNode grandChildNode : subNode.getChildren()) {

                    IRepositoryObject object = selectedJobNode.getObject();
                    String path = "";
                    if (object != null) {
                        path = object.getProperty().getItem().getState().getPath();
                    }

                    // Under the first layer of root node
                    if (path.equals("")) {
                        String label = grandChildNode.getObject().getProperty().getLabel();
                        String version = grandChildNode.getObject().getProperty().getVersion();
                        if (label.equals(object.getProperty().getLabel()) && version.equals(object.getProperty().getVersion())) {
                            return grandChildNode;
                        }
                    } else { // a/b/c
                        String[] pathArray = path.split("/");
                        int layerCount = pathArray.length;
                        for (RepositoryNode repositoryNode : grandChildNode.getChildren()) {

                        }

                    }
                }
            }
        }

        return null;
    }

    /**
     * 
     * Returns the repository view..
     * 
     * @return - the repository biew
     */
    public static IRepositoryView getViewPart() {
        IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench().getWorkbenchWindows();
        for (IWorkbenchWindow workbenchWindow : workbenchWindows) {
            IWorkbenchPage[] pages = workbenchWindow.getPages();
            for (IWorkbenchPage workbenchPage : pages) {
                IViewPart findView = workbenchPage.findView(IRepositoryView.VIEW_ID);
                if (findView != null) {
                    return (IRepositoryView) findView;
                }
            }
        }
        return null;
    }

    /**
     * ftang Comment method "deleteDocumentationFileAndNode".
     * 
     * @param docFolder
     */
    public static void deleteDocumentationFiles(File docFolder) {
        // Delete all files;
        deleteFiles(docFolder);

        // Delete all folders;
        deleteFolders(docFolder);

        // delete the root folder of job
        docFolder.delete();
    }

    /**
     * ftang Comment method "deleteFiles".
     * 
     * @param docFolder
     */
    private static void deleteFiles(File docFolder) {
        if (docFolder.listFiles() == null || docFolder.listFiles().length == 0) {
            docFolder.delete();
        } else {
            for (File file : docFolder.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                } else if (file.isDirectory()) {
                    deleteFiles(file);
                }
            }
        }

    }

    /**
     * ftang Comment method "deleteFolders".
     * 
     * @param docFolder
     */
    private static void deleteFolders(File docFolder) {
        if (docFolder.listFiles() == null || docFolder.listFiles().length == 0) {
            docFolder.delete();
        } else {
            for (File file : docFolder.listFiles()) {
                if (file.listFiles() != null && file.listFiles().length > 0) {
                    deleteFolders(file);
                } else {
                    file.delete();
                }
            }
        }
    }

    /**
     * Deletes preview pictures of job.
     * 
     * @param jobName
     * @param versionList
     */
    public static void deletePreviewPictures(Item item, String jobName, List<String> versionList) {

        if (versionList == null || versionList.size() == 0) {
            return;
        }

        String picName = "";
        for (String version : versionList) {
            picName = jobName + "_" + version + IHTMLDocConstants.JOB_PREVIEW_PIC_SUFFIX;
            IPath filePath = DocumentationPathProvider.getPathFileName(item, RepositoryConstants.IMG_DIRECTORY_OF_JOB_OUTLINE,
                    picName);
            if (filePath == null) {
                return;
            }

            String filePathStr = filePath.toOSString();
            File file = new File(filePathStr);
            if (file.exists()) {
                boolean delete = file.delete();
            }
        }
    }
}
