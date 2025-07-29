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

import java.net.Proxy;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.OkHttpClient;

import org.jspecify.annotations.Nullable;

import com.therepanic.funpay4j.client.FunPayClient;
import com.therepanic.funpay4j.client.OkHttpFunPayClient;
import com.therepanic.funpay4j.commands.game.GetPromoGames;
import com.therepanic.funpay4j.commands.lot.GetLot;
import com.therepanic.funpay4j.commands.offer.GetOffer;
import com.therepanic.funpay4j.commands.user.GetSellerReviews;
import com.therepanic.funpay4j.commands.user.GetUser;
import com.therepanic.funpay4j.exceptions.FunPayApiException;
import com.therepanic.funpay4j.exceptions.lot.LotNotFoundException;
import com.therepanic.funpay4j.exceptions.offer.OfferNotFoundException;
import com.therepanic.funpay4j.exceptions.user.UserNotFoundException;
import com.therepanic.funpay4j.objects.game.ParsedPromoGame;
import com.therepanic.funpay4j.objects.game.PromoGame;
import com.therepanic.funpay4j.objects.game.PromoGameCounter;
import com.therepanic.funpay4j.objects.lot.Lot;
import com.therepanic.funpay4j.objects.lot.LotCounter;
import com.therepanic.funpay4j.objects.lot.ParsedLot;
import com.therepanic.funpay4j.objects.offer.Offer;
import com.therepanic.funpay4j.objects.offer.ParsedOffer;
import com.therepanic.funpay4j.objects.offer.PreviewOffer;
import com.therepanic.funpay4j.objects.user.*;
import com.therepanic.funpay4j.objects.user.AdvancedSellerReview;
import com.therepanic.funpay4j.objects.user.PreviewSeller;
import com.therepanic.funpay4j.objects.user.Seller;
import com.therepanic.funpay4j.objects.user.SellerReview;
import com.therepanic.funpay4j.objects.user.User;
import com.therepanic.funpay4j.parser.FunPayParser;
import com.therepanic.funpay4j.parser.JsoupFunPayParser;

/**
 * This FunPay executor is used to execute commands
 *
 * @author therepanic
 * @since 1.0.0
 */
public class FunPayExecutor {
    protected final FunPayParser funPayParser;

    protected final FunPayClient funPayClient;

    /**
     * Creates a new FunPayExecutor instance
     *
     * @param baseURL base URL of the primary server
     * @param proxy proxy for forwarding requests
     */
    public FunPayExecutor(String baseURL, @Nullable Proxy proxy) {
        OkHttpClient httpClient;

        if (proxy == null) {
            httpClient = new OkHttpClient();
        } else {
            httpClient = new OkHttpClient.Builder().proxy(proxy).build();
        }

        this.funPayParser = new JsoupFunPayParser(httpClient, baseURL);
        this.funPayClient = new OkHttpFunPayClient(httpClient, baseURL);
    }

    /** Creates a new FunPayExecutor instance */
    public FunPayExecutor() {
        this(FunPayURL.BASE_URL, null);
    }

    /**
     * Creates a new FunPayExecutor instance
     *
     * @param baseURL base URL of the primary server
     */
    public FunPayExecutor(String baseURL) {
        this(baseURL, null);
    }

    /**
     * Creates a new FunPayExecutor instance
     *
     * @param proxy proxy for forwarding requests
     */
    public FunPayExecutor(Proxy proxy) {
        this(FunPayURL.BASE_URL, proxy);
    }

    /**
     * Execute to get lot
     *
     * @param command command that will be executed
     * @return lot
     * @throws FunPayApiException if the other api-related exception
     * @throws LotNotFoundException if the lot with id does not found
     */
    public Lot execute(GetLot command) throws FunPayApiException, LotNotFoundException {
        ParsedLot parsedLot = funPayParser.parseLot(command.getLotId());
        return Lot.builder()
                .id(parsedLot.getId())
                .gameId(parsedLot.getGameId())
                .title(parsedLot.getTitle())
                .description(parsedLot.getDescription())
                .lotCounters(
                        parsedLot.getLotCounters().stream()
                                .map(
                                        parsedLotCounter -> {
                                            return LotCounter.builder()
                                                    .lotId(parsedLotCounter.getLotId())
                                                    .param(parsedLotCounter.getParam())
                                                    .counter(parsedLotCounter.getCounter())
                                                    .build();
                                        })
                                .collect(Collectors.toList()))
                .previewOffers(
                        parsedLot.getPreviewOffers().stream()
                                .map(
                                        parsedPreviewOffer -> {
                                            ParsedPreviewSeller previewSeller =
                                                    parsedPreviewOffer.getSeller();
                                            return PreviewOffer.builder()
                                                    .offerId(parsedPreviewOffer.getOfferId())
                                                    .shortDescription(
                                                            parsedPreviewOffer
                                                                    .getShortDescription())
                                                    .price(parsedPreviewOffer.getPrice())
                                                    .isAutoDelivery(
                                                            parsedPreviewOffer.isAutoDelivery())
                                                    .isPromo(parsedPreviewOffer.isPromo())
                                                    .seller(
                                                            PreviewSeller.builder()
                                                                    .userId(
                                                                            previewSeller
                                                                                    .getUserId())
                                                                    .username(
                                                                            previewSeller
                                                                                    .getUsername())
                                                                    .avatarPhotoLink(
                                                                            previewSeller
                                                                                    .getAvatarPhotoLink())
                                                                    .isOnline(
                                                                            previewSeller
                                                                                    .isOnline())
                                                                    .reviewCount(
                                                                            previewSeller
                                                                                    .getReviewCount())
                                                                    .build())
                                                    .build();
                                        })
                                .collect(Collectors.toList()))
                .build();
    }

    /**
     * Execute to get promo games
     *
     * @param command command that will be executed
     * @return promo games
     * @throws FunPayApiException if the other api-related exception
     */
    public List<PromoGame> execute(GetPromoGames command) throws FunPayApiException {
        List<ParsedPromoGame> promoGame = funPayParser.parsePromoGames(command.getQuery());
        return promoGame.stream()
                .map(
                        parsedPromoGame -> {
                            return PromoGame.builder()
                                    .lotId(parsedPromoGame.getLotId())
                                    .title(parsedPromoGame.getTitle())
                                    .promoGameCounters(
                                            parsedPromoGame.getPromoGameCounters().stream()
                                                    .map(
                                                            parsedPromoGameCounter -> {
                                                                return PromoGameCounter.builder()
                                                                        .lotId(
                                                                                parsedPromoGameCounter
                                                                                        .getLotId())
                                                                        .title(
                                                                                parsedPromoGameCounter
                                                                                        .getTitle())
                                                                        .build();
                                                            })
                                                    .collect(Collectors.toList()))
                                    .build();
                        })
                .collect(Collectors.toList());
    }

    /**
     * Execute to get offer
     *
     * @param command command that will be executed
     * @return offer
     * @throws FunPayApiException if the other api-related exception
     * @throws OfferNotFoundException if the offer with id does not found
     */
    public Offer execute(GetOffer command) throws FunPayApiException, OfferNotFoundException {
        ParsedOffer offer = funPayParser.parseOffer(command.getOfferId());
        return Offer.builder()
                .id(offer.getId())
                .shortDescription(offer.getShortDescription())
                .detailedDescription(offer.getDetailedDescription())
                .isAutoDelivery(offer.isAutoDelivery())
                .price(offer.getPrice())
                .attachmentLinks(offer.getAttachmentLinks())
                .parameters(offer.getParameters())
                .seller(
                        PreviewSeller.builder()
                                .userId(offer.getSeller().getUserId())
                                .username(offer.getSeller().getUsername())
                                .avatarPhotoLink(offer.getSeller().getAvatarPhotoLink())
                                .isOnline(offer.getSeller().isOnline())
                                .reviewCount(offer.getSeller().getReviewCount())
                                .build())
                .build();
    }

    /**
     * Execute to get user
     *
     * @param command command that will be executed
     * @return user
     * @throws FunPayApiException if the other api-related exception
     * @throws UserNotFoundException if the user with id does not found
     */
    public User execute(GetUser command) throws FunPayApiException, UserNotFoundException {
        ParsedUser user = funPayParser.parseUser(command.getUserId());
        if (user instanceof ParsedSeller) {
            return Seller.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .avatarPhotoLink(user.getAvatarPhotoLink())
                    .isOnline(user.isOnline())
                    .badges(user.getBadges())
                    .lastSeenAt(user.getLastSeenAt())
                    .registeredAt(user.getRegisteredAt())
                    .rating(((ParsedSeller) user).getRating())
                    .reviewCount(((ParsedSeller) user).getReviewCount())
                    .previewOffers(
                            ((ParsedSeller) user)
                                    .getPreviewOffers().stream()
                                            .map(
                                                    parsedPreviewOffer -> {
                                                        ParsedPreviewSeller previewSeller =
                                                                parsedPreviewOffer.getSeller();
                                                        return PreviewOffer.builder()
                                                                .offerId(
                                                                        parsedPreviewOffer
                                                                                .getOfferId())
                                                                .shortDescription(
                                                                        parsedPreviewOffer
                                                                                .getShortDescription())
                                                                .price(
                                                                        parsedPreviewOffer
                                                                                .getPrice())
                                                                .isAutoDelivery(
                                                                        parsedPreviewOffer
                                                                                .isAutoDelivery())
                                                                .isPromo(
                                                                        parsedPreviewOffer
                                                                                .isPromo())
                                                                .seller(
                                                                        PreviewSeller.builder()
                                                                                .userId(
                                                                                        previewSeller
                                                                                                .getUserId())
                                                                                .username(
                                                                                        previewSeller
                                                                                                .getUsername())
                                                                                .avatarPhotoLink(
                                                                                        previewSeller
                                                                                                .getAvatarPhotoLink())
                                                                                .isOnline(
                                                                                        previewSeller
                                                                                                .isOnline())
                                                                                .reviewCount(
                                                                                        previewSeller
                                                                                                .getReviewCount())
                                                                                .build())
                                                                .build();
                                                    })
                                            .collect(Collectors.toList()))
                    .lastReviews(
                            ((ParsedSeller) user)
                                    .getLastReviews().stream()
                                            .map(
                                                    parsedSellerReview -> {
                                                        return SellerReview.builder()
                                                                .gameTitle(
                                                                        parsedSellerReview
                                                                                .getGameTitle())
                                                                .price(
                                                                        parsedSellerReview
                                                                                .getPrice())
                                                                .text(parsedSellerReview.getText())
                                                                .stars(
                                                                        parsedSellerReview
                                                                                .getStars())
                                                                .sellerReplyText(
                                                                        parsedSellerReview
                                                                                .getSellerReplyText())
                                                                .build();
                                                    })
                                            .collect(Collectors.toList()))
                    .build();
        } else {
            return User.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .avatarPhotoLink(user.getAvatarPhotoLink())
                    .isOnline(user.isOnline())
                    .badges(user.getBadges())
                    .lastSeenAt(user.getLastSeenAt())
                    .registeredAt(user.getRegisteredAt())
                    .build();
        }
    }

    /**
     * Execute to get seller reviews
     *
     * @param command command that will be executed
     * @return seller reviews
     * @throws FunPayApiException if the other api-related exception
     * @throws UserNotFoundException if the user with id does not found/seller
     */
    public List<SellerReview> execute(GetSellerReviews command)
            throws FunPayApiException, UserNotFoundException {
        List<ParsedSellerReview> sellerReviews;
        if (command.getStarsFilter() != null) {
            sellerReviews =
                    funPayParser.parseSellerReviews(
                            command.getUserId(), command.getPages(), command.getStarsFilter());
        } else {
            sellerReviews =
                    funPayParser.parseSellerReviews(command.getUserId(), command.getPages());
        }
        return sellerReviews.stream()
                .map(
                        parsedSellerReview -> {
                            if (parsedSellerReview instanceof ParsedAdvancedSellerReview) {
                                return AdvancedSellerReview.builder()
                                        .senderUserId(
                                                ((ParsedAdvancedSellerReview) parsedSellerReview)
                                                        .getSenderUserId())
                                        .senderUsername(
                                                ((ParsedAdvancedSellerReview) parsedSellerReview)
                                                        .getSenderUsername())
                                        .senderAvatarLink(
                                                ((ParsedAdvancedSellerReview) parsedSellerReview)
                                                        .getSenderAvatarLink())
                                        .orderId(
                                                ((ParsedAdvancedSellerReview) parsedSellerReview)
                                                        .getOrderId())
                                        .createdAt(
                                                ((ParsedAdvancedSellerReview) parsedSellerReview)
                                                        .getCreatedAt())
                                        .gameTitle(parsedSellerReview.getGameTitle())
                                        .price(parsedSellerReview.getPrice())
                                        .text(parsedSellerReview.getText())
                                        .stars(parsedSellerReview.getStars())
                                        .sellerReplyText(parsedSellerReview.getSellerReplyText())
                                        .build();
                            } else {
                                return SellerReview.builder()
                                        .gameTitle(parsedSellerReview.getGameTitle())
                                        .price(parsedSellerReview.getPrice())
                                        .text(parsedSellerReview.getText())
                                        .stars(parsedSellerReview.getStars())
                                        .sellerReplyText(parsedSellerReview.getSellerReplyText())
                                        .build();
                            }
                        })
                .collect(Collectors.toList());
    }
}
