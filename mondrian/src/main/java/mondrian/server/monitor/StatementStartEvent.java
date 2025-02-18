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
 * Event created just after a statement has been created.
 */
public class StatementStartEvent extends StatementEvent {
    /**
     * Creates a StatementStartEvent.
     *
     * @param timestamp Timestamp
     * @param serverId Server id
     * @param connectionId Connection id
     * @param statementId Statement id
     */
    public StatementStartEvent(
        long timestamp,
        int serverId,
        int connectionId,
        long statementId)
    {
        super(timestamp, serverId, connectionId, statementId);
    }

    @Override
	public String toString() {
        return new StringBuilder("StatementStartEvent(").append(statementId).append(")").toString();
    }

    @Override
	public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
}
