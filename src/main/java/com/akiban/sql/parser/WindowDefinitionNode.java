/**
 * Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */

/* The original from which this derives bore the following: */

/*

   Derby - Class org.apache.derby.impl.sql.compile.WindowDefinitionNode

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
 * This class represents an OLAP window definition.
 */
public final class WindowDefinitionNode extends WindowNode
{
  /**
   * True of the window definition was inlined.
   */
  private boolean inlined;

  /**
   * The order by list if the window definition contains a <window order
   * clause>, else null.
   */
  private OrderByList orderByList;

  /**
   * Initializer.
   *
   * @param arg1 The window name, null if in-lined definition
   * @param arg2 ORDER BY list
   * @exception StandardException
   */
  public void init(Object arg1, Object arg2) throws StandardException {
    String name = (String)arg1;

    orderByList = (OrderByList)arg2;

    if (name != null) {
      super.init(arg1);
      inlined = false;
    } 
    else {
      super.init("IN-LINE");
      inlined = true;
    }

    if (orderByList != null) {
      throw new StandardException("Not implemented: WINDOW/ORDER BY");
    }
  }

  /**
   * Fill this node with a deep copy of the given node.
   */
  public void copyFrom(QueryTreeNode node) throws StandardException {
    super.copyFrom(node);

    WindowDefinitionNode other = (WindowDefinitionNode)node;
    this.inlined = other.inlined;
    this.orderByList = (OrderByList)getNodeFactory().copyNode(other.orderByList,
                                                              getParserContext());
  }

  /**
   * java.lang.Object override.
   * @see QueryTreeNode#toString
   */
  public String toString() {
    return ("name: " + getName() + "\n" +
            "inlined: " + inlined + "\n" +
            "()\n");
  }

  /**
   * QueryTreeNode override. Prints the sub-nodes of this object.
   * @see QueryTreeNode#printSubNodes
   *
   * @param depth     The depth of this node in the tree
   */

  public void printSubNodes(int depth) {
    super.printSubNodes(depth);

    if (orderByList != null) {
      printLabel(depth, "orderByList: ");
      orderByList.treePrint(depth + 1);
    }
  }

  /**
   * Used to merge equivalent window definitions.
   *
   * @param wl list of window definitions
   * @return an existing window definition from wl, if 'this' is equivalent
   * to a window in wl.
   */
  public WindowDefinitionNode findEquivalentWindow(WindowList wl) {
    for (int i = 0; i < wl.size(); i++) {
      WindowDefinitionNode old = wl.get(i);
      if (isEquivalent(old)) {
        return old;
      }
    }
    return null;
  }

  /**
   * @return true if the window specifications are equal; no need to create
   * more than one window then.
   */
  private boolean isEquivalent(WindowDefinitionNode other) {
    if (orderByList == null && other.getOrderByList() == null) {
      return true;
    }

    assert false : "FIXME: ordering in windows not implemented yet";
    return false;
  }

  /**
   * @return the order by list of this window definition if any, else null.
   */
  public OrderByList getOrderByList() {
    return orderByList;
  }

}
