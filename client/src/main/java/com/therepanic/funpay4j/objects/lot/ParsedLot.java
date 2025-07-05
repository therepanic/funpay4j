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

package com.therepanic.funpay4j.objects.lot;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import com.therepanic.funpay4j.objects.offer.ParsedPreviewOffer;

/**
 * This object represents the parsed FunPay lot
 *
 * @author therepanic
 * @since 1.0.6
 */
@Data
@AllArgsConstructor
@Builder
public class ParsedLot {
    private long id;

    private long gameId;

    private String title;

    private String description;

    private List<ParsedLotCounter> lotCounters;

    private List<ParsedPreviewOffer> previewOffers;
}
