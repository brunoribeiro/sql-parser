/**
 * Copyright © 2012 Akiban Technologies, Inc.  All rights
 * reserved.
 *
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program may also be available under different license terms.
 * For more information, see www.akiban.com or contact
 * licensing@akiban.com.
 *
 * Contributors:
 * Akiban Technologies, Inc.
 */

package com.akiban.sql.parser;

import com.akiban.sql.StandardException;

public class AlterServerNode extends MiscellaneousStatementNode {


    public enum AlterType {
        SET_SERVER_VARIABLE,
        INTERRUPT_SESSION,
        DISCONNECT_SESSION,
        KILL_SESSION,
        SHUTDOWN
    }

    private Integer sessionID = null;
    private AlterType alterSessionType;
    private SetConfigurationNode scn = null;
    private boolean shutdownImmediate;
    
    
    
    public void init(Object config) {
      
        if (config instanceof SetConfigurationNode) {
            scn = (SetConfigurationNode)config;
            alterSessionType = AlterType.SET_SERVER_VARIABLE;
        } else if (config instanceof Boolean) {
            alterSessionType = AlterType.SHUTDOWN;
            shutdownImmediate = ((Boolean)config).booleanValue();
        }
    }
    
    public void init (Object interrupt, Object disconnect, Object kill, Object session)
    {
        if (interrupt != null) {
            alterSessionType = AlterType.INTERRUPT_SESSION;
        } else if (disconnect != null) {
            alterSessionType = AlterType.DISCONNECT_SESSION;
        } else if (kill != null) {
            alterSessionType = AlterType.KILL_SESSION;
        }
        if (session instanceof ConstantNode) {
            sessionID = (Integer)((ConstantNode)session).getValue();
        }
    }
    
    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);
        AlterServerNode other = (AlterServerNode)node;
        this.sessionID = other.sessionID;
        this.alterSessionType = other.alterSessionType;
        this.scn = (SetConfigurationNode)getNodeFactory().copyNode(other.scn, getParserContext());
        this.shutdownImmediate = other.shutdownImmediate;
    }
    
    @Override
    public String statementToString() {
        return "ALTER SERVER";
    }

    @Override
    public String toString() {
        String ret = null;
        switch (alterSessionType) {
        case SET_SERVER_VARIABLE:
            ret = scn.toString();
            break;
        case SHUTDOWN:
            ret = "shutdown immediate: " + shutdownImmediate;
            break;
        case INTERRUPT_SESSION:
        case DISCONNECT_SESSION:
        case KILL_SESSION:
            ret = "sessionType: " + alterSessionType.name() + "\n" +  
                    "sessionID: " + sessionID;
            break;
        }
        ret = super.toString() + ret;
        return ret;
    }
    
    public final Integer getSessionID() {
        return sessionID;
    }

    public final AlterType getAlterSessionType() {
        return alterSessionType;
    }

    public final boolean isShutdownImmediate() {
        return shutdownImmediate;
    }

    public String getVariable() {
        return scn.getVariable();
    }

    public String getValue() {
        return scn.getValue();
    }
    
}
