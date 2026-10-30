/*
 * The Genesis Project
 * Copyright (C) 2004-2008  Summa Technologies do Brasil Ltda.
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
package net.java.dev.genesis.ejb;

import java.lang.reflect.InvocationTargetException;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.EJBException;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import javax.naming.InitialContext;

import net.java.dev.genesis.command.TransactionalInjector;
import net.java.dev.genesis.command.CommandExecutionGuard;
import net.java.dev.genesis.reflection.ReflectionInvoker;

@Stateless(name = "CommandExecutor")
public class CommandExecutorEJB implements CommandExecutor, CommandExecutorLocal {
   private TransactionalInjector injector;
   @Resource
   private SessionContext sessionContext;
   
   @PostConstruct
   public void initialize() {
      try {
         final InitialContext ctx = new InitialContext();
         final String className = (String)ctx.lookup(
               "java:comp/env/TransactionalInjector"); // NOI18N
         injector = (TransactionalInjector)Class.forName(className, true, 
               Thread.currentThread().getContextClassLoader())
               .getDeclaredConstructor().newInstance();
      } catch (final Exception e) {
         throw new EJBException(e);
      }

      injector.init(sessionContext);
   }

   @Override
   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   public Object executeTransaction(Object o, String methodName,
         String[] classNames, Object[] args) throws ClassNotFoundException,
                              IllegalAccessException, NoSuchMethodException,
                              InvocationTargetException {
       return execute(o, methodName, classNames, args, true);
   }

   @Override
   @TransactionAttribute(TransactionAttributeType.SUPPORTS)
   public Object executeQuery(Object o, String methodName,
         String[] classNames, Object[] args) throws ClassNotFoundException,
                              IllegalAccessException, NoSuchMethodException,
                              InvocationTargetException {
      return execute(o, methodName, classNames, args, false);
   }
   
   private Object execute(Object o, String methodName, String[] classNames,
         Object[] args, boolean transactional) throws ClassNotFoundException,
                           IllegalAccessException, NoSuchMethodException,
                           InvocationTargetException {
      Exception e = null;

      try {
         injector.beforeInvocation(o, transactional);

         final Object ret;
         CommandExecutionGuard.enter();
         try {
            ret = ReflectionInvoker.getInstance().invoke(o, methodName,
                  classNames, args);
         } finally {
            CommandExecutionGuard.exit();
         }
         
         injector.afterInvocation();

         return ret;
      } catch (final ClassNotFoundException cnfe) {
         e = cnfe;
         throw cnfe;
      } catch (final IllegalAccessException iae) {
         e = iae;
         throw iae;
      } catch (final NoSuchMethodException nsme) {
         e = nsme;
         throw nsme;
      } catch (final InvocationTargetException ite) {
         e = ite;
         throw ite;
      } catch (final Exception ex) {
         e = ex;
         throw new InvocationTargetException(ex);
      } finally {
         if (e != null) {
            injector.onException(e);
         }

         try {
            injector.onFinally();
         } catch (final Exception ex) {
            throw new InvocationTargetException(combine(ex, e));
         }
      }
   }

   private Throwable combine(final Throwable t, final Exception e) {
      Throwable last = t;

      while (last.getCause() != null) {
         last = last.getCause();
      }

      last.initCause(e);

      return t;
   }
}