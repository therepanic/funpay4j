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

package com.panic08.funpay4j.user;

import com.panic08.funpay4j.AuthorizedFunPayExecutor;
import com.panic08.funpay4j.exceptions.FunPayApiException;
import com.panic08.funpay4j.exceptions.InvalidGoldenKeyException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This is an example of how to update user avatar
 *
 * @author panic08
 */
public class UpdateAvatar {
    public static void main(String[] args) throws IOException {
        //if we want to use a proxy
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 8000));

        AuthorizedFunPayExecutor authorizedExecutor = new AuthorizedFunPayExecutor("test-golden-key", proxy);

        try {
            authorizedExecutor.execute(com.panic08.funpay4j.commands.user.UpdateAvatar.builder()
                    .newAvatar(Files.readAllBytes(Paths.get("PATH-TO-IMAGE")))
                    .build());
        } catch (FunPayApiException e) {
            throw new RuntimeException(e);
        } catch (InvalidGoldenKeyException e) {
            System.out.println("golden key is invalid!");
        }
    }
}
