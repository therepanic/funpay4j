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

package com.panic08.funpay4j.objects.offer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import com.panic08.funpay4j.objects.user.PreviewSeller;

/**
 * This object represents the FunPay preview offer
 *
 * @author panic08
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@Builder
public class PreviewOffer {
    private long offerId;

    private String shortDescription;

    private double price;

    private boolean isAutoDelivery;

    private boolean isPromo;

    private PreviewSeller seller;
}
