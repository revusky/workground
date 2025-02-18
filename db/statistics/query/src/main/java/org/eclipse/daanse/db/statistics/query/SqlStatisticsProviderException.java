/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 *
 * Contributors:
 *   SmartCity Jena - major API, docs, code-quality changes
 *   Stefan Bischof (bipolis.org)
 */
package org.eclipse.daanse.db.statistics.query;

public class SqlStatisticsProviderException extends RuntimeException {

    public SqlStatisticsProviderException(Exception e) {
        super(e);
    }
}
