package net.java.dev.genesis.tests.embedded;

import javax.naming.InitialContext;
import net.java.dev.genesis.ejb.CommandExecutor;
import net.java.dev.genesis.tests.TestCase;

public class EmbeddedContainerSmokeTest extends TestCase {
   public void testPublishesEjbRemoteBusinessView() throws Exception {
      CommandExecutor executor = (CommandExecutor)new InitialContext().lookup(
            "java:global/container/CommandExecutor!net.java.dev.genesis.ejb.CommandExecutor");
      assertNotNull(executor);
   }
}
