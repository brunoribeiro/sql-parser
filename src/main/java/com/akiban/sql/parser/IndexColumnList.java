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

/**
 * List of IndexColumns. Also notes application of up to one function to
 * a consecutive list of IndexColumns.
 */
public class IndexColumnList extends QueryTreeNodeList<IndexColumn>
{
    private FunctionApplication functionApplication;

    public static enum FunctionType
    {
        Z_ORDER_LAT_LON
        // ADD MORE AS NEEDED
    }

    private static class FunctionApplication
    {
        public FunctionApplication(FunctionType functionType,
                                   int firstArgumentPosition,
                                   int nArguments)
        {
            this.functionType = functionType;
            this.firstArgumentPosition = firstArgumentPosition;
            this.lastArgumentPosition = firstArgumentPosition + nArguments - 1;
            this.nArguments = nArguments;
        }

        public final FunctionType functionType;
        public final int firstArgumentPosition;
        public final int lastArgumentPosition;
        public final int nArguments;
    }

    public void applyFunction(Object functionType,
                              int firstArgumentPosition,
                              int nArguments) throws StandardException
    {
        if (functionApplication != null) {
            throw new StandardException("Cannot use multiple functions in one index definition");
        }
        functionApplication = new FunctionApplication((FunctionType) functionType,
                                                      firstArgumentPosition,
                                                      nArguments);
    }

    public int firstFunctionArg()
    {
        return
            functionApplication == null
            ? Integer.MAX_VALUE
            : functionApplication.firstArgumentPosition;
    }

    public int lastFunctionArg()
    {
        return
            functionApplication == null
            ? Integer.MIN_VALUE
            : functionApplication.lastArgumentPosition;
    }

    public FunctionType functionType()
    {
        return functionApplication == null ? null : functionApplication.functionType;
    }

    @Override
    public void copyFrom(QueryTreeNode node) throws StandardException
    {
        super.copyFrom(node);
        IndexColumnList that = (IndexColumnList) node;
        this.functionApplication = that.functionApplication;
    }

    @Override
    public String toString()
    {
        return
            functionApplication != null
            ? String.format("\nmethodName: %s\nfirstArg: %s\nlastArg: %s\n",
                            functionApplication.functionType, functionApplication.firstArgumentPosition, functionApplication.lastArgumentPosition)
            : super.toString();
    }
}
