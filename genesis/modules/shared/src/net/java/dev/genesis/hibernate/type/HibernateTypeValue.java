/*
 * The Genesis Project
 * Copyright (C) 2004  Summa Technologies do Brasil Ltda.
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
package net.java.dev.genesis.hibernate.type;

import org.hibernate.MappingException;
import org.hibernate.type.CustomType;
import org.hibernate.type.Type;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

import java.util.Properties;

public class HibernateTypeValue {

   private Type type;
   private Object value;

   public HibernateTypeValue(Object value, Type type) {
      this.type = type;
      this.value = value;
   }

   public HibernateTypeValue(Object value, Class typeValue, Properties parameters)
         throws MappingException {
      this(value, createCustomType(typeValue, parameters));
   }

   private static Type createCustomType(Class typeValue, Properties parameters)
         throws MappingException {
      try {
         UserType userType = (UserType) typeValue.newInstance();
         if (userType instanceof ParameterizedType) {
            ((ParameterizedType) userType).setParameterValues(parameters);
         }
         return new CustomType(userType);
      } catch (InstantiationException e) {
         throw new MappingException("Could not instantiate user type " + typeValue, e);
      } catch (IllegalAccessException e) {
         throw new MappingException("Could not access user type " + typeValue, e);
      } catch (ClassCastException e) {
         throw new MappingException("Type does not implement UserType: " + typeValue, e);
      }
   }

   public Type getType() {
      return this.type;
   }

   public Object getValue() {
      return this.value;
   }
}