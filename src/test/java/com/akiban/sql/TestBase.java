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

package com.akiban.sql;

import org.junit.ComparisonFailure;
import org.junit.Ignore;
import static junit.framework.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Ignore
public class TestBase
{
    protected TestBase() {
    }

    protected String caseName, sql, expected, error;

    protected TestBase(String caseName, String sql, String expected, String error) {
        this.caseName = caseName;
        this.sql = sql;
        this.expected = expected;
        this.error = error;
    }

    public static File[] listSQLFiles(File dir) {
        File[] result = dir.listFiles(new RegexFilenameFilter(".*\\.sql"));
        Arrays.sort(result, new Comparator<File>() {
                        public int compare(File f1, File f2) {
                            return f1.getName().compareTo(f2.getName());
                        }
                    });
        return result;
    }

    public static File changeSuffix(File sqlFile, String suffix) {
        return new File(sqlFile.getParentFile(),
                        sqlFile.getName().replace(".sql", suffix));
    }

    public static String fileContents(File file) throws IOException {
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            StringBuilder str = new StringBuilder();
            char[] buf = new char[128];
            while (true) {
                int nc = reader.read(buf);
                if (nc < 0) break;
                str.append(buf, 0, nc);
            }
            return str.toString();
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ex) {
                }
            }
        }
    }

    public static String[] fileContentsArray(File file) throws IOException {
        FileReader reader = null;
        List<String> result = new ArrayList<String>();
        try {
            reader = new FileReader(file);
            BufferedReader buffered = new BufferedReader(reader);
            while (true) {
                String line = buffered.readLine();
                if (line == null) break;
                result.add(line);
            }
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ex) {
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }

    public static Collection<Object[]> sqlAndExpected(File dir) 
            throws IOException {
        return sqlAndExpected(dir, false);
    }

    public static Collection<Object[]> sqlAndExpectedAndParams(File dir) 
            throws IOException {
        return sqlAndExpected(dir, true);
    }
    
    static final boolean RUN_FAILING_TESTS = Boolean.getBoolean("akiban.sql.test.runFailing");

    public static Collection<Object[]> sqlAndExpected(File dir, 
                                                      boolean andParams)
            throws IOException {
        Collection<Object[]> result = new ArrayList<Object[]>();
        for (File sqlFile : listSQLFiles(dir)) {
            String caseName = sqlFile.getName().replace(".sql", "");
            if (changeSuffix(sqlFile, ".fail").exists() && !RUN_FAILING_TESTS)
                continue;
            String sql = fileContents(sqlFile);
            String expected, error;
            File expectedFile = changeSuffix(sqlFile, ".expected");
            if (expectedFile.exists())
                expected = fileContents(expectedFile);
            else
                expected = null;
            File errorFile = changeSuffix(sqlFile, ".error");
            if (errorFile.exists())
                error = fileContents(errorFile);
            else
                error = null;
            if (andParams) {
                String[] params = null;
                File paramsFile = changeSuffix(sqlFile, ".params");
                if (paramsFile.exists()) {
                    params = fileContentsArray(paramsFile);
                }
                result.add(new Object[] {
                               caseName, sql, expected, error, params
                           });
            }
            else {
                result.add(new Object[] {
                               caseName, sql, expected, error
                           });
            }
        }
        return result;
    }

    /** A class implementing this can call {@link #generateAndCheckResult(). */
    public interface GenerateAndCheckResult {
        public String generateResult() throws Exception;
        public void checkResult(String result) throws IOException;
    }

    public static void generateAndCheckResult(GenerateAndCheckResult handler,
                                              String caseName, 
                                              String expected, String error) 
            throws Exception {
        if ((expected != null) && (error != null)) {
            fail(caseName + ": both expected result and expected error specified.");
        }
        String result = null;
        Exception errorResult = null;
        try {
            result = handler.generateResult();
        }
        catch (Exception ex) {
            errorResult = ex;
        }
        if (error != null) {
            if (errorResult == null)
                fail(caseName + ": error expected but none thrown");
            else
                assertEquals(caseName, error, errorResult.toString());
        }
        else if (errorResult != null) {
            throw errorResult;
        }
        else if (expected == null) {
            fail(caseName + " no expected result given. actual='" + result + "'");
        }
        else {
            handler.checkResult(result);
        }
    }

    /** @see GenerateAndCheckResult */
    protected void generateAndCheckResult() throws Exception {
        generateAndCheckResult((GenerateAndCheckResult)this, caseName, expected, error);
    }

    public static void assertEqualsWithoutHashes(String caseName,
                                                 String expected, String actual) 
            throws IOException {
        assertEqualsWithoutPattern(caseName, 
                                   expected, actual, 
                                   CompareWithoutHashes.HASH_REGEX);
    }

    public static void assertEqualsWithoutPattern(String caseName,
                                                  String expected, String actual, 
                                                  String regex) 
            throws IOException {
        if (!new CompareWithoutHashes(regex).match(new StringReader(expected), 
                                                   new StringReader(actual)))
            throw new ComparisonFailure(caseName, expected, actual);
    }

}