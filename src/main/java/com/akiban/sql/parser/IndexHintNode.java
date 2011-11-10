/**
 * Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */

package com.akiban.sql.parser;

import com.akiban.sql.StandardException;

import java.util.List;

/**
 * MySQL's index hint.
 */
public class IndexHintNode extends QueryTreeNode
{
    public static enum HintType {
        USE, IGNORE, FORCE
    }
    
    public static enum HintScope {
        JOIN, ORDER_BY, GROUP_BY
    }

    private HintType hintType;
    private HintScope hintScope;
    private List<String> indexes;

    public void init(Object hintType,
                     Object hintScope,
                     Object indexes)
    {
        this.hintType = (HintType)hintType;
        this.hintScope = (HintScope)hintScope;
        this.indexes = (List<String>)indexes;
    }

    public HintType getHintType() {
        return hintType;
    }
    public HintScope getHintScope() {
        return hintScope;
    }
    public List<String> getIndexes() {
        return indexes;
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);

        IndexHintNode other = (IndexHintNode)node;
        this.hintType = other.hintType;
        this.hintScope = other.hintScope;
        this.indexes = other.indexes;
    }
    
    /**
     * Convert this object to a String.  See comments in QueryTreeNode.java
     * for how this should be done for tree printing.
     *
     * @return This object as a String
     */
    public String toString() {
        return "hintType: " + hintType + "\n" +
            "hintScope: " + hintScope + "\n" +
            "indexes: " + indexes + "\n" +
            super.toString();
    }

}
