package org.ekstep.kernel.extension;

import org.neo4j.kernel.extension.KernelExtensionFactory;
import org.neo4j.kernel.ha.HighlyAvailableGraphDatabase;
import org.neo4j.kernel.impl.spi.KernelContext;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;

public class RegisterTransactionEventHandlerExtensionFactory extends KernelExtensionFactory<RegisterTransactionEventHandlerExtensionFactory.Dependencies> {

    public interface Dependencies {
        HighlyAvailableGraphDatabase getGraphDatabaseService();
    }

    public RegisterTransactionEventHandlerExtensionFactory() {
        super("registerTransactionEventHandler");
    }

    @SuppressWarnings("unchecked")
    public Lifecycle newKernelExtension(final Dependencies dependencies) throws Throwable {
        return new LifecycleAdapter() {

            private EkStepTransactionEventHandler handler;

			@Override
            public void start() throws Throwable {
                handler = new EkStepTransactionEventHandler(dependencies.getGraphDatabaseService());
                dependencies.getGraphDatabaseService().registerTransactionEventHandler(handler);
            }

            @Override
            public void shutdown() throws Throwable {
                dependencies.getGraphDatabaseService().unregisterTransactionEventHandler(handler);
            }
        };
    }

    @SuppressWarnings("unchecked")
	@Override
	public Lifecycle newInstance(KernelContext context, final Dependencies dependencies) throws Throwable {
		return new LifecycleAdapter() {

            private EkStepTransactionEventHandler handler;

			@Override
            public void start() throws Throwable {
                handler = new EkStepTransactionEventHandler(dependencies.getGraphDatabaseService());
                dependencies.getGraphDatabaseService().registerTransactionEventHandler(handler);
            }

            @Override
            public void shutdown() throws Throwable {
                dependencies.getGraphDatabaseService().unregisterTransactionEventHandler(handler);
            }
        };
	}

}
