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

package com.panic08.funpay4j.game;

import com.panic08.funpay4j.FunPayExecutor;
import com.panic08.funpay4j.exceptions.FunPayApiException;
import com.panic08.funpay4j.objects.game.PromoGame;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

/**
 * This is an example of how to get promo games
 *
 * @author panic08
 */
public class GetPromoGames {
    public static void main(String[] args) {
        //if we want to use a proxy
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 8000));

        FunPayExecutor executor = new FunPayExecutor(proxy);

        List<PromoGame> promoGames;

        try {
            promoGames = executor.execute(com.panic08.funpay4j.commands.game.GetPromoGames.builder()
                    .query("dota")
                    .build());

            System.out.println(promoGames);
        } catch (FunPayApiException e) {
            throw new RuntimeException(e);
        }
    }
}
