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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompareWithoutHashes
{
    public static final String HASH_REGEX = "[\\p{Alnum}]*\\@[\\p{XDigit}]+";

    private Pattern pattern;
    private Map<String,String> equivalences;

    public CompareWithoutHashes() {
        this(HASH_REGEX);
    }

    public CompareWithoutHashes(String regex) {
        this.pattern = Pattern.compile(regex);
        this.equivalences = new HashMap<String,String>();
    }

    public boolean match(Reader r1, Reader r2) throws IOException {
        BufferedReader br1 = new BufferedReader(r1);
        BufferedReader br2 = new BufferedReader(r2);
        while (true) {
            String l1 = br1.readLine();
            String l2 = br2.readLine();

            if (l1 == null) {
                if (l2 == null)
                    break;
                l1 = "";
            }
            else if (l2 == null)
                l2 = "";
            if (!match(l1, l2)) 
                return false;
        }
        return true;
    }

    public boolean match(String s1, String s2) {
        if (s1.equals(s2))
            return true;

        String[] ha1 = findHashes(s1);
        String[] ha2 = findHashes(s2);
        if (ha1.length != ha2.length)
            return false;
        for (int i = 0; i < ha1.length; i++) {
            String h1 = ha1[i];
            String h2 = ha2[i];
            String oh2 = equivalences.put(h1, h2);
            if ((oh2 != null) && !oh2.equals(h2))
                return false;
        }

        // It's possible that equivalences swaps two matches, so need intermediate.
        for (int i = 0; i < ha1.length; i++) {
            s1 = s1.replace(ha1[i], "%!" + i + "!%");
        }
        for (int i = 0; i < ha1.length; i++) {
            s1 = s1.replace("%!" + i + "!%", ha2[i]);
        }
        
        return s1.equals(s2);
    }

    protected String[] findHashes(String s) {
        Matcher matcher = pattern.matcher(s);
        List<String> matches = new ArrayList<String>();
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        return matches.toArray(new String[matches.size()]);
    }

}
