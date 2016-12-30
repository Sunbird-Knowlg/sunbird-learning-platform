package org.ekstep.kernel.extension;

import org.neo4j.kernel.extension.KernelExtensionFactory;
import org.neo4j.kernel.impl.spi.KernelContext;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;
import org.neo4j.kernel.ha.HighlyAvailableGraphDatabase;


public class RegisterTransactionEventHandlerExtensionFactory extends KernelExtensionFactory<RegisterTransactionEventHandlerExtensionFactory.Dependencies> {

    public interface Dependencies {
        HighlyAvailableGraphDatabase getGraphDatabaseService();
    }

    public RegisterTransactionEventHandlerExtensionFactory() {
        super("registerTransactionEventHandler");
    }

    @SuppressWarnings("unchecked")
	@Override
	public Lifecycle newInstance(KernelContext context, final Dependencies dependencies) throws Throwable {
		return new LifecycleAdapter() {

            private EkStepTransactionEventHandler handler;

			@Override
            public void start() throws Throwable {
                try {
                    handler = new EkStepTransactionEventHandler(dependencies.getGraphDatabaseService());
                    dependencies.getGraphDatabaseService().registerTransactionEventHandler(handler);
                } catch (Exception e) {
                }
            }

            @Override
            public void shutdown() throws Throwable {
                try {
                    dependencies.getGraphDatabaseService().unregisterTransactionEventHandler(handler);
                } catch (Exception e) {
                }
            }
        };
	}

}
