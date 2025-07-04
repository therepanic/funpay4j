![FunPay4j Logo](./funpay4j.jpg)

[![CI](https://github.com/therepanic/funpay4j/actions/workflows/build.yml/badge.svg)](https://github.com/therepanic/funpay4j/actions/workflows/build.yml)
[![GitHub release](https://img.shields.io/github/v/release/therepanic/funpay4j)](https://github.com/therepanic/funpay4j/releases)
[![JitPack](https://jitpack.io/v/therepanic/funpay4j.svg)](https://jitpack.io/#therepanic/funpay4j)
[![Apache License](http://img.shields.io/badge/license-ASL-blue.svg)](https://github.com/therepanic/funpay4j/blob/master/COPYING)

---

The funpay4j library is a Java wrapper for FunPay private api
## Table of contents
- [Install](#install)
    - [Requirements](#requirements)
- [Overview](#overview)
- [Usage](#usage)
    - [Terms and Conditions](#terms-and-conditions)
    - [Quick Usage](#quick-usage)
        - [Create FunPayExecutor](#create-funpayexecutor)
        - [Get seller](#get-seller)
        - [Create AuthorizedFunPayExecutor](#create-authorizedfunpayexecutor)
        - [Raise all offers](#raise-all-offers)
- [Contributing](#contributing)

# Install

#### Example for gradle:
```java
repositories {
    maven {
        url 'https://jitpack.io'
    }
}

dependencies {
    implementation 'com.github.therepanic.funpay4j:core:1.0.6'
}
```

#### Example for maven:
```xml
<repositories>
    <repository>
        <id>jitpack</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
<dependency>
    <groupId>com.github.therepanic.funpay4j</groupId>
    <artifactId>core</artifactId>
    <version>1.0.6</version>
</dependency>
</dependencies>
```

## Requirements
This project depends on
- Java 8+
- Jsoup
- OkHttpClient
- Gson
- ProjectLombok

# Overview
This Java library allows you to interact with the FunPay. Most of the functionality that is present on the site is here

# Usage
## Terms and Conditions
This library is for personal use and for educational purposes due to the fact that there is no official FunPay api at this time

- Please, do not use this library to spam (botting, spam messaging, etc...)
- Use reasonable (human) delay in between sending requests

Contributors are not responsible for usage and maintainability. Due to the nature of this project, some features of the library are not guaranteed as they make change and break in the future. This library is licensed under ASL

## Quick Usage

### Create FunPayExecutor
Basic creation of FunPayExecutor
#### *Example:*
```java
FunPayExecutor funPayExecutor = new FunPayExecutor();
```

### Get seller
Basic seller's receipt
#### *Example:*
```java
FunPayExecutor funPayExecutor = ...

User user = null;

try {
    user = funPayExecutor.execute(GetUser.builder().userId(1).build());
} catch (FunPayApiException e) {
    throw new RuntimeException(e);
}

Seller seller = null;

if (user instanceof Seller) {
    seller = (Seller) user;
}
```

### Create AuthorizedFunPayExecutor
Basic creation of AuthorizedFunPayExecutor
#### *Example:*
```java
AuthorizedFunPayExecutor funPayExecutor = new AuthorizedFunPayExecutor("test-golden-key");
```

### Raise all offers
Basic raising of all offers
#### *Example:*
```java
AuthorizedFunPayExecutor funPayExecutor = ...

try {
    funPayExecutor.execute(RaiseAllOffers.builder().gameId(41).lotId(211).build());
} catch (FunPayApiException e) {
    throw new RuntimeException(e);
} catch (OfferAlreadyRaisedException e) {
    System.out.println("The offer has already been raised!!!");
}
```

# Contributing
If you have an addition you would like to make, please do not hesitate to make a pull request!
