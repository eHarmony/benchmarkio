[![Build Status](https://travis-ci.org/eHarmony/benchmarkio.svg?branch=master)](https://travis-ci.org/eHarmony/benchmarkio)

benchmarkio
===========

Library that contains common code for benchmarking different messaging brokers.
It was designed with idea in mind that it could be extended easily to add more broker implementations.
It uses A High Dynamic Range (HDR) Histogram for recording and analyzing data: https://github.com/HdrHistogram

Below are the steps needed to start it up locally:

*mvn clean install -U*


**Example for KAFKA:**

<pre>
java -cp target/benchmarkio-1.0.0-SNAPSHOT.jar benchmarkio.controlcenter.LaunchRocket
--host localhost --port 9092 --broker-type KAFKA --num-consumers 1 --num-producers 1
--total-number-of-messages 500000 --msg-size-in-kb 1 --zookeeper localhost:2181
</pre>

**Example for RabbitMQ:**

<pre>
java -cp target/benchmarkio-1.0.0-SNAPSHOT.jar benchmarkio.controlcenter.LaunchRocket
--host localhost --port 5672 --broker-type RABBITMQ --num-consumers 1 --num-producers 1
--total-number-of-messages 500000 --msg-size-in-kb 1
</pre>

**Example for ActiveMQ:**

<pre>
java -cp target/benchmarkio-1.0.0-SNAPSHOT.jar benchmarkio.controlcenter.LaunchRocket
--host localhost --port 61616 --broker-type ACTIVEMQ --num-consumers 1 --num-producers 1
--total-number-of-messages 500000 --msg-size-in-kb 1
</pre>

**USAGE**
<pre>
usage: benchmarkio.controlcenter.LaunchRocket
    --benchmark-type <arg>             benchmark type, one of
                                       PRODUCER_ONLY | CONSUMER_ONLY |
                                       PRODUCER_AND_CONSUMER |
                                       PRODUCER_NO_CONSUMER_THEN_CONSUMER.
                                       Will default to
                                       PRODUCER_AND_CONSUMER, if not
                                       specified.
    --broker-type                      broker type => KAFKA | RABBITMQ |
                                       ACTIVEMQ
    --durable <arg>                    boolean value, indicates whether we
                                       should test with durability
    --host <arg>                       host of the broker
    --kafka-producer-type <arg>        This parameter specifies whether
                                       the messages are sent
                                       asynchronously in a background
                                       thread. Valid values are (1) async
                                       for asynchronous send and (2) sync
                                       for synchronous send. By setting
                                       the producer to async we allow
                                       batching together of requests
                                       (which is great for throughput) but
                                       open the possibility of a failure
                                       of the client machine dropping
                                       unsent data. Will default to sync,
                                       if not specified.
    --msg-size-in-kb <arg>             message size in KB
    --num-consumers <arg>              consumer count
    --num-producers <arg>              producer count
    --port <arg>                       port of the broker
    --total-number-of-messages <arg>   total number of messages
 -u,--usage                            show usage
    --zookeeper <arg>                  zookeeperHost:zookeeperPort
</pre>

**SAMPLE OUTPUT AFTER RUNNING**
<pre>
[b.c.LaunchRocket] - Durable parameter was not specified, defaulting to: FALSE
[b.c.LaunchRocket] - destination (topic or queue): test
[b.c.LaunchRocket] - host: localhost
[b.c.LaunchRocket] - port: 5672
[b.c.LaunchRocket] - broker-type: RABBITMQ
[b.c.LaunchRocket] - benchmark-type: PRODUCER_AND_CONSUMER
[b.c.LaunchRocket] - durable: false
[b.c.LaunchRocket] - num-consumers: 3
[b.c.LaunchRocket] - num-producers: 3
[b.c.LaunchRocket] - total-number-of-messages: 100000
[b.c.LaunchRocket] - msg-size-in-kb: 1.0

[b.b.r.LoggingReport] - =======================================
[b.b.r.LoggingReport] - PRODUCER STATS
[b.b.r.LoggingReport] - =======================================
[b.b.r.LoggingReport] - All units are MILLISECONDS unless stated otherwise
[b.b.r.LoggingReport] - DURATION (SECOND):      8
[b.b.r.LoggingReport] - THROUGHPUT / SECOND:    11294
[b.b.r.LoggingReport] - MIN:                    0
[b.b.r.LoggingReport] - 25th percentile:        99.9129991299913
[b.b.r.LoggingReport] - 50th percentile:        99.9129991299913
[b.b.r.LoggingReport] - 75th percentile:        99.9129991299913
[b.b.r.LoggingReport] - 99th percentile:        99.9159991599916
[b.b.r.LoggingReport] - MAX:                    492
[b.b.r.LoggingReport] - MEAN:                   0.24287242872428724
[b.b.r.LoggingReport] - STD DEVIATION:          8.475568531128456

[b.b.r.LoggingReport] - =======================================
[b.b.r.LoggingReport] - CONSUMER STATS
[b.b.r.LoggingReport] - =======================================
[b.b.r.LoggingReport] - All units are MILLISECONDS unless stated otherwise
[b.b.r.LoggingReport] - DURATION (SECOND):      14
[b.b.r.LoggingReport] - THROUGHPUT / SECOND:    7104
[b.b.r.LoggingReport] - MIN:                    0
[b.b.r.LoggingReport] - 25th percentile:        99.9979999799998
[b.b.r.LoggingReport] - 50th percentile:        99.9989999899999
[b.b.r.LoggingReport] - 75th percentile:        99.9989999899999
[b.b.r.LoggingReport] - 99th percentile:        100.0
[b.b.r.LoggingReport] - MAX:                    86
[b.b.r.LoggingReport] - MEAN:                   0.003713370467038004
[b.b.r.LoggingReport] - STD DEVIATION:          0.3240070896707981
</pre>

**WANT TO CONTRIBUTE?**
---

Here is the structure of the classes:

* For new Benchmark type
    * Implement **benchmarkio.benchmark.Benchmark**
    * Add to **benchmarkio.controlcenter.BenchmarkType**
* For new Consumer type
    * Implement **benchmarkio.consumer.MessageConsumerCoordinator**
    * Implement **benchmarkio.consumer.MessageConsumer**
* For new Producer type
    * Implement **benchmarkio.producer.MessageProducerCoordinator**
    * Implement **benchmarkio.producer.MessageProducer**
* To connect new Broker type (you can do it after you have created the Consumer and Producer code)
    * Add to **benchmarkio.controlcenter.BrokerType**
* For new Report type
    * Implement **benchmarkio.benchmark.report.Report**
    * Modify **benchmarkio.controlcenter.LaunchRocket.start(...)** to use new report

