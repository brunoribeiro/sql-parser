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

package com.akiban.sql.compiler;

import com.akiban.sql.parser.*;

import com.akiban.sql.StandardException;
import com.akiban.sql.types.DataTypeDescriptor;
import com.akiban.sql.types.TypeId;

import java.sql.Types;

public class IntervalTypeCompiler extends TypeCompiler
{
    protected IntervalTypeCompiler(TypeId typeId) {
        super(typeId);
    }

    /**
     * Determine if this type (interval) can be converted to some other type.
     */
    public boolean convertible(TypeId otherType, boolean forDataTypeFunction) {
        if (otherType.isStringTypeId() && !otherType.isLongConcatableTypeId()) {
            return true;
        }
        return (getStoredFormatIdFromTypeId() == otherType.getTypeFormatId());
    }

    /**
     * Tell whether this type (interval) is compatible with the given type.
     *
     * @param otherType         The TypeId of the other type.
     */
    public boolean compatible(TypeId otherType) {
        return convertible(otherType, false);
    }
            
    /**
     * @see TypeCompiler#getCorrespondingPrimitiveTypeName
     */

    public String getCorrespondingPrimitiveTypeName() {
        return null;
    }

    /**
     * Get the method name for getting out the corresponding primitive
     * Java type.
     *
     * @return String The method call name for getting the
     *                              corresponding primitive Java type.
     */
    public String getPrimitiveMethodName() {
        return null;
    }

    /**
     * @see TypeCompiler#getCastToCharWidth
     */
    public int getCastToCharWidth(DataTypeDescriptor dtd) {
        TypeId typeId = dtd.getTypeId();
        if (typeId == TypeId.INTERVAL_YEAR_ID) {
            return dtd.getPrecision(); // yyyy
        }
        else if (typeId == TypeId.INTERVAL_MONTH_ID) {
            return dtd.getPrecision(); // mmmm
        }
        else if (typeId == TypeId.INTERVAL_YEAR_MONTH_ID) {
            return dtd.getPrecision() + 1 + 2; // yyyy-mm
        }
        else if (typeId == TypeId.INTERVAL_DAY_ID) {
            return dtd.getPrecision(); // dddd
        }
        else if (typeId == TypeId.INTERVAL_HOUR_ID) {
            return dtd.getPrecision(); // hhhh
        }
        else if (typeId == TypeId.INTERVAL_MINUTE_ID) {
            return dtd.getPrecision(); // mmmm
        }
        else if (typeId == TypeId.INTERVAL_SECOND_ID) {
            if (dtd.getScale() > 0)
                return dtd.getPrecision() + 1 + dtd.getScale(); // ssss.SSSS
            else
                return dtd.getPrecision(); // ssss
        }
        else if (typeId == TypeId.INTERVAL_DAY_HOUR_ID) {
            return dtd.getPrecision() + 1 + 2; // dddd hh
        }
        else if (typeId == TypeId.INTERVAL_DAY_MINUTE_ID) {
            return dtd.getPrecision() + 1 + 2 + 1 + 2; // dddd hh:mm
        }
        else if (typeId == TypeId.INTERVAL_DAY_SECOND_ID) {
            if (dtd.getScale() > 0)
                return dtd.getPrecision() + 1 + 2 + 1 + 2 + 1 + 2 + 1 + dtd.getScale(); // dd hh:mm:ss.SSSS
            else
                return dtd.getPrecision() + 1 + 2 + 1 + 2 + 1 + 2; // dd hh:mm:ss
        }
        else if (typeId == TypeId.INTERVAL_HOUR_MINUTE_ID) {
            return dtd.getPrecision() + 1 + 2; // hhhh:mm
        }
        else if (typeId == TypeId.INTERVAL_HOUR_SECOND_ID) {
            if (dtd.getScale() > 0)
                return dtd.getPrecision() + 1 + 2 + 1 + 2 + 1 + dtd.getScale(); // hhhh:mm:ss.SSSS
            else
                return dtd.getPrecision() + 1 + 2 + 1 + 2; // hhhh:mm:ss
        }
        else if (typeId == TypeId.INTERVAL_MINUTE_SECOND_ID) {
            if (dtd.getScale() > 0)
                return dtd.getPrecision() + 1 + 2 + 1 + dtd.getScale(); // mmmm:ss.SSSS
            else
                return dtd.getPrecision() + 1 + 2; // mmmm:ss
        }
        assert false : "unexpected typeId in getCastToCharWidth() - " + typeId;
        return 0;
    }

    /**
     * @see TypeCompiler#resolveArithmeticOperation
     *
     * @exception StandardException     Thrown on error
     */
    public DataTypeDescriptor resolveArithmeticOperation(DataTypeDescriptor leftType,
                                                         DataTypeDescriptor rightType,
                                                         String operator)
            throws StandardException {
        TypeId rightTypeId = rightType.getTypeId();
        TypeId leftTypeId = leftType.getTypeId();
        boolean nullable = leftType.isNullable() || rightType.isNullable();

        if (operator.equals(PLUS_OP) || operator.equals(MINUS_OP))
        {
            // date/time and interval
            TypeId datetimeType;
            if ((datetimeType = rightTypeId).isDateTimeTimeStampTypeId() && leftTypeId.isIntervalTypeId() ||
                  (datetimeType = leftTypeId).isDateTimeTimeStampTypeID() && rightTypeId.isIntervalTypeId())
                // Let specific datetime type resolve it.
                return getTypeCompiler(datetimeType).resolveArithmeticOperation(rightType, leftType, operator);
        
            // interval and interval
            int typeFormatId = 0;
            if (leftTypeId.isIntervalTypeId() && rightTypeId.isIntervalTypeId())
                // two intervals are exactly the same
                if (leftTypeId == rightTypeId)                    
                    return leftType.getNullabilityType(nullable);
                // two intervals are of the same *type*
                else if ((typeFormatId = leftTypeId.getTypeFormatId()) == rightTypeId.getTypeFormatId())
                    return new DataTypeDescriptor(typeFormatId == TypeId.FormatIds.INTERVAL_DAY_SECOND_ID ?
                                                    TypeId.INTERVAL_SECOND_ID : TypeId.INTERVAL_MONTH_ID,
                                                    nullable);
                        
            // varchar
             DataTypeDescriptor varcharType;
             if ((varcharType = leftType).getTypeId().isStringTypeId() && rightTypeId.isIntervalTypeId()||
                 (varcharType = rightType).getTypeId().isStringTypeId() && leftTypeId.isIntervalTypeId()
                    && operator.equals(PLUS_OP)) // when left is interval, only + is legal
                return new DataTypeDescriptor(varcharType.getPrecision() > 10 ? TypeId.DATETIME_ID : TypeId.DATE_ID, nullable);
        }
        else if (operator.equals(TIMES_OP) || operator.equals(DIVIDE_OP) || operator.equals(DIV_OP))
        {   
            // numeric / varchar and interval
            TypeId intervalId = null;
            if ((intervalId = leftTypeId).isIntervalTypeId() && 
                    (rightTypeId.isNumericTypeId() || rightTypeId.isStringTypeId())||
                (intervalId = rightTypeId).isIntervalTypeId() && 
                    (leftTypeId.isNumericTypeId() || leftTypeId.isStringTypeId()) &&
                    operator.equals(TIMES_OP)) // when right is interval, only * is legal
                return new DataTypeDescriptor(intervalId, nullable);            
        }        

        // Unsupported
        return super.resolveArithmeticOperation(leftType, rightType, operator);
    }

}
