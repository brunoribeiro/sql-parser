
package com.akiban.sql.compiler;

import com.akiban.sql.TestBase;

import com.akiban.sql.parser.StatementNode;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.unparser.NodeToString;

import org.junit.Before;
import org.junit.Ignore;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

@Ignore
public class ASTTransformTestBase extends TestBase
{
    protected ASTTransformTestBase(String caseName, String sql, 
                                   String expected, String error) {
        super(caseName, sql, expected, error);
    }

    public static final File RESOURCE_DIR = 
        new File("src/test/resources/"
                 + ASTTransformTestBase.class.getPackage().getName().replace('.', '/'));

    protected SQLParser parser;
    protected NodeToString unparser;

    @Before
    public void makeTransformers() throws Exception {
        parser = new SQLParser();
        unparser = new NodeToString();
    }

    protected String getTree(StatementNode stmt) throws IOException {
        StringWriter str = new StringWriter();
        stmt.treePrint(str);
        return str.toString().trim();
    }

}
