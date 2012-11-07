
package com.akiban.sql.parser;

public class AlterTableRenameNode extends TableElementNode
{
    @Override
    public void init(Object newTableName)
    {
        super.init(newTableName, ElementType.AT_RENAME);
    }
}
