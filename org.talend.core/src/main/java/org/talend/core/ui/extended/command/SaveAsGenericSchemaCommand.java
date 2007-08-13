// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2007 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.core.ui.extended.command;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.GenericSchemaConnection;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.editor.MetadataTableEditor;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.GenericSchemaConnectionItem;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNode.EProperties;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC Administrator class global comment. Detailled comment <br/>
 * 
 */
public class SaveAsGenericSchemaCommand extends Command {

    private ExtendedTableModel extendedTableModel;

    protected static final int WIZARD_WIDTH = 800;

    protected static final int WIZARD_HEIGHT = 475;

    public SaveAsGenericSchemaCommand(ExtendedTableModel extendedTableModel) {
        this.extendedTableModel = extendedTableModel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        if (extendedTableModel instanceof MetadataTableEditor) {

            MetadataTableEditor editor = (MetadataTableEditor) extendedTableModel;
            IMetadataTable metadataTable = editor.getMetadataTable();
            List<IMetadataColumn> listColumns = metadataTable.getListColumns();

            if (listColumns == null || listColumns.size() == 0) {
                return;
            }

            RepositoryNode genericSchemaNode = getGenericSchemaNode();
            boolean isGenericSchemaExisting = false;
            RepositoryObject repositoryObject = null;
            if (genericSchemaNode != null) {
                // isGenericSchemaExisting = isGenericSchemaExisting(genericSchemaNode);
            }

            IRepositoryService repositoryService = CorePlugin.getDefault().getRepositoryService();

            WizardDialog dialog = repositoryService.getGenericSchemaWizardDialog(new Shell(),
                    PlatformUI.getWorkbench(), false, null, null, true);
            dialog.setPageSize(WIZARD_WIDTH, WIZARD_HEIGHT);
            dialog.create();
            Property property = null;

            if (dialog.open() != 0) {
                return;
            } else {
                property = CorePlugin.getDefault().getRepositoryService().getPropertyFromWizardDialog();
            }

            // if (!isGenericSchemaExisting) {
            repositoryObject = new RepositoryObject();

            GenericSchemaConnectionItem item = PropertiesFactory.eINSTANCE.createGenericSchemaConnectionItem();
            // Property property = PropertiesFactory.eINSTANCE.createProperty();
            // property
            // .setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
            // .getUser());
            // property.setVersion(VersionUtils.DEFAULT_VERSION);
            // property.setStatusCode(""); //$NON-NLS-1$
            IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();
            String nextId = factory.getNextId();
            property.setId(nextId);
            // property.setLabel("test22333333");

            GenericSchemaConnection connection = ConnectionFactory.eINSTANCE.createGenericSchemaConnection();
            connection.setLabel("connectionLabel");
            connection.setComment("connectionComment");
            MetadataTable createMetadataTable = ConnectionFactory.eINSTANCE.createMetadataTable();
            createMetadataTable.setConnection(connection);
            createMetadataTable.setLabel("metadata");
            for (IMetadataColumn column : listColumns) {
                MetadataColumn createMetadataColumn = ConnectionFactory.eINSTANCE.createMetadataColumn();
                createMetadataColumn.setComment(column.getComment());
                createMetadataColumn.setLabel(column.getLabel());
                createMetadataColumn.setDefaultValue(column.getDefault());
                createMetadataColumn.setId(column.getId() + "");
                createMetadataColumn.setKey(column.isKey());
                if (column.getLength() != null) {
                    createMetadataColumn.setLength(column.getLength().intValue());
                }
                createMetadataColumn.setNullable(column.isNullable());
                createMetadataColumn.setOriginalField(column.getOriginalDbColumnName());
                createMetadataColumn.setTalendType(column.getTalendType());

                createMetadataTable.getColumns().add(createMetadataColumn);
            }
            connection.getTables().add(createMetadataTable);
            item.setProperty(property);
            item.setConnection(connection);

            // }
            IPath path = CorePlugin.getDefault().getRepositoryService().getPathForSaveAsGenericSchema();
            if (path == null) {
                this.saveMetaData(item, new Path(""));
            } else {
                this.saveMetaData(item, path);
            }
            getRepositoryView().refresh();
        }
    }

    // private boolean isGenericSchemaExisting(RepositoryNode genericSchemaNode) {
    // for (RepositoryNode repositoryNode : genericSchemaNode.getChildren()) {
    // String repositoryNodeLabel = repositoryNode.getLabel();
    //
    // }
    // return false;
    // }

    public IRepositoryView getRepositoryView() {
        IViewPart findView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(
                IRepositoryView.VIEW_ID);
        return (IRepositoryView) findView;
    }

    public RepositoryNode getGenericSchemaNode() {
        List<RepositoryNode> children = getRepositoryView().getRoot().getChildren();
        boolean isGenericSchema = false;
        for (RepositoryNode node : children) {
            isGenericSchema = node.getObjectType() == ERepositoryObjectType.METADATA_GENERIC_SCHEMA;
            if (isGenericSchema) {
                return node;
            } else {
                for (int i = 0; i < node.getChildren().size(); i++) {
                    RepositoryNode repositoryNode = node.getChildren().get(i);
                    Object properties = repositoryNode.getProperties(EProperties.CONTENT_TYPE);
                    isGenericSchema = properties == ERepositoryObjectType.METADATA_GENERIC_SCHEMA;
                    if (isGenericSchema) {
                        return repositoryNode;
                    }
                }
            }
        }
        return null;
    }

    /**
     * execute saveMetaData() on TableForm.
     */
    private void saveMetaData(ConnectionItem item, IPath path) {
        IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();
        try {
            factory.create(item, path);
        } catch (PersistenceException e) {
            // TODO
        }
    }
}
