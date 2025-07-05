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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.therepanic.funpay4j.commands.game.GetPromoGames;
import com.therepanic.funpay4j.commands.lot.GetLot;
import com.therepanic.funpay4j.commands.offer.GetOffer;
import com.therepanic.funpay4j.commands.user.GetSellerReviews;
import com.therepanic.funpay4j.commands.user.GetUser;
import com.therepanic.funpay4j.objects.game.PromoGame;
import com.therepanic.funpay4j.objects.lot.Lot;
import com.therepanic.funpay4j.objects.offer.Offer;
import com.therepanic.funpay4j.objects.user.AdvancedSellerReview;
import com.therepanic.funpay4j.objects.user.Seller;
import com.therepanic.funpay4j.objects.user.SellerReview;

/**
 * @author therepanic
 * @since 1.0.0
 */
class FunPayExecutorTest {
    private FunPayExecutor funPayExecutor;

    private MockWebServer mockWebServer;

    private static final String GET_USER_HTML_RESPONSE_PATH =
            "src/test/resources/html/client/getUserResponse.html";
    private static final String GET_LOT_HTML_RESPONSE_PATH =
            "src/test/resources/html/client/getLotResponse.html";
    private static final String GET_OFFER_HTML_RESPONSE_PATH =
            "src/test/resources/html/client/getOfferResponse.html";
    private static final String GET_SELLER_REVIEWS_HTML_RESPONSE_PATH =
            "src/test/resources/html/client/getSellerReviewsResponse.html";
    private static final String GET_PROMO_GAMES_JSON_RESPONSE_PATH =
            "src/test/resources/json/client/getPromoGamesResponse.json";

    @BeforeEach
    void setUp() {
        this.mockWebServer = new MockWebServer();
        this.funPayExecutor = new FunPayExecutor(this.mockWebServer.url("/").toString());
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void testGetLot() throws Exception {
        String htmlContent = new String(Files.readAllBytes(Paths.get(GET_LOT_HTML_RESPONSE_PATH)));

        mockWebServer.enqueue(new MockResponse().setBody(htmlContent).setResponseCode(200));

        Lot result = funPayExecutor.execute(GetLot.builder().lotId(149L).build());

        assertNotNull(result);
        assertFalse(result.getPreviewOffers().isEmpty());
        assertFalse(result.getLotCounters().isEmpty());
        assertEquals(result.getGameId(), 41);
    }

    @Test
    void testGetPromoGames() throws Exception {
        String jsonContent =
                new String(Files.readAllBytes(Paths.get(GET_PROMO_GAMES_JSON_RESPONSE_PATH)));

        mockWebServer.enqueue(new MockResponse().setBody(jsonContent).setResponseCode(200));

        List<PromoGame> result =
                funPayExecutor.execute(GetPromoGames.builder().query("dota").build());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertFalse(result.get(0).getPromoGameCounters().isEmpty());
    }

    @Test
    void testGetOffer() throws Exception {
        String htmlContent =
                new String(Files.readAllBytes(Paths.get(GET_OFFER_HTML_RESPONSE_PATH)));

        mockWebServer.enqueue(new MockResponse().setBody(htmlContent).setResponseCode(200));

        Offer result = funPayExecutor.execute(GetOffer.builder().offerId(33502824L).build());

        assertNotNull(result);
        assertTrue(result.isAutoDelivery());
        assertFalse(result.getAttachmentLinks().isEmpty());
        assertFalse(result.getParameters().isEmpty());
        assertNotNull(result.getSeller());
        assertTrue(result.getSeller().isOnline());
    }

    @Test
    void testGetUser() throws Exception {
        String htmlContent = new String(Files.readAllBytes(Paths.get(GET_USER_HTML_RESPONSE_PATH)));

        mockWebServer.enqueue(new MockResponse().setBody(htmlContent).setResponseCode(200));

        Seller result = (Seller) funPayExecutor.execute(GetUser.builder().userId(2L).build());

        assertNotNull(result);
        assertNotNull(result.getRegisteredAt());
        assertFalse(result.isOnline());
        assertFalse(result.getBadges().isEmpty());
        assertFalse(result.getLastReviews().isEmpty());
        assertFalse(result.getPreviewOffers().isEmpty());
    }

    @Test
    void testGetSellerReviews() throws Exception {
        String htmlContent =
                new String(Files.readAllBytes(Paths.get(GET_SELLER_REVIEWS_HTML_RESPONSE_PATH)));

        mockWebServer.enqueue(new MockResponse().setBody(htmlContent).setResponseCode(200));

        List<SellerReview> result =
                funPayExecutor.execute(GetSellerReviews.builder().pages(1).userId(2L).build());

        assertEquals(2, result.size());

        AdvancedSellerReview firstSellerReview = (AdvancedSellerReview) result.get(0);
        assertNotNull(firstSellerReview.getSenderUsername());
        assertNotNull(firstSellerReview.getOrderId());
        assertNull(firstSellerReview.getSenderAvatarLink());
        assertNotNull(firstSellerReview.getText());
        assertNotNull(firstSellerReview.getGameTitle());
        assertNotNull(firstSellerReview.getCreatedAt());

        assertNull(firstSellerReview.getSellerReplyText());

        SellerReview secondSellerReview = result.get(1);
        assertTrue(
                secondSellerReview.getSellerReplyText() != null
                        && !secondSellerReview.getSellerReplyText().isEmpty());
    }
}
