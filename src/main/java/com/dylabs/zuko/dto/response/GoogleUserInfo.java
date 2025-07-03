package com.dylabs.zuko.dto.response;

public record GoogleUserInfo(
        String sub,
        String name,
        String givenName,
        String familyName,
        String picture,
        String email,
        Boolean emailVerified,
        String locale
) {
}
