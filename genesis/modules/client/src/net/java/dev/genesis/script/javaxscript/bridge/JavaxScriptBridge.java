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
package net.java.dev.genesis.script.javaxscript.bridge;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngineManager;

/**
 * Thin façade over the standard {@code javax.script} API (JSR-223).
 *
 * <p>The original implementation loaded {@code javax.script} classes via
 * reflection so the code could compile against JDK 1.4.  Genesis now targets
 * JDK 11 and above, so the proxy layer is no longer needed.  This class is
 * kept as a convenience singleton so call-sites in the rest of the
 * {@code javaxscript} package do not need to change.</p>
 */
public final class JavaxScriptBridge {

   private static final JavaxScriptBridge INSTANCE = new JavaxScriptBridge();

   private JavaxScriptBridge() {
   }

   public static JavaxScriptBridge getInstance() {
      return INSTANCE;
   }

   /** Always {@code true} on JDK 6+. */
   public boolean supportsJavaxScript() {
      return true;
   }

   public int getEngineScope() {
      return javax.script.ScriptContext.ENGINE_SCOPE;
   }

   /**
    * Evaluates a pre-compiled script against the supplied real
    * {@link javax.script.ScriptContext}.
    */
   public Object eval(Object realCompiledScript, Object realCtx) throws Exception {
      CompiledScript compiled = (CompiledScript) realCompiledScript;
      javax.script.ScriptContext ctx = (javax.script.ScriptContext) realCtx;
      return compiled.eval(ctx);
   }

   /**
    * Creates a new {@link ScriptEngineManager} wrapper backed by
    * {@code javax.script.ScriptEngineManager}.
    */
   public net.java.dev.genesis.script.javaxscript.bridge.ScriptEngineManager
         createScriptEngineManager() {
      return new DirectScriptEngineManager(new ScriptEngineManager());
   }

   // -----------------------------------------------------------------------
   // Internal adapters – package-private to keep the public API minimal
   // -----------------------------------------------------------------------

   static final class DirectScriptEngineManager
         implements net.java.dev.genesis.script.javaxscript.bridge.ScriptEngineManager {

      private final javax.script.ScriptEngineManager delegate;

      DirectScriptEngineManager(javax.script.ScriptEngineManager delegate) {
         this.delegate = delegate;
      }

      public net.java.dev.genesis.script.javaxscript.bridge.ScriptEngine
            getEngineByName(String shortName) {
         javax.script.ScriptEngine engine = delegate.getEngineByName(shortName);
         if (engine == null) {
            return null;
         }
         return new DirectScriptEngine(engine);
      }
   }

   static final class DirectScriptEngine
         implements net.java.dev.genesis.script.javaxscript.bridge.ScriptEngine {

      private final javax.script.ScriptEngine delegate;

      DirectScriptEngine(javax.script.ScriptEngine delegate) {
         this.delegate = delegate;
      }

      public Object eval(String script) throws Exception {
         return delegate.eval(script);
      }

      public net.java.dev.genesis.script.javaxscript.bridge.ScriptContext getContext() {
         return new DirectScriptContext(delegate.getContext(), delegate);
      }

      public Object compile(String script) throws Exception {
         if (delegate instanceof Compilable) {
            try {
               return ((Compilable) delegate).compile(script);
            } catch (Error | Exception e) {
               // Some engines (e.g. BeanShell) advertise Compilable but throw
               // at runtime — treat as non-compilable and fall back to eval.
               return null;
            }
         }
         return null; // engine does not support compilation; fall back to eval
      }
   }

   static final class DirectScriptContext
         implements net.java.dev.genesis.script.javaxscript.bridge.ScriptContext {

      private final javax.script.ScriptContext delegate;
      /** Kept so that setAttribute/eval target the same engine scope. */
      private final javax.script.ScriptEngine engine;

      DirectScriptContext(javax.script.ScriptContext delegate,
            javax.script.ScriptEngine engine) {
         this.delegate = delegate;
         this.engine = engine;
      }

      public void setAttribute(String name, Object value, int scope) {
         delegate.setAttribute(name, value, scope);
      }

      public java.util.Map getBindings(int scope) {
         return delegate.getBindings(scope);
      }

      public Object getAttribute(String name) {
         return delegate.getAttribute(name);
      }

      public Object removeAttribute(String name, int scope) {
         return delegate.removeAttribute(name, scope);
      }

      /** Returns the raw {@link javax.script.ScriptContext} for compiled-script eval. */
      public Object getRealContext() {
         return delegate;
      }
   }
}
