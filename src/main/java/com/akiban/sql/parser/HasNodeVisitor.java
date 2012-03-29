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

/* The original from which this derives bore the following: */

/*

   Derby - Class org.apache.derby.impl.sql.compile.HasNodeVisitor

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package com.akiban.sql.parser;

import com.akiban.sql.StandardException;

/**
 * Find out if we have a particular node anywhere in the
 * tree.    Stop traversal as soon as we find one.
 * <p>
 * Can find any type of node -- the class or class name
 * of the target node is passed in as a constructor
 * parameter.
 *
 */
public class HasNodeVisitor implements Visitor
{
    protected boolean hasNode;
    private Class nodeClass;
    private Class skipOverClass;
    /**
     * Construct a visitor
     *
     * @param nodeClass the class of the node that 
     * we are looking for.
     */
    public HasNodeVisitor(Class nodeClass) {
        this.nodeClass = nodeClass;
    }

    /**
     * Construct a visitor
     *
     * @param nodeClass the class of the node that 
     * we are looking for.
     * @param skipOverClass do not go below this
     * node when searching for nodeClass.
     */
    public HasNodeVisitor(Class nodeClass, Class skipOverClass) {
        this.nodeClass = nodeClass;
        this.skipOverClass = skipOverClass;
    }

    ////////////////////////////////////////////////
    //
    // VISITOR INTERFACE
    //
    ////////////////////////////////////////////////

    /**
     * If we have found the target node, we are done.
     *
     * @param node the node to process
     *
     * @return me
     */
    public Visitable visit(Visitable node) {
        if (nodeClass.isInstance(node)) {
            hasNode = true;
        }
        return node;
    }

    /**
     * Stop traversal if we found the target node
     *
     * @return true/false
     */
    public boolean stopTraversal() {
        return hasNode;
    }

    /**
     * Don't visit childen under the skipOverClass
     * node, if it isn't null.
     *
     * @return true/false
     */
    public boolean skipChildren(Visitable node) {
        return (skipOverClass == null) ? false: skipOverClass.isInstance(node);
    }

    /**
     * Visit parent before children.
     */
    public boolean visitChildrenFirst(Visitable node) {
        return false;
    }

    ////////////////////////////////////////////////
    //
    // CLASS INTERFACE
    //
    ////////////////////////////////////////////////
    /**
     * Indicate whether we found the node in
     * question
     *
     * @return true/false
     */
    public boolean hasNode() {
        return hasNode;
    }

    /**
     * Reset the status so it can be run again.
     *
     */
    public void reset() {
        hasNode = false;
    }
}
