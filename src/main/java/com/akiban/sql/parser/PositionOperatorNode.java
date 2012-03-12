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

import com.akiban.sql.types.ValueClassName;

public class PositionOperatorNode extends BinaryOperatorNode
{    
    public void init(Object leftOperand, Object rightOperand) 
    {
        super.init(leftOperand, rightOperand,
                   ValueClassName.StringDataValue, ValueClassName.StringDataValue);
    }
    
    public void setNodeType(int nodeType) 
    {
        if (nodeType == NodeTypes.POSITION_FUNCTION_NODE)
        {
            setOperator("position");
            setMethodName("position");
            super.setNodeType(nodeType);
        }
        else
            assert false : "Unexpected nodeType:" + nodeType;
        
    }
}
