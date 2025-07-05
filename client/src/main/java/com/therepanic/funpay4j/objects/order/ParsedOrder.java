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

package com.therepanic.funpay4j.objects.order;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import com.therepanic.funpay4j.objects.user.ParsedPreviewUser;

/**
 * This object represents the parsed FunPay order
 *
 * @author therepanic
 * @since 1.0.7
 */
@Data
@AllArgsConstructor
@Builder
public class ParsedOrder {

    private String id;

    private List<String> statuses;

    private String shortDescription;

    private String detailedDescription;

    private Map<String, String> params;

    private double price;

    private ParsedPreviewUser other;
}
