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
import java.lang.reflect.InvocationTargetException;
import javax.naming.InitialContext;

import net.java.dev.genesis.ejb.CommandExecutorLocal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class LocalEJBCommandExecutionAspect {
   private static final Log log =
         LogFactory.getLog(LocalEJBCommandExecutionAspect.class);

   private final ThreadLocal threadLocal = new InheritableThreadLocal();
   private CommandExecutorLocal session;

   @Pointcut("(execution(* net.java.dev.genesis.command.Command+.invoke*(..))"
         + " && (execution(* net.java.dev.genesis.command.Query+.*(..))"
         + " || execution(* net.java.dev.genesis.command.Transaction+.*(..))))"
         + " || execution(@net.java.dev.genesis.annotation.Remotable * *(..))"
         + " || execution(@net.java.dev.genesis.annotation.Transactional * *(..))")
   public void commandExecution() {
   }

   @Around("commandExecution()")
   public Object commandExecutionAdvice(ProceedingJoinPoint joinPoint)
         throws Throwable {
      if (!localEjbEnabled()) {
         return joinPoint.proceed();
      }

      Method method = ((MethodSignature)joinPoint.getSignature()).getMethod();
      Method nestedMethod = (Method)threadLocal.get();
      if (nestedMethod != null && nestedMethod.equals(method)) {
         try {
            return joinPoint.proceed();
         } finally {
            threadLocal.remove();
         }
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

      try {
         CommandExecutorLocal executor = getCommandExecutor();
         threadLocal.set(method);
         if (CommandInvocationAspect.isTransactional(method)) {
            return executor.executeTransaction(joinPoint.getTarget(),
                  method.getName(), classNames, joinPoint.getArgs());
         }
         return executor.executeQuery(joinPoint.getTarget(), method.getName(),
               classNames, joinPoint.getArgs());
      } catch (InvocationTargetException e) {
         throw e.getTargetException();
      } finally {
         threadLocal.remove();
      }
   }

   public static boolean localEjbEnabled() {
      return Boolean.getBoolean("genesis.aspect.localEjb");
   }

   private CommandExecutorLocal getCommandExecutor() {
      if (session == null) {
         try {
            String jndiName = System.getProperty(
                  "genesis.aspect.localEjbJndiName");
            if (jndiName == null) {
               throw new IllegalStateException(
                     "Set genesis.aspect.localEjbJndiName before enabling local EJB execution");
            }
            session = (CommandExecutorLocal)new InitialContext()
                  .lookup(jndiName);
         } catch (Exception e) {
            session = null;
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
         }
      }
      return session;
   }
}
