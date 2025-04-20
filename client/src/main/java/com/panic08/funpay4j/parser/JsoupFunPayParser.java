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

package com.panic08.funpay4j.parser;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.NonNull;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.JsonParser;
import com.panic08.funpay4j.FunPayUserUtil;
import com.panic08.funpay4j.exceptions.FunPayApiException;
import com.panic08.funpay4j.exceptions.InvalidGoldenKeyException;
import com.panic08.funpay4j.exceptions.lot.LotNotFoundException;
import com.panic08.funpay4j.exceptions.offer.OfferNotFoundException;
import com.panic08.funpay4j.exceptions.order.OrderNotFoundException;
import com.panic08.funpay4j.exceptions.user.UserNotFoundException;
import com.panic08.funpay4j.objects.CsrfTokenAndPHPSESSID;
import com.panic08.funpay4j.objects.game.ParsedPromoGame;
import com.panic08.funpay4j.objects.game.ParsedPromoGameCounter;
import com.panic08.funpay4j.objects.lot.ParsedLot;
import com.panic08.funpay4j.objects.lot.ParsedLotCounter;
import com.panic08.funpay4j.objects.offer.ParsedOffer;
import com.panic08.funpay4j.objects.offer.ParsedPreviewOffer;
import com.panic08.funpay4j.objects.order.ParsedOrder;
import com.panic08.funpay4j.objects.transaction.ParsedTransaction;
import com.panic08.funpay4j.objects.transaction.ParsedTransactionStatus;
import com.panic08.funpay4j.objects.transaction.ParsedTransactionType;
import com.panic08.funpay4j.objects.user.*;

/**
 * This implementation of FunPayParser uses Jsoup to parse
 *
 * @author panic08
 * @since 1.0.0
 */
public class JsoupFunPayParser implements FunPayParser {
    @NonNull private final OkHttpClient httpClient;

    @NonNull private final String baseURL;

    /**
     * Creates a new JsoupFunPayParser instance
     *
     * @param httpClient httpClient required to send http requests
     * @param baseURL base URL of the primary server
     */
    public JsoupFunPayParser(@NonNull OkHttpClient httpClient, @NonNull String baseURL) {
        this.httpClient = httpClient;
        this.baseURL = baseURL;
    }

    /** {@inheritDoc} */
    @Override
    public ParsedLot parseLot(long lotId) throws FunPayApiException, LotNotFoundException {
        try (Response funPayHtmlResponse =
                httpClient
                        .newCall(
                                new Request.Builder()
                                        .get()
                                        .url(baseURL + "/lots/" + lotId + "/")
                                        .build())
                        .execute()) {
            String funPayHtmlPageBody = funPayHtmlResponse.body().string();

            Document funPayDocument = Jsoup.parse(funPayHtmlPageBody);

            if (isNonExistentFunPayPage(funPayDocument)) {
                throw new LotNotFoundException("Lot with lotId " + lotId + " does not found");
            }

            // take the second element, since we don't need a container from the element with the
            // page-content-full class
            // is named contentBodyContainer because it is the container on top of the content-body
            Element funPayContentBodyContainerElement =
                    funPayDocument
                            .getElementById("content-body")
                            .getElementsByClass("container")
                            .get(1);
            Element funPayContentWithCdElement =
                    funPayDocument.getElementsByClass("content-with-cd").first();

            String title = funPayContentWithCdElement.selectFirst("h1").text();
            String description = funPayContentWithCdElement.selectFirst("p").text();
            long gameId =
                    Long.parseLong(
                            funPayContentBodyContainerElement
                                    .getElementsByClass("content-with-cd-wide showcase")
                                    .attr("data-game"));
            List<ParsedLotCounter> lotCounters = new ArrayList<>();
            List<ParsedPreviewOffer> previewOffers = new ArrayList<>();

            List<Element> funPayCountersElements =
                    funPayDocument.getElementsByClass("counter-list").first().select("a");

            for (Element counterItem : funPayCountersElements) {
                String counterHrefAttributeValue = counterItem.attr("href");

                // skip chips, as they are not supported yet
                if (counterHrefAttributeValue.contains("chips")) continue;

                long counterLotId =
                        Integer.parseInt(
                                counterHrefAttributeValue.substring(
                                        24, counterHrefAttributeValue.length() - 1));

                if (lotId == counterLotId) {
                    continue;
                }

                String counterParam = counterItem.getElementsByClass("counter-param").text();
                int counterValue =
                        Integer.parseInt(counterItem.getElementsByClass("counter-value").text());

                lotCounters.add(
                        ParsedLotCounter.builder()
                                .lotId(counterLotId)
                                .param(counterParam)
                                .counter(counterValue)
                                .build());
            }

            List<Element> funPayPreviewOffersElements =
                    funPayContentBodyContainerElement.getElementsByClass("tc").first().select("a");

            for (Element previewOffer : funPayPreviewOffersElements) {
                String previewOfferHrefAttributeValue = previewOffer.attr("href");
                String previewOfferSellerStyleAttributeValue =
                        previewOffer.getElementsByClass("avatar-photo").attr("style");

                long offerId = Long.parseLong(previewOfferHrefAttributeValue.substring(33));
                String previewOfferShortDescription =
                        previewOffer.getElementsByClass("tc-desc-text").text();
                double previewOfferPrice =
                        Double.parseDouble(
                                previewOffer.getElementsByClass("tc-price").attr("data-s"));
                boolean isHasPreviewOfferAutoDelivery =
                        previewOffer.getElementsByClass("auto-dlv-icon").first() != null;
                boolean isHasPreviewOfferPromo =
                        previewOffer.getElementsByClass("promo-offer-icon").first() != null;

                String previewSellerDataHrefAttributeValue =
                        previewOffer.getElementsByClass("avatar-photo").attr("data-href");
                Element previewSellerReviewCountElement =
                        previewOffer.getElementsByClass("rating-mini-count").first();

                long previewSellerUserId =
                        Long.parseLong(
                                previewSellerDataHrefAttributeValue.substring(
                                        25, previewSellerDataHrefAttributeValue.length() - 1));
                String previewSellerUsername =
                        previewOffer.getElementsByClass("media-user-name").text();
                String previewSellerAvatarPhotoLink =
                        previewOfferSellerStyleAttributeValue.substring(
                                22, previewOfferSellerStyleAttributeValue.length() - 2);
                boolean isPreviewSellerOnline =
                        previewOffer
                                        .getElementsByClass("media media-user online style-circle")
                                        .first()
                                != null;
                int previewSellerReviewCount =
                        previewSellerReviewCountElement == null
                                ? 0
                                : Integer.parseInt(previewSellerReviewCountElement.text());

                // if the previewUser has a regular photo
                if (previewSellerAvatarPhotoLink.equals("/img/layout/avatar.png"))
                    previewSellerAvatarPhotoLink = null;

                previewOffers.add(
                        ParsedPreviewOffer.builder()
                                .offerId(offerId)
                                .shortDescription(previewOfferShortDescription)
                                .price(previewOfferPrice)
                                .isAutoDelivery(isHasPreviewOfferAutoDelivery)
                                .isPromo(isHasPreviewOfferPromo)
                                .seller(
                                        ParsedPreviewSeller.builder()
                                                .userId(previewSellerUserId)
                                                .username(previewSellerUsername)
                                                .avatarPhotoLink(previewSellerAvatarPhotoLink)
                                                .isOnline(isPreviewSellerOnline)
                                                .reviewCount(previewSellerReviewCount)
                                                .build())
                                .build());
            }

            return ParsedLot.builder()
                    .id(lotId)
                    .title(title)
                    .description(description)
                    .gameId(gameId)
                    .lotCounters(lotCounters)
                    .previewOffers(previewOffers)
                    .build();
        } catch (IOException e) {
            throw new FunPayApiException(e.getLocalizedMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<ParsedPromoGame> parsePromoGames(@NonNull String query) throws FunPayApiException {
        List<ParsedPromoGame> currentPromoGames = new ArrayList<>();

        RequestBody requestBody =
                new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("query", query)
                        .build();

        try (Response response =
                httpClient
                        .newCall(
                                new Request.Builder()
                                        .post(requestBody)
                                        .url(baseURL + "/games/promoFilter")
                                        .addHeader("x-requested-with", "XMLHttpRequest")
                                        .build())
                        .execute()) {
            String promoGamesHtml =
                    JsonParser.parseString(response.body().string())
                            .getAsJsonObject()
                            .get("html")
                            .getAsString();

            List<Element> promoGameElements =
                    Jsoup.parse(promoGamesHtml).getElementsByClass("promo-games");

            for (Element promoGameElement : promoGameElements) {
                Element titleElement =
                        promoGameElement.getElementsByClass("game-title").first().selectFirst("a");
                String titleElementHrefAttributeValue = titleElement.attr("href");

                // Skip chips, as they are not supported yet
                if (titleElementHrefAttributeValue.contains("chips")) continue;

                long lotId =
                        Long.parseLong(
                                titleElementHrefAttributeValue.substring(
                                        24, titleElementHrefAttributeValue.length() - 1));
                String title = titleElement.text();

                List<ParsedPromoGameCounter> promoGameCounters = new ArrayList<>();

                for (Element promoGameCounterElement :
                        promoGameElement.getElementsByClass("list-inline").select("li")) {
                    Element counterTitleElement = promoGameCounterElement.selectFirst("a");
                    String counterTitleElementHrefAttributeValue = counterTitleElement.attr("href");

                    long counterLotId =
                            Long.parseLong(
                                    counterTitleElementHrefAttributeValue.substring(
                                            24,
                                            counterTitleElementHrefAttributeValue.length() - 1));

                    if (counterLotId == lotId) {
                        continue;
                    }

                    String counterTitle = counterTitleElement.text();

                    promoGameCounters.add(
                            ParsedPromoGameCounter.builder()
                                    .lotId(counterLotId)
                                    .title(counterTitle)
                                    .build());
                }

                currentPromoGames.add(
                        ParsedPromoGame.builder()
                                .lotId(lotId)
                                .title(title)
                                .promoGameCounters(promoGameCounters)
                                .build());
            }

            return currentPromoGames;
        } catch (IOException e) {
            throw new FunPayApiException(e.getLocalizedMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public ParsedOffer parseOffer(long offerId) throws FunPayApiException, OfferNotFoundException {
        try (Response funPayHtmlResponse =
                httpClient
                        .newCall(
                                new Request.Builder()
                                        .get()
                                        .url(baseURL + "/lots/offer?id=" + offerId)
                                        .build())
                        .execute()) {
            String funPayHtmlPageBody = funPayHtmlResponse.body().string();

            Document funPayDocument = Jsoup.parse(funPayHtmlPageBody);

            if (isNonExistentFunPayPage(funPayDocument)) {
                throw new OfferNotFoundException(
                        "Offer with offerId " + offerId + " does not found");
            }

            Element paramListElement = funPayDocument.getElementsByClass("param-list").first();
            // Get paramItemElements nested in the current item and not in any other way
            List<Element> paramItemElements = funPayDocument.select(".param-list > .param-item");

            // Selected total price in rubles
            String totalPriceValue =
                    funPayDocument
                            .getElementsByClass("form-control input-lg selectpicker")
                            .first()
                            .children()
                            .get(0)
                            .attr("data-content");

            String shortDescription = null;
            String detailedDescription = null;

            if (paramItemElements.size() == 1) {
                // if there is no shortDescription

                detailedDescription = paramItemElements.get(0).selectFirst("div").text();
            } else if (paramItemElements.size() >= 2) {
                shortDescription = paramItemElements.get(0).selectFirst("div").text();
                detailedDescription = paramItemElements.get(1).selectFirst("div").text();
            }

            boolean isAutoDelivery =
                    !funPayDocument.getElementsByClass("offer-header-auto-dlv-label").isEmpty();
            // Select a floating point number from a string like "from 1111.32 ₽"
            double price =
                    Double.parseDouble(totalPriceValue.replaceAll("[^0-9.]", "").split("\\s+")[0]);
            List<String> attachmentLinks = new ArrayList<>();

            if (paramItemElements.size() > 2) {
                // if the offer has attachments

                for (Element attachmentElement :
                        paramItemElements.get(2).getElementsByClass("attachments-item")) {
                    String attachmentLink = attachmentElement.selectFirst("a").attr("href");

                    attachmentLinks.add(attachmentLink);
                }
            }

            Map<String, String> parameters = new HashMap<>();

            for (Element paramItemElement :
                    paramListElement
                            .getElementsByClass("row")
                            .first()
                            .getElementsByClass("col-xs-6")) {
                Element parameterElement =
                        paramItemElement.getElementsByClass("param-item").first();

                String key = parameterElement.selectFirst("h5").text();
                String value = parameterElement.getElementsByClass("text-bold").text();

                parameters.put(key, value);
            }

            Element previewSellerReviewCountElement =
                    funPayDocument.getElementsByClass("text-mini text-light mb5").first();

            // Select rating from string like "219 reviews over 2 years"
            int previewSellerReviewCount =
                    Integer.parseInt(
                            previewSellerReviewCountElement.text().replaceAll("\\D.*", ""));
            ParsedPreviewUser parsedPreviewUser = extractPreviewUserFromProductPage(funPayDocument);

            return ParsedOffer.builder()
                    .id(offerId)
                    .shortDescription(shortDescription)
                    .detailedDescription(detailedDescription)
                    .isAutoDelivery(isAutoDelivery)
                    .price(price)
                    .attachmentLinks(attachmentLinks)
                    .parameters(parameters)
                    .seller(
                            ParsedPreviewSeller.builder()
                                    .userId(parsedPreviewUser.getUserId())
                                    .avatarPhotoLink(parsedPreviewUser.getAvatarPhotoLink())
                                    .username(parsedPreviewUser.getUsername())
                                    .isOnline(parsedPreviewUser.isOnline())
                                    .reviewCount(previewSellerReviewCount)
                                    .build())
                    .build();
        } catch (IOException e) {
            throw new FunPayApiException(e.getLocalizedMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public ParsedUser parseUser(long userId) throws FunPayApiException, UserNotFoundException {
        return parseUserInternal(null, userId);
    }

    /** {@inheritDoc} */
    @Override
    public ParsedUser parseUser(String goldenKey, long userId)
            throws FunPayApiException, UserNotFoundException {
        return parseUserInternal(goldenKey, userId);
    }

    /** {@inheritDoc} */
    @Override
    public List<ParsedSellerReview> parseSellerReviews(long userId, int pages)
            throws FunPayApiException, UserNotFoundException {
        return parseSellerReviewsInternal(null, userId, pages, null);
    }

    /** {@inheritDoc} */
    @Override
    public List<ParsedSellerReview> parseSellerReviews(String goldenKey, long userId, int pages)
            throws FunPayApiException, UserNotFoundException {
        return parseSellerReviewsInternal(goldenKey, userId, pages, null);
    }

    /** {@inheritDoc} */
    @Override
    public List<ParsedSellerReview> parseSellerReviews(long userId, int pages, int starsFilter)
            throws FunPayApiException, UserNotFoundException {
        return parseSellerReviewsInternal(null, userId, pages, String.valueOf(starsFilter));
    }

    /** {@inheritDoc} */
    @Override
    public List<ParsedSellerReview> parseSellerReviews(
            String goldenKey, long userId, int pages, int starsFilter)
            throws FunPayApiException, UserNotFoundException {
        return parseSellerReviewsInternal(goldenKey, userId, pages, String.valueOf(starsFilter));
    }

    /** {@inheritDoc} */
    @Override
    public List<ParsedTransaction> parseTransactions(
            String goldenKey, long userId, ParsedTransactionType type, int pages)
            throws FunPayApiException, UserNotFoundException, InvalidGoldenKeyException {
        return parseTransactionsInternal(goldenKey, userId, type, pages);
    }

    /** {@inheritDoc} */
    @Override
    public List<ParsedTransaction> parseTransactions(String goldenKey, long userId, int pages)
            throws FunPayApiException, UserNotFoundException, InvalidGoldenKeyException {
        return parseTransactionsInternal(goldenKey, userId, null, pages);
    }

    /** {@inheritDoc} */
    @Override
    public ParsedOrder parseOrder(String goldenKey, String orderId)
            throws FunPayApiException, OrderNotFoundException {
        try (Response funPayHtmlResponse =
                httpClient
                        .newCall(
                                new Request.Builder()
                                        .get()
                                        .addHeader("Cookie", "golden_key=" + goldenKey)
                                        .url(baseURL + "/orders/" + orderId + "/")
                                        .build())
                        .execute()) {
            String funPayHtmlPageBody = funPayHtmlResponse.body().string();

            Document funPayDocument = Jsoup.parse(funPayHtmlPageBody);

            if (isNonExistentFunPayPage(funPayDocument)) {
                throw new OrderNotFoundException(
                        "Order with orderId " + orderId + " does not found");
            }

            List<String> statuses = new ArrayList<>();

            List<Element> pageContentChildren =
                    funPayDocument.getElementsByClass("page-content").first().children();

            Element pageHeader = pageContentChildren.get(0);
            for (Element statusElement : pageHeader.children()) {
                statuses.add(statusElement.text());
            }

            Element paramList = pageContentChildren.get(1);

            Map<String, String> params = new HashMap<>();

            for (Element col : paramList.getElementsByClass("row").first().children()) {
                List<Element> paramItemChildren =
                        col.getElementsByClass("param-item").first().children();
                params.put(paramItemChildren.get(0).text(), paramItemChildren.get(1).text());
            }

            List<Element> paramRows = paramList.getElementsByClass("row");

            Double price = null;

            for (Element paramRow : paramRows) {
                for (Element paramCol : paramRow.children()) {
                    List<Element> paramItemChildren =
                            paramCol.getElementsByClass("param-item").first().children();
                    if (paramItemChildren.get(0).text().equals("Сумма")) {
                        price =
                                Double.parseDouble(
                                        paramItemChildren.get(1).children().get(0).text());
                    } else {
                        params.put(
                                paramItemChildren.get(0).text(), paramItemChildren.get(1).text());
                    }
                }
            }

            List<Element> paramListChildren = paramList.children();

            String shortDescription = paramListChildren.get(1).children().get(1).text();
            String detailedDescription = paramListChildren.get(2).children().get(1).text();

            return ParsedOrder.builder()
                    .id(orderId)
                    .statuses(statuses)
                    .params(params)
                    .shortDescription(shortDescription)
                    .detailedDescription(detailedDescription)
                    .price(price)
                    .other(extractPreviewUserFromProductPage(funPayDocument))
                    .build();
        } catch (IOException e) {
            throw new FunPayApiException(e.getLocalizedMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public CsrfTokenAndPHPSESSID parseCsrfTokenAndPHPSESSID(@NonNull String goldenKey)
            throws FunPayApiException {
        // We send a request to /unknown URL that doesn't exist to get a page where it will be
        // reported that the page doesn't exist.
        // This is necessary because such a page is the smallest size
        try (Response funPayHtmlResponse =
                httpClient
                        .newCall(
                                new Request.Builder()
                                        .get()
                                        .url(baseURL + "/unknown/")
                                        .addHeader("Cookie", "golden_key=" + goldenKey)
                                        .build())
                        .execute()) {
            String funPayHtmlPageBody = funPayHtmlResponse.body().string();

            Document funPayDocument = Jsoup.parse(funPayHtmlPageBody);

            String dataAppData = funPayDocument.getElementsByTag("body").attr("data-app-data");

            String csrfToken =
                    JsonParser.parseString(dataAppData)
                            .getAsJsonObject()
                            .get("csrf-token")
                            .getAsString();
            // Use this regex to get the value of the PHP_SESSION_ID key from the Set-Cookie header
            String phpSessionId =
                    funPayHtmlResponse
                            .header("Set-Cookie")
                            .replaceAll(".*PHPSESSID=([^;]*).*", "$1");

            return CsrfTokenAndPHPSESSID.builder()
                    .csrfToken(csrfToken)
                    .PHPSESSID(phpSessionId)
                    .build();
        } catch (IOException e) {
            throw new FunPayApiException(e.getLocalizedMessage());
        }
    }

    /**
     * Common method to parse user
     *
     * @param goldenKey golden key which will be used to authorize the user
     * @param userId user id by which user will be parsed
     * @return user
     * @throws FunPayApiException if the other api-related exception
     * @throws UserNotFoundException if the user with id does not found
     */
    private ParsedUser parseUserInternal(String goldenKey, long userId)
            throws FunPayApiException, UserNotFoundException {
        Request.Builder newCallBuilder =
                new Request.Builder().get().url(baseURL + "/users/" + userId + "/");

        if (goldenKey != null) {
            newCallBuilder.addHeader("Cookie", "golden_key=" + goldenKey);
        }

        try (Response funPayHtmlResponse = httpClient.newCall(newCallBuilder.build()).execute()) {
            String funPayHtmlPageBody = funPayHtmlResponse.body().string();

            Document funPayDocument = Jsoup.parse(funPayHtmlPageBody);

            if (isNonExistentFunPayPage(funPayDocument)) {
                throw new UserNotFoundException("User with userId " + userId + " does not found");
            }

            Element containerProfileHeader =
                    funPayDocument.getElementsByClass("container profile-header").first();

            Element profileElement = funPayDocument.getElementsByClass("profile").first();

            Element mediaUserStatusElement =
                    profileElement.getElementsByClass("media-user-status").first();
            Element avatarPhotoElement =
                    containerProfileHeader.getElementsByClass("avatar-photo").first();
            Element userBadgesElement = profileElement.getElementsByClass("user-badges").first();

            String avatarPhotoElementStyle = avatarPhotoElement.attr("style");

            String username = profileElement.getElementsByClass("mr4").text();
            String avatarPhotoLink =
                    avatarPhotoElementStyle.substring(22, avatarPhotoElementStyle.length() - 2);

            // if the user has a regular photo
            if (avatarPhotoLink.equals("/img/layout/avatar.png")) avatarPhotoLink = null;

            boolean isOnline = profileElement.getElementsByClass("mb40 online").first() != null;
            List<String> badges = new ArrayList<>();

            if (userBadgesElement != null) {
                for (Element badgeElement : userBadgesElement.children()) {
                    badges.add(badgeElement.text());
                }
            }

            String registeredAtStr =
                    profileElement.getElementsByClass("text-nowrap").first().text();
            Date registeredAt;

            try {
                registeredAt = FunPayUserUtil.convertRegisterDateStringToDate(registeredAtStr);
            } catch (ParseException e) {
                // might be the case if the account was created a few seconds/minutes/hours ago
                // such cases are not taken into account yet, so the logical thing to do is to cast
                // a new Date
                registeredAt = new Date();
            }

            String lastSeenAtStr =
                    mediaUserStatusElement == null ? "" : mediaUserStatusElement.text();
            Date lastSeenAt;

            if (lastSeenAtStr.contains("После регистрации на сайт не заходил")) {
                // if the user has not accessed the site after authorization

                lastSeenAt = new Date(registeredAt.getTime());
            } else if (lastSeenAtStr.contains("Онлайн")) {
                // if the user is online then the last time of login will be the current time

                lastSeenAt = new Date();
            } else {
                try {
                    lastSeenAt = FunPayUserUtil.convertLastSeenAtStringToDate(lastSeenAtStr);
                } catch (ParseException e) {
                    lastSeenAt = null;
                }
            }

            Element sellerElement = funPayDocument.getElementsByClass("param-item mb10").first();

            if (sellerElement != null) {
                // if user is seller too

                String ratingStr = sellerElement.getElementsByClass("big").first().text();

                double rating = ratingStr.equals("?") ? 0 : Double.parseDouble(ratingStr);
                // Select rating from string like "219 reviews over 2 years"
                int reviewCount =
                        Integer.parseInt(
                                sellerElement
                                        .getElementsByClass("text-mini text-light mb5")
                                        .text()
                                        .replaceAll("\\D.*", ""));

                List<ParsedPreviewOffer> previewOffers = new ArrayList<>();

                List<Element> previewOfferElements = funPayDocument.getElementsByClass("tc-item");

                for (Element previewOfferElement : previewOfferElements) {
                    Element previewOfferPriceElement =
                            previewOfferElement.getElementsByClass("tc-price").first();

                    String previewOfferElementHrefAttributeValue = previewOfferElement.attr("href");

                    long offerId =
                            Long.parseLong(previewOfferElementHrefAttributeValue.substring(33));
                    String previewOfferShortDescription =
                            previewOfferElement.getElementsByClass("tc-desc-text").text();
                    double previewOfferPrice =
                            Double.parseDouble(previewOfferPriceElement.attr("data-s"));
                    boolean isHasPreviewOfferAutoDelivery =
                            previewOfferPriceElement.getElementsByClass("auto-dlv-icon").first()
                                    != null;
                    // Since the promo value is not shown in the profile in offers
                    boolean isHasPreviewOfferPromo = false;

                    previewOffers.add(
                            ParsedPreviewOffer.builder()
                                    .offerId(offerId)
                                    .shortDescription(previewOfferShortDescription)
                                    .price(previewOfferPrice)
                                    .isAutoDelivery(isHasPreviewOfferAutoDelivery)
                                    .isPromo(isHasPreviewOfferPromo)
                                    .seller(
                                            ParsedPreviewSeller.builder()
                                                    .userId(userId)
                                                    .username(username)
                                                    .avatarPhotoLink(avatarPhotoLink)
                                                    .isOnline(isOnline)
                                                    .reviewCount(reviewCount)
                                                    .build())
                                    .build());
                }

                List<ParsedSellerReview> lastReviews = new ArrayList<>();

                extractReviewsFromReviewsHtml(funPayDocument, lastReviews);

                return ParsedSeller.builder()
                        .id(userId)
                        .username(username)
                        .avatarPhotoLink(avatarPhotoLink)
                        .isOnline(isOnline)
                        .badges(badges)
                        .lastSeenAt(lastSeenAt)
                        .registeredAt(registeredAt)
                        .rating(rating)
                        .reviewCount(reviewCount)
                        .previewOffers(previewOffers)
                        .lastReviews(lastReviews)
                        .build();
            } else {
                return ParsedUser.builder()
                        .id(userId)
                        .username(username)
                        .avatarPhotoLink(avatarPhotoLink)
                        .isOnline(isOnline)
                        .badges(badges)
                        .lastSeenAt(lastSeenAt)
                        .registeredAt(registeredAt)
                        .build();
            }

        } catch (IOException e) {
            throw new FunPayApiException(e.getLocalizedMessage());
        }
    }

    /**
     * Common method to parse transactions
     *
     * @param goldenKey golden key which will be used to authorize the user
     * @param userId user id by which transactions pages will be parsed
     * @param type type of transaction will be parsed
     * @param pages number of pages indicating how many transactions will be parsed
     * @return transactions
     * @throws FunPayApiException if the other api-related exception
     * @throws UserNotFoundException if the user with id does not found
     * @throws InvalidGoldenKeyException if the golden key is incorrect
     */
    private List<ParsedTransaction> parseTransactionsInternal(
            String goldenKey, long userId, ParsedTransactionType type, int pages)
            throws FunPayApiException, UserNotFoundException, InvalidGoldenKeyException {
        List<ParsedTransaction> parsedTransactions = new ArrayList<>();

        String userIdFormData = String.valueOf(userId);
        String continueArg = null;
        String typeStr;
        if (type == null) {
            typeStr = "";
        } else {
            switch (type) {
                case PAYMENT:
                    typeStr = "replenishment";
                    break;
                case WITHDRAW:
                    typeStr = "withdraw";
                    break;
                case ORDER:
                    typeStr = "order";
                    break;
                case OTHER:
                    typeStr = "other";
                    break;
                default:
                    typeStr = "";
                    break;
            }
        }

        for (int currentPageCount = 0; currentPageCount < pages; currentPageCount++) {
            RequestBody requestBody =
                    new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("user_id", userIdFormData)
                            .addFormDataPart("filter", typeStr)
                            .addFormDataPart("continue", continueArg == null ? "" : continueArg)
                            .build();

            Request.Builder newCallBuilder =
                    new Request.Builder()
                            .post(requestBody)
                            .url(baseURL + "/users/transactions")
                            .addHeader("x-requested-with", "XMLHttpRequest");

            if (goldenKey != null) {
                newCallBuilder.addHeader("Cookie", "golden_key=" + goldenKey);
            }

            try (Response funPayHtmlResponse =
                    httpClient.newCall(newCallBuilder.build()).execute()) {
                if (funPayHtmlResponse.code() == 400) {
                    throw new UserNotFoundException(
                            "User with userId " + userId + " does not found");
                } else if (funPayHtmlResponse.code() == 403) {
                    throw new InvalidGoldenKeyException("goldenKey is invalid");
                }

                Document transactionsHtml = Jsoup.parse(funPayHtmlResponse.body().string());
                List<Element> transactionElements = transactionsHtml.getElementsByClass("tc-item");

                for (Element transactionElement : transactionElements) {
                    String classAttribute = transactionElement.attr("class");

                    ParsedTransactionStatus status;
                    if (classAttribute.endsWith("complete")) {
                        status = ParsedTransactionStatus.COMPLETED;
                    } else if (classAttribute.endsWith("cancel")) {
                        status = ParsedTransactionStatus.CANCELED;
                    } else {
                        status = ParsedTransactionStatus.WAITING;
                    }
                    long id =
                            Long.parseLong(
                                    transactionElement.attribute("data-transaction").getValue());
                    String title = transactionElement.getElementsByClass("tc-title").text();
                    String paymentNumber =
                            transactionElement.getElementsByClass("tc-payment-number").text();
                    double price =
                            Double.parseDouble(
                                    transactionElement
                                            .getElementsByClass("tc-price")
                                            .text()
                                            .replace("−", "-")
                                            .replaceAll("[^0-9.-]", ""));
                    Date date =
                            FunPayUserUtil.convertRegisterDateStringToDate(
                                    transactionElement.getElementsByClass("tc-date-time").text());

                    parsedTransactions.add(
                            ParsedTransaction.builder()
                                    .id(id)
                                    .title(title)
                                    .price(price)
                                    .paymentNumber(paymentNumber)
                                    .status(status)
                                    .date(date)
                                    .build());

                    Element dynTableFormElement =
                            transactionsHtml.getElementsByClass("dyn-table-form").first();

                    if (dynTableFormElement == null) break;

                    List<Element> inputElements = dynTableFormElement.select("input");

                    Element continueElement = inputElements.isEmpty() ? null : inputElements.get(1);

                    if (continueElement == null || continueElement.attr("value").isEmpty()) break;

                    continueArg = continueElement.attr("value");
                }
            } catch (IOException e) {
                throw new FunPayApiException(e.getLocalizedMessage());
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return parsedTransactions;
    }

    /**
     * Common method to parse seller reviews
     *
     * @param goldenKey golden key which will be used to authorize the user
     * @param userId user id by which seller reviews pages will be parsed
     * @param pages number of pages indicating how many seller reviews will be parsed
     * @param starsFilter number of stars filter, can be null
     * @return sellerReviews
     * @throws FunPayApiException if the other api-related exception
     * @throws UserNotFoundException if the user with id does not found/seller
     */
    private List<ParsedSellerReview> parseSellerReviewsInternal(
            String goldenKey, long userId, int pages, String starsFilter)
            throws FunPayApiException, UserNotFoundException {
        List<ParsedSellerReview> currentSellerReviews = new ArrayList<>();

        String userIdFormData = String.valueOf(userId);
        String starsFilterFormData = starsFilter == null ? "" : starsFilter;
        String continueArg = null;

        for (int currentPageCount = 0; currentPageCount < pages; currentPageCount++) {
            RequestBody requestBody =
                    new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("user_id", userIdFormData)
                            .addFormDataPart("filter", starsFilterFormData)
                            .addFormDataPart("continue", continueArg == null ? "" : continueArg)
                            .build();

            Request.Builder newCallBuilder =
                    new Request.Builder()
                            .post(requestBody)
                            .url(baseURL + "/users/reviews")
                            .addHeader("x-requested-with", "XMLHttpRequest");

            if (goldenKey != null) {
                newCallBuilder.addHeader("Cookie", "golden_key=" + goldenKey);
            }

            try (Response funPayHtmlResponse =
                    httpClient.newCall(newCallBuilder.build()).execute()) {
                // TODO: Figure out what is worth throwing out here, since a user can also be a
                // non-existent but also a non-seller,
                // and we can't distinguish between the two just like that
                if (funPayHtmlResponse.code() == 404)
                    throw new UserNotFoundException(
                            "User with userId " + userId + " does not found/seller");

                Document reviewsHtml = Jsoup.parse(funPayHtmlResponse.body().string());

                extractReviewsFromReviewsHtml(reviewsHtml, currentSellerReviews);

                Element dynTableFormElement =
                        reviewsHtml.getElementsByClass("dyn-table-form").first();

                if (dynTableFormElement == null) break;

                List<Element> inputElements = dynTableFormElement.select("input");

                Element continueElement = inputElements.isEmpty() ? null : inputElements.get(1);

                if (continueElement == null || continueElement.attr("value").isEmpty()) break;

                continueArg = continueElement.attr("value");
            } catch (IOException e) {
                throw new FunPayApiException(e.getLocalizedMessage());
            }
        }

        return currentSellerReviews;
    }

    private ParsedPreviewUser extractPreviewUserFromProductPage(Document funPayDocument) {
        System.out.println();
        Element previewSellerUsernameElement =
                funPayDocument.getElementsByClass("media-user-name").first().selectFirst("a");
        Element previewSellerImgElement =
                funPayDocument.getElementsByClass("media-user").first().selectFirst("img");
        Element previewSellerReviewCountElement =
                funPayDocument.getElementsByClass("text-mini text-light mb5").first();

        String previewSellerUsernameElementHrefAttributeValue =
                previewSellerUsernameElement.attr("href");

        long previewSellerUserId =
                Long.parseLong(
                        previewSellerUsernameElementHrefAttributeValue.substring(
                                25, previewSellerUsernameElementHrefAttributeValue.length() - 1));
        String previewSellerUsername = previewSellerUsernameElement.text();
        String previewSellerAvatarPhotoLink = previewSellerImgElement.attr("src");

        // if the previewUser has a regular photo
        if (previewSellerAvatarPhotoLink.equals("/img/layout/avatar.png"))
            previewSellerAvatarPhotoLink = null;
        boolean isPreviewSellerOnline =
                funPayDocument.getElementsByClass("media media-user online").first() != null;
        return ParsedPreviewUser.builder()
                .userId(previewSellerUserId)
                .username(previewSellerUsername)
                .avatarPhotoLink(previewSellerAvatarPhotoLink)
                .isOnline(isPreviewSellerOnline)
                .build();
    }

    private void extractReviewsFromReviewsHtml(
            Document reviewsHtml, List<ParsedSellerReview> currentSellerReviews) {
        List<Element> reviewContainerElements = reviewsHtml.getElementsByClass("review-container");

        for (Element lastReviewElement : reviewContainerElements) {
            Element reviewCompiledReviewElement =
                    lastReviewElement.getElementsByClass("review-compiled-review").first();
            Element starsElement = reviewCompiledReviewElement.getElementsByClass("rating").first();

            String[] gameTitlePriceSplit =
                    reviewCompiledReviewElement
                            .getElementsByClass("review-item-detail")
                            .text()
                            .split(", ");

            String lastReviewGameTitle = gameTitlePriceSplit[0];
            // Select a floating point number from a string like "from 1111.32 ₽"
            double lastReviewPrice =
                    Double.parseDouble(
                            gameTitlePriceSplit[gameTitlePriceSplit.length - 1].replaceAll(
                                            "[^0-9.]", "")
                                    .split("\\s+")[0]);
            String lastReviewText =
                    reviewCompiledReviewElement.getElementsByClass("review-item-text").text();
            String lastReviewAnswer =
                    Optional.ofNullable(
                                    lastReviewElement
                                            .getElementsByClass("review-compiled-reply")
                                            .first())
                            .map(element -> element.children().text())
                            .orElse(null);
            int lastReviewStars = 0;

            if (starsElement != null) {
                // if the review has rating

                lastReviewStars = Integer.parseInt(starsElement.child(0).className().substring(6));
            }

            Element mediaUsernameElement =
                    reviewCompiledReviewElement.getElementsByClass("media-user-name").first();
            Element reviewItemOrderElement =
                    reviewCompiledReviewElement.getElementsByClass("review-item-order").first();
            Element reviewItemPhotoElement =
                    reviewCompiledReviewElement.getElementsByClass("review-item-photo").first();
            Element reviewItemDateElement =
                    reviewCompiledReviewElement.getElementsByClass("review-item-date").first();

            if (mediaUsernameElement != null
                    && reviewItemOrderElement != null
                    && reviewItemPhotoElement.child(0).childrenSize() > 0) {
                String mediaUsernameHrefAttributeValue = mediaUsernameElement.child(0).attr("href");
                String reviewItemPhotoSrcAttributeValue =
                        reviewItemPhotoElement.child(0).child(0).attr("src");
                String reviewItemOrderHrefAttributeValue =
                        reviewItemOrderElement.child(0).attr("href");

                long lastReviewSenderUserId =
                        Long.parseLong(
                                mediaUsernameHrefAttributeValue.substring(
                                        25, mediaUsernameHrefAttributeValue.length() - 1));
                String lastReviewOrderId =
                        reviewItemOrderHrefAttributeValue.substring(
                                26, reviewItemOrderHrefAttributeValue.length() - 1);
                String lastReviewSenderUsername = mediaUsernameElement.child(0).text();
                String lastReviewSenderAvatarPhotoLink =
                        reviewItemPhotoSrcAttributeValue.equals("/img/layout/avatar.png")
                                ? null
                                : reviewItemPhotoSrcAttributeValue;
                Date lastReviewCreatedAtDate = null;

                try {
                    lastReviewCreatedAtDate =
                            FunPayUserUtil.convertAdvancedSellerReviewCreatedAtToDate(
                                    reviewItemDateElement.text());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                currentSellerReviews.add(
                        ParsedAdvancedSellerReview.builder()
                                .gameTitle(lastReviewGameTitle)
                                .price(lastReviewPrice)
                                .text(lastReviewText)
                                .stars(lastReviewStars)
                                .orderId(lastReviewOrderId)
                                .sellerReplyText(lastReviewAnswer)
                                .senderUserId(lastReviewSenderUserId)
                                .senderUsername(lastReviewSenderUsername)
                                .senderAvatarLink(lastReviewSenderAvatarPhotoLink)
                                .createdAt(lastReviewCreatedAtDate)
                                .build());
            } else {
                currentSellerReviews.add(
                        ParsedSellerReview.builder()
                                .gameTitle(lastReviewGameTitle)
                                .price(lastReviewPrice)
                                .text(lastReviewText)
                                .stars(lastReviewStars)
                                .sellerReplyText(lastReviewAnswer)
                                .build());
            }
        }
    }

    private boolean isNonExistentFunPayPage(Document funPayDocument) {
        Element pageContentFullElement =
                funPayDocument.getElementsByClass("page-content-full").first();

        if (pageContentFullElement == null) {
            return false;
        }

        Element pageHeaderElement =
                pageContentFullElement.getElementsByClass("page-header").first();

        return pageHeaderElement != null;
    }
}
