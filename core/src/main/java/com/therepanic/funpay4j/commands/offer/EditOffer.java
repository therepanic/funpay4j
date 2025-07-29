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

package com.therepanic.funpay4j.commands.offer;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import org.jspecify.annotations.Nullable;

/**
 * Use this command to edit offer
 *
 * @author therepanic
 * @since 1.0.4
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class EditOffer {
    private Long lotId;

    private Long offerId;

    @Nullable private String shortDescriptionRu;

    private String shortDescriptionEn;

    @Nullable private String descriptionRu;

    @Nullable private String descriptionEn;

    @Nullable private String paymentMessageRu;

    @Nullable private String paymentMessageEn;

    @Nullable private Map<String, String> fields;

    private boolean isAutoDelivery;

    private boolean isActive;

    @Nullable private List<String> secrets;

    @Nullable private List<Long> imageIds;

    private Double price;

    private Integer amount;
}
