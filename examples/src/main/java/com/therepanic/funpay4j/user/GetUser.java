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

import com.therepanic.funpay4j.FunPayExecutor;
import com.therepanic.funpay4j.exceptions.FunPayApiException;
import com.therepanic.funpay4j.exceptions.user.UserNotFoundException;
import com.therepanic.funpay4j.objects.user.Seller;
import com.therepanic.funpay4j.objects.user.User;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * This is an example of how to get user
 *
 * @author therepanic
 */
public class GetUser {
    public static void main(String[] args) {
        //if we want to use a proxy
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 8000));

        FunPayExecutor executor = new FunPayExecutor(proxy);

        User user;

        try {
            user = executor.execute(com.therepanic.funpay4j.commands.user.GetUser.builder()
                    .userId(1940073L)
                    .build());

            System.out.println(user);

            if (user instanceof Seller) {
                //сhecking whether the user is also a Seller

                Seller seller = (Seller) user;
            }
        } catch (FunPayApiException e) {
            throw new RuntimeException(e);
        } catch (UserNotFoundException e) {
            System.out.println("The user with such an id does not found!");
        }
    }
}
