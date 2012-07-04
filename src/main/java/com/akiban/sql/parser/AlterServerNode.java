/**
 * END USER LICENSE AGREEMENT (“EULA”)
 *
 * READ THIS AGREEMENT CAREFULLY (date: 9/13/2011):
 * http://www.akiban.com/licensing/20110913
 *
 * BY INSTALLING OR USING ALL OR ANY PORTION OF THE SOFTWARE, YOU ARE ACCEPTING
 * ALL OF THE TERMS AND CONDITIONS OF THIS AGREEMENT. YOU AGREE THAT THIS
 * AGREEMENT IS ENFORCEABLE LIKE ANY WRITTEN AGREEMENT SIGNED BY YOU.
 *
 * IF YOU HAVE PAID A LICENSE FEE FOR USE OF THE SOFTWARE AND DO NOT AGREE TO
 * THESE TERMS, YOU MAY RETURN THE SOFTWARE FOR A FULL REFUND PROVIDED YOU (A) DO
 * NOT USE THE SOFTWARE AND (B) RETURN THE SOFTWARE WITHIN THIRTY (30) DAYS OF
 * YOUR INITIAL PURCHASE.
 *
 * IF YOU WISH TO USE THE SOFTWARE AS AN EMPLOYEE, CONTRACTOR, OR AGENT OF A
 * CORPORATION, PARTNERSHIP OR SIMILAR ENTITY, THEN YOU MUST BE AUTHORIZED TO SIGN
 * FOR AND BIND THE ENTITY IN ORDER TO ACCEPT THE TERMS OF THIS AGREEMENT. THE
 * LICENSES GRANTED UNDER THIS AGREEMENT ARE EXPRESSLY CONDITIONED UPON ACCEPTANCE
 * BY SUCH AUTHORIZED PERSONNEL.
 *
 * IF YOU HAVE ENTERED INTO A SEPARATE WRITTEN LICENSE AGREEMENT WITH AKIBAN FOR
 * USE OF THE SOFTWARE, THE TERMS AND CONDITIONS OF SUCH OTHER AGREEMENT SHALL
 * PREVAIL OVER ANY CONFLICTING TERMS OR CONDITIONS IN THIS AGREEMENT.
 */

package com.akiban.sql.parser;

public class AlterServerNode extends MiscellaneousStatementNode {

    public enum AlterType {
        SET_SERVER_VARIABLE,
        INTERRUPT_SESSION,
        DISCONNECT_SESSION,
        KILL_SESSION,
        SHUTDOWN
    }

    private int sessionID;
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
        }
        ret = super.toString() + ret;
        return ret;
    }
}
