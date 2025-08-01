/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.therepanic.funpay4j.exceptions.lot;

/**
 * Base class for exception related to the fact that the lot is not found
 *
 * @author therepanic
 * @since 1.0.3
 */
public class LotNotFoundException extends RuntimeException {
    /**
     * Initializes a new LotNotFoundException exception
     *
     * @param message exception message
     */
    public LotNotFoundException(String message) {
        super(message);
    }
}
