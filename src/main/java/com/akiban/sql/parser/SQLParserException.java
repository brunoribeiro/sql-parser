
package com.akiban.sql.parser;

import com.akiban.sql.StandardException;

public class SQLParserException extends StandardException
{
    private int errorPosition;

    public SQLParserException(String msg, Throwable cause, int errorPosition) {
        super(msg, cause);
        this.errorPosition = errorPosition;
    }

    public int getErrorPosition() {
        return errorPosition;
    }
}
