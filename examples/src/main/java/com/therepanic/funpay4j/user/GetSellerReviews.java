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

package com.therepanic.funpay4j.user;

import com.therepanic.funpay4j.AuthorizedFunPayExecutor;
import com.therepanic.funpay4j.exceptions.FunPayApiException;
import com.therepanic.funpay4j.exceptions.user.UserNotFoundException;
import com.therepanic.funpay4j.objects.user.AdvancedSellerReview;
import com.therepanic.funpay4j.objects.user.SellerReview;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

/**
 * This is an example of how to get seller reviews
 *
 * @author therepanic
 */
public class GetSellerReviews {
    public static void main(String[] args) {
        //if we want to use a proxy
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 8000));

        AuthorizedFunPayExecutor authorizedExecutor = new AuthorizedFunPayExecutor("test-golden-key", proxy);

        List<SellerReview> sellerReviews;

        try {
            sellerReviews = authorizedExecutor.execute(com.therepanic.funpay4j.commands.user.GetSellerReviews.builder()
                    .pages(2)
                    .userId(1940073L)
                    .starsFilter(null)
                    .build());

            for (SellerReview sellerReview : sellerReviews) {
                //if userId matches the userId from where goldenKey is taken from
                if (sellerReview instanceof AdvancedSellerReview advancedSellerReview) {
                    System.out.println(advancedSellerReview);
                }
            }

            System.out.println(sellerReviews);
        } catch (FunPayApiException e) {
            throw new RuntimeException(e);
        } catch (UserNotFoundException e) {
            System.out.println("The user with such an id does not found/seller!");
        }
    }
}
