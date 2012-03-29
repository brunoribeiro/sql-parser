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

import com.akiban.sql.TestBase;

import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.StatementNode;

import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.*;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runner.RunWith;

import java.io.*;
import java.util.*;

@RunWith(Parameterized.class)
public class SQLParserTest extends TestBase implements TestBase.GenerateAndCheckResult
{
    public static final File RESOURCE_DIR = 
        new File("src/test/resources/"
                 + SQLParserTest.class.getPackage().getName().replace('.', '/'));

    protected SQLParser parser;
    protected File featuresFile;

    @Before
    public void before() throws Exception {
        parser = new SQLParser();
        if (featuresFile != null)
            parseFeatures(featuresFile, parser.getFeatures());
    }

    protected void parseFeatures(File file, Set<SQLParserFeature> features) 
            throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                boolean add;
                switch (line.charAt(0)) {
                case '+':
                    add = true;
                    break;
                case '-':
                    add = false;
                    break;
                default:
                    throw new IOException("Malformed features line: should start with + or - " + line);
                }
                SQLParserFeature feature = SQLParserFeature.valueOf(line.substring(1));
                if (add)
                    features.add(feature);
                else
                    features.remove(feature);
            }
        }
        finally {
            reader.close();
        }
    }

    protected String getTree(StatementNode stmt) throws IOException {
        StringWriter str = new StringWriter();
        stmt.treePrint(str);
        return str.toString().trim();
    }

    @Parameters
    public static Collection<Object[]> queries() throws Exception {
        Collection<Object[]> result = new ArrayList<Object[]>();
        for (Object[] args : sqlAndExpected(RESOURCE_DIR)) {
            File featuresFile = new File(RESOURCE_DIR, args[0] + ".features");
            if (!featuresFile.exists())
                featuresFile = null;
            Object[] nargs = new Object[args.length+1];
            nargs[0] = args[0];
            nargs[1] = featuresFile;
            System.arraycopy(args, 1, nargs, 2, args.length-1);
            result.add(nargs);
        }
        return result;
    }

    public SQLParserTest(String caseName, File featuresFile,
                         String sql, String expected, String error) {
        super(caseName, sql, expected, error);
        this.featuresFile = featuresFile;
    }

    @Test
    public void testParser() throws Exception {
        generateAndCheckResult();
    }

    @Override
    public String generateResult() throws Exception {
        StatementNode stmt = parser.parseStatement(sql);
        return getTree(stmt);
    }

    @Override
    public void checkResult(String result) throws IOException {
        assertEqualsWithoutHashes(caseName, expected, result);
    }

}
