
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
