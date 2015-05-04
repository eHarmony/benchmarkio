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

package benchmarkio.benchmark;

import org.HdrHistogram.Histogram;

import benchmarkio.controlcenter.Consts;

/**
 * Utility class that provides shortcut methods for
 * histogram initializations.
 */
public final class Histograms {

    /**
     * Creates a new {@link org.HdrHistogram.Histogram} with {@link Consts.HIGHEST_TRACKABLE_VALUE}
     * and {@link Consts.NUMBER_OF_SIGNIFICANT_VALUE_DIGITS} as arguments.
     *
     * @return
     */
    public static Histogram create() {
        return new Histogram(Consts.HIGHEST_TRACKABLE_VALUE, Consts.NUMBER_OF_SIGNIFICANT_VALUE_DIGITS);
    }

    /**
     * The caller references the static methods using <tt>Histograms.create()</tt>,
     * and so on. Thus, the caller should be prevented from constructing objects of
     * this class, by declaring this private constructor.
     */
    private Histograms() {
        //this prevents even the native class from
        //calling this ctor as well :
        throw new AssertionError();
    }
}
