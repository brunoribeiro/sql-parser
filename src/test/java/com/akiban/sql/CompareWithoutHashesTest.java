/**
 * Copyright © 2012 Akiban Technologies, Inc.  All rights
 * reserved.
 *
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program may also be available under different license terms.
 * For more information, see www.akiban.com or contact
 * licensing@akiban.com.
 *
 * Contributors:
 * Akiban Technologies, Inc.
 */
package com.akiban.sql;

import static org.junit.Assert.*;
import org.junit.*;

public class CompareWithoutHashesTest {
    
    public CompareWithoutHashesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of match method, of class CompareWithoutHashes.
     */
    @Test
    public void testMatch_String_String() {
        System.out.println("match");
        String s1 = "ABC@123";
        String s2 = "XYZ@456";
        CompareWithoutHashes instance = new CompareWithoutHashes();
        boolean expResult = false;
        boolean result = instance.match(s1, s2);
        assertEquals(result, expResult);
    }

    /**
     * Test of converter method, of class CompareWithoutHashes.
     */
    @Test
    public void testConverter() {
        System.out.println("converter");
        String s1 = "ABC@123 XYZ@789";
        String s2 = "XYZ@456";
        CompareWithoutHashes instance = new CompareWithoutHashes();
        String expResult = "ABC@123 XYZ@456";
        String result = instance.converter(s1, s2);
        assertEquals(expResult, result);
    }
}
