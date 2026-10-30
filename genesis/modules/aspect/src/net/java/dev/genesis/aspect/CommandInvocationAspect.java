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
import java.io.Serializable;

import net.java.dev.genesis.annotation.Remotable;
import net.java.dev.genesis.annotation.Transactional;
import net.java.dev.genesis.command.Query;
import net.java.dev.genesis.command.Transaction;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareMixin;

@Aspect
public class CommandInvocationAspect {
   @DeclareMixin("net.java.dev.genesis.command.Command+")
   public static CommandResolver createCommandResolver() {
      return new CommandResolverImpl();
   }

   public static boolean isRemotable(Method method) {
      return !"false".equals(System.getProperty("genesis.aspect.useFastMode",
            "true"))
            || method.getAnnotation(Remotable.class) != null
            || Query.class.isAssignableFrom(method.getDeclaringClass())
            || isTransactional(method);
   }

   public static boolean isTransactional(Method method) {
      return method.getAnnotation(Transactional.class) != null
            || Transaction.class.isAssignableFrom(method.getDeclaringClass());
   }

   public static class CommandResolverImpl implements CommandResolver,
         Serializable {
      private static final long serialVersionUID = 1L;

      public boolean isRemotable(Method method) {
         return CommandInvocationAspect.isRemotable(method);
      }

      public boolean isTransactional(Method method) {
         return CommandInvocationAspect.isTransactional(method);
      }
   }
}
