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

import com.akiban.server.api.dml.scan.NewRow;
import com.akiban.server.api.dml.scan.NiceRow;
import com.akiban.server.itests.ApiTestBase;
import com.akiban.server.service.ServiceManagerImpl;
import com.akiban.server.service.config.Property;

import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import java.util.Collection;
import java.util.Collections;

public class PostgresServerIT extends ApiTestBase
{
  @Override
  protected Collection<Property> startupConfigProperties() {
    // ServiceManagerImpl.CUSTOM_LOAD_SERVICE
    return Collections.singleton(new Property(Property.parseKey("akserver.services.customload"),
                                              PostgresServerManager.class.getName()));
  }

  @Before
  public void loadSchema() throws Exception {
    int id = createTable("user", "customers",
                         "cid int NOT NULL auto_increment",
                         "PRIMARY KEY(cid)",
                         "name varchar(32) NOT NULL",
                         "KEY(name)");
    NewRow row = new NiceRow(id);
    Object[] data = new Object[] { "1", "Smith" };
    for (int i = 0; i < data.length; i++)
      row.put(i, data[i]);
    dml().writeRow(session(), row);
  }

  @Test
  public void foo() throws Exception {
    Class.forName("org.postgresql.Driver");
    Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:15432/user",
                                                  "user", "user");
    String sql = "SELECT * FROM customers";
    System.out.println(sql);
    PreparedStatement stmt = conn.prepareStatement(sql);
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
  }
}
