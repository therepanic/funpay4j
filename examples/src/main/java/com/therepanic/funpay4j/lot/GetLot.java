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

package com.therepanic.funpay4j.lot;

import com.therepanic.funpay4j.FunPayExecutor;
import com.therepanic.funpay4j.exceptions.FunPayApiException;
import com.therepanic.funpay4j.exceptions.lot.LotNotFoundException;
import com.therepanic.funpay4j.objects.lot.Lot;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * This is an example of how to get lot
 *
 * @author therepanic
 */
public class GetLot {
    public static void main(String[] args) {
        //if we want to use a proxy
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 8000));

        FunPayExecutor executor = new FunPayExecutor(proxy);

        Lot lot;

        try {
            lot = executor.execute(com.therepanic.funpay4j.commands.lot.GetLot.builder()
                    .lotId(81L)
                    .build());

            System.out.println(lot);
        } catch (FunPayApiException e) {
            throw new RuntimeException(e);
        } catch (LotNotFoundException e) {
            System.out.println("The lot with such an id does not found!");
        }
    }
}
