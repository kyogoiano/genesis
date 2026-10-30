/*
 * The Genesis Project
 * Copyright (C) 2005-2009  Summa Technologies do Brasil Ltda.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */
package net.java.dev.genesis.aspect;

import net.java.dev.genesis.ui.metadata.DefaultViewMetadataFactory;
import net.java.dev.genesis.ui.metadata.ViewMetadataFactory;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareMixin;

@Aspect
public class ViewMetadataFactoryAspect {
   @DeclareMixin("@net.java.dev.genesis.annotation.ViewHandler *")
   public static ViewMetadataFactory createViewMetadataFactory(Object view) {
      return new AspectViewMetadataFactory();
   }

   public static class AspectViewMetadataFactory
         extends DefaultViewMetadataFactory {
      public AspectViewMetadataFactory() {
         setSkipSystemClasses(!"false".equals(System.getProperty(
               "genesis.aspect.skipSystemClasses", "true")));
      }
   }
}
