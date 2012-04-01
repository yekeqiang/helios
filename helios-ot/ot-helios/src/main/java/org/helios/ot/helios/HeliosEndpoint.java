/**
 * Helios, OpenSource Monitoring
 * Brought to you by the Helios Development Group
 *
 * Copyright 2007, Helios Development Group and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org. 
 *
 */
package org.helios.ot.helios;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.helios.helpers.JMXHelper;
import org.helios.jmx.dynamic.annotations.JMXAttribute;
import org.helios.jmx.dynamic.annotations.JMXManagedObject;
import org.helios.jmx.dynamic.annotations.options.AttributeMutabilityOption;
import org.helios.jmxenabled.threads.ExecutorBuilder;
import org.helios.ot.endpoint.AbstractEndpoint;
import org.helios.ot.endpoint.EndpointConnectException;
import org.helios.ot.endpoint.EndpointTraceException;
import org.helios.ot.trace.Trace;
import org.helios.ot.trace.types.ITraceValue;
import org.helios.ot.tracer.disruptor.TraceCollection;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.SocketChannel;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

/**
 * <p>Title: HeliosEndpoint</p>
 * <p>Description: OpenTrace endpoint optimized for the Helios OT Server</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.ot.helios.HeliosEndpoint</code></p>
 */
@JMXManagedObject (declared=false, annotated=true)
public class HeliosEndpoint<T extends Trace<? extends ITraceValue>> extends AbstractEndpoint<T> implements UncaughtExceptionHandler {
	/** The helios OT server host name or ip address */
	protected String host;
	/** The helios OT server listening port */
	protected int port;
	/** The helios OT server comm protocol */
	protected Protocol protocol;
	/** The helios OT connector of the configured protocol */
	protected final AbstractEndpointConnector connector;
	
	/** The count of exceptions */
	protected final AtomicLong exceptionCount = new AtomicLong(0);
	
	/** A set of connect listeners that will be added when an asynch connect is initiated */
	protected final Set<ChannelFutureListener> connectListeners = new CopyOnWriteArraySet<ChannelFutureListener>();
	
	
	/**  */
	private static final long serialVersionUID = -433677190518825263L;

	
	/**
	 * Creates a new HeliosEndpoint from system properties and an optional external XML file
	 */
	public HeliosEndpoint() {
		// Read the basic config
		host = HeliosEndpointConstants.getHost();
		port = HeliosEndpointConstants.getPort();
		protocol = HeliosEndpointConstants.getProtocol();
		connector = protocol.createConnector(this); 		
	}
	
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.ot.endpoint.AbstractEndpoint#connectImpl()
	 */
	@Override
	protected void connectImpl() throws EndpointConnectException {
		connector.connect();
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.ot.endpoint.AbstractEndpoint#disconnectImpl()
	 */
	@Override
	protected void disconnectImpl() {
		connector.disconnect();
	}
	
	
	public static void main(String[] args) {
		BasicConfigurator.configure();
		Logger LOG = Logger.getLogger(HeliosEndpoint.class);
		LOG.info("Test");
		HeliosEndpoint he = new HeliosEndpoint();
		he.reflectObject(he.connector.getInstrumentation());
		boolean b = he.connect();
		LOG.info("Connected:"+b);
		for(int i = 0; i < 1000; i++) {
			try {
				he.connector.write(new byte[10]);
				Thread.sleep(5000);
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
		try { Thread.currentThread().join(); } catch (Exception e) {}
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
	 */
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		log.error("Uncaught exception on thread [" + t + "]", e);
	}
	
	
	/**
	 * {@inheritDoc}
	 * @see org.helios.ot.endpoint.AbstractEndpoint#newBuilder()
	 */
	@Override
	public org.helios.ot.endpoint.AbstractEndpoint.Builder newBuilder() {
		return null;
	}

	

	/**
	 * {@inheritDoc}
	 * @see org.helios.ot.endpoint.AbstractEndpoint#processTracesImpl(org.helios.ot.tracer.disruptor.TraceCollection)
	 */
	@Override
	protected boolean processTracesImpl(TraceCollection<T> traceCollection) throws EndpointConnectException, EndpointTraceException {
		return false;
	}

	
	

	/**
	 * Returns the helios OT server host name or ip address
	 * @return the host
	 */
	@JMXAttribute(name="Host", description="The helios OT server host name or ip address", mutability=AttributeMutabilityOption.READ_ONLY)
	public String getHost() {
		return host;
	}

	/**
	 * Returns the helios OT server listening port
	 * @return the port
	 */
	@JMXAttribute(name="Port", description="The helios OT server listening port", mutability=AttributeMutabilityOption.READ_ONLY)
	public int getPort() {
		return port;
	}

	/**
	 * Returns the helios OT server comm protocol
	 * @return the protocol
	 */
	@JMXAttribute(name="Protocol", description="The helios OT server comm protocol", mutability=AttributeMutabilityOption.READ_ONLY)
	public String getProtocol() {
		return protocol.name();
	}

	/**
	 * Returns the cumulative exception count
	 * @return the exceptionCount
	 */
	@JMXAttribute(name="ExceptionCount", description="The cumulative exception count", mutability=AttributeMutabilityOption.READ_ONLY)
	public long getExceptionCount() {
		return exceptionCount.get();
	}

	/**
	 * Constructs a <code>String</code> with all attributes
	 * in name = value format.
	 *
	 * @return a <code>String</code> representation 
	 * of this object.
	 */
	public String toString() {
	    StringBuilder retValue = new StringBuilder("HeliosEndpoint [")
	        .append("host:").append(this.host)
	        .append(" port:").append(this.port)
	        .append(" protocol:").append(this.protocol)
	        .append(" connected:").append(isConnected.get())
	        .append("]");    
	    return retValue.toString();
	}






}