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
package org.helios.server.ot.wiretaps;

import org.apache.camel.Exchange;
import org.helios.ot.trace.Trace;
import org.helios.server.ot.listener.helios.protocol.HeliosProtocolServerInvoker;

/**
 * <p>Title: TraceCountWireTap</p>
 * <p>Description: Trace count wire tap</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.helios.server.ot.wiretaps.TraceCountWireTap</code></p>
 */

public class TraceCountWireTap {
	/**
	 * Simple wire tap endpoint for returning a trace count
	 * @param traces An array of traces
	 * @return the number of traces.
	 */
	public void wireTap(Exchange exchange) {
		Object body = exchange.getIn().getBody();
		if(body!=null &&  body instanceof Trace[]) {
			int result = ((Trace[])body).length;
			exchange.getIn().setHeader(HeliosProtocolServerInvoker.OT_AGENT_RESPONSE, result);
			exchange.getIn().setBody(result);
		} else {
			exchange.getIn().setHeader(HeliosProtocolServerInvoker.OT_AGENT_RESPONSE, -1);
			exchange.getIn().setBody(-1);
		}
	}

}
