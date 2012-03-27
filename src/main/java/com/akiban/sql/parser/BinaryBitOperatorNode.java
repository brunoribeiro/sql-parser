/**
 * Copyright (C) 2012 Akiban Technologies Inc.
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

/**
 * This node represents a binary arithmetic operator, like + or *.
 *
 */

public class BinaryBitOperatorNode extends BinaryOperatorNode
{
    /**
     * Initializer for a BinaryBitOperatorNode
     *
     * @param leftOperand The left operand
     * @param rightOperand  The right operand
     */

    public void init(Object operatorType, Object leftOperand, Object rightOperand) {
        super.init(leftOperand, rightOperand,
                   ValueClassName.NumberDataValue, ValueClassName.NumberDataValue);

        String operator = null;
        String methodName = null;

        switch ((OperatorType)operatorType) {
        case BITAND:
            operator = "&";
            methodName = "bitand";
            break;

        case BITOR:
            operator = "|";
            methodName = "bitor";
            break;

        case BITXOR:
            operator = "^";
            methodName = "bitxor";
            break;

        case LEFT_SHIFT:
            operator = "<<";
            methodName = "leftshift";
            break;

        case RIGHT_SHIFT:
            operator = ">>";
            methodName = "rightshift";
            break;

        default:
            assert false : "Unexpected operator:" + operatorType;
        }
        setOperator(operator);
        setMethodName(methodName);
    }

}
