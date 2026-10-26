package net.java.dev.genesis.command;

public final class CommandExecutionGuard {
   private static final ThreadLocal<Integer> DEPTH = new ThreadLocal<Integer>();

   private CommandExecutionGuard() {
   }

   public static boolean isExecuting() {
      return DEPTH.get() != null;
   }

   public static void enter() {
      Integer depth = DEPTH.get();
      DEPTH.set(depth == null ? 1 : depth + 1);
   }

   public static void exit() {
      Integer depth = DEPTH.get();
      if (depth == null || depth <= 1) {
         DEPTH.remove();
      } else {
         DEPTH.set(depth - 1);
      }
   }
}
