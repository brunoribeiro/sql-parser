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

package com.akiban.sql.compiler;

import com.akiban.sql.TestBase;

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
public class BooleanNormalizerTest extends ASTTransformTestBase implements TestBase.GenerateAndCheckResult
{
    public static final File RESOURCE_DIR = 
        new File(ASTTransformTestBase.RESOURCE_DIR, "normalize");

    protected BooleanNormalizer booleanNormalizer;

    @Before
    public void makeNormalizer() throws Exception {
        booleanNormalizer = new BooleanNormalizer(parser);
    }

    @Parameters
    public static Collection<Object[]> statements() throws Exception {
        return sqlAndExpected(RESOURCE_DIR);
    }

    public BooleanNormalizerTest(String caseName, String sql, 
                                 String expected, String error) {
        super(caseName, sql, expected, error);
    }

    @Test
    public void testNormalizer() throws Exception {
        generateAndCheckResult();
    }

    @Override
    public String generateResult() throws Exception {
        StatementNode stmt = parser.parseStatement(sql);
        stmt = booleanNormalizer.normalize(stmt);
        return unparser.toString(stmt);
    }

    @Override
    public void checkResult(String result) {
        assertEquals(caseName, expected, result);
    }

}
