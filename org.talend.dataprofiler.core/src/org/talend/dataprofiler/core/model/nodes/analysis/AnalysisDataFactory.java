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
package org.talend.dataprofiler.core.model.nodes.analysis;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.talend.dataquality.analysis.category.AnalysisCategories;
import org.talend.dataquality.analysis.category.AnalysisCategory;
import org.talend.dq.analysis.category.CategoryHandler;

/**
 * @author zqin
 * 
 */
public class AnalysisDataFactory {

    public static Object createTreeData() {
        List<AnalysisTypeNode> returnList = new ArrayList<AnalysisTypeNode>();

        AnalysisTypeNode typeNode = null;
        // TODO zqin use CategoryHandler
        AnalysisCategories analysisCategories = CategoryHandler.getAnalysisCategories();
        // TODO zqin use this tree (use label attribute of each Category instance)
        EList<AnalysisCategory> categories = analysisCategories.getCategories();
        
        
        for (AnalysisCategory category : categories) {
            
            typeNode = new AnalysisTypeNode(category.getLabel(), category.getLabel(), null);
            if (category.getSubCategories() != null) {
                List<AnalysisTypeNode> subCategories = null;
                for (AnalysisCategory subCategory : category.getSubCategories()) {
                    subCategories = new ArrayList<AnalysisTypeNode>();
                    subCategories.add(new AnalysisTypeNode(subCategory.getLabel(), subCategory.getLabel(), typeNode));
                }
                typeNode.setChildren(subCategories.toArray());
            }
            
            returnList.add(typeNode);
        }

        return returnList;

    }
    
}
