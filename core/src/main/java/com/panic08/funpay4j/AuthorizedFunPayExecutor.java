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

package com.panic08.funpay4j;

import java.net.Proxy;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import com.panic08.funpay4j.commands.offer.CreateOffer;
import com.panic08.funpay4j.commands.offer.CreateOfferImage;
import com.panic08.funpay4j.commands.offer.DeleteOffer;
import com.panic08.funpay4j.commands.offer.EditOffer;
import com.panic08.funpay4j.commands.offer.RaiseAllOffers;
import com.panic08.funpay4j.commands.order.GetOrder;
import com.panic08.funpay4j.commands.transaction.GetTransactions;
import com.panic08.funpay4j.commands.user.GetSellerReviews;
import com.panic08.funpay4j.commands.user.GetUser;
import com.panic08.funpay4j.commands.user.UpdateAvatar;
import com.panic08.funpay4j.exceptions.FunPayApiException;
import com.panic08.funpay4j.exceptions.InvalidCsrfTokenOrPHPSESSIDException;
import com.panic08.funpay4j.exceptions.InvalidGoldenKeyException;
import com.panic08.funpay4j.exceptions.offer.OfferAlreadyRaisedException;
import com.panic08.funpay4j.exceptions.order.OrderNotFoundException;
import com.panic08.funpay4j.exceptions.user.UserNotFoundException;
import com.panic08.funpay4j.objects.CsrfTokenAndPHPSESSID;
import com.panic08.funpay4j.objects.order.Order;
import com.panic08.funpay4j.objects.order.ParsedOrder;
import com.panic08.funpay4j.objects.transaction.ParsedTransaction;
import com.panic08.funpay4j.objects.transaction.ParsedTransactionType;
import com.panic08.funpay4j.objects.transaction.Transaction;
import com.panic08.funpay4j.objects.transaction.TransactionStatus;
import com.panic08.funpay4j.objects.user.AdvancedSellerReview;
import com.panic08.funpay4j.objects.user.ParsedAdvancedSellerReview;
import com.panic08.funpay4j.objects.user.ParsedSellerReview;
import com.panic08.funpay4j.objects.user.ParsedUser;
import com.panic08.funpay4j.objects.user.SellerReview;
import com.panic08.funpay4j.objects.user.User;
import com.panic08.funpay4j.request.SaveOfferRequest;

/**
 * This Authorized FunPay executor is used to execute authorized commands
 *
 * @author panic08
 * @since 1.0.3
 */
@Getter
@Setter
public class AuthorizedFunPayExecutor extends FunPayExecutor {
    private final String goldenKey;

    private String PHPSESSID;

    private String csrfToken;

    /**
     * Creates a new AuthorizedFunPayExecutor instance
     *
     * @param goldenKey golden key which will be used to authorize the user
     */
    public AuthorizedFunPayExecutor(@NonNull String goldenKey) {
        super();

        this.goldenKey = goldenKey;
    }

    /**
     * Creates a new AuthorizedFunPayExecutor instance
     *
     * @param goldenKey golden key which will be used to authorize the user
     * @param baseURL base URL of the primary server
     * @param proxy proxy for forwarding requests
     */
    public AuthorizedFunPayExecutor(
            @NonNull String goldenKey, @NonNull String baseURL, @NonNull Proxy proxy) {
        super(baseURL, proxy);

        this.goldenKey = goldenKey;
    }

    /**
     * Creates a new AuthorizedFunPayExecutor instance
     *
     * @param goldenKey golden key which will be used to authorize the user
     * @param baseURL base URL of the primary server
     */
    public AuthorizedFunPayExecutor(@NonNull String goldenKey, @NonNull String baseURL) {
        super(baseURL);

        this.goldenKey = goldenKey;
    }

    /**
     * Creates a new AuthorizedFunPayExecutor instance
     *
     * @param goldenKey golden key which will be used to authorize the user
     * @param proxy proxy for forwarding requests
     */
    public AuthorizedFunPayExecutor(@NonNull String goldenKey, @NonNull Proxy proxy) {
        super(proxy);

        this.goldenKey = goldenKey;
    }

    /**
     * Execute to update user avatar
     *
     * @param command command that will be executed
     * @throws FunPayApiException if the other api-related exception
     * @throws InvalidGoldenKeyException if the golden key is incorrect
     */
    public void execute(UpdateAvatar command) throws FunPayApiException, InvalidGoldenKeyException {
        funPayClient.updateAvatar(goldenKey, command.getNewAvatar());
    }

    /**
     * Execute to raise all offers
     *
     * @param command command that will be executed
     * @throws FunPayApiException if the other api-related exception
     * @throws InvalidGoldenKeyException if the golden key is incorrect
     * @throws OfferAlreadyRaisedException if the offer already raised
     */
    public void execute(RaiseAllOffers command)
            throws FunPayApiException, InvalidGoldenKeyException, OfferAlreadyRaisedException {
        funPayClient.raiseAllOffers(goldenKey, command.getGameId(), command.getLotId());
    }

    /**
     * Execute to create offer
     *
     * @param command command that will be executed
     * @throws FunPayApiException if the other api-related exception
     * @throws InvalidGoldenKeyException if the golden key is incorrect
     */
    public void execute(CreateOffer command) throws FunPayApiException, InvalidGoldenKeyException {
        SaveOfferRequest request =
                SaveOfferRequest.builder()
                        .nodeId(command.getLotId())
                        .summaryRu(command.getShortDescriptionRu())
                        .summaryEn(command.getShortDescriptionEn())
                        .descRu(command.getDescriptionRu())
                        .descEn(command.getDescriptionEn())
                        .paymentMessageRu(command.getPaymentMessageRu())
                        .paymentMessageEn(command.getPaymentMessageEn())
                        .fields(command.getFields())
                        .isAutoDelivery(command.isAutoDelivery())
                        .isActive(command.isActive())
                        .isDeleted(false)
                        .secrets(command.getSecrets())
                        .images(command.getImageIds())
                        .price(command.getPrice())
                        .amount(command.getAmount())
                        .build();

        if (PHPSESSID == null || csrfToken == null) {
            try {
                updateCsrfTokenAndPHPSESSID();
            } catch (FunPayApiException e) {
                throw new RuntimeException(e.getCause());
            }
        }

        // attempt to regenerate csrfToken and PHPSESSID
        try {
            funPayClient.saveOffer(goldenKey, csrfToken, PHPSESSID, request);
        } catch (InvalidCsrfTokenOrPHPSESSIDException e) {
            updateCsrfTokenAndPHPSESSID();

            try {
                funPayClient.saveOffer(goldenKey, csrfToken, PHPSESSID, request);
            } catch (InvalidCsrfTokenOrPHPSESSIDException e1) {
                // TODO: Throw something more contextual than RuntimeException
                throw new RuntimeException(e1.getLocalizedMessage());
            }
        }
    }

    /**
     * Execute to edit offer
     *
     * @param command command that will be executed
     * @throws FunPayApiException if the other api-related exception
     * @throws InvalidGoldenKeyException if the golden key is incorrect
     */
    public void execute(EditOffer command) throws FunPayApiException, InvalidGoldenKeyException {
        SaveOfferRequest request =
                SaveOfferRequest.builder()
                        .nodeId(command.getLotId())
                        .offerId(command.getOfferId())
                        .summaryRu(command.getShortDescriptionRu())
                        .summaryEn(command.getShortDescriptionEn())
                        .descRu(command.getDescriptionRu())
                        .descEn(command.getDescriptionEn())
                        .paymentMessageRu(command.getPaymentMessageRu())
                        .paymentMessageEn(command.getPaymentMessageEn())
                        .fields(command.getFields())
                        .isAutoDelivery(command.isAutoDelivery())
                        .isActive(command.isActive())
                        .isDeleted(false)
                        .secrets(command.getSecrets())
                        .images(command.getImageIds())
                        .price(command.getPrice())
                        .amount(command.getAmount())
                        .build();

        if (PHPSESSID == null || csrfToken == null) {
            try {
                updateCsrfTokenAndPHPSESSID();
            } catch (FunPayApiException e) {
                throw new RuntimeException(e.getCause());
            }
        }

        // attempt to regenerate csrfToken and PHPSESSID
        try {
            funPayClient.saveOffer(goldenKey, csrfToken, PHPSESSID, request);
        } catch (InvalidCsrfTokenOrPHPSESSIDException e) {
            updateCsrfTokenAndPHPSESSID();

            try {
                funPayClient.saveOffer(goldenKey, csrfToken, PHPSESSID, request);
            } catch (InvalidCsrfTokenOrPHPSESSIDException e1) {
                // TODO: Throw something more contextual than RuntimeException
                throw new RuntimeException(e1.getLocalizedMessage());
            }
        }
    }

    /**
     * Execute to delete offer
     *
     * @param command command that will be executed
     * @throws FunPayApiException if the other api-related exception
     * @throws InvalidGoldenKeyException if the golden key is incorrect
     */
    public void execute(DeleteOffer command) throws FunPayApiException, InvalidGoldenKeyException {
        SaveOfferRequest request =
                SaveOfferRequest.builder()
                        .nodeId(command.getLotId())
                        .offerId(command.getOfferId())
                        .isDeleted(true)
                        .build();

        if (PHPSESSID == null || csrfToken == null) {
            try {
                updateCsrfTokenAndPHPSESSID();
            } catch (FunPayApiException e) {
                throw new RuntimeException(e.getCause());
            }
        }

        // attempt to regenerate csrfToken and PHPSESSID
        try {
            funPayClient.saveOffer(goldenKey, csrfToken, PHPSESSID, request);
        } catch (InvalidCsrfTokenOrPHPSESSIDException e) {
            updateCsrfTokenAndPHPSESSID();

            try {
                funPayClient.saveOffer(goldenKey, csrfToken, PHPSESSID, request);
            } catch (InvalidCsrfTokenOrPHPSESSIDException e1) {
                // TODO: Throw something more contextual than RuntimeException
                throw new RuntimeException(e1.getLocalizedMessage());
            }
        }
    }

    /**
     * Execute to create offer image
     *
     * @param command command that will be executed
     * @return imageId
     * @throws FunPayApiException if the other api-related exception
     * @throws InvalidGoldenKeyException if the golden key is incorrect
     */
    public Long execute(CreateOfferImage command)
            throws FunPayApiException, InvalidGoldenKeyException {
        return funPayClient.addOfferImage(goldenKey, command.getImage());
    }

    /**
     * Execute to get user authorized
     *
     * @param command command that will be executed
     * @return user
     * @throws FunPayApiException if the other api-related exception
     * @throws UserNotFoundException if the user with id does not found
     */
    public User execute(GetUser command) throws FunPayApiException, UserNotFoundException {
        ParsedUser user = funPayParser.parseUser(goldenKey, command.getUserId());
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

    /**
     * Execute to get transactions authorized
     *
     * @param command command that will be executed
     * @return transactions
     * @throws FunPayApiException if the other api-related exception
     * @throws UserNotFoundException if the user with id does not found
     * @throws InvalidGoldenKeyException if the golden key is incorrect
     */
    public List<Transaction> execute(GetTransactions command)
            throws FunPayApiException, UserNotFoundException, InvalidGoldenKeyException {
        List<ParsedTransaction> transactions;
        if (command.getType() != null) {
            transactions =
                    funPayParser.parseTransactions(
                            goldenKey,
                            command.getUserId(),
                            ParsedTransactionType.valueOf(command.getType().name()),
                            command.getPages());
        } else {
            transactions =
                    funPayParser.parseTransactions(
                            goldenKey, command.getUserId(), null, command.getPages());
        }
        return transactions.stream()
                .map(
                        parsedTransaction -> {
                            return Transaction.builder()
                                    .id(parsedTransaction.getId())
                                    .title(parsedTransaction.getTitle())
                                    .status(
                                            TransactionStatus.valueOf(
                                                    parsedTransaction.getStatus().name()))
                                    .paymentNumber(parsedTransaction.getPaymentNumber())
                                    .date(parsedTransaction.getDate())
                                    .price(parsedTransaction.getPrice())
                                    .build();
                        })
                .collect(Collectors.toList());
    }

    /**
     * Execute to get order authorized
     *
     * @param command command that will be executed
     * @return order
     * @throws FunPayApiException if the other api-related exception
     * @throws OrderNotFoundException if the order with id does not found
     */
    public Order execute(GetOrder command) throws FunPayApiException, OrderNotFoundException {
        ParsedOrder parsedOrder = funPayParser.parseOrder(goldenKey, command.getOrderId());
        return Order.builder()
                .id(parsedOrder.getId())
                .statuses(parsedOrder.getStatuses())
                .shortDescription(parsedOrder.getShortDescription())
                .detailedDescription(parsedOrder.getDetailedDescription())
                .price(parsedOrder.getPrice())
                .params(parsedOrder.getParams())
                .other(parsedOrder.getOther())
                .build();
    }

    /**
     * Execute to get seller reviews authorized
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
                            goldenKey,
                            command.getUserId(),
                            command.getPages(),
                            command.getStarsFilter());
        } else {
            sellerReviews =
                    funPayParser.parseSellerReviews(
                            goldenKey, command.getUserId(), command.getPages());
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

    /**
     * Update csrfToken and PHPSESSID
     *
     * @throws FunPayApiException if the other api-related exception
     */
    public void updateCsrfTokenAndPHPSESSID() throws FunPayApiException {
        // TODO: It might be worth reconsidering and finding another way to update csrf and
        // PHPSESSID
        // that doesn't require making such relatively expensive queries

        CsrfTokenAndPHPSESSID csrfTokenAndPHPSESSID =
                funPayParser.parseCsrfTokenAndPHPSESSID(goldenKey);

        this.csrfToken = csrfTokenAndPHPSESSID.getCsrfToken();
        this.PHPSESSID = csrfTokenAndPHPSESSID.getPHPSESSID();
    }
}
