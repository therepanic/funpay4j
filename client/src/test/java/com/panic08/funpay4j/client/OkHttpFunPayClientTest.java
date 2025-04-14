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

package com.panic08.funpay4j.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Collections;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.panic08.funpay4j.exceptions.InvalidCsrfTokenOrPHPSESSIDException;
import com.panic08.funpay4j.exceptions.InvalidGoldenKeyException;
import com.panic08.funpay4j.exceptions.offer.OfferAlreadyRaisedException;
import com.panic08.funpay4j.request.SaveOfferRequest;

/**
 * @author panic08
 * @since 1.0.6
 */
class OkHttpFunPayClientTest {
    private OkHttpClient httpClient;
    private MockWebServer mockWebServer;
    private OkHttpFunPayClient client;

    private static final String BASE_URL = "/";

    @BeforeEach
    void setUp() throws IOException {
        this.httpClient = new OkHttpClient();
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();
        this.client =
                new OkHttpFunPayClient(
                        this.httpClient, this.mockWebServer.url(BASE_URL).toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        this.mockWebServer.shutdown();
    }

    @Test
    void testUpdateAvatar() throws Exception {
        String goldenKey = "valid_golden_key";
        byte[] newAvatar = new byte[] {1, 2, 3};

        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        client.updateAvatar(goldenKey, newAvatar);

        assertEquals(1, mockWebServer.getRequestCount());
    }

    @Test
    void testUpdateAvatarInvalidGoldenKey() throws Exception {
        String goldenKey = "invalid_golden_key";
        byte[] newAvatar = new byte[] {1, 2, 3};

        mockWebServer.enqueue(new MockResponse().setResponseCode(403));

        assertThrows(
                InvalidGoldenKeyException.class, () -> client.updateAvatar(goldenKey, newAvatar));
    }

    @Test
    void testRaiseAllOffers() throws Exception {
        String goldenKey = "valid_golden_key";
        long gameId = 41L;
        long lotId = 149L;

        mockWebServer.enqueue(
                new MockResponse().setBody("{\"msg\": \"success\"}").setResponseCode(200));

        client.raiseAllOffers(goldenKey, gameId, lotId);

        assertEquals(1, mockWebServer.getRequestCount());
    }

    @Test
    void testRaiseAllOffersInvalidGoldenKey() throws Exception {
        String goldenKey = "invalid_golden_key";
        long gameId = 41L;
        long lotId = 149L;

        mockWebServer.enqueue(new MockResponse().setResponseCode(403));

        assertThrows(
                InvalidGoldenKeyException.class,
                () -> client.raiseAllOffers(goldenKey, gameId, lotId));
    }

    @Test
    void testRaiseAllOffersAlreadyRaised() throws Exception {
        String goldenKey = "valid_golden_key";
        long gameId = 41L;
        long lotId = 149L;

        mockWebServer.enqueue(
                new MockResponse().setBody("{\"msg\": \"Подождите...\"}").setResponseCode(200));

        assertThrows(
                OfferAlreadyRaisedException.class,
                () -> client.raiseAllOffers(goldenKey, gameId, lotId));
    }

    @Test
    void testSaveOffer() throws Exception {
        String goldenKey = "valid_golden_key";
        String csrfToken = "valid_csrf_token";
        String phpSessId = "valid_phpsessid";
        SaveOfferRequest request =
                SaveOfferRequest.builder()
                        .offerId(33502824L)
                        .nodeId(149L)
                        .isDeleted(false)
                        .isAutoDelivery(true)
                        .isActive(true)
                        .secrets(Collections.singletonList("secret1"))
                        .images(Collections.singletonList(123L))
                        .price(100.0)
                        .amount(10)
                        .summaryRu("Summary RU")
                        .summaryEn("Summary EN")
                        .descRu("Description RU")
                        .descEn("Description EN")
                        .paymentMessageRu("Payment Message RU")
                        .paymentMessageEn("Payment Message EN")
                        .fields(Collections.singletonMap("field1", "value1"))
                        .build();

        mockWebServer.enqueue(new MockResponse().setBody("{\"done\": true}").setResponseCode(200));

        client.saveOffer(goldenKey, csrfToken, phpSessId, request);

        assertEquals(1, mockWebServer.getRequestCount());
    }

    @Test
    void testSaveOfferInvalidGoldenKey() throws Exception {
        String goldenKey = "invalid_golden_key";
        String csrfToken = "valid_csrf_token";
        String phpSessId = "valid_phpsessid";
        SaveOfferRequest request = SaveOfferRequest.builder().build();

        mockWebServer.enqueue(new MockResponse().setResponseCode(403));

        assertThrows(
                InvalidGoldenKeyException.class,
                () -> client.saveOffer(goldenKey, csrfToken, phpSessId, request));
    }

    @Test
    void testSaveOfferInvalidCsrfTokenOrPHPSESSID() throws Exception {
        String goldenKey = "valid_golden_key";
        String csrfToken = "invalid_csrf_token";
        String phpSessId = "invalid_phpsessid";
        SaveOfferRequest request = SaveOfferRequest.builder().build();

        mockWebServer.enqueue(
                new MockResponse()
                        .setBody("{\"msg\": \"Обновите страницу и повторите попытку.\"}")
                        .setResponseCode(400));

        assertThrows(
                InvalidCsrfTokenOrPHPSESSIDException.class,
                () -> client.saveOffer(goldenKey, csrfToken, phpSessId, request));
    }

    @Test
    void testSaveOfferErrorResponse() throws Exception {
        String goldenKey = "valid_golden_key";
        String csrfToken = "valid_csrf_token";
        String phpSessId = "valid_phpsessid";
        SaveOfferRequest request = SaveOfferRequest.builder().build();

        mockWebServer.enqueue(
                new MockResponse()
                        .setBody("{\"done\": false, \"error\": \"some error\", \"errors\": {}}")
                        .setResponseCode(200));

        assertThrows(
                RuntimeException.class,
                () -> client.saveOffer(goldenKey, csrfToken, phpSessId, request));
    }

    @Test
    void testAddOfferImage() throws Exception {
        String goldenKey = "valid_golden_key";
        byte[] image = new byte[] {1, 2, 3}; // Пример байтового массива

        mockWebServer.enqueue(
                new MockResponse().setBody("{\"fileId\": 12345}").setResponseCode(200));

        Long fileId = client.addOfferImage(goldenKey, image);

        assertNotNull(fileId);
        assertEquals(12345L, fileId);
        assertEquals(1, mockWebServer.getRequestCount());
    }

    @Test
    void testAddOfferImageInvalidGoldenKey() throws Exception {
        String goldenKey = "invalid_golden_key";
        byte[] image = new byte[] {1, 2, 3};

        mockWebServer.enqueue(new MockResponse().setResponseCode(403));

        assertThrows(InvalidGoldenKeyException.class, () -> client.addOfferImage(goldenKey, image));
    }
}
