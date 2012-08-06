/**
 * END USER LICENSE AGREEMENT (“EULA”)
 *
 * READ THIS AGREEMENT CAREFULLY (date: 9/13/2011):
 * http://www.akiban.com/licensing/20110913
 *
 * BY INSTALLING OR USING ALL OR ANY PORTION OF THE SOFTWARE, YOU ARE ACCEPTING
 * ALL OF THE TERMS AND CONDITIONS OF THIS AGREEMENT. YOU AGREE THAT THIS
 * AGREEMENT IS ENFORCEABLE LIKE ANY WRITTEN AGREEMENT SIGNED BY YOU.
 *
 * IF YOU HAVE PAID A LICENSE FEE FOR USE OF THE SOFTWARE AND DO NOT AGREE TO
 * THESE TERMS, YOU MAY RETURN THE SOFTWARE FOR A FULL REFUND PROVIDED YOU (A) DO
 * NOT USE THE SOFTWARE AND (B) RETURN THE SOFTWARE WITHIN THIRTY (30) DAYS OF
 * YOUR INITIAL PURCHASE.
 *
 * IF YOU WISH TO USE THE SOFTWARE AS AN EMPLOYEE, CONTRACTOR, OR AGENT OF A
 * CORPORATION, PARTNERSHIP OR SIMILAR ENTITY, THEN YOU MUST BE AUTHORIZED TO SIGN
 * FOR AND BIND THE ENTITY IN ORDER TO ACCEPT THE TERMS OF THIS AGREEMENT. THE
 * LICENSES GRANTED UNDER THIS AGREEMENT ARE EXPRESSLY CONDITIONED UPON ACCEPTANCE
 * BY SUCH AUTHORIZED PERSONNEL.
 *
 * IF YOU HAVE ENTERED INTO A SEPARATE WRITTEN LICENSE AGREEMENT WITH AKIBAN FOR
 * USE OF THE SOFTWARE, THE TERMS AND CONDITIONS OF SUCH OTHER AGREEMENT SHALL
 * PREVAIL OVER ANY CONFLICTING TERMS OR CONDITIONS IN THIS AGREEMENT.
 */

package com.akiban.sql.parser;

import com.akiban.sql.StandardException;

public class ZOrderFuncNode extends IndexColumnList
{
    public static enum FunctionType
    {
        Z_ORDER_LAT_LON
        // ADD MORE AS NEEDED
    }
    private FunctionType methodName;
      
    @Override
    public void init(Object lonColumnName,
                     Object latColumnName,
                     Object functionName)
    {
        add((IndexColumn) latColumnName);
        add((IndexColumn) lonColumnName);
        this.methodName = ((FunctionType)functionName);
    }
    
    @Override
    public void copyFrom(QueryTreeNode node) throws StandardException
    {
        super.copyFrom(node);
        ZOrderFuncNode other = (ZOrderFuncNode) node;
        this.methodName = other.methodName;
    }

    @Override
    public String toString()
    {
        return super.toString() + "\n" 
                + "methodName: " + methodName + "\n"
                ;
    }
 
    @Override
    public void acceptChildren(Visitor v) throws StandardException
    {
        super.accept(v);
    }

    public FunctionType getMethodName()
    {
        return methodName;
    }
 
    public IndexColumn getLatColumn()
    {
        return get(0);
    }
    
    public IndexColumn getLonColumn()
    {
        return get(1);
    }
}
