/*
 * The Genesis Project
 * Copyright (C) 2008 Summa Technologies do Brasil Ltda.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.java.dev.genesis.ui.swing.components.table;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import net.java.dev.genesis.GenesisTestCase;

public class DefaultJTableIndexResolverTest extends GenesisTestCase {
   private JComponent unsortableTable;
   private MockSortableTable sortableTable;
   private JTableIndexResolver defaultIndexResolver;

   public DefaultJTableIndexResolverTest() {
      super("DefaultJTableIndexResolver Unit Test");
   }

   protected void setUp() throws Exception {
      unsortableTable = new JComponent() {
      };
      sortableTable = new MockSortableTable();

      defaultIndexResolver = new DefaultJTableIndexResolver();
   }

   public void testTableIndexResolver() throws Exception {
      assertFalse(defaultIndexResolver.needsConversion(unsortableTable));
      assertTrue(defaultIndexResolver.needsConversion(sortableTable));

      try {
         assertEquals(0, defaultIndexResolver.convertRowIndexToView(
               unsortableTable, 0));
         fail("Shouldn't be possible to convert a index when needsConversion is false");
      } catch (NullPointerException npe) {
      }

      try {
         assertEquals(0, defaultIndexResolver.convertRowIndexToModel(
               unsortableTable, 0));
         fail("Shouldn't be possible to convert a index when needsConversion is false");
      } catch (NullPointerException npe) {
      }

      Map map = new HashMap();
      map.put(1, 3);
      map.put(2, 2);
      map.put(3, 4);
      map.put(4, 0);
      map.put(5, 1);

      for (int i = 1; i <= 5; i++) {
         int modelIndex = (Integer) map.get(i);
         int viewIndex = modelIndex;

         validateIndexes(modelIndex, viewIndex);

         assertEquals(defaultIndexResolver.convertRowIndexToModel(sortableTable,
               viewIndex), defaultIndexResolver.convertRowIndexToView(
               sortableTable, modelIndex));
      }

      sortableTable.sort();

      for (int i = 1; i <= 5; i++) {
         int modelIndex = (Integer) map.get(i);
         int viewIndex = i - 1;

         validateIndexes(modelIndex, viewIndex);
      }
   }

   private void validateIndexes(int modelIndex, int viewIndex) throws Exception {
      assertEquals(modelIndex, defaultIndexResolver.convertRowIndexToModel(
            sortableTable, viewIndex));
      assertEquals(viewIndex, defaultIndexResolver.convertRowIndexToView(
            sortableTable, modelIndex));
   }
}
