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

package com.akiban.sql.pg;
import com.akiban.sql.RegexFilenameFilter;
import static com.akiban.sql.TestBase.*;

import com.akiban.server.api.dml.scan.NewRow;
import com.akiban.server.api.dml.scan.NiceRow;
import com.akiban.server.itests.ApiTestBase;
import com.akiban.server.service.ServiceManagerImpl;
import com.akiban.server.service.config.Property;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PostgresServerIT extends ApiTestBase
{
  public static final File RESOURCE_DIR = 
    new File("src/test/resources/"
             + PostgresServerIT.class.getPackage().getName().replace('.', '/'));

  public static final String SCHEMA_NAME = "user";
  public static final String DRIVER_NAME = "org.postgresql.Driver";
  public static final String CONNECTION_URL = "jdbc:postgresql://localhost:15432/user";
  public static final String USER_NAME = "user";
  public static final String USER_PASSWORD = "user";

  @Override
  protected Collection<Property> startupConfigProperties() {
    // ServiceManagerImpl.CUSTOM_LOAD_SERVICE
    return Collections.singleton(new Property(Property.parseKey("akserver.services.customload"),
                                              PostgresServerManager.class.getName()));
  }

  @Before
  public void loadDatabase() throws Exception {
    loadSchemaFile(new File(RESOURCE_DIR, "schema.ddl"));
    for (File data : RESOURCE_DIR.listFiles(new RegexFilenameFilter(".*\\.dat"))) {
      loadDataFile(data);
    }
  }

  protected void loadSchemaFile(File file) throws Exception {
    Reader rdr = null;
    try {
      rdr = new FileReader(file);
      BufferedReader brdr = new BufferedReader(rdr);
      String tableName = null;
      List<String> tableDefinition = new ArrayList<String>();
      while (true) {
        String line = brdr.readLine();
        if (line == null) break;
        line = line.trim();
        if (line.startsWith("CREATE TABLE "))
          tableName = line.substring(13);
        else if (line.startsWith("("))
          tableDefinition.clear();
        else if (line.startsWith(")"))
          createTable(SCHEMA_NAME, tableName, 
                      tableDefinition.toArray(new String[tableDefinition.size()]));
        else {
          if (line.endsWith(","))
            line = line.substring(0, line.length() - 1);
          tableDefinition.add(line);
        }
      }
    }
    finally {
      if (rdr != null) {
        try {
          rdr.close();
        }
        catch (IOException ex) {
        }
      }
    }
  }

  protected void loadDataFile(File file) throws Exception {
    String tableName = file.getName().replace(".dat", "");
    int tableId = tableId(SCHEMA_NAME, tableName);
    Reader rdr = null;
    try {
      rdr = new FileReader(file);
      BufferedReader brdr = new BufferedReader(rdr);
      while (true) {
        String line = brdr.readLine();
        if (line == null) break;
        String[] cols = line.split("\t");
        NewRow row = new NiceRow(tableId);
        for (int i = 0; i < cols.length; i++)
          row.put(i, cols[i]);
        dml().writeRow(session(), row);
      }
    }
    finally {
      if (rdr != null) {
        try {
          rdr.close();
        }
        catch (IOException ex) {
        }
      }
    }
  }

  protected Connection connection;

  @Before
  public void openConnection() throws Exception {
    Class.forName(DRIVER_NAME);
    connection = DriverManager.getConnection(CONNECTION_URL, USER_NAME, USER_PASSWORD);
  }

  @After
  public void closeConnection() {
    if (connection != null) {
      try {
        connection.close();
      }
      catch (SQLException ex) {
      }
      connection = null;
    }
  }
  
  @Test
  public void foo() throws Exception {
    String sql = "SELECT * FROM customers";
    System.out.println(sql);
    PreparedStatement stmt = connection.prepareStatement(sql);
    ResultSet rs = stmt.executeQuery();
    ResultSetMetaData md = rs.getMetaData();
    for (int i = 1; i <= md.getColumnCount(); i++) {
      if (i > 1) System.out.print("\t");
      System.out.print(md.getColumnName(i));
    }
    System.out.println();
    while (rs.next()) {
      for (int i = 1; i <= md.getColumnCount(); i++) {
        if (i > 1) System.out.print("\t");
        System.out.print(rs.getString(i));
      }
      System.out.println();
    }
    stmt.close();
  }
}
