/*
* This software is subject to the terms of the Eclipse Public License v1.0
* Agreement, available at the following URL:
* http://www.eclipse.org/legal/epl-v10.html.
* You must accept the terms of that agreement to use this software.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package mondrian.util;

/**
 * Type-safe value that contains an immutable string. Two instances are
 * the same if they have identical type and contain equal strings.
 */
public abstract class StringKey {
    private String value;

    /** Creates a StringKey. */
    protected StringKey(String value) {
        assert value != null;
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        // Class must be identical (different subclasses of StringHolder not
        // OK).
        return obj != null && obj.getClass() == getClass()
            && value.equals(((StringKey) obj).value);
    }
}
