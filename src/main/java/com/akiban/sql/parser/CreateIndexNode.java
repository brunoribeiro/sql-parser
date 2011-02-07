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

   Derby - Class org.apache.derby.impl.sql.compile.CreateIndexNode

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

import java.util.List;
import java.util.Properties;

/**
 * A CreateIndexNode is the root of a QueryTree that represents a CREATE INDEX
 * statement.
 *
 */

public class CreateIndexNode extends DDLStatementNode
{
  boolean unique;
  String indexType;
  TableName indexName;
  TableName tableName;
  List<String> columnNameList;
  Properties properties;

  /**
   * Initializer for a CreateIndexNode
   *
   * @param unique True means it's a unique index
   * @param indexType The type of index
   * @param indexName The name of the index
   * @param tableName The name of the table the index will be on
   * @param columnNameList A list of column names, in the order they
   *        appear in the index.
   * @param properties The optional properties list associated with the index.
   *
   * @exception StandardException Thrown on error
   */
  public void init(Object unique,
                   Object indexType,
                   Object indexName,
                   Object tableName,
                   Object columnNameList,
                   Object properties) 
      throws StandardException {
    initAndCheck(indexName);
    this.unique = ((Boolean)unique).booleanValue();
    this.indexType = (String)indexType;
    this.indexName = (TableName)indexName;
    this.tableName = (TableName)tableName;
    this.columnNameList = (List<String>)columnNameList;
    this.properties = (Properties)properties;
  }

  /**
   * Convert this object to a String.  See comments in QueryTreeNode.java
   * for how this should be done for tree printing.
   *
   * @return This object as a String
   */

  public String toString() {
    return super.toString() +
      "unique: " + unique + "\n" +
      "indexType: " + indexType + "\n" +
      "indexName: " + indexName + "\n" +
      "tableName: " + tableName + "\n" +
      "properties: " + properties + "\n";
  }

  public String statementToString() {
    return "CREATE INDEX";
  }

  public boolean getUniqueness() { 
    return unique; 
  }
  public String getIndexType() { 
    return indexType;
  }
  public TableName getIndexName() { 
    return indexName; 
  }
  public Properties getProperties() { 
    return properties; 
  }
  public TableName getIndexTableName() {
    return tableName; 
  }

}
