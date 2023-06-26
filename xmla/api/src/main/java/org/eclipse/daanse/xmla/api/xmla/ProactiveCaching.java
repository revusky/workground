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

import java.time.Duration;
import java.util.Optional;

/**
 * The ProactiveCaching complex type represents proactive caching settings for an object.
 */
public interface ProactiveCaching {

    /**
     * @return A string that specifies whether the
     * dimension/partition is brought back
     * online immediately when the rebuilding
     * of the cache is initiated or only when
     * the rebuilding of the cache is complete.
     * default "Immediate"
     */
    Optional<String> onlineMode();

    /**
     * @return A string that specifies the storage
     * method for aggregations. Applies only
     * to partitions. On a dimension, it MUST
     * be "Regular".
     * default "Regular"
     */
    Optional<String> aggregationStorage();

    /**
     * @return The binding of the proactive caching.
     * This regulates the notification
     * mechanisms as well as the processing
     * options.
     * default "ProactiveCachingInheritedBinding"
     */
    Optional<ProactiveCachingBinding> source();

    /**
     * @return The minimum amount of quiet time (in
     * milliseconds) that occurs before the
     * cache rebuild starts. The default value,
     * -1 second, is used to specify an infinite
     * interval.
     */
    Optional<Duration> silenceInterval();

    /**
     * @return The grace period between the earliest
     * notification and the moment when the
     * current cache is dropped. The default
     * value, -1 second, is used to specify an
     * infinite interval.
     */
    Optional<Duration> latency();

    /**
     * @return The amount of time that elapses after
     * an initial notification after which the
     * cache rebuild begins unconditionally.
     * The default value, -1 second, is used to
     * specify an infinite interval.
     */
    Optional<Duration> silenceOverrideInterval();

    /**
     * @return The amount of time that elapses after a
     * cache becomes available after which
     * the cache rebuild begins
     * unconditionally. The default value, -1
     * second, is used to specify an infinite
     * interval.
     */
    Optional<Duration> forceRebuildInterval();

    /**
     * @return When true, specifies that proactive
     * caching is enabled; otherwise, false.
     */
    Optional<Boolean> enabled();
}
