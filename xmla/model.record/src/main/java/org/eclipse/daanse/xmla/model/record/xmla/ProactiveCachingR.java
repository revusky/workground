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
package org.eclipse.daanse.xmla.model.record.xmla;

import java.time.Duration;
import java.util.Optional;

import org.eclipse.daanse.xmla.api.xmla.ProactiveCaching;
import org.eclipse.daanse.xmla.api.xmla.ProactiveCachingBinding;

public record ProactiveCachingR(Optional<String> onlineMode,
                                Optional<String> aggregationStorage,
                                Optional<ProactiveCachingBinding> source,
                                Optional<Duration> silenceInterval,
                                Optional<Duration> latency,
                                Optional<Duration> silenceOverrideInterval,
                                Optional<Duration> forceRebuildInterval,
                                Optional<Boolean> enabled) implements ProactiveCaching {

}
