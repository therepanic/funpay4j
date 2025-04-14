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

package com.panic08.funpay4j.transaction;

import com.panic08.funpay4j.exceptions.FunPayApiException;
import com.panic08.funpay4j.exceptions.InvalidGoldenKeyException;
import com.panic08.funpay4j.AuthorizedFunPayExecutor;
import com.panic08.funpay4j.objects.transaction.TransactionType;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * This is an example of how to get transactions
 *
 * @author panic08
 */
public class GetTransactions {
    public static void main(String[] args) {
        //if we want to use a proxy
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 8000));

        AuthorizedFunPayExecutor executor = new AuthorizedFunPayExecutor("test-golden-key", proxy);


        try {
            System.out.println(executor.execute(com.panic08.funpay4j.commands.transaction.GetTransactions.builder()
                            .pages(2)
                            .userId(1940073L)
                            .type(TransactionType.WITHDRAW)
                    .build()));
        } catch (FunPayApiException e) {
            throw new RuntimeException(e);
        } catch (InvalidGoldenKeyException e) {
            System.out.println("golden key is invalid!");
        }
    }
}
