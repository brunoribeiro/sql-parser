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
