/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
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

package io.helidon.security.jwt;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import io.helidon.common.CollectionsHelper;
import io.helidon.common.Errors;
import io.helidon.common.OptionalHelper;
import io.helidon.security.jwt.jwk.Jwk;

/**
 * JWT token.
 * <p>
 * Representation of a JSON web token (a generic one).
 */
@SuppressWarnings("WeakerAccess") // getters should be public
public class Jwt {
    /*
    Header claims
    */
    private final Map<String, JsonValue> headerClaims;
    //"alg":"RS256"
    // "HS256" - HMAC SHA-256
    // "none" - no signature or encryption
    private final Optional<String> algorithm;
    //"kid":"2f7c5316f335f2786d0d311bb3d385c9e18500db"
    private final Optional<String> keyId;
    //"typ":"JWT" | JWE
    private final Optional<String> type;
    //"cty":"JWT" - for nested tokens only
    private final Optional<String> contentType;

    /*
    Payload claims
     */
    private final Map<String, JsonValue> payloadClaims;

    // iss
    // "iss":"accounts.google.com",
    private final Optional<String> issuer;
    // exp
    // "exp":1495734457,
    /*
     The "exp" (expiration time) claim identifies the expiration time on
     or after which the JWT MUST NOT be accepted for processing.  The
     processing of the "exp" claim requires that the current date/time
     MUST be before the expiration date/time listed in the "exp" claim.
     Implementers MAY provide for some small leeway, usually no more than
     a few minutes, to account for clock skew.  Its value MUST be a number
     containing a NumericDate value.  Use of this claim is OPTIONAL.
     */
    private final Optional<Instant> expirationTime;
    /*
     The "iat" (issued at) claim identifies the time at which the JWT was
   issued.  This claim can be used to determine the age of the JWT.  Its
   value MUST be a number containing a NumericDate value.  Use of this
   claim is OPTIONAL.
     */
    // "iat":1495730857,
    private final Optional<Instant> issueTime;

    /*
    The "nbf" (not before) claim identifies the time before which the JWT
   MUST NOT be accepted for processing.  The processing of the "nbf"
   claim requires that the current date/time MUST be after or equal to
   the not-before date/time listed in the "nbf" claim.  Implementers MAY
   provide for some small leeway, usually no more than a few minutes, to
   account for clock skew.  Its value MUST be a number containing a
   NumericDate value.  Use of this claim is OPTIONAL.
    */
    // "nbf":
    private final Optional<Instant> notBefore;
    /*
    The "sub" (subject) claim identifies the principal that is the
   subject of the JWT.  The claims in a JWT are normally statements
   about the subject.  The subject value MUST either be scoped to be
   locally unique in the context of the issuer or be globally unique.
   The processing of this claim is generally application specific.  The
   "sub" value is a case-sensitive string containing a StringOrURI
   value.  Use of this claim is OPTIONAL.
    */
    // "sub":"106482221621567111461",
    private final Optional<String> subject;

    /*
     Microprofile specification JWT Auth:
     A human readable claim that uniquely identifies the subject or user principal of the token, across the MicroProfile
     services the token will be accessed with.
     */
    // "upn":"john.doe@example.org"
    private final Optional<String> userPrincipal;
    /*
    Microprofile specification JWT Auth:
    The token subject’s group memberships that will be mapped to Java EE style application level roles in the MicroProfile
    service container.
     */
    // "groups": ["normalUsers", "abnormalUsers"]
    private final Optional<List<String>> userGroups;
    // "aud":"1048216952820-6a6ke9vrbjlhngbc0al0dkj9qs9tqbk2.apps.googleusercontent.com",
    /*
   The "aud" (audience) claim identifies the recipients that the JWT is
   intended for.  Each principal intended to process the JWT MUST
   identify itself with a value in the audience claim.  If the principal
   processing the claim does not identify itself with a value in the
   "aud" claim when this claim is present, then the JWT MUST be
   rejected.  In the general case, the "aud" value is an array of case-
   sensitive strings, each containing a StringOrURI value.  In the
   special case when the JWT has one audience, the "aud" value MAY be a
   single case-sensitive string containing a StringOrURI value.  The
   interpretation of audience values is generally application specific.
   Use of this claim is OPTIONAL.
    */
    private final Optional<List<String>> audience;

    /*
    The "jti" (JWT ID) claim provides a unique identifier for the JWT.
   The identifier value MUST be assigned in a manner that ensures that
   there is a negligible probability that the same value will be
   accidentally assigned to a different data object; if the application
   uses multiple issuers, collisions MUST be prevented among values
   produced by different issuers as well.  The "jti" claim can be used
   to prevent the JWT from being replayed.  The "jti" value is a case-
   sensitive string.  Use of this claim is OPTIONAL.
    */
    // "jti":"JWT ID"
    private final Optional<String> jwtId;
    // "email":"tomas.langer@gmail.com",
    private final Optional<String> email;
    // "email_verified":true,
    private final Optional<Boolean> emailVerified;
    // "name":"Tomas Langer",
    private final Optional<String> fullName;
    // "given_name":"Tomas",
    private final Optional<String> givenName;
    // "middle_name":""
    private final Optional<String> middleName;
    // "family_name":"Langer",
    private final Optional<String> familyName;
    // "locale":"en-GB"
    private final Optional<Locale> locale;
    // "nickname":""
    private final Optional<String> nickname;
    // "preferred_username": ""
    private final Optional<String> preferredUsername;
    private final Optional<URI> profile;
    // "picture":"https://lh6.googleusercontent.com/-3GYr_xIFNCU/AAAAAAAAAAI/AAAAAAAAAAA/B39Zgxdo8Kc/s96-c/photo.jpg",
    private final Optional<URI> picture;
    private final Optional<URI> website;
    private final Optional<String> gender; //female/male
    private final Optional<LocalDate> birthday;
    // zoneinfo "Europe/Paris"
    private final Optional<ZoneId> timeZone;
    // phone_number: ""
    private final Optional<String> phoneNumber;
    // phone_number_verified: true
    private final Optional<Boolean> phoneNumberVerified;
    // updated_at: 455874455
    private final Optional<Instant> updatedAt;
    // address: json structure
    private final Optional<JwtUtil.Address> address;
    // scope: space separated scopes
    private final Optional<List<String>> scopes;
    /*
    Access Token hash value. Its value is the base64url encoding of the left-most half of the hash of the octets of the ASCII
    representation of the access_token value, where the hash algorithm used is the hash algorithm used in the alg Header
    Parameter of the ID Token's JOSE Header. For instance, if the alg is RS256, hash the access_token value with SHA-256, then
    take the left-most 128 bits and base64url encode them. The at_hash value is a case sensitive string.
    If the ID Token is issued from the Authorization Endpoint with an access_token value, which is the case for the response_type
    value code id_token token, this is REQUIRED; otherwise, its inclusion is OPTIONAL.
     */
    // "at_hash":"MpgDTpOEkRLKaB6bAz5IwA"
    private final Optional<byte[]> atHash;
    /*
    Code hash value. Its value is the base64url encoding of the left-most half of the hash of the octets of the ASCII
    representation of the code value, where the hash algorithm used is the hash algorithm used in the alg Header Parameter of
    the ID Token's JOSE Header. For instance, if the alg is HS512, hash the code value with SHA-512, then take the left-most
    256 bits and base64url encode them. The c_hash value is a case sensitive string.
    If the ID Token is issued from the Authorization Endpoint with a code, which is the case for the response_type values code
    id_token and code id_token token, this is REQUIRED; otherwise, its inclusion is OPTIONAL.
    */
    // "c_hash"
    private final Optional<byte[]> cHash;
    //Use of the nonce Claim is REQUIRED for hybrid flow.
    private final Optional<String> nonce;

    /**
     * Create a token based on json.
     *
     * @param headerJson  headers
     * @param payloadJson payload
     */
    public Jwt(JsonObject headerJson, JsonObject payloadJson) {
        // generic stuff
        this.headerClaims = getClaims(headerJson);
        this.payloadClaims = getClaims(payloadJson);

        // known headers
        this.algorithm = JwtUtil.getString(headerJson, "alg");
        this.keyId = JwtUtil.getString(headerJson, "kid");
        this.type = JwtUtil.getString(headerJson, "typ");
        this.contentType = JwtUtil.getString(headerJson, "cty");

        // known payload
        this.issuer = JwtUtil.getString(payloadJson, "iss");
        this.expirationTime = JwtUtil.toInstant(payloadJson, "exp");
        this.issueTime = JwtUtil.toInstant(payloadJson, "iat");
        this.notBefore = JwtUtil.toInstant(payloadJson, "nbf");
        this.subject = JwtUtil.getString(payloadJson, "sub");
        JsonValue groups = payloadJson.get("groups");
        if (groups instanceof JsonArray) {
            this.userGroups = JwtUtil.getStrings(payloadJson, "groups");
        } else {
            this.userGroups = JwtUtil.getString(payloadJson, "groups").map(CollectionsHelper::listOf);
        }

        JsonValue aud = payloadJson.get("aud");
        // support both a single string and an array
        if (aud instanceof JsonArray) {
            this.audience = JwtUtil.getStrings(payloadJson, "aud");
        } else {
            this.audience = JwtUtil.getString(payloadJson, "aud").map(CollectionsHelper::listOf);
        }

        this.jwtId = JwtUtil.getString(payloadJson, "jti");
        this.email = JwtUtil.getString(payloadJson, "email");
        this.emailVerified = JwtUtil.toBoolean(payloadJson, "email_verified");
        this.fullName = JwtUtil.getString(payloadJson, "name");
        this.givenName = JwtUtil.getString(payloadJson, "given_name");
        this.middleName = JwtUtil.getString(payloadJson, "middle_name");
        this.familyName = JwtUtil.getString(payloadJson, "family_name");
        this.locale = JwtUtil.toLocale(payloadJson, "locale");
        this.nickname = JwtUtil.getString(payloadJson, "nickname");
        this.preferredUsername = JwtUtil.getString(payloadJson, "preferred_username");
        this.profile = JwtUtil.toUri(payloadJson, "profile");
        this.picture = JwtUtil.toUri(payloadJson, "picture");
        this.website = JwtUtil.toUri(payloadJson, "website");
        this.gender = JwtUtil.getString(payloadJson, "gender");
        this.birthday = JwtUtil.toDate(payloadJson, "birthday");
        this.timeZone = JwtUtil.toTimeZone(payloadJson, "zoneinfo");
        this.phoneNumber = JwtUtil.getString(payloadJson, "phone_number");
        this.phoneNumberVerified = JwtUtil.toBoolean(payloadJson, "phone_number_verified");
        this.updatedAt = JwtUtil.toInstant(payloadJson, "updated_at");
        this.address = JwtUtil.toAddress(payloadJson, "address");
        this.atHash = JwtUtil.getByteArray(payloadJson, "at_hash", "at_hash value");
        this.cHash = JwtUtil.getByteArray(payloadJson, "c_hash", "c_hash value");
        this.nonce = JwtUtil.getString(payloadJson, "nonce");
        this.scopes = JwtUtil.toScopes(payloadJson);
        this.userPrincipal = OptionalHelper.from(JwtUtil.getString(payloadJson, "upn"))
                .or(() -> preferredUsername)
                .or(() -> subject)
                .asOptional();
    }

    private Jwt(Builder builder) {
        // generic stuff
        this.headerClaims = new HashMap<>();
        this.headerClaims.putAll(JwtUtil.transformToJson(builder.headerClaims));
        this.payloadClaims = new HashMap<>();
        this.payloadClaims.putAll(JwtUtil.transformToJson(builder.payloadClaims));

        // known headers
        this.algorithm = OptionalHelper.from(builder.algorithm).or(() -> toOptionalString(builder.payloadClaims, "alg"))
                .asOptional();
        this.keyId = OptionalHelper.from(builder.keyId).or(() -> toOptionalString(builder.payloadClaims, "kid")).asOptional();
        this.type = OptionalHelper.from(builder.type).or(() -> toOptionalString(builder.payloadClaims, "typ")).asOptional();
        this.contentType = OptionalHelper.from(builder.contentType).or(() -> toOptionalString(builder.payloadClaims, "cty"))
                .asOptional();

        // known payload
        this.issuer = builder.issuer;
        this.expirationTime = builder.expirationTime;
        this.issueTime = builder.issueTime;
        this.notBefore = builder.notBefore;
        this.subject = OptionalHelper.from(builder.subject).or(() -> toOptionalString(builder.payloadClaims, "sub")).asOptional();
        this.audience = builder.audience;
        this.jwtId = builder.jwtId;
        this.email = OptionalHelper.from(builder.email).or(() -> toOptionalString(builder.payloadClaims, "email")).asOptional();
        this.emailVerified = OptionalHelper.from(builder.emailVerified)
                .or(() -> getClaim(builder.payloadClaims, "email_verified")).asOptional();
        this.fullName = OptionalHelper.from(builder.fullName).or(() -> toOptionalString(builder.payloadClaims, "name"))
                .asOptional();
        this.givenName = OptionalHelper.from(builder.givenName).or(() -> toOptionalString(builder.payloadClaims, "given_name"))
                .asOptional();
        this.middleName = OptionalHelper.from(builder.middleName).or(() -> toOptionalString(builder.payloadClaims, "middle_name"))
                .asOptional();
        this.familyName = OptionalHelper.from(builder.familyName).or(() -> toOptionalString(builder.payloadClaims, "family_name"))
                .asOptional();
        this.locale = OptionalHelper.from(builder.locale).or(() -> getClaim(builder.payloadClaims, "locale")).asOptional();
        this.nickname = OptionalHelper.from(builder.nickname).or(() -> toOptionalString(builder.payloadClaims, "nickname"))
                .asOptional();
        this.preferredUsername = OptionalHelper.from(builder.preferredUsername)
                .or(() -> toOptionalString(builder.payloadClaims, "preferred_username")).asOptional();
        this.profile = OptionalHelper.from(builder.profile).or(() -> getClaim(builder.payloadClaims, "profile")).asOptional();
        this.picture = OptionalHelper.from(builder.picture).or(() -> getClaim(builder.payloadClaims, "picture")).asOptional();
        this.website = OptionalHelper.from(builder.website).or(() -> getClaim(builder.payloadClaims, "website")).asOptional();
        this.gender = OptionalHelper.from(builder.gender).or(() -> toOptionalString(builder.payloadClaims, "gender"))
                .asOptional();
        this.birthday = OptionalHelper.from(builder.birthday).or(() -> getClaim(builder.payloadClaims, "birthday")).asOptional();
        this.timeZone = OptionalHelper.from(builder.timeZone).or(() -> getClaim(builder.payloadClaims, "zoneinfo")).asOptional();
        this.phoneNumber = OptionalHelper.from(builder.phoneNumber)
                .or(() -> toOptionalString(builder.payloadClaims, "phone_number")).asOptional();
        this.phoneNumberVerified = OptionalHelper.from(builder.phoneNumberVerified)
                .or(() -> getClaim(builder.payloadClaims, "phone_number_verified")).asOptional();

        this.updatedAt = builder.updatedAt;
        this.address = builder.address;
        this.atHash = builder.atHash;
        this.cHash = builder.cHash;
        this.nonce = builder.nonce;
        this.scopes = builder.scopes;

        this.userPrincipal = OptionalHelper.from(builder.userPrincipal)
                .or(() -> toOptionalString(builder.payloadClaims, "upn"))
                .or(() -> preferredUsername)
                .or(() -> subject)
                .asOptional();

        this.userGroups = builder.userGroups;
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<T> getClaim(Map<String, Object> claims, String claim) {
        return Optional.ofNullable((T) claims.get(claim));
    }

    private static Optional<String> toOptionalString(Map<String, Object> claims, String claim) {
        Object value = claims.get(claim);
        if (null == value) {
            return Optional.empty();
        }
        if (value instanceof String) {
            return Optional.of((String) value);
        }
        return Optional.of(String.valueOf(value));
    }

    /**
     * Return a list of validators to validate expiration time, issue time and not-before time.
     *
     * By default the time skew allowed is 5 seconds and all fields are optional.
     *
     * @return list of validators
     */
    public static List<Validator<Jwt>> defaultTimeValidators() {
        List<Validator<Jwt>> validators = new LinkedList<>();
        validators.add(new ExpirationValidator());
        validators.add(new IssueTimeValidator());
        validators.add(new NotBeforeValidator());
        return validators;
    }

    /**
     * Return a list of validators to validate expiration time, issue time and not-before time.
     *
     * @param now            Time that acts as the "now" instant (this allows us to validate if a token was valid at an instant in
     *                       the past
     * @param timeSkewAmount time skew allowed when validating (amount - such as 5)
     * @param timeSkewUnit   time skew allowed when validating (unit - such as {@link ChronoUnit#SECONDS})
     * @param mandatory      whether the field is mandatory. True for mandatory, false for optional (for all default time
     *                       validators)
     * @return list of validators
     */
    public static List<Validator<Jwt>> defaultTimeValidators(Instant now,
                                                             int timeSkewAmount,
                                                             ChronoUnit timeSkewUnit,
                                                             boolean mandatory) {
        List<Validator<Jwt>> validators = new LinkedList<>();
        validators.add(new ExpirationValidator(now, timeSkewAmount, timeSkewUnit, mandatory));
        validators.add(new IssueTimeValidator(now, timeSkewAmount, timeSkewUnit, mandatory));
        validators.add(new NotBeforeValidator(now, timeSkewAmount, timeSkewUnit, mandatory));
        return validators;
    }

    /**
     * Add validator of issuer to the collection of validators.
     *
     * @param validators collection of validators
     * @param issuer     issuer expected to be in the token
     * @param mandatory  whether issuer field is mandatory in the token (true - mandatory, false - optional)
     */
    public static void addIssuerValidator(Collection<Validator<Jwt>> validators, String issuer, boolean mandatory) {
        validators.add(FieldValidator.create(Jwt::getIssuer, "Issuer", issuer, mandatory));
    }

    /**
     * Add validator of audience to the collection of validators.
     *
     * @param validators collection of validators
     * @param audience   audience expected to be in the token
     * @param mandatory  whether the audience field is mandatory in the token
     */
    public static void addAudienceValidator(Collection<Validator<Jwt>> validators, String audience, boolean mandatory) {
        validators.add((jwt, collector) -> {
            Optional<List<String>> jwtAudiences = jwt.getAudience();
            if (jwtAudiences.isPresent()) {
                if (jwtAudiences.get().contains(audience)) {
                    return;
                }
                collector.fatal(jwt, "Audience must contain " + audience + ", yet it is: " + jwtAudiences);
            } else {
                if (mandatory) {
                    collector.fatal(jwt, "Audience is expected to be: " + audience + ", yet no audience in JWT");
                }
            }

        });
    }

    /**
     * Get a builder to create a JWT.
     *
     * @return new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    private Map<String, JsonValue> getClaims(JsonObject headerJson) {
        return Collections.unmodifiableMap(headerJson);
    }

    public Optional<List<String>> getScopes() {
        return scopes.map(Collections::unmodifiableList);
    }

    /**
     * Get a claim by its name from header.
     *
     * @param claim name of a claim
     * @return claim value if present
     */
    public Optional<JsonValue> getHeaderClaim(String claim) {
        return Optional.ofNullable(headerClaims.get(claim));
    }

    /**
     * Get a claim by its name from payload.
     *
     * @param claim name of a claim
     * @return claim value if present
     */
    public Optional<JsonValue> getPayloadClaim(String claim) {
        JsonValue rawValue = payloadClaims.get(claim);

        switch (claim) {
        case "aud":
            return Optional.ofNullable(ensureJsonArray(rawValue));
        default:
            return Optional.ofNullable(rawValue);
        }
    }

    private JsonValue ensureJsonArray(JsonValue rawValue) {
        if (rawValue instanceof JsonArray) {
            return rawValue;
        }

        return Json.createArrayBuilder()
                .add(rawValue)
                .build();
    }

    public Map<String, JsonValue> getPayloadClaims() {
        return Collections.unmodifiableMap(payloadClaims);
    }

    public Optional<String> getAlgorithm() {
        return algorithm;
    }

    public Optional<String> getKeyId() {
        return keyId;
    }

    public Optional<String> getType() {
        return type;
    }

    public Optional<String> getContentType() {
        return contentType;
    }

    public Optional<String> getIssuer() {
        return issuer;
    }

    public Optional<Instant> getExpirationTime() {
        return expirationTime;
    }

    public Optional<Instant> getIssueTime() {
        return issueTime;
    }

    public Optional<Instant> getNotBefore() {
        return notBefore;
    }

    public Optional<String> getSubject() {
        return subject;
    }

    public Optional<String> getUserPrincipal() {
        return userPrincipal;
    }

    public Optional<List<String>> getUserGroups() {
        return userGroups.map(Collections::unmodifiableList);
    }

    public Optional<List<String>> getAudience() {
        return audience;
    }

    public Optional<String> getJwtId() {
        return jwtId;
    }

    public Optional<String> getEmail() {
        return email;
    }

    public Optional<Boolean> getEmailVerified() {
        return emailVerified;
    }

    public Optional<String> getFullName() {
        return fullName;
    }

    public Optional<String> getGivenName() {
        return givenName;
    }

    public Optional<String> getMiddleName() {
        return middleName;
    }

    public Optional<String> getFamilyName() {
        return familyName;
    }

    public Optional<Locale> getLocale() {
        return locale;
    }

    public Optional<String> getNickname() {
        return nickname;
    }

    public Optional<String> getPreferredUsername() {
        return preferredUsername;
    }

    public Optional<URI> getProfile() {
        return profile;
    }

    public Optional<URI> getPicture() {
        return picture;
    }

    public Optional<URI> getWebsite() {
        return website;
    }

    public Optional<String> getGender() {
        return gender;
    }

    public Optional<LocalDate> getBirthday() {
        return birthday;
    }

    public Optional<ZoneId> getTimeZone() {
        return timeZone;
    }

    public Optional<String> getPhoneNumber() {
        return phoneNumber;
    }

    public Optional<Boolean> getPhoneNumberVerified() {
        return phoneNumberVerified;
    }

    public Optional<Instant> getUpdatedAt() {
        return updatedAt;
    }

    public Optional<JwtUtil.Address> getAddress() {
        return address;
    }

    public Optional<byte[]> getAtHash() {
        return atHash;
    }

    public Optional<byte[]> getCHash() {
        return cHash;
    }

    public Optional<String> getNonce() {
        return nonce;
    }

    /**
     * Create a JSON header object.
     *
     * @return JsonObject for header
     */
    public JsonObject getHeaderJson() {
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        headerClaims.forEach(objectBuilder::add);

        algorithm.ifPresent(it -> objectBuilder.add("alg", it));
        keyId.ifPresent(it -> objectBuilder.add("kid", it));
        type.ifPresent(it -> objectBuilder.add("typ", it));
        contentType.ifPresent(it -> objectBuilder.add("cty", it));

        return objectBuilder.build();
    }

    /**
     * Create a JSON payload object.
     *
     * @return JsonObject for payload
     */
    public JsonObject getPayloadJson() {
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        payloadClaims.forEach(objectBuilder::add);

        // known payload
        this.issuer.ifPresent(it -> objectBuilder.add("iss", it));
        this.expirationTime.ifPresent(it -> objectBuilder.add("exp", it.getEpochSecond()));
        this.issueTime.ifPresent(it -> objectBuilder.add("iat", it.getEpochSecond()));
        this.notBefore.ifPresent(it -> objectBuilder.add("nbf", it.getEpochSecond()));
        this.subject.ifPresent(it -> objectBuilder.add("sub", it));
        this.userPrincipal.ifPresent(it -> objectBuilder.add("upn", it));
        this.userGroups.ifPresent(it -> {
            JsonArrayBuilder jab = Json.createArrayBuilder();
            it.forEach(jab::add);
            objectBuilder.add("groups", jab);
        });
        this.audience.ifPresent(it -> {
            JsonArrayBuilder jab = Json.createArrayBuilder();
            it.forEach(jab::add);
            objectBuilder.add("aud", jab);
        });
        this.jwtId.ifPresent(it -> objectBuilder.add("jti", it));
        this.email.ifPresent(it -> objectBuilder.add("email", it));
        this.emailVerified.ifPresent(it -> objectBuilder.add("email_verified", it));
        this.fullName.ifPresent(it -> objectBuilder.add("name", it));
        this.givenName.ifPresent(it -> objectBuilder.add("given_name", it));
        this.middleName.ifPresent(it -> objectBuilder.add("middle_name", it));
        this.familyName.ifPresent(it -> objectBuilder.add("family_name", it));
        this.locale.ifPresent(it -> objectBuilder.add("locale", it.toLanguageTag()));
        this.nickname.ifPresent(it -> objectBuilder.add("nickname", it));
        this.preferredUsername.ifPresent(it -> objectBuilder.add("preferred_username", it));
        this.profile.ifPresent(it -> objectBuilder.add("profile", it.toASCIIString()));
        this.picture.ifPresent(it -> objectBuilder.add("picture", it.toASCIIString()));
        this.website.ifPresent(it -> objectBuilder.add("website", it.toASCIIString()));
        this.gender.ifPresent(it -> objectBuilder.add("gender", it));
        this.birthday.ifPresent(it -> objectBuilder.add("birthday", JwtUtil.toDate(it)));
        this.timeZone.ifPresent(it -> objectBuilder.add("zoneinfo", it.getId()));
        this.phoneNumber.ifPresent(it -> objectBuilder.add("phone_number", it));
        this.phoneNumberVerified.ifPresent(it -> objectBuilder.add("phone_number_verified", it));
        this.updatedAt.ifPresent(it -> objectBuilder.add("updated_at", it.getEpochSecond()));
        this.address.ifPresent(it -> objectBuilder.add("address", it.getJson()));
        this.atHash.ifPresent(it -> objectBuilder.add("at_hash", JwtUtil.base64Url(it)));
        this.cHash.ifPresent(it -> objectBuilder.add("c_hash", JwtUtil.base64Url(it)));
        this.nonce.ifPresent(it -> objectBuilder.add("nonce", it));

        return objectBuilder.build();
    }

    /**
     * Validate this JWT against provided validators.
     *
     * @param validators Validators to validate with. Obtain them through (e.g.) {@link #defaultTimeValidators()}
     *                   , {@link #addAudienceValidator(Collection, String, boolean)}
     *                   , {@link #addIssuerValidator(Collection, String, boolean)}
     * @return errors instance to check if valid and access error messages
     */
    public Errors validate(List<Validator<Jwt>> validators) {
        Errors.Collector collector = Errors.collector();
        validators.forEach(it -> it.validate(this, collector));
        return collector.collect();
    }

    /**
     * Validates all default values.
     * Values validated:
     * <ul>
     * <li>{@link #getExpirationTime() Expiration time} if defined</li>
     * <li>{@link #getIssueTime() Issue time} if defined</li>
     * <li>{@link #getNotBefore() Not before time} if defined</li>
     * <li>{@link #getIssuer()} Issuer} if defined</li>
     * <li>{@link #getAudience() Audience} if defined</li>
     * </ul>
     *
     * @param issuer   validates that this JWT was issued by this issuer. Setting this to non-null value will make
     *                 issuer claim mandatory
     * @param audience validates that this JWT was issued for this audience. Setting this to non-null value will make
     *                 audience claim mandatory
     * @return errors instance to check for validation result
     */
    public Errors validate(String issuer, String audience) {
        List<Validator<Jwt>> validators = defaultTimeValidators();
        if (null != issuer) {
            addIssuerValidator(validators, issuer, true);
        }
        if (null != audience) {
            addAudienceValidator(validators, audience, true);
        }
        return validate(validators);
    }

    private abstract static class OptionalValidator {
        private boolean mandatory;

        OptionalValidator() {
            this.mandatory = false;
        }

        OptionalValidator(boolean mandatory) {
            this.mandatory = mandatory;
        }

        <T> Optional<T> validate(String name, Optional<T> optional, Errors.Collector collector) {
            if (mandatory && !optional.isPresent()) {
                collector.fatal("Field " + name + " is mandatory, yet not defined in JWT");
            }
            return optional;
        }
    }

    private abstract static class InstantValidator extends OptionalValidator {
        private final Instant instant;
        private final long allowedTimeSkewAmount;
        private final TemporalUnit allowedTimeSkewUnit;

        private InstantValidator() {
            this.instant = Instant.now();
            this.allowedTimeSkewAmount = 5;
            this.allowedTimeSkewUnit = ChronoUnit.SECONDS;
        }

        private InstantValidator(Instant instant, int allowedTimeSkew, TemporalUnit allowedTimeSkewUnit, boolean mandatory) {
            super(mandatory);

            this.instant = instant;
            this.allowedTimeSkewAmount = allowedTimeSkew;
            this.allowedTimeSkewUnit = allowedTimeSkewUnit;
        }

        Instant latest() {
            return instant.plus(allowedTimeSkewAmount, allowedTimeSkewUnit);
        }

        Instant earliest() {
            return instant.minus(allowedTimeSkewAmount, allowedTimeSkewUnit);
        }
    }

    /**
     * Validator of a string field obtained from a JWT.
     */
    public static class FieldValidator extends OptionalValidator implements Validator<Jwt> {
        private final Function<Jwt, Optional<String>> fieldAccessor;
        private String expectedValue;
        private String fieldName;

        private FieldValidator(Function<Jwt, Optional<String>> fieldAccessor,
                               String fieldName,
                               String expectedValue,
                               boolean mandatory) {
            super(mandatory);
            this.fieldAccessor = fieldAccessor;
            this.fieldName = fieldName;
            this.expectedValue = expectedValue;
        }

        /**
         * A generic optional field validator based on a function to get the field.
         *
         * @param fieldAccessor function to extract field from JWT
         * @param name          descriptive name of the field
         * @param expectedValue value to expect
         * @return validator instance
         */
        public static FieldValidator create(Function<Jwt, Optional<String>> fieldAccessor,
                                            String name,
                                            String expectedValue) {

            return create(fieldAccessor, name, expectedValue, false);
        }

        /**
         * A generic field validator based on a function to get the field.
         *
         * @param fieldAccessor function to extract field from JWT
         * @param name          descriptive name of the field
         * @param expectedValue value to expect
         * @param mandatory     true for mandatory, false for optional
         * @return validator instance
         */
        public static FieldValidator create(Function<Jwt, Optional<String>> fieldAccessor,
                                            String name,
                                            String expectedValue,
                                            boolean mandatory) {

            return new FieldValidator(fieldAccessor, name, expectedValue, mandatory);
        }

        /**
         * An optional header field validator.
         *
         * @param fieldKey      name of the header claim
         * @param name          descriptive name of the field
         * @param expectedValue value to expect
         * @return validator instance
         */
        public static FieldValidator createForHeader(String fieldKey,
                                                     String name,
                                                     String expectedValue) {

            return createForHeader(fieldKey, name, expectedValue, false);
        }

        /**
         * A header field validator.
         *
         * @param fieldKey      name of the header claim
         * @param name          descriptive name of the field
         * @param expectedValue value to expect
         * @param mandatory     whether the field is mandatory or optional
         * @return validator instance
         */
        public static FieldValidator createForHeader(String fieldKey,
                                                     String name,
                                                     String expectedValue,
                                                     boolean mandatory) {

            return create(jwt -> jwt.getHeaderClaim(fieldKey)
                                  .map(it -> ((JsonString) it).getString()),
                          name,
                          expectedValue,
                          mandatory);
        }

        /**
         * An optional payload field validator.
         *
         * @param fieldKey      name of the payload claim
         * @param name          descriptive name of the field
         * @param expectedValue value to expect
         * @return validator instance
         */
        public static FieldValidator createForPayload(String fieldKey,
                                                      String name,
                                                      String expectedValue) {

            return createForPayload(fieldKey, name, expectedValue, false);
        }

        /**
         * A payload field validator.
         *
         * @param fieldKey      name of the payload claim
         * @param name          descriptive name of the field
         * @param expectedValue value to expect
         * @param mandatory     whether the field is mandatory or optional
         * @return validator instance
         */
        public static FieldValidator createForPayload(String fieldKey,
                                                      String name,
                                                      String expectedValue,
                                                      boolean mandatory) {
            return create(jwt -> jwt.getPayloadClaim(fieldKey)
                                  .map(it -> ((JsonString) it).getString()),
                          name,
                          expectedValue,
                          false);
        }

        @Override
        public void validate(Jwt token, Errors.Collector collector) {
            super.validate(fieldName, fieldAccessor.apply(token), collector)
                    .ifPresent(it -> {
                        if (!expectedValue.equals(it)) {
                            collector.fatal(token,
                                            "Expected value of field \"" + fieldName + "\" was \"" + expectedValue + "\", but "
                                                    + "actual value is: \"" + it);
                        }
                    });
        }
    }

    /**
     * Validator of issue time claim.
     */
    public static class IssueTimeValidator extends InstantValidator implements Validator<Jwt> {
        /**
         * New instance with default values (allowed time skew 5 seconds, optional).
         */
        public IssueTimeValidator() {
        }

        /**
         * New instance with explicit values.
         *
         * @param now                 time to validate against (to be able to validate past tokens)
         * @param allowedTimeSkew     allowed time skew amount (such as 5)
         * @param allowedTimeSkewUnit allowed time skew unit (such as {@link ChronoUnit#SECONDS}
         * @param mandatory           true for mandatory, false for optional
         */
        public IssueTimeValidator(Instant now, int allowedTimeSkew, TemporalUnit allowedTimeSkewUnit, boolean mandatory) {
            super(now, allowedTimeSkew, allowedTimeSkewUnit, mandatory);
        }

        @Override
        public void validate(Jwt token, Errors.Collector collector) {
            token.getIssueTime().ifPresent(it -> {
                // must be issued in the past
                if (latest().isBefore(it)) {
                    collector.fatal(token, "Token was not issued in the past: " + it);
                }
            });
        }
    }

    /**
     * Validator of expiration claim.
     */
    public static class ExpirationValidator extends InstantValidator implements Validator<Jwt> {
        /**
         * New instance with default values (allowed time skew 5 seconds, optional).
         */
        public ExpirationValidator() {
        }

        /**
         * New instance with explicit values.
         *
         * @param now                 time to validate against (to be able to validate past tokens)
         * @param allowedTimeSkew     allowed time skew amount (such as 5)
         * @param allowedTimeSkewUnit allowed time skew unit (such as {@link ChronoUnit#SECONDS}
         * @param mandatory           true for mandatory, false for optional
         */
        public ExpirationValidator(Instant now, int allowedTimeSkew, TemporalUnit allowedTimeSkewUnit, boolean mandatory) {
            super(now, allowedTimeSkew, allowedTimeSkewUnit, mandatory);
        }

        @Override
        public void validate(Jwt token, Errors.Collector collector) {
            token.getExpirationTime().ifPresent(it -> {
                if (earliest().isAfter(it)) {
                    collector.fatal(token, "Token no longer valid, expiration: " + it);
                }
            });
        }
    }

    /**
     * Validator of not before claim.
     */
    public static class NotBeforeValidator extends InstantValidator implements Validator<Jwt> {
        /**
         * New instance with default values (allowed time skew 5 seconds, optional).
         */
        public NotBeforeValidator() {
        }

        /**
         * New instance with explicit values.
         *
         * @param now                 time to validate against (to be able to validate past tokens)
         * @param allowedTimeSkew     allowed time skew amount (such as 5)
         * @param allowedTimeSkewUnit allowed time skew unit (such as {@link ChronoUnit#SECONDS}
         * @param mandatory           true for mandatory, false for optional
         */
        public NotBeforeValidator(Instant now, int allowedTimeSkew, TemporalUnit allowedTimeSkewUnit, boolean mandatory) {
            super(now, allowedTimeSkew, allowedTimeSkewUnit, mandatory);
        }

        @Override
        public void validate(Jwt token, Errors.Collector collector) {
            token.getNotBefore().ifPresent(it -> {
                if (latest().isBefore(it)) {
                    collector.fatal(token, "Token not yet valid, not before: " + it);
                }
            });
        }
    }

    /**
     * Builder of a {@link Jwt}.
     */
    public static class Builder implements io.helidon.common.Builder<Jwt> {
        private final Map<String, Object> headerClaims = new HashMap<>();
        private final Map<String, Object> payloadClaims = new HashMap<>();
        private Optional<String> algorithm = Optional.empty();
        private Optional<String> keyId = Optional.empty();
        private Optional<String> type = Optional.empty();
        private Optional<String> contentType = Optional.empty();
        private Optional<String> issuer = Optional.empty();
        private Optional<Instant> expirationTime = Optional.empty();
        private Optional<Instant> issueTime = Optional.empty();
        private Optional<Instant> notBefore = Optional.empty();
        private Optional<String> subject = Optional.empty();
        private Optional<String> userPrincipal = Optional.empty();
        private Optional<List<String>> userGroups = Optional.empty();
        private Optional<List<String>> audience = Optional.empty();
        private Optional<String> jwtId = Optional.empty();
        private Optional<String> email = Optional.empty();
        private Optional<Boolean> emailVerified = Optional.empty();
        private Optional<String> fullName = Optional.empty();
        private Optional<String> givenName = Optional.empty();
        private Optional<String> middleName = Optional.empty();
        private Optional<String> familyName = Optional.empty();
        private Optional<Locale> locale = Optional.empty();
        private Optional<String> nickname = Optional.empty();
        private Optional<String> preferredUsername = Optional.empty();
        private Optional<URI> profile = Optional.empty();
        private Optional<URI> picture = Optional.empty();
        private Optional<URI> website = Optional.empty();
        private Optional<String> gender = Optional.empty();
        private Optional<LocalDate> birthday = Optional.empty();
        private Optional<ZoneId> timeZone = Optional.empty();
        private Optional<String> phoneNumber = Optional.empty();
        private Optional<Boolean> phoneNumberVerified = Optional.empty();
        private Optional<Instant> updatedAt = Optional.empty();
        private Optional<JwtUtil.Address> address = Optional.empty();
        private Optional<byte[]> atHash = Optional.empty();
        private Optional<byte[]> cHash = Optional.empty();
        private Optional<String> nonce = Optional.empty();
        private Optional<List<String>> scopes = Optional.empty();

        private Builder() {
        }

        /**
         * Key id to be used to sign/verify this JWT.
         *
         * @param keyId key id (pointing to a JWK)
         * @return updated builder instance
         */
        public Builder keyId(String keyId) {
            this.keyId = Optional.of(keyId);
            return this;
        }

        /**
         * Type of this JWT.
         *
         * @param type type definition (JWT, JWE)
         * @return updated builder instance
         */
        public Builder type(String type) {
            this.type = Optional.ofNullable(type);
            return this;
        }

        /**
         * OAuth2 scope claims to set.
         *
         * @param scopes scope claims to add to a JWT
         * @return update builder instance
         */
        public Builder scopes(List<String> scopes) {
            List<String> list = new LinkedList<>(scopes);
            this.scopes = Optional.of(list);
            return this;
        }

        /**
         * OAuth2 scope claim to add.
         *
         * @param scope scope claim to add to a JWT
         * @return updated builder instance
         */
        public Builder addScope(String scope) {
            this.scopes = OptionalHelper.from(this.scopes).or(() -> Optional.of(new LinkedList<>())).asOptional();
            this.scopes.ifPresent(it -> it.add(scope));
            return this;
        }

        /**
         * A user group claim to add.
         * Based on Microprofile JWT Auth specification, uses claim "groups".
         *
         * @param group group name to add to the list of groups
         * @return updated builder instance
         */
        public Builder addUserGroup(String group) {
            this.userGroups = OptionalHelper.from(this.userGroups).or(() -> Optional.of(new LinkedList<>())).asOptional();
            this.userGroups.ifPresent(it -> it.add(group));
            return this;
        }

        /**
         * This header claim should only be used when nesting or encrypting JWT.
         * See <a href="https://tools.ietf.org/html/rfc7519#section-5.2">RFC 7519, section 5.2</a>.
         *
         * @param contentType content type to use, use "JWT" if nested
         * @return updated builder instance
         */
        public Builder contentType(String contentType) {
            this.contentType = Optional.ofNullable(contentType);
            return this;
        }

        /**
         * Add a generic header claim.
         *
         * @param claim claim to add
         * @param value value of the header claim
         * @return updated builder instance
         */
        public Builder addHeaderClaim(String claim, Object value) {
            addClaim(headerClaims, claim, value);
            return this;
        }

        private void addClaim(Map<String, Object> claims, String claim, Object value) {
            claims.put(claim, value);
        }

        /**
         * Add a generic payload claim.
         *
         * @param claim claim to add
         * @param value value of the payload claim
         * @return updated builder instance
         */
        public Builder addPayloadClaim(String claim, Object value) {
            addClaim(payloadClaims, claim, value);
            return this;
        }

        /**
         * The "alg" claim is used to define the signature algorithm.
         * Note that this algorithm should be the same as is supported by
         * the JWK used to sign (or verify) the JWT.
         *
         * @param algorithm algorithm to use, {@link Jwk#ALG_NONE} for none
         * @return updated builder instance
         */
        public Builder algorithm(String algorithm) {
            this.algorithm = Optional.of(algorithm);
            return this;
        }

        /**
         * The issuer claim identifies the principal that issued the JWT.
         *
         * See <a href="https://tools.ietf.org/html/rfc7519#section-4.1.1">RFC 7519, section 4.1.1</a>.
         *
         * @param issuer issuer name or URL
         * @return updated builder instance
         */
        public Builder issuer(String issuer) {
            this.issuer = Optional.ofNullable(issuer);
            return this;
        }

        /**
         * The expiration time defines the time that this JWT loses validity.
         *
         * See <a href="https://tools.ietf.org/html/rfc7519#section-4.1.4">RFC 7519, section 4.1.4</a>.
         *
         * @param expirationTime when this JWT expires
         * @return updated builder instance
         */
        public Builder expirationTime(Instant expirationTime) {
            this.expirationTime = Optional.ofNullable(expirationTime);
            return this;
        }

        /**
         * The issue time defines the time that this JWT was issued.
         *
         * See <a href="https://tools.ietf.org/html/rfc7519#section-4.1.6">RFC 7519, section 4.1.6</a>.
         *
         * @param issueTime when this JWT was created
         * @return updated builder instance
         */
        public Builder issueTime(Instant issueTime) {
            this.issueTime = Optional.ofNullable(issueTime);
            return this;
        }

        /**
         * The not before time defines the time that this JWT starts being valid.
         *
         * See <a href="https://tools.ietf.org/html/rfc7519#section-4.1.5">RFC 7519, section 4.1.5</a>.
         *
         * @param notBefore JWT is not valid before this time
         * @return updated builder instance
         */
        public Builder notBefore(Instant notBefore) {
            this.notBefore = Optional.ofNullable(notBefore);
            return this;
        }

        /**
         * Subject defines the principal this JWT was issued for (e.g. user id).
         *
         * See <a href="https://tools.ietf.org/html/rfc7519#section-4.1.2">RFC 7519, section 4.1.2</a>.
         *
         * @param subject subject of this JWt
         * @return updated builder instance
         */
        public Builder subject(String subject) {
            this.subject = Optional.ofNullable(subject);
            return this;
        }

        /**
         * User principal claim as defined by Microprofile JWT Auth spec.
         * Uses "upn" claim.
         *
         * @param principal name of the principal, falls back to {@link #preferredUsername(String)} and then to
         *                  {@link #subject(String)}
         * @return updated builder instance
         */
        public Builder userPrincipal(String principal) {
            this.userPrincipal = Optional.ofNullable(principal);
            return this;
        }

        /**
         * Audience identifies the expected recipients of this JWT (optional).
         * Multiple audience may be added
         *
         * See <a href="https://tools.ietf.org/html/rfc7519#section-4.1.3">RFC 7519, section 4.1.3</a>.
         *
         * @param audience audience of this JWT
         * @return updated builder instance
         */
        public Builder audience(String audience) {
            List<String> audiences = this.audience.orElse(new LinkedList<>());
            audiences.add(audience);
            this.audience = Optional.of(audiences);
            return this;
        }

        /**
         * A unique identifier of this JWT (optional) - must be unique across issuers.
         *
         * See <a href="https://tools.ietf.org/html/rfc7519#section-4.1.7">RFC 7519, section 4.1.7</a>.
         *
         * @param jwtId unique identifier
         * @return updated builder instance
         */
        public Builder jwtId(String jwtId) {
            this.jwtId = Optional.ofNullable(jwtId);
            return this;
        }

        /**
         * Email claim.
         *
         * @param email email claim for this JWT's subject
         * @return updated builder instance
         */
        public Builder email(String email) {
            this.email = Optional.ofNullable(email);
            return this;
        }

        /**
         * Claim defining whether e-mail is verified or not.
         *
         * @param emailVerified true if verified
         * @return updated builder instance
         */
        public Builder emailVerified(Boolean emailVerified) {
            this.emailVerified = Optional.ofNullable(emailVerified);
            return this;
        }

        /**
         * Full name of subject.
         *
         * @param fullName full name of the subject
         * @return updated builder instance
         */
        public Builder fullName(String fullName) {
            this.fullName = Optional.ofNullable(fullName);
            return this;
        }

        /**
         * Given name of subject (first name).
         *
         * @param givenName given name of the subject
         * @return updated builder instance
         */
        public Builder givenName(String givenName) {
            this.givenName = Optional.ofNullable(givenName);
            return this;
        }

        /**
         * Middle name of subject.
         *
         * @param middleName middle name of the subject
         * @return updated builder instance
         */
        public Builder middleName(String middleName) {
            this.middleName = Optional.ofNullable(middleName);
            return this;
        }

        /**
         * Family name of subject (surname).
         *
         * @param familyName family name of the subject
         * @return updated builder instance
         */
        public Builder familyName(String familyName) {
            this.familyName = Optional.ofNullable(familyName);
            return this;
        }

        /**
         * Locale of the subject.
         *
         * @param locale locale to use
         * @return updated builder instance
         */
        public Builder locale(Locale locale) {
            this.locale = Optional.ofNullable(locale);
            return this;
        }

        /**
         * Nickname of the subject.
         *
         * @param nickname nickname
         * @return updated builder instance
         */
        public Builder nickname(String nickname) {
            this.nickname = Optional.ofNullable(nickname);
            return this;
        }

        /**
         * Preferred username of the subject.
         *
         * @param preferredUsername username to view
         * @return updated builder instance
         */
        public Builder preferredUsername(String preferredUsername) {
            this.preferredUsername = Optional.ofNullable(preferredUsername);
            return this;
        }

        /**
         * Profile URI of the subject.
         *
         * @param profile link to profile of subject
         * @return updated builder instance
         */
        public Builder profile(URI profile) {
            this.profile = Optional.ofNullable(profile);
            return this;
        }

        /**
         * Profile picture URI of the subject.
         *
         * @param picture link to picture of subject
         * @return updated builder instance
         */
        public Builder picture(URI picture) {
            this.picture = Optional.ofNullable(picture);
            return this;
        }

        /**
         * Website URI of the subject.
         *
         * @param website link to website of subject
         * @return updated builder instance
         */
        public Builder website(URI website) {
            this.website = Optional.ofNullable(website);
            return this;
        }

        /**
         * Gender of the subject.
         * As this is an extension (e.g. a custom claim) used by some of the
         * issuers, the content may be arbitrary, though base values are male and female.
         *
         * @param gender gender to use
         * @return updated builder instance
         */
        public Builder gender(String gender) {
            this.gender = Optional.ofNullable(gender);
            return this;
        }

        /**
         * Birthday of the subject.
         *
         * @param birthday birthday
         * @return updated builder instance
         */
        public Builder birthday(LocalDate birthday) {
            this.birthday = Optional.ofNullable(birthday);
            return this;
        }

        /**
         * Time zone of the subject.
         *
         * @param timeZone time zone
         * @return updated builder instance
         */
        public Builder timeZone(ZoneId timeZone) {
            this.timeZone = Optional.ofNullable(timeZone);
            return this;
        }

        /**
         * Phone number of the subject.
         *
         * @param phoneNumber phone number
         * @return updated builder instance
         */
        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = Optional.ofNullable(phoneNumber);
            return this;
        }

        /**
         * Whether the phone number is verified or not.
         *
         * @param phoneNumberVerified true if number is verified
         * @return updated builder instance
         */
        public Builder phoneNumberVerified(Boolean phoneNumberVerified) {
            this.phoneNumberVerified = Optional.ofNullable(phoneNumberVerified);
            return this;
        }

        /**
         * Last time the subject's record was updated.
         *
         * @param updatedAt instant of update
         * @return updated builder instance
         */
        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = Optional.ofNullable(updatedAt);
            return this;
        }

        /**
         * Address of the subject.
         *
         * @param address address to use
         * @return updated builder instance
         */
        public Builder address(JwtUtil.Address address) {
            this.address = Optional.ofNullable(address);
            return this;
        }

        /**
         * Access Token hash value. Its value is the bytes of the left-most half of the hash of the octets of the
         * ASCII representation of the access_token value, where the hash algorithm used is the hash algorithm used in the
         * alg Header Parameter of the ID Token's JOSE Header. For instance, if the alg is RS256, hash the access_token value
         * with SHA-256, then take the left-most 128 bits and set them here.
         * If the ID Token is issued from the Authorization Endpoint with an access_token value, which is the case for the
         * response_type value code id_token token, this is REQUIRED; otherwise, its inclusion is OPTIONAL.
         *
         * See <a href="http://openid.net/specs/openid-connect-core-1_0.html#CodeIDToken">OIDC 1.0 section 3.1.3.6</a>.
         *
         * @param atHash hash to use (explicit). If not defined, it will be computed if needed.
         * @return updated builder instance
         */
        public Builder atHash(byte[] atHash) {
            this.atHash = Optional.ofNullable(atHash);
            return this;
        }

        /**
         * Code hash value. Its value is the bytes of the left-most half of the hash of the octets of the ASCII
         * representation of the code value, where the hash algorithm used is the hash algorithm used in the alg Header Parameter
         * of the ID Token's JOSE Header. For instance, if the alg is HS512, hash the code value with SHA-512, then take the
         * left-most 256 bits.
         * If the ID Token is issued from the Authorization Endpoint with a code, which is the case for the response_type values
         * code id_token and code id_token token, this is REQUIRED; otherwise, its inclusion is OPTIONAL.
         *
         * @param cHash hash bytes (explicit). If not defined, it will be computed if needed.
         * @return updated builder instance
         */
        public Builder cHash(byte[] cHash) {
            this.cHash = Optional.ofNullable(cHash);
            return this;
        }

        /**
         * Nonce value is used to prevent replay attacks and must be returned if it was sent in authentication request.
         *
         * @param nonce nonce value
         * @return updated builder instance
         */
        public Builder nonce(String nonce) {
            this.nonce = Optional.ofNullable(nonce);
            return this;
        }

        /**
         * Build and instance of the {@link Jwt}.
         *
         * @return a new token instance
         */
        @Override
        public Jwt build() {
            return new Jwt(this);
        }

        /**
         * Remove a payload claim by its name.
         *
         * @param name name of the claim to remove
         * @return updated builder instance
         */
        public Builder removePayloadClaim(String name) {
            this.payloadClaims.remove(name);
            return this;
        }
    }
}
