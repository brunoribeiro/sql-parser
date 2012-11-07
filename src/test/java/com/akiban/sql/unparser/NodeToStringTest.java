
package com.akiban.sql.unparser;

import com.akiban.sql.TestBase;

import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.StatementNode;

import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.*;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Collection;

@RunWith(Parameterized.class)
public class NodeToStringTest extends TestBase implements TestBase.GenerateAndCheckResult
{
    public static final File RESOURCE_DIR = 
        new File("src/test/resources/"
                 + NodeToStringTest.class.getPackage().getName().replace('.', '/'));

    protected SQLParser parser;
    protected NodeToString unparser;

    @Before
    public void before() throws Exception {
        parser = new SQLParser();
        unparser = new NodeToString();
    }

    @Parameters
    public static Collection<Object[]> statements() throws Exception {
        return sqlAndExpected(RESOURCE_DIR);
    }

    public NodeToStringTest(String caseName, String sql, 
                            String expected, String error) {
        super(caseName, sql, expected, error);
    }

    @Test
    public void testUnparser() throws Exception {
        generateAndCheckResult();
    }

    @Override
    public String generateResult() throws Exception {
        StatementNode stmt = parser.parseStatement(sql);
        return unparser.toString(stmt);
    }

    @Override
    public void checkResult(String result) {
        assertEquals(caseName, expected, result);
    }

}
