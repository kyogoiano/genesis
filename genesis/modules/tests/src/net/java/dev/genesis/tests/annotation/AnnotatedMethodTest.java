/*
 * The Genesis Project
 * Copyright (C) 2006  Summa Technologies do Brasil Ltda.
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
package net.java.dev.genesis.tests.annotation;

import net.java.dev.genesis.annotation.Remotable;
import net.java.dev.genesis.tests.TestCase;

import java.lang.annotation.Annotation;

public class AnnotatedMethodTest extends TestCase {
   public void testNotOverridedMethod() throws Exception {
      ParentClass parent = new ParentClass();

      Annotation an = parent
               .getClass().getMethod("method", new Class[0]).getAnnotation(Remotable.class);


       assertNotNull(an);
      assertEquals(an.annotationType(), Remotable.class);

      parent = new ChildClass();

      an = parent.getClass()
            .getMethod("method", new Class[0]).getAnnotation(Remotable.class);

      assertNotNull(an);
      assertEquals(an.annotationType(), Remotable.class);

      final ChildClass child = new ChildClass();

      an = child.getClass()
            .getMethod("method", new Class[0]).getAnnotation(Remotable.class);

      assertNotNull(an);
      assertEquals(an.annotationType(), Remotable.class);
   }

   public void testOverridedMethod() throws Exception {
      ParentClass parent = new ParentClass();

      Annotation an = parent
            .getClass().getMethod("override", new Class[0]).getAnnotation(Remotable.class);

      assertNotNull(an);
      assertEquals(an.annotationType(), Remotable.class);

      parent = new ChildClass();

      an = parent.getClass()
            .getMethod("override", new Class[0]).getAnnotation(Remotable.class);

      assertNull(an);

      final ChildClass child = new ChildClass();

      an = child.getClass()
            .getMethod("override", new Class[0]).getAnnotation(Remotable.class);

      assertNull(an);
   }

   public static class ParentClass {
      @Remotable
      public void override() {
      }

      @Remotable
      public void method() {
      }
   }

   public static class ChildClass extends ParentClass {
      public void override() {
      }
   }
}