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

package benchmarkio.benchmark.report;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import benchmarkio.benchmark.Histograms;
import benchmarkio.controlcenter.Consts;

import com.google.common.base.Stopwatch;

/**
 * Report implementation that is logging to the logger.
 */
public class LoggingReport implements Report {

    private final static Logger log = LoggerFactory.getLogger(LoggingReport.class);

    @Override
    public void aggregateAndPrintResults(final CoordinatorType coordinatorType,
                                         final CompletionService<Histogram> executorCompletionService,
                                         final int numTasks,
                                         final long totalNumberOfMessages,
                                         final Stopwatch stopwatch) {

        // Used to accumulate results from all histograms.
        final Histogram totalHistogram = Histograms.create();

        for (int i = 0; i < numTasks; i++) {
            try {
                final Future<Histogram> histogramFuture = executorCompletionService.take();
                totalHistogram.add(histogramFuture.get());
            } catch (final InterruptedException e) {
                log.error("Failed to retrieve data, got inturrupt signal", e);
                Thread.currentThread().interrupt();

                break;
            } catch (final ExecutionException e) {
                log.error("Failed to retrieve data", e);
            }
        }

        stopwatch.stop();
        final long durationInSeconds    = stopwatch.elapsed(TimeUnit.SECONDS);
        final long durationInMs         = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        // Using the durationInMs, since I would loose precision when using durationInSeconds.
        final long throughputPerSecond  = 1000 * totalNumberOfMessages / durationInMs;

        final long min              = totalHistogram.getMinValue();
        final double percentile25   = totalHistogram.getPercentileAtOrBelowValue(25);
        final double percentile50   = totalHistogram.getPercentileAtOrBelowValue(50);
        final double percentile75   = totalHistogram.getPercentileAtOrBelowValue(75);
        final double percentile99   = totalHistogram.getPercentileAtOrBelowValue(99);
        final long max              = totalHistogram.getMaxValue();
        final double mean           = totalHistogram.getMean();
        final double stdDev         = totalHistogram.getStdDeviation();

        log.info("=======================================");
        if (coordinatorType == CoordinatorType.PRODUCER) {
            log.info("PRODUCER STATS");
        } else {
            log.info("CONSUMER STATS");
        }
        log.info("=======================================");
        log.info("All units are {} unless stated otherwise", Consts.TIME_UNIT_FOR_REPORTING);
        log.info("DURATION (SECOND):      {}", durationInSeconds);
        log.info("THROUGHPUT / SECOND:    {}", throughputPerSecond);
        log.info("MIN:                    {}", min);
        log.info("25th percentile:        {}", percentile25);
        log.info("50th percentile:        {}", percentile50);
        log.info("75th percentile:        {}", percentile75);
        log.info("99th percentile:        {}", percentile99);
        log.info("MAX:                    {}", max);
        log.info("MEAN:                   {}", mean);
        log.info("STD DEVIATION:          {}", stdDev);
        log.info("\n\n\n");
    }

}
