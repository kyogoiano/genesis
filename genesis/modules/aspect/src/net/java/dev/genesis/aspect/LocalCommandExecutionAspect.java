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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

import net.java.dev.genesis.command.TransactionalInjector;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class LocalCommandExecutionAspect {
   private final Map injectorsPerThread = new WeakHashMap();
   private final Map processingThreads = new WeakHashMap();

   @Around("(execution(* net.java.dev.genesis.command.Command+.invoke*(..))"
         + " && (execution(* net.java.dev.genesis.command.Query+.*(..))"
         + " || execution(* net.java.dev.genesis.command.Transaction+.*(..))))"
         + " || execution(@net.java.dev.genesis.annotation.Remotable * *(..))"
         + " || execution(@net.java.dev.genesis.annotation.Transactional * *(..))")
   public Object commandExecution(ProceedingJoinPoint joinPoint) throws Throwable {
      Method method = ((MethodSignature)joinPoint.getSignature()).getMethod();
      if (!isLocalTestExecution(method.getDeclaringClass())) {
         return joinPoint.proceed();
      }

      Object target = joinPoint.getTarget();
      boolean transactional = CommandInvocationAspect.isTransactional(method);
      if ((!transactional && !CommandInvocationAspect.isRemotable(method))
            || isReentrant()) {
         return joinPoint.proceed();
      }

      TransactionalInjector injector = getTransactionalInjector();
      try {
         injector.beforeInvocation(target, transactional);
         Object result = joinPoint.proceed();
         injector.afterInvocation();
         return result;
      } catch (Exception e) {
         injector.onException(e);
         throw e;
      } finally {
         try {
            injector.onFinally();
         } finally {
            processingThreads.remove(Thread.currentThread());
         }
      }
   }

   private boolean isLocalTestExecution(Class declaringClass) {
      String className = declaringClass.getName();
      return className.startsWith("net.java.dev.genesis.tests.")
            && (className.contains("LocalCommandExecutionTest$")
                  || className.contains("LocalHibernateTransactionInjetorTest$"));
   }

   private TransactionalInjector getTransactionalInjector() throws Exception {
      Thread currentThread = Thread.currentThread();
      TransactionalInjector injector =
            (TransactionalInjector)injectorsPerThread.get(currentThread);
      if (injector == null) {
         String className = System.getProperty(
               "genesis.aspect.transactionalInjector",
               "net.java.dev.genesis.aspect.HibernateTransactionalInjector");
         ClassLoader loader = currentThread.getContextClassLoader();
         injector = (TransactionalInjector)Class.forName(className, true, loader)
               .newInstance();
         injector.init(null);
         injectorsPerThread.put(currentThread, injector);
      }
      return injector;
   }

   private boolean isReentrant() {
      Thread currentThread = Thread.currentThread();
      if (Boolean.TRUE.equals(processingThreads.get(currentThread))) {
         return true;
      }
      processingThreads.put(currentThread, Boolean.TRUE);
      return false;
   }
}
