/*
 * The Genesis Project
 * Copyright (C) 2005-2008  Summa Technologies do Brasil Ltda.
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
package net.java.dev.genesis.script.el;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.beans.FeatureDescriptor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ValueExpression;

import org.apache.taglibs.standard.functions.Functions;

import net.java.dev.genesis.script.PrimitiveFunctions;
import net.java.dev.genesis.script.ScriptContext;
import net.java.dev.genesis.script.ScriptException;
import net.java.dev.genesis.script.ScriptExpression;
import net.java.dev.genesis.script.ScriptFunctionsAdapter;

public class ELScriptContext extends ScriptContext {
   public static final String FORM_NS = "form"; // NOI18N
   public static final String GENESIS_FUNCTIONS_NS = "g"; // NOI18N

   private final Map variables = new HashMap();
   private final Map unmodifiableMap = Collections.unmodifiableMap(variables);
   private final Map functions = new HashMap();
   private final Map<String, ValueExpression> expressions =
         new ConcurrentHashMap<String, ValueExpression>();
   private final ExpressionFactory expressionFactory;
   private final ELContext elContext;

   protected ELScriptContext(Object root, ExpressionFactory expressionFactory) {
      this.expressionFactory = expressionFactory;
      this.elContext = new GenesisELContext();
      variables.put(FORM_NS, root);
      registerFunctions("", Functions.class); // NOI18N
      registerFunctions("", PrimitiveFunctions.class); // NOI18N
      registerFunctions(GENESIS_FUNCTIONS_NS, getFunctions());
   }

   protected Object doEval(ScriptExpression expr) {
      try {
         String expression = expr.getExpressionString();
         ValueExpression valueExpression = expressions.get(expression);
         if (valueExpression == null) {
            valueExpression = expressionFactory.createValueExpression(
                  elContext, expression, Object.class);
            expressions.put(expression, valueExpression);
         }
         return valueExpression.getValue(elContext);
      } catch (ELException e) {
         throw new ScriptException(e.getMessage(), e);
      }
   }

   protected ScriptExpression newScriptExpression(String expression) {
      return new ELExpression(expression);
   }

   public void registerFunctions(String prefix, Class functionClass) {
      Method methods[] = functionClass.getMethods();
      for (int i = 0; i < methods.length; i++) {
         Method m = methods[i];
         if (Modifier.isStatic(m.getModifiers())) {
            functions.put(prefix + ':' + m.getName(), m);
         }
      }
      expressions.clear();
   }

   public Method resolveFunction(String prefix, String localName) {
      return (Method)functions.get(prefix + ':' + localName);
   }

   public void declare(String name, Object value) {
      variables.put(name, value);
   }

   public Object lookup(String name) {
      return variables.get(name);
   }

   public void undeclare(String name) {
      variables.remove(name);
   }

   public Map getContextMap() {
      return unmodifiableMap;
   }

   protected Class getFunctions() {
      return ScriptFunctionsAdapter.class;
   }

   private class GenesisELContext extends ELContext {
      private final CompositeELResolver resolver = new CompositeELResolver();
      private final FunctionMapper functionMapper = new FunctionMapper() {
         public Method resolveFunction(String prefix, String localName) {
            return ELScriptContext.this.resolveFunction(prefix, localName);
         }
      };

      private GenesisELContext() {
         putContext(ExpressionFactory.class, expressionFactory);
         resolver.add(new VariableELResolver());
         resolver.add(new MapELResolver());
         resolver.add(new ListELResolver());
         resolver.add(new ArrayELResolver());
         resolver.add(new BeanELResolver());
      }

      public ELResolver getELResolver() {
         return resolver;
      }

      public FunctionMapper getFunctionMapper() {
         return functionMapper;
      }

      public javax.el.VariableMapper getVariableMapper() {
         return null;
      }

      public Object convertToType(Object value, Class<?> targetType) {
         return expressionFactory.coerceToType(value, targetType);
      }
   }

   private class VariableELResolver extends ELResolver {
      public Object getValue(ELContext context, Object base, Object property) {
         if (base == null && variables.containsKey(property)) {
            context.setPropertyResolved(base, property);
            return variables.get(property);
         }
         return null;
      }

      public Class getType(ELContext context, Object base, Object property) {
         if (base == null && variables.containsKey(property)) {
            context.setPropertyResolved(base, property);
            Object value = variables.get(property);
            return value == null ? Object.class : value.getClass();
         }
         return null;
      }

      public void setValue(ELContext context, Object base, Object property,
            Object value) {
         if (base == null && variables.containsKey(property)) {
            context.setPropertyResolved(base, property);
            variables.put(property, value);
         }
      }

      public boolean isReadOnly(ELContext context, Object base,
            Object property) {
         if (base == null && variables.containsKey(property)) {
            context.setPropertyResolved(base, property);
            return false;
         }
         return false;
      }

      public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context,
            Object base) {
         return null;
      }

      public Class getCommonPropertyType(ELContext context, Object base) {
         return base == null ? String.class : null;
      }
   }
}