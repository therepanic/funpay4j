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

package com.therepanic.funpay4j.request;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

/**
 * This object represents the shell for sending a request to save an offer
 *
 * @author therepanic
 * @since 1.0.4
 */
@Getter
@Builder
public class SaveOfferRequest {
    private Long offerId;

    private Long nodeId;

    private String summaryRu;

    private String summaryEn;

    private String descRu;

    private String descEn;

    private String paymentMessageRu;

    private String paymentMessageEn;

    private Map<String, String> fields;

    private boolean isAutoDelivery;

    private boolean isActive;

    private boolean isDeleted;

    private List<String> secrets;

    private List<Long> images;

    private Double price;

    private Integer amount;
}
