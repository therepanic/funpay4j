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

package com.therepanic.funpay4j.client;

import com.therepanic.funpay4j.exceptions.FunPayApiException;
import com.therepanic.funpay4j.exceptions.InvalidCsrfTokenOrPHPSESSIDException;
import com.therepanic.funpay4j.exceptions.InvalidGoldenKeyException;
import com.therepanic.funpay4j.exceptions.offer.OfferAlreadyRaisedException;
import com.therepanic.funpay4j.request.SaveOfferRequest;

/**
 * Interface for sending thematic requests to FunPay
 *
 * @author therepanic
 * @since 1.0.3
 */
public interface FunPayClient {
    /**
     * Send a request to update avatar
     *
     * @param goldenKey golden key which will be used to authorize the user
     * @param newAvatar avatar to be updated
     * @throws FunPayApiException if the other api-related exception
     * @throws InvalidGoldenKeyException if the golden key is invalid
     */
    void updateAvatar(String goldenKey, byte[] newAvatar)
            throws FunPayApiException, InvalidGoldenKeyException;

    /**
     * Send a request to raise all offers
     *
     * @param goldenKey golden key which will be used to authorize the user
     * @param gameId game id for which offers will be raised
     * @param lotId lot id for which offers will be raised
     * @throws FunPayApiException if the other api-related exception
     * @throws InvalidGoldenKeyException if the golden key is invalid
     * @throws OfferAlreadyRaisedException if the offer already raised
     */
    void raiseAllOffers(String goldenKey, long gameId, long lotId)
            throws FunPayApiException, InvalidGoldenKeyException, OfferAlreadyRaisedException;

    /**
     * Send a request to save offer
     *
     * @param goldenKey golden key which will be used to authorize the user
     * @param csrfToken csrf token which is required to interact with the user in this operation
     * @param phpSessionId user session without which the csrf token will be useless
     * @param request request storing all necessary data for saving offer
     * @throws FunPayApiException if the other api-related exception
     * @throws InvalidGoldenKeyException if the golden key is invalid
     * @throws InvalidCsrfTokenOrPHPSESSIDException if the csrf token or PHPSESSID is invalid
     */
    void saveOffer(
            String goldenKey, String csrfToken, String phpSessionId, SaveOfferRequest request)
            throws FunPayApiException, InvalidGoldenKeyException,
                    InvalidCsrfTokenOrPHPSESSIDException;

    /**
     * Send a request to add offer image
     *
     * @param goldenKey golden key which will be used to authorize the user
     * @param image byte file representing the image
     * @return imageId
     * @throws FunPayApiException if the other api-related exception
     * @throws InvalidGoldenKeyException if the golden key is invalid
     */
    Long addOfferImage(String goldenKey, byte[] image)
            throws FunPayApiException, InvalidGoldenKeyException;
}
