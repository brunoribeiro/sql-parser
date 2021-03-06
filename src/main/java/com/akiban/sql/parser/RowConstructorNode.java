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

public class RowConstructorNode extends ValueNode
{
    private ValueNodeList list;
    private int depth; // max depth
    
    @Override
    public void init(Object list, Object count)
    {
        this.list = (ValueNodeList)list;
        depth = ((int[])count)[0];
    }

    /**
     * @inheritDoc
     */
    @Override
    protected boolean isEquivalent(ValueNode o) throws StandardException
    {
        if (!isSameNodeType(o))
        {
            return false;
        }
        
        RowConstructorNode other = (RowConstructorNode)o;
        return list.isEquivalent(other.list) && depth == other.depth;
    }

    @Override
    public void copyFrom(QueryTreeNode o) throws StandardException
    {
        super.copyFrom(o);
        RowConstructorNode other = (RowConstructorNode) o;
        list = (ValueNodeList)getNodeFactory().copyNode(other.list,
                                                        getParserContext());
        depth = other.depth;
    }

     /**
     * Accept the visitor for all visitable children of this node.
     * 
     * @param v the visitor
     *
     * @exception StandardException on error
     */
    @Override
    void acceptChildren(Visitor v) throws StandardException 
    {
        super.acceptChildren(v);

        if (list != null)
            list.accept(v);
    }
    
    @Override
    public String toString()
    {
        return list.toString() + "depth: " + depth + "\n";
    }

    public int getDepth()
    {
        return depth;
    }

    public ValueNodeList getNodeList()
    {
        return list;
    }
    
    public int listSize()
    {
        return list.size();
    }
}
