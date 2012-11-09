/**
 * Copyright © 2012 Akiban Technologies, Inc.  All rights
 * reserved.
 *
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program may also be available under different license terms.
 * For more information, see www.akiban.com or contact
 * licensing@akiban.com.
 *
 * Contributors:
 * Akiban Technologies, Inc.
 */

package com.akiban.sql.parser;

import com.akiban.sql.StandardException;

public class AlterTableRenameNode extends TableElementNode
{
    private TableName newName;
    
    @Override
    public void init(Object newTableName)
    {
        newName = (TableName)newTableName;
        super.init(newName.getFullTableName(), ElementType.AT_RENAME);
    }

    @Override
    public void copyFrom(QueryTreeNode node) throws StandardException
    {
        super.copyFrom(node);
        
        newName = ((AlterTableRenameNode)node).newName;
    }
    
    public TableName newName()
    {
        return newName;
    }
}
