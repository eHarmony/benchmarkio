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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests {@link benchmarkio.controlcenter.LaunchRocket}.
 */
public class LaunchRocketTest {

    @Test
    public void createMessage() {
        // GIVEN
        final double msgSizeInKB    = 1.3;
        // Java chars are 2 bytes
        final int expectedCharCount = (int) (msgSizeInKB * 1024 / 2);

        // WHEN
        final String actualMessage  = LaunchRocket.createMessage(msgSizeInKB);
        final int actualCharCount   = actualMessage.length();

        // THEN
        assertEquals(expectedCharCount, actualCharCount);
    }
}
