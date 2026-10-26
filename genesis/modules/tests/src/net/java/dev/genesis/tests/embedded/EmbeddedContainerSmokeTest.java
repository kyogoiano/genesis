package net.java.dev.genesis.tests.embedded;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import net.java.dev.genesis.ejb.CommandExecutor;
import net.java.dev.genesis.ejb.CommandExecutorHome;
import net.java.dev.genesis.tests.TestCase;

public class EmbeddedContainerSmokeTest extends TestCase {
   public void testPublishesEjb2Home() throws Exception {
      Object value = new InitialContext().lookup(
            "java:global/container/CommandExecutor!net.java.dev.genesis.ejb.CommandExecutorHome");
      CommandExecutorHome home = (CommandExecutorHome) PortableRemoteObject
            .narrow(value, CommandExecutorHome.class);
      CommandExecutor executor = home.create();
      assertNotNull(executor);
      executor.remove();
   }
}
