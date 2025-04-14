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

package com.panic08.funpay4j.objects.transaction;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import org.jetbrains.annotations.Nullable;

/**
 * This object represents the FunPay transaction
 *
 * @author panic08
 * @since 1.0.6
 */
@Data
@AllArgsConstructor
@Builder
public class Transaction {
    private long id;

    private String title;

    private double price;

    private TransactionStatus status;

    @Nullable private String paymentNumber;

    private Date date;
}
