/* Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

/* The original from which this derives bore the following: */

/*

   Derby - Class org.apache.derby.impl.sql.compile.JavaToSQLValueNode

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
 * This node type converts a value from the Java domain to the SQL domain.
 */

public class JavaToSQLValueNode extends ValueNode
{
  JavaValueNode javaNode;

  /**
   * Initializer for a JavaToSQLValueNode
   *
   * @param value The Java value to convert to the SQL domain
   */
  public void init(Object value) {
    this.javaNode = (JavaValueNode)value;
  }

  /**
   * Prints the sub-nodes of this object.  See QueryTreeNode for
   * how tree printing is supposed to work.
   *
   * @param depth The depth of this node in the tree
   */

  public void printSubNodes(int depth) {
    super.printSubNodes(depth);

    printLabel(depth, "javaNode: ");
    javaNode.treePrint(depth + 1);
  }

  /**
   * Get the JavaValueNode that lives under this JavaToSQLValueNode.
   *
   * @return The JavaValueNode that lives under this node.
   */

  public JavaValueNode getJavaValueNode() {
    return javaNode;
  }

  /**
   * Accept the visitor for all visitable children of this node.
   * 
   * @param v the visitor
   *
   * @exception StandardException on error
   */
  void acceptChildren(Visitor v) throws StandardException {
    super.acceptChildren(v);

    if (javaNode != null) {
      javaNode = (JavaValueNode)javaNode.accept(v);
    }
  }
        
  /**
   * {@inheritDoc}
   */
  protected boolean isEquivalent(ValueNode o) {
    // anything in the java domain is not equiavlent.
    return false;
  }

}
