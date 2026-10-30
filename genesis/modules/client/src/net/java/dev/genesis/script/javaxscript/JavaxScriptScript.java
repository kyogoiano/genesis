/*
 * The Genesis Project
 * Copyright (C) 2006-2024  Summa Technologies do Brasil Ltda.
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
package net.java.dev.genesis.script.javaxscript;

import net.java.dev.genesis.script.Script;
import net.java.dev.genesis.script.ScriptContext;
import net.java.dev.genesis.script.ScriptExpression;
import net.java.dev.genesis.script.javaxscript.bridge.JavaxScriptBridge;
import net.java.dev.genesis.script.javaxscript.bridge.ScriptEngine;
import net.java.dev.genesis.script.javaxscript.bridge.ScriptEngineManager;

/**
 * {@link Script} implementation backed by a JSR-223 {@link javax.script.ScriptEngine}.
 *
 * <p>When the requested language is {@code "javascript"} the engine is resolved
 * using the following priority:</p>
 * <ol>
 *   <li>{@code "javascript"} — accepted by Rhino's JSR-223 adapter
 *       ({@code org.mozilla:rhino-engine}).</li>
 *   <li>{@code "rhino"} — alternative name registered by the same adapter.</li>
 * </ol>
 * <p>Nashorn (JDK 8-11 built-in) was removed in JDK 15 and is therefore
 * never used.</p>
 *
 * <p>A fresh engine is obtained per {@link ScriptContext} via
 * {@link #newContext(Object)} so that each form binding has its own isolated
 * variable scope.</p>
 */
public class JavaxScriptScript implements Script {

   private final ScriptEngineManager manager;
   /** Engine kept only for {@link #compile(String)}; not shared across contexts. */
   private final ScriptEngine compilationEngine;
   private final String lang;

   protected JavaxScriptScript(String lang) {
      this.lang = lang;
      this.manager = JavaxScriptBridge.getInstance().createScriptEngineManager();
      this.compilationEngine = resolveEngine(lang);
   }

   private ScriptEngine resolveEngine(String language) {
      ScriptEngine resolved = manager.getEngineByName(language);
      if (resolved == null && "javascript".equalsIgnoreCase(language)) { // NOI18N
         // Nashorn was removed in JDK 15; Rhino's JSR-223 adapter registers
         // under "rhino" (and "javascript" on newer Rhino versions as well).
         resolved = manager.getEngineByName("rhino"); // NOI18N
      }
      if (resolved == null) {
         throw new IllegalStateException(
               "No JSR-223 ScriptEngine found for language: " + language); // NOI18N
      }
      return resolved;
   }

   public ScriptContext newContext(Object root) {
      // Each context needs its own engine so that variable bindings are isolated.
      return new JavaxScriptScriptContext(resolveEngine(lang), root);
   }

   public ScriptExpression compile(String expression) {
      return new JavaxScriptExpression(expression, compilationEngine);
   }
}
