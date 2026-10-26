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

import net.java.dev.genesis.ui.controller.DefaultFormControllerFactory;
import net.java.dev.genesis.ui.controller.FormControllerFactory;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareMixin;

@Aspect
public class FormControllerFactoryAspect {
   @DeclareMixin("@net.java.dev.genesis.annotation.Form *")
   public static FormControllerFactory createFormControllerFactory(Object form) {
      AspectFormControllerFactory factory = new AspectFormControllerFactory();
      String times = System.getProperty(
            "genesis.aspect.maximumEvaluationTimes");
      if (times != null) {
         factory.setMaximumEvaluationTimes(Integer.parseInt(times));
      }
      factory.setResetOnDataProviderChange(!"false".equals(
            System.getProperty("genesis.aspect.resetOnDataProviderChange",
                  "true")));
      return factory;
   }

   private static class AspectFormControllerFactory
         extends DefaultFormControllerFactory {
   }
}
