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

public class TrimOperatorNode extends BinaryOperatorNode
{
    @Override
    public void init(Object trimSource, Object trimChar, Object operatorType)
    {   
        BinaryOperatorNode.OperatorType optype = (BinaryOperatorNode.OperatorType)operatorType;
        switch(optype)
        {
            default:     assert false : "TrimOperatorNode.init(trimSource, trimChar, operatorType) called with wrong OperatoryType: " + operatorType;
            case LTRIM:
            case TRIM:
            case RTRIM:  super.init(trimSource,
                                    trimChar,
                                    "TRIM",
                                    optype.name().toLowerCase(),
                                    ValueClassName.StringDataValue,
                                    ValueClassName.StringDataValue);
        }
    }
}
