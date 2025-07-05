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

package com.therepanic.funpay4j.offer;

import com.therepanic.funpay4j.FunPayExecutor;
import com.therepanic.funpay4j.exceptions.FunPayApiException;
import com.therepanic.funpay4j.exceptions.offer.OfferNotFoundException;
import com.therepanic.funpay4j.objects.offer.Offer;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * This is an example of how to get offer
 *
 * @author therepanic
 */
public class GetOffer {
    public static void main(String[] args) {
        //if we want to use a proxy
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 8000));

        FunPayExecutor executor = new FunPayExecutor(proxy);

        Offer offer;

        try {
            offer = executor.execute(com.therepanic.funpay4j.commands.offer.GetOffer.builder()
                    .offerId(26021761L)
                    .build());

            System.out.println(offer);
        } catch (FunPayApiException e) {
            throw new RuntimeException(e);
        } catch (OfferNotFoundException e) {
            System.out.println("The offer with such an id does not found!");
        }
    }
}
