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

public class RowConstructorNode extends ValueNode
{
    private ValueNodeList list;

    @Override
    public void init(Object list)
    {
        this.list = (ValueNodeList)list;
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
        return this.list.isEquivalent(other.list);
    }

    @Override
    public void copyFrom(QueryTreeNode o) throws StandardException
    {
        super.copyFrom(o);
        
        RowConstructorNode other = (RowConstructorNode) o;
        list.copyFrom(other.list);
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
        return "{List contains: \n" + list.getList() + "\n, size: " + list.size() + "}";
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
