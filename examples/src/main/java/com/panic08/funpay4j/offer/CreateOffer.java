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

package com.panic08.funpay4j.offer;

import com.panic08.funpay4j.commands.offer.CreateOfferImage;
import com.panic08.funpay4j.AuthorizedFunPayExecutor;
import com.panic08.funpay4j.exceptions.FunPayApiException;
import com.panic08.funpay4j.exceptions.InvalidGoldenKeyException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is an example of how to create offer
 *
 * @author panic08
 */
public class CreateOffer {
    public static void main(String[] args) throws IOException {
        //if we want to use a proxy
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 8000));

        AuthorizedFunPayExecutor executor = new AuthorizedFunPayExecutor("test-golden-key", proxy);

        try {
            Long imageId1 = executor.execute(CreateOfferImage.builder()
                    .image(Files.readAllBytes(Paths.get("PATH-TO-IMAGE1")))
                    .build());
            Long imageId2 = executor.execute(CreateOfferImage.builder()
                    .image(Files.readAllBytes(Paths.get("PATH-TO-IMAGE2")))
                    .build());

            List<Long> imageIds = new ArrayList<>();

            imageIds.add(imageId1);
            imageIds.add(imageId2);

            Map<String, String> fields = new HashMap<>();
            fields.put("fields[type]", "Одежда");
            fields.put("fields[hero]", "Abaddon");
            fields.put("fields[rare]", "Common");
            fields.put("fields[quality]", "Inscribed");
            fields.put("fields[method]", "Мгновенно");

            executor.execute(com.panic08.funpay4j.commands.offer.CreateOffer.builder()
                    .lotId(210L)
                    .price(200D)
                    .amount(5)
                    .shortDescriptionEn("Dota 2 Item!")
                    .fields(fields)
                    .imageIds(imageIds)
                    .build());
        } catch (FunPayApiException e) {
            throw new RuntimeException(e);
        } catch (InvalidGoldenKeyException e) {
            System.out.println("golden key is invalid!");
        }
    }
}
