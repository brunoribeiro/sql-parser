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

package com.akiban.sql.types;

import com.akiban.sql.StandardException;

/** Character set and collation for character types. */
public final class CharacterTypeAttributes
{
    public static enum CollationDerivation {
        NONE, IMPLICIT, EXPLICIT
    }

    private String characterSet;
    private String collation;
    private CollationDerivation collationDerivation;

    public CharacterTypeAttributes(String characterSet,
                                   String collation, 
                                   CollationDerivation collationDerivation) {
        this.characterSet = characterSet;
        this.collation = collation;
        this.collationDerivation = collationDerivation;
    }

    public String getCharacterSet() {
        return characterSet;
    }

    public String getCollation() {
        return collation;
    }

    public CollationDerivation getCollationDerivation() {
        return collationDerivation;
    }

    public static CharacterTypeAttributes forCharacterSet(String characterSet) {
        return new CharacterTypeAttributes(characterSet, null, null);
    }

    public static CharacterTypeAttributes forCollation(CharacterTypeAttributes base,
                                                       String collation) {
        String characterSet = null;
        if (base != null)
            characterSet = base.characterSet;
        return new CharacterTypeAttributes(characterSet, 
                                           collation, CollationDerivation.EXPLICIT);
    }

    public static CharacterTypeAttributes mergeCollations(CharacterTypeAttributes ta1,
                                                          CharacterTypeAttributes ta2)
            throws StandardException {
        if ((ta1 == null) || (ta1.collationDerivation == null)) {
            return ta2;
        }
        else if ((ta2 == null) || (ta2.collationDerivation == null)) {
            return ta1;
        }
        else if (ta1.collationDerivation == CollationDerivation.EXPLICIT) {
            if (ta2.collationDerivation == CollationDerivation.EXPLICIT) {
                if (!ta1.collation.equals(ta2.collation))
                    throw new StandardException("Incompatible collations: " +
                                                ta1 + " " + ta1.collation + " and " +
                                                ta2 + " " + ta2.collation);
            }
            return ta1;
        }
        else if (ta2.collationDerivation == CollationDerivation.EXPLICIT) {
            return ta2;
        }
        else if ((ta1.collationDerivation == CollationDerivation.IMPLICIT) &&
                 (ta2.collationDerivation == CollationDerivation.IMPLICIT) &&
                 ta1.collation.equals(ta2.collation)) {
            return ta1;
        }
        else {
            return new CharacterTypeAttributes(null, null, CollationDerivation.NONE);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CharacterTypeAttributes)) return false;
        CharacterTypeAttributes other = (CharacterTypeAttributes)o;
        return (((characterSet == null) ?
                 (other.characterSet == null) :
                 characterSet.equals(other.characterSet)) &&
                ((collation == null) ?
                 (other.collation == null) :
                 collation.equals(other.collation)));
    }

    @Override
    public String toString() {
        if ((characterSet == null) && (collation == null)) return "none";
        StringBuilder str = new StringBuilder();
        if (characterSet != null) {
            str.append("CHARACTER SET ");
            str.append(characterSet);
        }
        if (collation != null) {
            if (characterSet != null) str.append(" ");
            str.append("COLLATE ");
            str.append(collation);
        }
        return str.toString();
    }
    
}
