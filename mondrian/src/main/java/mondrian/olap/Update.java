/*
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* History:
*  This files came from the mondrian project. Some of the Flies
*  (mostly the Tests) did not have License Header.
*  But the Project is EPL Header. 2002-2022 Hitachi Vantara.
*
* Contributors:
*   Hitachi Vantara.
*   SmartCity Jena - initial  Java 8, Junit5
*/
// Copyright (c) 2021 Sergei Semenkov.  All rights reserved.

package mondrian.olap;

import java.io.PrintWriter;
import java.util.List;

public class Update extends QueryPart {
    private final String cubeName;
    private List<UpdateClause> updateClauses;

    Update(String cubeName, List<UpdateClause> updateClauses)
    {
        this.cubeName = cubeName;
        this.updateClauses = updateClauses;
    }

    @Override
    public void unparse(PrintWriter pw) {
        pw.print(new StringBuilder("UPDATE CUBE [").append(cubeName).append("]").toString());
    }

    @Override
    public Object[] getChildren() {
        return new Object[] {cubeName};
    }

    public String getCubeName() {
        return cubeName;
    }

    public List<UpdateClause> getUpdateClauses() {
        return this.updateClauses;
    }

    public enum Allocation {
        NO_ALLOCATION,
        USE_EQUAL_ALLOCATION,
        USE_EQUAL_INCREMENT,
        USE_WEIGHTED_ALLOCATION,
        USE_WEIGHTED_INCREMENT
    }

    public static class UpdateClause extends QueryPart {
        private final Exp tuple;
        private Exp value;
        private Allocation allocation;
        private Exp weight;

        public UpdateClause(Exp tuple, Exp value, Allocation allocation, Exp weight) {
            this.tuple = tuple;
            this.value = value;
            this.allocation = allocation;
            this.weight = weight;
        }

        public Exp getTupleExp() {
            return this.tuple;
        }

        public Exp getValueExp() {
            return this.value;
        }
    }
}

