// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.model.routines;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.CorePlugin;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.designer.core.model.utils.emf.talendfile.ItemInforType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * ggu class global comment. Detailled comment
 */
public final class RoutinesUtil {

    private RoutinesUtil() {
    }

    public static boolean allowDeletedRoutine() {
        return false;
    }

    public static List<IRepositoryViewObject> getCurrentSystemRoutines() {
        List<IRepositoryViewObject> repositoryObjects = new ArrayList<IRepositoryViewObject>();
        IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();

        try {
            List<IRepositoryViewObject> all = factory.getAll(ProjectManager.getInstance().getCurrentProject(),
                    ERepositoryObjectType.ROUTINES);
            for (IRepositoryViewObject obj : all) {
                Item item = obj.getProperty().getItem();
                if (item instanceof RoutineItem && ((RoutineItem) item).isBuiltIn()) {
                    repositoryObjects.add(obj);
                }
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }

        return repositoryObjects;

    }

    /**
     * 
     * ggu Comment method "collectRelatedRoutines".
     * 
     * @param includeRoutineIdOrNames if null, will add all.
     * @param system
     * @return
     */
    public static List<IRepositoryViewObject> collectRelatedRoutines(Set<String> includeRoutineIdOrNames, boolean system) {
        List<IRepositoryViewObject> allRoutines = new ArrayList<IRepositoryViewObject>();
        if (system) {
            List<IRepositoryViewObject> systemRoutines = RoutinesUtil.getCurrentSystemRoutines();
            for (IRepositoryViewObject object : systemRoutines) {
                if (includeRoutineIdOrNames == null || includeRoutineIdOrNames.contains(object.getLabel())) {
                    allRoutines.add(object);
                }
            }
        } else {
            collectUserRoutines(allRoutines, ProjectManager.getInstance().getCurrentProject(), includeRoutineIdOrNames);
        }
        return allRoutines;
    }

    private static void collectUserRoutines(List<IRepositoryViewObject> allRoutines,
            org.talend.core.model.general.Project project, Set<String> includeRoutineIdOrNames) {
        try {
            List<IRepositoryViewObject> all = CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory().getAll(
                    project, ERepositoryObjectType.ROUTINES, allowDeletedRoutine());
            for (IRepositoryViewObject obj : all) {
                if (includeRoutineIdOrNames == null || includeRoutineIdOrNames.contains(obj.getId())) {
                    allRoutines.add(obj);
                }
            }
            for (org.talend.core.model.general.Project p : ProjectManager.getInstance().getReferencedProjects(project)) {
                collectUserRoutines(allRoutines, p, includeRoutineIdOrNames);
            }
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
    }

    public static List<ItemInforType> createJobRoutineDependencies(boolean system) throws PersistenceException {
        List<ItemInforType> itemInfors = new ArrayList<ItemInforType>();
        if (system) {
            List<IRepositoryViewObject> systemRoutines = RoutinesUtil.getCurrentSystemRoutines();
            for (IRepositoryViewObject object : systemRoutines) {
                ItemInforType itemInfor = createItemInforType((RoutineItem) object.getProperty().getItem());
                itemInfors.add(itemInfor);
            }
        } else {
            Project p = ProjectManager.getInstance().getCurrentProject();
            createJobRoutineDependencies(itemInfors, p);
        }
        return itemInfors;
    }

    private static void createJobRoutineDependencies(List<ItemInforType> itemInfors, Project project) throws PersistenceException {

        List<IRepositoryViewObject> all = CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory().getAll(
                project, ERepositoryObjectType.ROUTINES, allowDeletedRoutine());
        for (IRepositoryViewObject object : all) {
            Property property = object.getProperty();
            RoutineItem item = (RoutineItem) property.getItem();
            if (!item.isBuiltIn()) {
                ItemInforType itemInfor = createItemInforType(item);
                itemInfors.add(itemInfor);
            }
        }
        for (Project p : ProjectManager.getInstance().getReferencedProjects(project)) {
            createJobRoutineDependencies(itemInfors, p);
        }
    }

    private static ItemInforType createItemInforType(RoutineItem routineItem) {
        Property property = routineItem.getProperty();

        ItemInforType itemRecordType = TalendFileFactory.eINSTANCE.createItemInforType();

        itemRecordType.setSystem(routineItem.isBuiltIn());
        if (itemRecordType.isSystem()) {
            itemRecordType.setIdOrName(property.getLabel());
        } else {
            itemRecordType.setIdOrName(property.getId());
        }
        return itemRecordType;
    }
}
