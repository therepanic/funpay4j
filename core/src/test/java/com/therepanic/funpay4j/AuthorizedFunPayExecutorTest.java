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

package com.therepanic.funpay4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.therepanic.funpay4j.commands.offer.CreateOffer;
import com.therepanic.funpay4j.commands.offer.CreateOfferImage;
import com.therepanic.funpay4j.commands.offer.DeleteOffer;
import com.therepanic.funpay4j.commands.offer.EditOffer;
import com.therepanic.funpay4j.commands.offer.RaiseAllOffers;
import com.therepanic.funpay4j.commands.order.GetOrder;
import com.therepanic.funpay4j.commands.transaction.GetTransactions;
import com.therepanic.funpay4j.commands.user.UpdateAvatar;
import com.therepanic.funpay4j.exceptions.InvalidGoldenKeyException;
import com.therepanic.funpay4j.exceptions.offer.OfferAlreadyRaisedException;
import com.therepanic.funpay4j.objects.order.Order;
import com.therepanic.funpay4j.objects.transaction.Transaction;

/**
 * @author therepanic
 * @since 1.0.3
 */
class AuthorizedFunPayExecutorTest {
    private AuthorizedFunPayExecutor funPayExecutor;

    private MockWebServer mockWebServer;

    private static final String GET_CSRF_TOKEN_AND_PHPSESSID_HTML_RESPONSE_PATH =
            "src/test/resources/html/client/getCsrfTokenAndPHPSESSIDResponse.html";
    private static final String GET_TRANSACTIONS_HTML_RESPONSE_PATH =
            "src/test/resources/html/client/getTransactionsResponse.html";
    private static final String GET_ORDER_HTML_RESPONSE_PATH =
            "src/test/resources/html/client/getOrderResponse.html";

    @BeforeEach
    void setUp() throws Exception {
        this.mockWebServer = new MockWebServer();
        this.funPayExecutor =
                new AuthorizedFunPayExecutor("example", this.mockWebServer.url("/").toString());
        this.funPayExecutor.setPHPSESSID("old");
        this.funPayExecutor.setCsrfToken("old");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void testUpdateCsrfTokenAndPHPSESSID() throws Exception {
        String oldCsrfToken = funPayExecutor.getCsrfToken();
        String oldPHPSESSID = funPayExecutor.getPHPSESSID();

        String htmlContent =
                new String(
                        Files.readAllBytes(
                                Paths.get(GET_CSRF_TOKEN_AND_PHPSESSID_HTML_RESPONSE_PATH)));

        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(htmlContent)
                        .setHeader("Set-Cookie", "PHPSESSID=new;")
                        .setResponseCode(200));

        funPayExecutor.updateCsrfTokenAndPHPSESSID();

        assertNotNull(funPayExecutor.getCsrfToken());
        assertNotNull(funPayExecutor.getPHPSESSID());

        assertNotEquals(oldCsrfToken, funPayExecutor.getCsrfToken());
        assertNotEquals(oldPHPSESSID, funPayExecutor.getPHPSESSID());
    }

    @Test
    void testUpdateAvatarInvalidGoldenKeyException() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(403));

        assertThrows(
                InvalidGoldenKeyException.class,
                () -> {
                    funPayExecutor.execute(UpdateAvatar.builder().newAvatar(new byte[] {}).build());
                });
    }

    @Test
    void testRaiseAllOffersInvalidGoldenKeyException() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(403));

        assertThrows(
                InvalidGoldenKeyException.class,
                () -> {
                    funPayExecutor.execute(RaiseAllOffers.builder().lotId(1L).gameId(1L).build());
                });
    }

    @Test
    void testRaiseAllOffersOfferAlreadyRaisedException() throws Exception {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody("{\"msg\":\"Подождите 4 часа.\", \"error\": 1}"));

        assertThrows(
                OfferAlreadyRaisedException.class,
                () -> {
                    funPayExecutor.execute(RaiseAllOffers.builder().lotId(1L).gameId(1L).build());
                });
    }

    @Test
    void testCreateOfferInvalidCsrfTokenOrPHPSESSIDException() throws Exception {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(400)
                        .setBody(
                                "{\"msg\": \"Обновите страницу и повторите попытку.\", \"error\": 1}"));

        String htmlContent =
                new String(
                        Files.readAllBytes(
                                Paths.get(GET_CSRF_TOKEN_AND_PHPSESSID_HTML_RESPONSE_PATH)));

        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(htmlContent)
                        .setHeader("Set-Cookie", "PHPSESSID=new;")
                        .setResponseCode(200));

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"done\":true,\"error\":false,\"errors\":[],\"url\":\"https://funpay.com/lots/210/trade\"}"));

        String currentCsrfToken = funPayExecutor.getCsrfToken();
        String currentPHPSESSID = funPayExecutor.getPHPSESSID();

        funPayExecutor.execute(
                CreateOffer.builder()
                        .lotId(210L)
                        .price(5.0)
                        .amount(5)
                        .shortDescriptionEn("test")
                        .fields(new HashMap<>())
                        .build());

        assertNotNull(funPayExecutor.getCsrfToken());
        assertNotNull(funPayExecutor.getPHPSESSID());
        assertNotEquals(currentCsrfToken, funPayExecutor.getCsrfToken());
        assertNotEquals(currentPHPSESSID, funPayExecutor.getPHPSESSID());
    }

    @Test
    void testEditOfferInvalidCsrfTokenOrPHPSESSIDException() throws Exception {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(400)
                        .setBody(
                                "{\"msg\": \"Обновите страницу и повторите попытку.\", \"error\": 1}"));

        String htmlContent =
                new String(
                        Files.readAllBytes(
                                Paths.get(GET_CSRF_TOKEN_AND_PHPSESSID_HTML_RESPONSE_PATH)));

        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(htmlContent)
                        .setHeader("Set-Cookie", "PHPSESSID=new;")
                        .setResponseCode(200));

        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        String currentCsrfToken = funPayExecutor.getCsrfToken();
        String currentPHPSESSID = funPayExecutor.getPHPSESSID();

        funPayExecutor.execute(
                EditOffer.builder()
                        .lotId(210L)
                        .offerId(210L)
                        .price(5.0)
                        .amount(5)
                        .shortDescriptionEn("test")
                        .fields(new HashMap<>())
                        .build());

        assertNotNull(funPayExecutor.getCsrfToken());
        assertNotNull(funPayExecutor.getPHPSESSID());
        assertNotEquals(currentCsrfToken, funPayExecutor.getCsrfToken());
        assertNotEquals(currentPHPSESSID, funPayExecutor.getPHPSESSID());
    }

    @Test
    void testDeleteOfferInvalidCsrfTokenOrPHPSESSIDException() throws Exception {
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(400)
                        .setBody(
                                "{\"msg\": \"Обновите страницу и повторите попытку.\", \"error\": 1}"));

        String htmlContent =
                new String(
                        Files.readAllBytes(
                                Paths.get(GET_CSRF_TOKEN_AND_PHPSESSID_HTML_RESPONSE_PATH)));

        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(htmlContent)
                        .setHeader("Set-Cookie", "PHPSESSID=new;")
                        .setResponseCode(200));

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(
                                "{\"done\":true,\"error\":false,\"errors\":[],\"url\":\"https://funpay.com/lots/210/trade\"}"));

        String currentCsrfToken = funPayExecutor.getCsrfToken();
        String currentPHPSESSID = funPayExecutor.getPHPSESSID();

        funPayExecutor.execute(DeleteOffer.builder().lotId(210L).offerId(5543534L).build());

        assertNotNull(funPayExecutor.getCsrfToken());
        assertNotNull(funPayExecutor.getPHPSESSID());
        assertNotEquals(currentCsrfToken, funPayExecutor.getCsrfToken());
        assertNotEquals(currentPHPSESSID, funPayExecutor.getPHPSESSID());
    }

    @Test
    void testAddOfferImage() throws Exception {
        Long expectedFileId = 114254551L;

        mockWebServer.enqueue(
                new MockResponse()
                        .setBody("{\"fileId\": " + expectedFileId + "}")
                        .setResponseCode(200));

        Long actualFileId =
                funPayExecutor.execute(CreateOfferImage.builder().image(new byte[] {}).build());

        assertEquals(expectedFileId, actualFileId);
    }

    @Test
    void testGetTransactions() throws Exception {
        String htmlContent =
                new String(Files.readAllBytes(Paths.get(GET_TRANSACTIONS_HTML_RESPONSE_PATH)));
        mockWebServer.enqueue(new MockResponse().setBody(htmlContent).setResponseCode(200));

        long userId = 123L;
        int pages = 1;

        List<Transaction> result =
                funPayExecutor.execute(
                        GetTransactions.builder().userId(userId).pages(pages).build());

        assertNotNull(result);

        Transaction firstTransaction = result.get(0);
        assertEquals(75266034L, firstTransaction.getId());
        assertEquals(-68.52, firstTransaction.getPrice());
        assertNotNull(firstTransaction.getTitle());
        assertNotNull(firstTransaction.getStatus());
        assertNotNull(firstTransaction.getDate());
        assertFalse(firstTransaction.getTitle().isEmpty());
    }

    @Test
    void testGetOrder() throws Exception {
        String orderId = "GFHMZY4Z";

        String htmlContent =
                new String(Files.readAllBytes(Paths.get(GET_ORDER_HTML_RESPONSE_PATH)));

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody(htmlContent)
                        .setHeader("Content-Type", "application/json"));

        Order order = funPayExecutor.execute(GetOrder.builder().orderId(orderId).build());

        assertNotNull(order);
        assertEquals(orderId, order.getId());
        assertNotNull(order.getStatuses());
        assertFalse(order.getStatuses().isEmpty());
        assertNotNull(order.getShortDescription());
        assertNotNull(order.getDetailedDescription());
        assertEquals(90.0, order.getPrice());
        assertNotNull(order.getParams());
        assertNotNull(order.getOther());
    }
}
