/*
 * The Genesis Project
 * Copyright (C) 2004  Summa Technologies do Brasil Ltda.
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
package net.java.dev.genesis.aspect;

import java.awt.Component;
import java.awt.Cursor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class WaitCursorAspect {
   
   @Around("execution(* thinlet.Thinlet.invokeImpl(..))")
   public Object waitCursorExecution(final ProceedingJoinPoint joinPoint)
         throws Throwable {
      final Component c = (Component)joinPoint.getTarget();
      final Cursor oldCursor = c.getCursor();

      Object ret = null;

      try {
         c.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         ret = joinPoint.proceed();
      } finally {
         c.setCursor(oldCursor);
      }

      return ret;
   }
}