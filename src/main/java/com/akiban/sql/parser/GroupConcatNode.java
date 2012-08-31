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

public class GroupConcatNode extends AggregateNode
{
    private String sep;
    private OrderByList orderCols;
    
    @Override
    public void init(Object value,
                     Object aggClass,
                     Object distinct,
                     Object aggName,
                     Object orderCols,
                     Object sep)
            throws StandardException
    {
        super.init(value,
                  aggClass,
                  distinct,
                  aggName);
        
        this.orderCols = (OrderByList) orderCols;
        this.sep = (String) sep;
    }
    
    @Override
    public void copyFrom(QueryTreeNode node) throws StandardException
    {
        super.copyFrom(node);
        
        GroupConcatNode other = (GroupConcatNode) node;
        this.sep = other.sep;
        this.orderCols = (OrderByList) getNodeFactory().copyNode(other.orderCols,
                                                   getParserContext());
    }
    
    @Override
    void acceptChildren(Visitor v) throws StandardException
    {
        super.acceptChildren(v);
        
        if (orderCols != null)
            orderCols.acceptChildren(v);
    }

     /**
     * @inheritDoc
     */
    @Override
    protected boolean isEquivalent(ValueNode o) throws StandardException
    {
        if (!isSameNodeType(o))
            return false;
        
        GroupConcatNode other = (GroupConcatNode) o;
        
        return  this.sep.equals(other.sep)
             && this.orderCols.equals(other.orderCols);
    }

    @Override
    public String toString()
    {
        return super.toString() + 
               "\nseparator: " + sep +
               "\norderyByList: "+ orderCols;
                
    }
    
    public String getSeparator()
    {
        return sep;
    }
    
    public OrderByList getOrderBy()
    {
        return orderCols;
    }
}
