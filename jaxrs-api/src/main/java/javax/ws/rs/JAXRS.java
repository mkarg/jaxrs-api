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

package javax.ws.rs;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * Bootstrap class that is used to startup a JAX-RS application in Java SE
 * environments.
 * <p>
 * The {@code JAXRS} class is available in a Jakarta EE container environment as
 * well; however, support for the Java SE bootstrapping APIs is <em>not
 * required</em> in container environments.
 * </p>
 * <p>
 * In a Java SE environment an application is getting started by the following
 * command using default configuration values (i. e. mounting application at
 * {@code http://localhost:80/} <em>or a different port</em> (there is <em>no
 * particular default port</em> mandated by this specification). As the JAX-RS
 * implementation is free to choose any port by default, the caller will not
 * know the actual port unless explicitly checking the actual configuration of
 * the instance started:
 * </p>
 *
 * <pre>
 * Application app = new MyApplication();
 * JAXRS.Configuration config = JAXRS.Configuration.builder().build();
 * JAXRS.start(app, config).thenAccept(instance -&gt; instance.configuration().port());
 * </pre>
 *
 * <p>
 * Running instances can be instructed to stop serving the application:
 * </p>
 *
 * <pre>
 * JAXRS.start(app, config).thenAccept(instance -&gt; { ... instance.stop(); } );
 * </pre>
 *
 * <p>
 * A shutdown callback can be registered which will get invoked once the
 * implementation stops serving the application:
 * </p>
 *
 * <pre>
 * instance.stop().thenAccept(stopResult -&gt; ...));
 * </pre>
 *
 * {@code stopResult} is not further defined but solely acts as a wrapper around
 * a native result provided by the particular JAX-RS implementation. Portable
 * applications should not assume any particular data type or value.
 *
 * <p>
 * Protocol, host address, port and root path can be overridden explicitly. As
 * the JAX-RS implementation is bound to that values, no querying of the actual
 * configuration is needed in that case:
 * </p>
 *
 * <pre>
 * JAXRS.Configuration.builder().protocol("HTTPS").host("0.0.0.0").port(8443).rootPath("api").build();
 * </pre>
 *
 * <p>
 * TLS can be configured by explicitly passing a customized {@link SSLContext}:
 * </p>
 *
 * <pre>
 * SSLContext tls = SSLContext.getInstance("TLSv1.2");
 * // ...further initialize context here (see JSSE API)...
 * JAXRS.Configuration.builder().protocol("HTTPS").sslContext(tls).build();
 * </pre>
 *
 * <p>
 * In case of HTTPS, client authentication can be enforced to ensure that only
 * trusted clients will connect:
 * </p>
 *
 * <pre>
 * JAXRS.Configuration.builder().protocol("HTTPS").sslClientAuthentication(SSLClientAuthentication.MANDATORY).build();
 * </pre>
 *
 * <p>
 * Implementations are free to support more use case by native properties, which
 * effectively render the application non-portable:
 * </p>
 *
 * <pre>
 * JAXRS.Configuration.builder().property("productname.foo", "bar").build()
 * </pre>
 *
 * @author Markus KARG (markus@headcrashing.eu)
 *
 * @since 2.2
 */
public interface JAXRS {

    /**
     * Invoked in Java SE environments to start the provided application at the
     * specified root URL.
     *
     * @param application
     *            The application to start up.
     * @param configuration
     *            Provides information needed for bootstrapping the application.
     * @return {@code CompletionStage} asynchronously producing handle of the
     *         running application {@link JAXRS.Instance instance}.
     */
    static CompletionStage<Instance> start(final Application application, final Configuration configuration) {
        return RuntimeDelegate.getInstance().bootstrap(application, configuration);
    }

    /**
     * Provides information needed by the JAX-RS implementation for bootstrapping an
     * application.
     * <p>
     * The configuration essentially consists of a set of parameters. While the set
     * of actually effective keys is product specific, the key constants defined by
     * the {@link JAXRS.Configuration} interface MUST be effective on all compliant
     * products. Any unknown key MUST be silently ignored.
     * </p>
     *
     * @author Markus KARG (markus@headcrashing.eu)
     *
     * @since 2.2
     */
    public static interface Configuration {

        /**
         * Configuration key for the protocol an application is bound to. A compliant
         * implementation at least MUST accept the strings {@code "HTTP"} and
         * {@code "HTTPS"} if these protocols are supported. The default value is
         * {@code "HTTP"}.
         */
        static final String PROTOCOL = "javax.ws.rs.JAXRS.Protocol";

        /**
         * Configuration key for the hostname or IP address an application is bound to.
         * If a hostname is provided, the application MUST be bound to <em>all</em> IP
         * addresses assigned to that hostname. A compliant implementation at least MUST
         * accept strings bearing hostnames, IP4 address strings, and IP6 address
         * strings. The default value is {@code "localhost"}.
         */
        static final String HOST = "javax.ws.rs.JAXRS.Host";

        /**
         * Configuration key for the TCP port an application is bound to. A compliant
         * implementation MUST accept {@code java.lang.Integer} values. There is no
         * default <em>port</em> mandated by this specification, but the default
         * <em>value</em> of this property is {@link #DEFAULT_PORT} (i. e.
         * <code>-1</code>). A compliant implementation MUST use its own default
         * <em>port</em> when the <em>value</em> <code>-1</code> is provided, and MAY
         * apply (but is not obligated to) auto-selection and range-scanning algorithms.
         */
        static final String PORT = "javax.ws.rs.JAXRS.Port";

        /**
         * Configuration key for the root path an application is bound to. The default
         * value is {@code "/"}.
         */
        static final String ROOT_PATH = "javax.ws.rs.JAXRS.RootPath";

        /**
         * Configuration key for the secure socket configuration to be used. The default
         * value is {@link SSLContext#getDefault()}.
         */
        static final String SSL_CONTEXT = "javax.ws.rs.JAXRS.SSLContext";

        /**
         * Configuration key for the secure socket client authentication policy.
         *
         * <p>
         * A compliant implementation MUST accept {@link SSLClientAuthentication} enums.
         * The default value is {@code SSLClientAuthentication#NONE}.
         * </p>
         */
        static final String SSL_CLIENT_AUTHENTICATION = "javax.ws.rs.JAXRS.SSLClientAuthentication";

        /**
         * Secure socket client authentication policy
         *
         * <p>
         * This policy is used in secure socket handshake to control whether the server
         * <em>requests</em> client authentication, and whether <em>successful</em>
         * client authentication is mandatory (i. e. connection attempt will fail for
         * invalid clients).
         * </p>
         *
         * @author Markus KARG (markus@headcrashing.eu)
         *
         * @since 2.2
         */
        public enum SSLClientAuthentication {

            /**
             * Server will <em>not request</em> client authentication.
             */
            NONE,

            /**
             * Client authentication is performed, but invalid clients are
             * <em>accepted</em>.
             */
            OPTIONAL,

            /**
             * Client authentication is performed, and invalid clients are
             * <em>rejected</em>.
             */
            MANDATORY
        }

        /**
         * Special value for {@link #PORT} property indicating that the implementation
         * MUST use its default port.
         */
        static final int DEFAULT_PORT = -1;

        /**
         * Returns the value of the property with the given name, or {@code null} if
         * there is no property of that name.
         *
         * @param name
         *            a {@code String} specifying the name of the property.
         * @return an {@code Object} containing the value of the property, or
         *         {@code null} if no property exists matching the given name.
         */
        Object property(String name);

        /**
         * Convenience method to get the {@code protocol} to be used.
         * <p>
         * Same as if calling {@link #property(String) property(PROTOCOL)}.
         * </p>
         *
         * @return protocol to be used (e. g. {@code "HTTP")}.
         * @throws ClassCastException
         *             if protocol is not a {@link String}.
         * @see JAXRS.Configuration#PROTOCOL
         */
        default String protocol() {
            return (String) property(PROTOCOL);
        }

        /**
         * Convenience method to get the {@code host} to be used.
         * <p>
         * Same as if calling {@link #property(String) (String) property(HOST)}.
         * </p>
         *
         * @return host name or IP address to be used (e. g. {@code "localhost"} or
         *         {@code "0.0.0.0"}).
         * @throws ClassCastException
         *             if host is not a {@link String}.
         * @see JAXRS.Configuration#HOST
         */
        default String host() {
            return (String) property(HOST);
        }

        /**
         * Convenience method to get the actually used {@code port}.
         * <p>
         * Same as if calling {@link #property(String) property(PORT)}.
         * </p>
         * <p>
         * If the port was <em>not explicitly</em> given, this will return the port
         * chosen implicitly by the JAX-RS implementation.
         * </p>
         *
         * @return port number to be used (e. g. {@code 8080}).
         * @throws ClassCastException
         *             if port is not an {@code int}.
         * @see JAXRS.Configuration#PORT
         */
        default int port() {
            return (int) property(PORT);
        }

        /**
         * Convenience method to get the {@code rootPath} to be used.
         * <p>
         * Same as if calling {@link #property(String) property(ROOT_PATH)}.
         * </p>
         *
         * @return root path to be used, e. g. {@code "/"}.
         * @throws ClassCastException
         *             if root path is not a {@link String}.
         * @see JAXRS.Configuration#ROOT_PATH
         */
        default String rootPath() {
            return (String) property(ROOT_PATH);
        }

        /**
         * Convenience method to get the {@code sslContext} to be used.
         * <p>
         * Same as if calling {@link #property(String) property(SSL_CONTEXT)}.
         * </p>
         *
         * @return root path to be used, e. g. {@code "/"}.
         * @throws ClassCastException
         *             if sslContext is not a {@link SSLContext}.
         * @see JAXRS.Configuration#SSL_CONTEXT
         */
        default SSLContext sslContext() {
            return (SSLContext) property(SSL_CONTEXT);
        }

        /**
         * Convenience method to get the secure socket client authentication policy.
         * <p>
         * Same as if calling {@link #property(String)
         * property(SSL_CLIENT_AUTHENTICATION)}.
         * </p>
         *
         * @return client authentication mode.
         * @throws ClassCastException
         *             if sslClientAuthentication is not a
         *             {@link SSLClientAuthentication}.
         * @see JAXRS.Configuration#SSL_CLIENT_AUTHENTICATION
         */
        default SSLClientAuthentication sslClientAuthentication() {
            return (SSLClientAuthentication) property(SSL_CLIENT_AUTHENTICATION);
        }

        /**
         * @return {@link Builder} for bootstrap configuration.
         */
        static Builder builder() {
            return RuntimeDelegate.getInstance().createConfigurationBuilder();
        };

        /**
         * Builder for bootstrap {@link Configuration}.
         *
         * @author Markus KARG (markus@headcrashing.eu)
         *
         * @since 2.2
         */
        static interface Builder {

            /**
             * @return {@link Configuration} built from provided property values.
             */
            Configuration build();

            /**
             * Sets the property {@code name} to the provided {@code value}.
             * <p>
             * This method does not check the validity, type or syntax of the provided
             * value.
             * </p>
             *
             * @param name
             *            name of the parameter to set
             * @param value
             *            value to set, or {@code null} to use the default value.
             * @return the updated builder.
             */
            Builder property(String name, Object value);

            /**
             * Convenience method to set the {@code protocol} to be used.
             * <p>
             * Same as if calling {@link #property(String, Object) property(PROTOCOL,
             * Object)}.
             * </p>
             *
             * @param protocol
             *            protocol parameter of this configuration, or {@code null} to use
             *            the default value.
             * @return the updated builder.
             * @see JAXRS.Configuration#PROTOCOL
             */
            default Builder protocol(String protocol) {
                return property(PROTOCOL, protocol);
            }

            /**
             * Convenience method to set the {@code host} to be used.
             * <p>
             * Same as if calling {@link #property(String, Object) property(HOST, Object)}.
             * </p>
             *
             * @param host
             *            host parameter (IP address or hostname) of this configuration, or
             *            {@code null} to use the default value.
             * @return the updated builder.
             * @see JAXRS.Configuration#HOST
             */
            default Builder host(String host) {
                return property(HOST, host);
            }

            /**
             * Convenience method to set the {@code port} to be used.
             * <p>
             * Same as if calling {@link #property(String, Object) property(PORT, Object)}.
             * </p>
             *
             * @param port
             *            port parameter of this configuration, or {@code null} to use the
             *            default value.
             * @return the updated builder.
             * @see JAXRS.Configuration#PORT
             */
            default Builder port(int port) {
                return property(PORT, port);
            }

            /**
             * Convenience method to set the {@code rootPath} to be used.
             * <p>
             * Same as if calling {@link #property(String, Object) property(ROOT_PATH,
             * Object)}.
             * </p>
             *
             * @param rootPath
             *            rootPath parameter of this configuration, or {@code null} to use
             *            the default value.
             * @return the updated builder.
             * @throws IllegalArgumentException
             *             if the rootPath is {@code null}.
             * @see JAXRS.Configuration#ROOT_PATH
             */
            default Builder rootPath(String rootPath) {
                return property(ROOT_PATH, rootPath);
            }

            /**
             * Convenience method to set the {@code sslContext} to be used.
             * <p>
             * Same as if calling {@link #property(String, Object) property(SSL_CONTEXT,
             * Object)}.
             * </p>
             *
             * @param sslContext
             *            sslContext parameter of this configuration, or {@code null} to use
             *            the default value.
             * @return the updated builder.
             * @see JAXRS.Configuration#SSL_CONTEXT
             */
            default Builder sslContext(SSLContext sslContext) {
                return property(SSL_CONTEXT, sslContext);
            }

            /**
             * Convenience method to set SSL client authentication policy.
             * <p>
             * Same as if calling {@link #property(String, Object)
             * property(SSL_CLIENT_AUTHENTICATION, Object)}.
             * </p>
             *
             * @param sslClientAuthentication
             *            SSL client authentication mode of this configuration
             * @return the updated builder.
             * @see JAXRS.Configuration#SSL_CLIENT_AUTHENTICATION
             */
            default Builder sslClientAuthentication(SSLClientAuthentication sslClientAuthentication) {
                return property(SSL_CLIENT_AUTHENTICATION, sslClientAuthentication);
            }

            /**
             * Convenience method for bulk-loading configuration from a property supplier.
             * <p>
             * Implementations ask the passed provider function for the actual values of all
             * their supported properties once in a single loop, before returning from this
             * configuration method. For each single request the implementation provides the
             * name of the property and the expected data type of the value. If no such
             * property exists (i. e. either the name is unknown or misspelled, or the type
             * does not exactly match), the {@link Optional} is {@link Optional#empty()
             * empty}.
             * </p>
             *
             * @param <T>
             *            Type of the requested property value.
             * @param propertiesProvider
             *            Retrieval function of externally managed properties. MUST NOT
             *            return {@code null}.
             * @return the updated builder.
             */
            <T> Builder from(BiFunction<String, Class<T>, Optional<T>> propertiesProvider);

            /**
             * Optional convenience method to bulk-load external configuration.
             * <p>
             * Implementations are free to support any external configuration mechanics, or
             * none at all. It is completely up to the implementation what set of properties
             * is effectively loaded from the provided external configuration, possibly none
             * at all.
             * </p>
             * <p>
             * If the passed external configuration mechanics is unsupported, this method
             * MUST simply do nothing.
             * </p>
             * <p>
             * Portable applications should not call this method, as the outcome is
             * completely implementation-specific.
             * </p>
             *
             * @param externalConfig
             *            source of externally managed properties
             * @return the updated builder.
             */
            default Builder from(Object externalConfig) {
                return this;
            }

        }
    }

    /**
     * Handle of the running application instance.
     *
     * @author Markus KARG (markus@headcrashing.eu)
     *
     * @since 2.2
     */
    public interface Instance {

        /**
         * Provides access to the configuration used to create this instance.
         *
         * @return The configuration used to create this instance.
         */
        public Configuration configuration();

        /**
         * Shutdown running application instance.
         *
         * @return {@code CompletionStage} asynchronously shutting down this application
         *         instance.
         */
        public CompletionStage<StopResult> stop();

        /**
         * Result of stopping the application instance.
         *
         * @author Markus KARG (markus@headcrashing.eu)
         */
        public interface StopResult {

            /**
             * Provides access to the wrapped native shutdown result.
             *
             * @param <T>
             *            Requested type of the native result to return.
             * @param nativeClass
             *            Requested type of the native result to return.
             * @return Native result of shutting down the running application instance or
             *         {@code null} if the implementation has no native handle.
             * @throws ClassCastException
             *             if the object is not {@code null} and is not assignable to the
             *             type {@code T}.
             */
            public <T> T unwrap(Class<T> nativeClass);
        }

        /**
         * Provides access to the wrapped native handle of the application instance.
         *
         * @param <T>
         *            Requested type of the native handle to return.
         * @param nativeClass
         *            Requested type of the native handle to return.
         * @return Native handle of the running application instance or {@code null} if
         *         the implementation has no native handle.
         * @throws ClassCastException
         *             if the object is not {@code null} and is not assignable to the
         *             type {@code T}.
         */
        public <T> T unwrap(Class<T> nativeClass);
    }

}
