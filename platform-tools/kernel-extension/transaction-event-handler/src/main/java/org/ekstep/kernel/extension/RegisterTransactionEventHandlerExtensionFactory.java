package org.ekstep.kernel.extension;

import org.neo4j.kernel.extension.KernelExtensionFactory;
import org.neo4j.kernel.ha.HighlyAvailableGraphDatabase;
import org.neo4j.kernel.impl.spi.KernelContext;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            private ExecutorService executor;

			@Override
            public void start() throws Throwable {
                executor = Executors.newFixedThreadPool(2);
                handler = new EkStepTransactionEventHandler(dependencies.getGraphDatabaseService(), executor);
                dependencies.getGraphDatabaseService().registerTransactionEventHandler(handler);
            }

            @Override
            public void shutdown() throws Throwable {
                executor.shutdown();
                dependencies.getGraphDatabaseService().unregisterTransactionEventHandler(handler);
            }
        };
    }

    @SuppressWarnings("unchecked")
	@Override
	public Lifecycle newInstance(KernelContext context, final Dependencies dependencies) throws Throwable {
		return new LifecycleAdapter() {

            private EkStepTransactionEventHandler handler;
            private ExecutorService executor;

			@Override
            public void start() throws Throwable {
                executor = Executors.newFixedThreadPool(2);
                handler = new EkStepTransactionEventHandler(dependencies.getGraphDatabaseService(), executor);
                dependencies.getGraphDatabaseService().registerTransactionEventHandler(handler);
            }

            @Override
            public void shutdown() throws Throwable {
                executor.shutdown();
                dependencies.getGraphDatabaseService().unregisterTransactionEventHandler(handler);
            }
        };
	}

}
