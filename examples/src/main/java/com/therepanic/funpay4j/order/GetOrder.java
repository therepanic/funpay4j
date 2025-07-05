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

package com.therepanic.funpay4j.order;

import com.therepanic.funpay4j.AuthorizedFunPayExecutor;
import com.therepanic.funpay4j.exceptions.FunPayApiException;
import com.therepanic.funpay4j.exceptions.InvalidGoldenKeyException;

import java.net.InetSocketAddress;
import java.net.Proxy;

public class GetOrder {
    public static void main(String[] args) {
        //if we want to use a proxy
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 8000));

        AuthorizedFunPayExecutor executor = new AuthorizedFunPayExecutor("test-golden-key", proxy);

        try {
            System.out.println(executor.execute(com.therepanic.funpay4j.commands.order.GetOrder.builder()
                            .orderId("YOUR ORDER ID")
                    .build()));
        } catch (FunPayApiException e) {
            throw new RuntimeException(e);
        } catch (InvalidGoldenKeyException e) {
            System.out.println("golden key is invalid!");
        }
    }
}
