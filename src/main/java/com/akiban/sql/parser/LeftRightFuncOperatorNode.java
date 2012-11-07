
package com.akiban.sql.parser;

import com.akiban.sql.types.ValueClassName;

public class LeftRightFuncOperatorNode extends BinaryOperatorNode
{
    @Override
    public void init (Object leftOperand, Object rightOperand)
    {
        super.init(leftOperand, rightOperand,
                ValueClassName.StringDataValue, ValueClassName.NumberDataValue);
    }
    
    @Override
    public void setNodeType(int nodeType) 
    {
        String op = null;
        String method = null;
        
        switch(nodeType)
        {
            case NodeTypes.LEFT_FN_NODE:
                op = "getLeft";
                method = "getLeft";
                break;
            case NodeTypes.RIGHT_FN_NODE:
                op = "getRight";
                method = "getRight";
                break;
            default:
                assert false : "Unexpected nodeType: " + nodeType;
        }
        setOperator(op);
        setMethodName(method);
        super.setNodeType(nodeType);
    }
}
