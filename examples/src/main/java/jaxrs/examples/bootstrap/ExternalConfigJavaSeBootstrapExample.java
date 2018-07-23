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

import java.security.GeneralSecurityException;

import javax.ws.rs.JAXRS;
import javax.ws.rs.JAXRS.Configuration;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * Java SE Bootstrap Example utilizing an external configuration system
 * <p>
 * This example demonstrates bootstrapping on Java SE platforms using values
 * retrieved from an external configuration system plus statically overriden
 * properties. In particular, this demo first uses Eclipse Microprofile Config
 * to let the actual implementation retrieve all properties from an external
 * source of configuration, but then explicitly enforces HTTPS within the
 * bootstrap code.
 * </p>
 * <p>
 * To actually run this example, an implementation of Eclipse Microprofile
 * Config has to be added to the classpath, and the configuration options to be
 * customized have to be provided, e. g. as environment variables:
 * </p>
 *
 * <pre>
 * export javax.ws.rs.JAXRS.Port=8888;
 * java -Djavax.ws.rs.JAXRS.Host=127.0.0.1 PropertyProviderJavaSeBootstrapExample;
 * </pre>
 * <p>
 * This example uses some basic <em>external</em> JSSE configuration:
 * </p>
 * <ul>
 * <li>{@code javax.net.ssl.keyStore=~/.keystore} - HTTPS: Path to a keystore
 * holding an X.509 certificate for {@code CN=localhost}</li>
 * <li>{@code javax.net.ssl.keyStorePassword=...} - HTTPS: Password of that
 * keystore</li>
 * </ul>
 * <p>
 * Note that support for external configuration is <em>not mandatory</em>.
 * Implementations could choose to not implement it, or to support other
 * external configuration mechanics not mentioned here. Hence, as the example
 * relies particularly on Microprofile Config being supported by the
 * implementation, it is <em>not necessarily</em> portable.
 * </p>
 *
 * @author Markus KARG (markus@headcrashing.eu)
 */
public final class ExternalConfigJavaSeBootstrapExample {

    /**
     * Runs this example.
     *
     * @param args
     *            unused command line arguments
     * @throws InterruptedException
     *             when process is killed
     */
    public static final void main(final String[] args) throws InterruptedException, GeneralSecurityException {
        final Application application = new HelloWorld();

        final Config config = ConfigProvider.getConfig();

        final JAXRS.Configuration requestedConfiguration = JAXRS.Configuration.builder().from(config).protocol("HTTPS")
                .build();

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
