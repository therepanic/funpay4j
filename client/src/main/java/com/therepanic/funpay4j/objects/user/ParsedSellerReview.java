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

package com.therepanic.funpay4j.objects.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import org.jetbrains.annotations.Nullable;

/**
 * This object represents the parsed FunPay seller review
 *
 * @author therepanic
 * @since 1.0.6
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ParsedSellerReview {
    private String gameTitle;

    private double price;

    private String text;

    private int stars;

    @Nullable private String sellerReplyText;
}
