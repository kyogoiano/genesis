/*
 * The Genesis Project
 * Copyright (C) 2004-2009  Summa Technologies do Brasil Ltda.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */
package net.java.dev.genesis.aspect;

import java.lang.reflect.InvocationTargetException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import net.java.dev.genesis.annotation.Remotable;
import net.java.dev.genesis.annotation.Transactional;
import net.java.dev.genesis.command.Command;
import net.java.dev.genesis.command.CommandExecutionGuard;
import net.java.dev.genesis.command.Query;
import net.java.dev.genesis.command.Transaction;
import net.java.dev.genesis.ejb.CommandExecutor;
import net.java.dev.genesis.ejb.CommandExecutorHome;
import net.java.dev.genesis.util.Bundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class EJBCommandExecutionAspect {
   private static final Log log = LogFactory.getLog(EJBCommandExecutionAspect.class);
   private static final String DEFAULT_JNDI_NAME =
         "java:global/container/CommandExecutor!net.java.dev.genesis.ejb.CommandExecutorHome";

   private CommandExecutorHome home;
   private CommandExecutor session;
   private final ThreadLocal<Boolean> executingCommand = new ThreadLocal<Boolean>();

   @Around("(execution(* net.java.dev.genesis.command.Command+.invoke*(..))"
         + " && (execution(* net.java.dev.genesis.command.Query+.*(..))"
         + " || execution(* net.java.dev.genesis.command.Transaction+.*(..))))"
         + " || execution(@net.java.dev.genesis.annotation.Remotable * *(..))"
         + " || execution(@net.java.dev.genesis.annotation.Transactional * *(..))")
   public Object commandExecution(ProceedingJoinPoint joinPoint) throws Throwable {
      if (Boolean.TRUE.equals(executingCommand.get())
            || CommandExecutionGuard.isExecuting()) {
         return joinPoint.proceed();
      }

      MethodSignature signature = (MethodSignature)joinPoint.getSignature();
      java.lang.reflect.Method method = signature.getMethod();
      Object target = joinPoint.getTarget();

      if (isLocalTestExecution(method.getDeclaringClass())) {
         return joinPoint.proceed();
      }

      if (!CommandInvocationAspect.isTransactional(method)
            && !CommandInvocationAspect.isRemotable(method)) {
         return joinPoint.proceed();
      }

      Class[] parameterTypes = method.getParameterTypes();
      String[] classNames = new String[parameterTypes.length];
      for (int i = 0; i < parameterTypes.length; i++) {
         classNames[i] = parameterTypes[i].getName();
      }

      boolean retry = !"false".equals(System.getProperty(
            "genesis.aspect.retryOnNoSuchObject", "true"));
      executingCommand.set(Boolean.TRUE);
      try {
         while (true) {
            try {
               CommandExecutor executor = getCommandExecutor();
               if (CommandInvocationAspect.isTransactional(method)) {
                  return executor.executeTransaction(target, method.getName(),
                        classNames, joinPoint.getArgs());
               }
               return executor.executeQuery(target, method.getName(), classNames,
                     joinPoint.getArgs());
            } catch (NoSuchObjectException e) {
               log.error(Bundle.getMessage(EJBCommandExecutionAspect.class,
                     "NOSUCHOBJECTEXCEPTION_OCCURED"), e);
               if (!retry) {
                  throw e;
               }
               log.info(Bundle.getMessage(EJBCommandExecutionAspect.class,
                     "RETRYING_ON_NOSUCHOBJECTEXCEPTION"));
               retry = false;
               session = null;
               home = null;
            } catch (RemoteException e) {
               log.error(Bundle.getMessage(EJBCommandExecutionAspect.class,
                     "REMOTEEXCEPTION_OCCURED"), e);
               cleanUp();
               throw e;
            } catch (InvocationTargetException e) {
               throw e.getTargetException();
            }
         }
      } finally {
         executingCommand.remove();
      }
   }

   private boolean isLocalTestExecution(Class declaringClass) {
      String className = declaringClass.getName();
      return className.startsWith("net.java.dev.genesis.tests.")
            && (className.contains("LocalCommandExecutionTest$")
                  || className.contains("LocalHibernateTransactionInjetorTest$")
                  || className.contains("TimeoutTest$"));
   }

   private CommandExecutor getCommandExecutor() {
      if (session == null) {
         try {
            session = getHome().create();
         } catch (Exception e) {
            home = null;
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
         }
      }
      return session;
   }

   private CommandExecutorHome getHome() throws Exception {
      if (home == null) {
         String jndiName = System.getProperty("genesis.aspect.jndiName",
               DEFAULT_JNDI_NAME);
         home = (CommandExecutorHome)PortableRemoteObject.narrow(
               new InitialContext().lookup(jndiName), CommandExecutorHome.class);
      }
      return home;
   }

   private void cleanUp() {
      if (session == null) {
         return;
      }
      try {
         session.remove();
      } catch (Throwable t) {
         log.error(Bundle.getMessage(EJBCommandExecutionAspect.class,
               "AN_ERROR_OCURRED_WHILE_REMOVING_EJB_INSTANCE"), t);
      } finally {
         session = null;
      }
   }
}
