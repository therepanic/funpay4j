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

package com.therepanic.funpay4j.parser;

import java.util.List;

import com.therepanic.funpay4j.exceptions.FunPayApiException;
import com.therepanic.funpay4j.exceptions.InvalidGoldenKeyException;
import com.therepanic.funpay4j.exceptions.lot.LotNotFoundException;
import com.therepanic.funpay4j.exceptions.offer.OfferNotFoundException;
import com.therepanic.funpay4j.exceptions.order.OrderNotFoundException;
import com.therepanic.funpay4j.exceptions.user.UserNotFoundException;
import com.therepanic.funpay4j.objects.CsrfTokenAndPHPSESSID;
import com.therepanic.funpay4j.objects.game.ParsedPromoGame;
import com.therepanic.funpay4j.objects.lot.ParsedLot;
import com.therepanic.funpay4j.objects.offer.ParsedOffer;
import com.therepanic.funpay4j.objects.order.ParsedOrder;
import com.therepanic.funpay4j.objects.transaction.ParsedTransaction;
import com.therepanic.funpay4j.objects.transaction.ParsedTransactionType;
import com.therepanic.funpay4j.objects.user.ParsedSellerReview;
import com.therepanic.funpay4j.objects.user.ParsedUser;

/**
 * Interface for parsing data from FunPay
 *
 * @author therepanic
 * @since 1.0.0
 */
public interface FunPayParser {
    /**
     * Parse lot
     *
     * @param lotId lot id by which lot will be parsed
     * @return lot
     * @throws FunPayApiException if the other api-related exception
     * @throws LotNotFoundException if the lot with id does not found
     */
    ParsedLot parseLot(long lotId) throws FunPayApiException, LotNotFoundException;

    /**
     * Parse promo games
     *
     * @param query query by which promo games will be parsed
     * @return promo games
     * @throws FunPayApiException if the other api-related exception
     */
    List<ParsedPromoGame> parsePromoGames(String query) throws FunPayApiException;

    /**
     * Parse offer
     *
     * @param offerId offer id by which offer will be parsed
     * @return offer
     * @throws FunPayApiException if the other api-related exception
     * @throws OfferNotFoundException if the offer with id does not found
     */
    ParsedOffer parseOffer(long offerId) throws FunPayApiException, OfferNotFoundException;

    /**
     * Parse user
     *
     * @param userId user id by which user will be parsed
     * @return user
     * @throws FunPayApiException if the other api-related exception
     * @throws UserNotFoundException if the user with id does not found
     */
    ParsedUser parseUser(long userId) throws FunPayApiException, UserNotFoundException;

    /**
     * Parse user authorized
     *
     * @param goldenKey golden key which will be used to authorize the user
     * @param userId user id by which user will be parsed
     * @return user
     * @throws FunPayApiException if the other api-related exception
     * @throws UserNotFoundException if the user with id does not found
     */
    ParsedUser parseUser(String goldenKey, long userId)
            throws FunPayApiException, UserNotFoundException;

    /**
     * Parse seller reviews
     *
     * @param userId user id by which seller reviews pages will be parsed
     * @param pages number of pages indicating how many seller reviews will be parsed
     * @return sellerReviews
     * @throws FunPayApiException if the other api-related exception
     * @throws UserNotFoundException if the user with id does not found
     * @throws InvalidGoldenKeyException if the golden key is incorrect
     */
    List<ParsedSellerReview> parseSellerReviews(long userId, int pages)
            throws FunPayApiException, UserNotFoundException, InvalidGoldenKeyException;

    /**
     * Parse seller reviews authorized
     *
     * @param goldenKey golden key which will be used to authorize the user
     * @param userId user id by which seller reviews pages will be parsed
     * @param pages number of pages indicating how many seller reviews will be parsed
     * @return sellerReviews
     * @throws FunPayApiException if the other api-related exception
     * @throws UserNotFoundException if the user with id does not found
     * @throws InvalidGoldenKeyException if the golden key is incorrect
     */
    List<ParsedSellerReview> parseSellerReviews(String goldenKey, long userId, int pages)
            throws FunPayApiException, UserNotFoundException, InvalidGoldenKeyException;

    /**
     * Parse seller reviews with stars filter
     *
     * @param userId user id by which seller reviews pages will be parsed
     * @param pages number of pages indicating how many seller reviews will be parsed
     * @param starsFilter number of stars by which the reviews will be parsed
     * @return sellerReviews
     * @throws FunPayApiException if the other api-related exception
     * @throws UserNotFoundException if the user with id does not found/seller
     */
    List<ParsedSellerReview> parseSellerReviews(long userId, int pages, int starsFilter)
            throws FunPayApiException, UserNotFoundException;

    /**
     * Parse seller reviews with stars filter authorized
     *
     * @param goldenKey golden key which will be used to authorize the user
     * @param userId user id by which seller reviews pages will be parsed
     * @param pages number of pages indicating how many seller reviews will be parsed
     * @param starsFilter number of stars by which the reviews will be parsed
     * @return sellerReviews
     * @throws FunPayApiException if the other api-related exception
     * @throws UserNotFoundException if the user with id does not found/seller
     */
    List<ParsedSellerReview> parseSellerReviews(
            String goldenKey, long userId, int pages, int starsFilter)
            throws FunPayApiException, UserNotFoundException;

    /**
     * Parse transactions with type authorized
     *
     * @param goldenKey golden key which will be used to authorize the user
     * @param userId user id by which transactions pages will be parsed
     * @param type type of transaction will be parsed
     * @param pages number of pages indicating how many transactions will be parsed
     * @return transactions
     * @throws FunPayApiException if the other api-related exception
     * @throws UserNotFoundException if the user with id does not found/seller
     * @throws InvalidGoldenKeyException if the golden key is incorrect
     */
    List<ParsedTransaction> parseTransactions(
            String goldenKey, long userId, ParsedTransactionType type, int pages)
            throws FunPayApiException, UserNotFoundException, InvalidGoldenKeyException;

    /**
     * Parse transactions authorized
     *
     * @param goldenKey golden key which will be used to authorize the user
     * @param userId user id by which transactions pages will be parsed
     * @param pages number of pages indicating how many transactions will be parsed
     * @return transactions
     * @throws FunPayApiException if the other api-related exception
     * @throws UserNotFoundException if the user with id does not found/seller
     * @throws InvalidGoldenKeyException if the golden key is incorrect
     */
    List<ParsedTransaction> parseTransactions(String goldenKey, long userId, int pages)
            throws FunPayApiException, UserNotFoundException, InvalidGoldenKeyException;

    /**
     * Parse order authorized
     *
     * @param goldenKey golden key which will be used to authorize the user
     * @param orderId order id by which order will be parsed
     * @return order
     * @throws FunPayApiException if the other api-related exception
     * @throws OrderNotFoundException if the order with id does not found
     */
    ParsedOrder parseOrder(String goldenKey, String orderId)
            throws FunPayApiException, OrderNotFoundException;

    /**
     * Parse csrf-token and PHPSESSID
     *
     * @param goldenKey golden key which will be used to authorize the user
     * @return csrf-token and PHPSESSID
     * @throws FunPayApiException if the other api-related exception
     */
    CsrfTokenAndPHPSESSID parseCsrfTokenAndPHPSESSID(String goldenKey) throws FunPayApiException;
}
