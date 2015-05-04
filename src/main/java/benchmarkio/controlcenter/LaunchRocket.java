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

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import benchmarkio.benchmark.report.LoggingReport;
import benchmarkio.benchmark.report.Report;
import benchmarkio.consumer.MessageConsumerCoordinator;
import benchmarkio.producer.MessageProducerCoordinator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

/**
 * Primary entry point for launching different benchmarks.
 * Details on launcher commands options can be found in the README.md file.
 */
public class LaunchRocket {
    private final static Logger log = LoggerFactory.getLogger(LaunchRocket.class);

    // create the command line Options.
    private static final Options options = initializeOptions();

    public static void start(final BrokerType brokerType,
                             final BenchmarkType benchmarkType,
                             final boolean durable,
                             final String host,
                             final int port,
                             final int numConsumers,
                             final int numProducers,
                             final long totalNumberOfMessages,
                             final double msgSizeInKB,
                             final Optional<String> optionalZookeeper,
                             final Optional<String> optionalKafkaProducerType) throws Exception {

        final String generatedMessage = createMessage(msgSizeInKB);

        try (
                final MessageConsumerCoordinator messageConsumerCoordinator =
                        brokerType.getMessageConsumerCoordinator(host, port, Consts.DESTINATION_NAME, numConsumers, durable, optionalZookeeper, Optional.of("anyGroupId"));

                final MessageProducerCoordinator messageProducerCoordinator =
                        brokerType.getMessageProducerCoordinator(host, port, Consts.DESTINATION_NAME, generatedMessage, totalNumberOfMessages, numProducers, durable, optionalKafkaProducerType);
            ) {

            // In the future we can have multiple reports chosen
            // from the command line.
            final Report report = new LoggingReport();

            benchmarkType.getBenchmark().run(
                    messageConsumerCoordinator,
                    messageProducerCoordinator,
                    numConsumers,
                    numProducers,
                    totalNumberOfMessages,
                    report);
        }
    }

    @VisibleForTesting
    public static String createMessage(final double msgSizeInKB) {
        final int msgSizeInBytes = (int) (msgSizeInKB * 1024);
        // Java chars are 2 bytes
        final int msgSizeInChars = msgSizeInBytes / 2;
        final String message     = Strings.padStart("", msgSizeInChars, 'a');

        return message;
    }

    @SuppressWarnings("static-access")
    private static Options initializeOptions() {
        final Options options = new Options();

        options.addOption("u", "usage", false, "show usage");

        // Required options
        options.addOption(OptionBuilder.withLongOpt("broker-type")
                .withDescription("broker type => KAFKA | RABBITMQ | ACTIVEMQ")
                .isRequired()
                .hasArg()
                .create());
        options.addOption(OptionBuilder.withLongOpt("host")
                .withDescription("host of the broker")
                .isRequired()
                .hasArg()
                .create());
        options.addOption(OptionBuilder.withLongOpt("port")
                .withDescription("port of the broker")
                .isRequired()
                .hasArg()
                .create());
        options.addOption(OptionBuilder.withLongOpt("num-consumers")
                .withDescription("consumer count")
                .isRequired()
                .hasArg()
                .create());
        options.addOption(OptionBuilder.withLongOpt("num-producers")
                .withDescription("producer count")
                .isRequired()
                .hasArg()
                .create());
        options.addOption(OptionBuilder.withLongOpt("total-number-of-messages")
                .withDescription("total number of messages")
                .isRequired()
                .hasArg()
                .create());
        options.addOption(OptionBuilder.withLongOpt("msg-size-in-kb")
                .withDescription("message size in KB")
                .isRequired()
                .hasArg()
                .create());

        // Optional options
        options.addOption(OptionBuilder.withLongOpt("benchmark-type")
                .withDescription("benchmark type, one of PRODUCER_ONLY | CONSUMER_ONLY | PRODUCER_AND_CONSUMER | PRODUCER_NO_CONSUMER_THEN_CONSUMER. Will default to PRODUCER_AND_CONSUMER, if not specified.")
                .hasArg()
                .create());
        options.addOption(OptionBuilder.withLongOpt("durable")
                .withDescription("boolean value, indicates whether we should test with durability")
                .hasArg()
                .create());
        // Kafka Specific
        options.addOption(OptionBuilder.withLongOpt("zookeeper")
                .withDescription("zookeeperHost:zookeeperPort")
                .hasArg()
                .create());
        options.addOption(OptionBuilder.withLongOpt("kafka-producer-type")
                .withDescription("This parameter specifies whether the messages are sent asynchronously in a background thread. Valid values are (1) async for asynchronous send and (2) sync for synchronous send. By setting the producer to async we allow batching together of requests (which is great for throughput) but open the possibility of a failure of the client machine dropping unsent data. Will default to sync, if not specified.")
                .hasArg()
                .create());

        return options;
    }

    /**
     * Displays help for the command line arguments.
     */
    private static void displayHelp() {
        // This prints out some help
        final HelpFormatter formater = new HelpFormatter();

        formater.printHelp(LaunchRocket.class.getName(), options);
        System.exit(0);
    }

    public static void main(final String[] args) throws Exception {
        // create the parser
        final CommandLineParser parser = new BasicParser();

        // parse the command line arguments
        final CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption("u")) {
            displayHelp();
        }

        final String host               = cmd.getOptionValue("host");
        final int port                  = Integer.parseInt(cmd.getOptionValue("port"));
        final BrokerType brokerType     = BrokerType.valueOf(cmd.getOptionValue("broker-type"));
        final int numConsumers          = Integer.parseInt(cmd.getOptionValue("num-consumers"));
        final int numProducers          = Integer.parseInt(cmd.getOptionValue("num-producers"));
        final int totalNumberOfMessages = Integer.parseInt(cmd.getOptionValue("total-number-of-messages"));
        final double msgSizeInKB        = Double.parseDouble(cmd.getOptionValue("msg-size-in-kb"));

        // Optional options
        final Optional<String> optionalBenchmarkType    = Optional.fromNullable(cmd.getOptionValue("benchmark-type"));
        final Optional<String> optionalDurable          = Optional.fromNullable(cmd.getOptionValue("durable"));
        // Kafka Specific
        final Optional<String> optionalZookeeper        = Optional.fromNullable(cmd.getOptionValue("zookeeper"));
        Optional<String> optionalKafkaProducerType      = Optional.fromNullable(cmd.getOptionValue("kafka-producer-type"));

        BenchmarkType benchmarkType;
        if (optionalBenchmarkType.isPresent()) {
            benchmarkType = BenchmarkType.valueOf(optionalBenchmarkType.get());
        } else {
            log.info("Benchmark type was not specified, defaulting to: {}", BenchmarkType.PRODUCER_AND_CONSUMER);

            benchmarkType = BenchmarkType.PRODUCER_AND_CONSUMER;
        }

        boolean durable = false;
        if (optionalDurable.isPresent()) {
            durable = Boolean.valueOf(optionalDurable.get());
        } else {
            log.info("Durable parameter was not specified, defaulting to: FALSE");
        }

        if (brokerType == BrokerType.KAFKA) {
            if (!optionalZookeeper.isPresent()) {
                log.error("zookeeper is missing, it is a required property for KAFKA broker");

                System.exit(0);
            }

            if (!optionalKafkaProducerType.isPresent()) {
                log.info("kafka-producer-type is not specified, defaulting to sync");

                optionalKafkaProducerType = Optional.of("sync");
            } else if (!optionalKafkaProducerType.get().equals("sync") && !optionalKafkaProducerType.get().equals("async")) {
                log.warn("kafka-producer-type is not one of the accepted sync | async values, defaulting to sync");

                optionalKafkaProducerType = Optional.of("sync");
            }
        }

        log.info("destination (topic or queue): {}",    Consts.DESTINATION_NAME);
        log.info("host: {}",                            host);
        log.info("port: {}",                            port);
        log.info("broker-type: {}",                     brokerType);
        log.info("benchmark-type: {}",                  benchmarkType);
        log.info("durable: {}",                         durable);
        log.info("num-consumers: {}",                   numConsumers);
        log.info("num-producers: {}",                   numProducers);
        log.info("total-number-of-messages: {}",        totalNumberOfMessages);
        log.info("msg-size-in-kb: {}",                  msgSizeInKB);

        if (brokerType == BrokerType.KAFKA) {
            log.info("zookeeper: {}",           optionalZookeeper.get());
            log.info("kafka-producer-type: {}", optionalKafkaProducerType.get());
        }

        LaunchRocket.start(brokerType,
                           benchmarkType,
                           durable,
                           host,
                           port,
                           numConsumers,
                           numProducers,
                           totalNumberOfMessages,
                           msgSizeInKB,
                           optionalZookeeper,
                           optionalKafkaProducerType);

        System.exit(0);
    }
}
