/*
 * Copyright 2015 eHarmony, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package benchmarkio.controlcenter;

import java.util.concurrent.TimeUnit;

/**
 * Class for grouping reusable constants.
 * All members of this class are immutable.
 */
public final class Consts {

    /**
     * The highest value to be tracked by the histogram.
     */
    public static final long HIGHEST_TRACKABLE_VALUE            = 3600000000000L;

    /**
     * Specifies the precision to use.
     */
    public static final int NUMBER_OF_SIGNIFICANT_VALUE_DIGITS  = 5;

    /**
     * Specifies the unit of time to report in histogram.
     */
    public static final TimeUnit TIME_UNIT_FOR_REPORTING = TimeUnit.MILLISECONDS;

    /**
     * Destination name (topic or queue).
     * TODO: Extract into an option.
     */
    public static final String DESTINATION_NAME = "test";

    /**
     * Defines the amount of idle time after which a polling consumer
     * can recycle its connections and shutdown.
     */
    public static final long POLLING_CONSUMER_MAX_IDLE_TIME_MS = 5000;

    /**
     * Zookeeper session timeout.
     * If the consumer fails to heartbeat to zookeeper for this period of time it is considered dead and a rebalance will occur.
     */
    public static final long ZOOKEEPER_SESSION_TIMEOUT_MS = 6000;

    /**
     * How far a ZK follower can be behind a ZK leader.
     */
    public static final long ZOOKEEPER_SYNC_TIME_MS = 200;

    /**
     * The frequency in ms that the consumer offsets are committed to zookeeper.
     */
    public static final long KAFKA_AUTO_COMMIT_INTERVAL_MS = 1000;

    /**
     * The caller references the constants using <tt>Consts.HIGHEST_TRACKABLE_VALUE</tt>,
     * and so on. Thus, the caller should be prevented from constructing objects of
     * this class, by declaring this private constructor.
     */
    private Consts() {
        //this prevents even the native class from
        //calling this ctor as well :
        throw new AssertionError();
    }
}
