/*
 * Copyright (c) 2018 Markus KARG. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package jaxrs.examples.bootstrap;

import javax.ws.rs.JAXRS;
import javax.ws.rs.JAXRS.Configuration;
import javax.ws.rs.core.Application;

/**
 * Java SE Bootstrap Example using HTTPS
 * <p>
 * This example demonstrates bootstrapping on Java SE platforms using HTTPS. It
 * relies mostly on defaults but explicitly sets the protocol to HTTPS. The
 * example will effectively startup the {@link HelloWorld} application at the
 * URL {@code https://localhost/} on an implementation-specific default IP port
 * (e. g. 443, 8443, or someting completely different). The actual configuration
 * needs to be queried after bootstrapping, otherwise callers would be unaware
 * of the actual chosen port.
 * </p>
 * <p>
 * This example uses some basic <em>external</em> JSSE configuration:
 * </p>
 * <ul>
 * <li>{@code javax.net.ssl.keyStore=~/.keystore} - HTTPS: Path to a keystore
 * holding an X.509 certificate for {@code CN=localhost}</li>
 * <li>{@code javax.net.ssl.keyStorePassword=...} - HTTPS: Password of that
 * keystore</li>
 * </ul>
 * </p>
 *
 * @author Markus KARG (markus@headcrashing.eu)
 */
public final class HttpsJavaSeBootstrapExample {

    /**
     * Runs this example.
     *
     * @param args
     *            unused command line arguments
     * @throws InterruptedException
     *             when process is killed
     */
    public static final void main(final String[] args) throws InterruptedException {
        final Application application = new HelloWorld();

        final JAXRS.Configuration requestedConfiguration = JAXRS.Configuration.builder().protocol("HTTPS").build();

        JAXRS.start(application, requestedConfiguration).thenAccept(instance -> {
            Runtime.getRuntime()
                    .addShutdownHook(new Thread(() -> instance.stop()
                            .thenAccept(stopResult -> System.out.printf("Stop result: %s [Native stop result: %s].%n",
                                    stopResult, stopResult.unwrap(Object.class)))));

            final Configuration actualConfigurarion = instance.configuration();
            System.out.printf("Instance %s running at %s://%s:%d%s [Native handle: %s].%n", instance,
                    actualConfigurarion.protocol().toLowerCase(), actualConfigurarion.host(),
                    actualConfigurarion.port(), actualConfigurarion.rootPath(), instance.unwrap(Object.class));
            System.out.println("Send SIGKILL to shutdown.");
        });

        Thread.currentThread().join();
    }

}
