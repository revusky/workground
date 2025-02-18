/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.server.monitor;

/**
 * Event indicating that a connection has been created.
 */
public class ConnectionStartEvent extends ConnectionEvent {
    /**
     * Creates a ConnectionStartEvent.
     *
     * @param timestamp Timestamp
     * @param serverId Server id
     * @param connectionId Connection id
     */
    public ConnectionStartEvent(
        long timestamp,
        int serverId,
        int connectionId)
    {
        super(timestamp, serverId, connectionId);
    }

    @Override
	public String toString() {
        return new StringBuilder("ConnectionStartEvent(").append(connectionId).append(")").toString();
    }

    @Override
	public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
