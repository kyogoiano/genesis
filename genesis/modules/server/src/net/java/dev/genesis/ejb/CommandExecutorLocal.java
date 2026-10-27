package net.java.dev.genesis.ejb;

import java.lang.reflect.InvocationTargetException;
import javax.ejb.Local;

@Local
public interface CommandExecutorLocal {
   Object executeTransaction(Object target, String methodName,
         String[] classNames, Object[] args) throws ClassNotFoundException,
         IllegalAccessException, NoSuchMethodException,
         InvocationTargetException;

   Object executeQuery(Object target, String methodName, String[] classNames,
         Object[] args) throws ClassNotFoundException, IllegalAccessException,
         NoSuchMethodException, InvocationTargetException;
}
