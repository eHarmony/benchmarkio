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

package benchmarkio.consumer.kafka;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.ConsumerTimeoutException;
import kafka.consumer.KafkaStream;

import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import benchmarkio.benchmark.Histograms;
import benchmarkio.consumer.MessageConsumer;
import benchmarkio.controlcenter.Consts;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

public class BlockingKafkaMessageConsumer implements MessageConsumer {
    private final static Logger logger = LoggerFactory.getLogger(BlockingKafkaMessageConsumer.class);

    private final KafkaStream stream;
    private final Histogram histogram;

    public BlockingKafkaMessageConsumer(final KafkaStream stream) {
        this.stream         = Preconditions.checkNotNull(stream);
        this.histogram      = Histograms.create();
    }

    @Override
    public Histogram call() throws Exception {
        int messageCount = 0;

        try {
            final ConsumerIterator<byte[], byte[]> it = stream.iterator();
            while (it.hasNext()) {
                try {
                    // Start
                    final Stopwatch stopwatch = Stopwatch.createStarted();

                    final byte[] message = it.next().message();

                    messageCount++;

                    // End
                    stopwatch.stop();
                    histogram.recordValue(stopwatch.elapsed(Consts.TIME_UNIT_FOR_REPORTING));
                } catch (final Exception e) {
                    logger.error("Error processing message", e);
                }
            }
        } catch (final ConsumerTimeoutException e) {
            // This is by purpose, hence no error logging.
            logger.info("Consumer was terminated through timeout");
        }

        logger.info("In total consumed {} messages", messageCount);

        return histogram;
    }

    @Override
    public void close() throws Exception {
        // do nothing
    }
}
