/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */
package org.eclipse.daanse.xmla.api.xmla;

import java.util.List;

public non-sealed interface Backup extends Command {


     ObjectReference object();

     String file();

     String security();

     Boolean applyCompression();

     Boolean allowOverwrite();

     String password();

     Boolean backupRemotePartitions();

     Backup.Locations locations();


    public interface Locations {

         List<LocationBackup> location();
    }

}