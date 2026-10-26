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

import java.util.Map;

import net.java.dev.genesis.script.ScriptRegistry;
import net.java.dev.genesis.ui.metadata.DefaultFormMetadataFactory;
import net.java.dev.genesis.ui.metadata.FormMetadataFactory;
import net.java.dev.genesis.util.GenesisUtils;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareMixin;

@Aspect
public class FormMetadataFactoryAspect {
   @DeclareMixin("@net.java.dev.genesis.annotation.Form *")
   public static FormMetadataFactory createFormMetadataFactory(Object form) {
      String scriptFactoryName = System.getProperty(
            "genesis.aspect.scriptFactory");
      if (scriptFactoryName != null) {
         ScriptRegistry.getInstance().setScriptFactoryName(scriptFactoryName);
      }

      String properties = System.getProperty(
            "genesis.aspect.scriptFactoryProperties");
      if (properties != null) {
         Map scriptProperties = GenesisUtils.getAttributesMap(properties, ",", "=");
         ScriptRegistry.getInstance().setScriptFactoryProperties(scriptProperties);
      }
      ScriptRegistry.getInstance().initialize();
      return new DefaultFormMetadataFactory();
   }
}
