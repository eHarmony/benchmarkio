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

package benchmarkio.consumer;

import java.util.concurrent.Callable;

import org.HdrHistogram.Histogram;

/**
 * Primary interface that defines message consumer structure.
 * Implement this if you want to add a new consumer type.
 */
public interface MessageConsumer extends Callable<Histogram>, AutoCloseable {
}
