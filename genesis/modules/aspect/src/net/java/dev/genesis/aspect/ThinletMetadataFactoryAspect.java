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

import net.java.dev.genesis.ui.metadata.ViewMetadata;
import net.java.dev.genesis.ui.thinlet.BaseThinlet;
import net.java.dev.genesis.ui.thinlet.metadata.ThinletMetadata;
import net.java.dev.genesis.ui.thinlet.metadata.ThinletMetadataFactory;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareMixin;

@Aspect
@Deprecated
public class ThinletMetadataFactoryAspect {
   @DeclareMixin("net.java.dev.genesis.ui.thinlet.BaseThinlet+")
   @Deprecated
   public static ThinletMetadataFactory createThinletMetadataFactory(
         BaseThinlet thinlet) {
      return new AspectThinletMetadataFactory();
   }

   @Deprecated
   public static class AspectThinletMetadataFactory
         extends ViewMetadataFactoryAspect.AspectViewMetadataFactory
         implements ThinletMetadataFactory {
      public ThinletMetadata getThinletMetadata(Class thinletClass) {
         return (ThinletMetadata)getViewMetadata(thinletClass);
      }

      protected ViewMetadata createViewMetadata(Class viewClass) {
         return new ThinletMetadata(viewClass);
      }
   }
}
