package org.ekstep.kernel.extension;

import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.extension.ExtensionFactory;
import org.neo4j.kernel.extension.ExtensionType;
import org.neo4j.kernel.extension.context.ExtensionContext;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;

import java.io.IOException;


public class RegisterTransactionEventHandlerExtensionFactory extends ExtensionFactory<RegisterTransactionEventHandlerExtensionFactory.Dependencies> {

    public interface Dependencies {
        GraphDatabaseService getGraphDatabaseService();
        DatabaseManagementService getDatabaseManagementService();
    }

    public RegisterTransactionEventHandlerExtensionFactory() {
        super(ExtensionType.DATABASE, "registerUserDefinedExtension");
    }


    /**
     * Create a new instance of this kernel extension.
     *
     * @param context      the context the extension should be created for
     * @param dependencies deprecated
     * @return the {@link Lifecycle} for the extension
     */
    @Override
    public Lifecycle newInstance(ExtensionContext context, Dependencies dependencies) {
        return new LifecycleAdapter() {
            private EkStepTransactionEventHandler handler;
            
            @Override
            public void start() throws IOException {
                try {
                    handler = new EkStepTransactionEventHandler(dependencies.getGraphDatabaseService());
                    dependencies.getDatabaseManagementService().registerTransactionEventListener(GraphDatabaseSettings.default_database.defaultValue(), handler);
                    System.out.println("Registering the kernel ext for transaction-event-handler - complete.");
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }

            @Override
            public void shutdown() {
                try {
                    dependencies.getDatabaseManagementService().unregisterTransactionEventListener(GraphDatabaseSettings.default_database.defaultValue(), handler);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
        };
    }

}
