# Identity Module

The `identity` module is the part of Workzeno's backend responsible for
user accounts, registration, authentication, access-token validation,
refresh-session lifecycle, and the current-user endpoint.

It is organized as a **feature-oriented package structure**:
authentication endpoints and their supporting workflow live under
`auth`, user-account data and account lookup live under `user`,
cross-cutting Spring Security/JWT components live under `security`, and
initial administrative setup lives under `bootstrap`.



## 1. What the module is responsible for

At a high level, Identity owns four related responsibilities:

1.  **Account management** --- represent users, roles, and account
    status; persist accounts; expose the authenticated user's profile.
2.  **Authentication** --- register users and verify email/password
    credentials during login.
3.  **Token and session security** --- issue signed JWT access tokens,
    validate incoming bearer tokens through Spring Security Resource
    Server, and rotate opaque refresh tokens backed by database
    sessions.
4.  **Administrative bootstrap** --- optionally create or reset an
    initial administrator during application startup, according to
    configuration.

### Main package map

``` text
identity/
├── auth/
│   ├── controller/
│   ├── domain/
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   ├── exception/
│   ├── repository/
│   ├── service/
│   │   └── result/
│   └── validation/
├── bootstrap/
├── security/
│   ├── config/
│   ├── principal/
│   ├── properties/
│   └── service/
└── user/
    ├── controller/
    ├── domain/
    ├── dto/
    ├── repository/
    └── service/
```

  -----------------------------------------------------------------------
  Package                             Responsibility
  ----------------------------------- -----------------------------------
  `auth.controller`                   HTTP endpoints for register, login,
                                      refresh, and logout.

  `auth.dto.request`                  Validated request payload types
                                      accepted by authentication
                                      endpoints.

  `auth.dto.response`                 Response payload types returned by
                                      registration and authentication.

  `auth.domain`                       Persistence model for refresh-token
                                      sessions.

  `auth.repository`                   Database access for refresh
                                      sessions, including the
                                      lookup/locking operations used
                                      during rotation.

  `auth.service`                      Orchestrates registration, login,
                                      refresh, and logout use cases.

  `auth.service.result`               Internal result type used to pass
                                      both the public authentication
                                      response and raw refresh token from
                                      service to controller.

  `auth.validation`                   Validates the origin of
                                      browser-originated refresh/logout
                                      requests.

  `auth.exception`                    Authentication-specific exception
                                      types.

  `security.config`                   Spring Security, JWT
                                      encoder/decoder, and
                                      password-encoder configuration.

  `security.principal`                Adapter that exposes a Workzeno
                                      `User` as Spring Security's
                                      `UserDetails`.

  `security.properties`               Typed configuration properties for
                                      JWT and refresh-token settings.

  `security.service`                  Issues access tokens,
                                      generates/hashes refresh tokens,
                                      and exposes the current
                                      authenticated user's ID.

  `user.domain`                       User persistence model and account
                                      role/status enums.

  `user.repository`                   Database access for user accounts.

  `user.service`                      Loads account details for Spring
                                      Security credential authentication.

  `user.controller`                   HTTP endpoint for retrieving the
                                      current user's profile.

  `user.dto`                          Public representation of the
                                      current user's profile.

  `bootstrap`                         Startup-time initial-admin creation
                                      and administrative password-reset
                                      runners.
  -----------------------------------------------------------------------

## 2. The two-token design

Workzeno uses two different credentials because they have different
jobs.

### Access token (JWT)

-   A short-lived, signed JSON Web Token.
-   Returned in the login/refresh response body.
-   Sent by the client on protected API requests using
    `Authorization: Bearer <access-token>`.
-   Validated by Spring Security's OAuth2 Resource Server support.
-   Carries identity and authorization claims, including the user's ID
    as the subject and the user's role.
-   It is a signed token, **not an encrypted secret**. Its claims can be
    decoded by whoever possesses it, so secrets and passwords must never
    be placed in it.

### Refresh token

-   A long-lived, opaque random value; it is **not a JWT**.
-   Delivered to the browser in an HttpOnly cookie.
-   Used only with the refresh endpoint to obtain a new access token and
    a replacement refresh token.
-   The database stores a SHA-256 hash of the token, not the raw token.
-   Each successful refresh rotates the token: the old session is
    revoked and linked to its replacement.

### Why keep them separate?

The access token is presented frequently to protected APIs. The refresh
token is a more sensitive credential used less often, and its
server-side session can be revoked and rotated. Keeping the refresh
token out of JavaScript-readable storage reduces its exposure to script
access.

## 3. Class-by-class reference

This section lists the classes in the reviewed source snapshot and their
roles.

### `auth.controller`

#### `AuthController`

The HTTP boundary for authentication use cases. It maps requests under
`/api/v1/auth`:

-   `POST /register` --- validates a `RegisterRequest`, calls
    `AuthService.register`, and returns a created response.
-   `POST /login` --- calls `AuthService.login`, puts the raw refresh
    token into a cookie using `RefreshCookieService`, and returns the
    access-token response body.
-   `POST /refresh` --- reads the refresh cookie, validates the request
    origin, rejects a missing cookie, calls `AuthService.refresh`, and
    sends the rotated refresh cookie.
-   `POST /logout` --- validates the request origin, asks the service to
    revoke the refresh session when a cookie is present, and clears the
    browser cookie.

It also uses `ApiResponseFactory` to keep the API response envelope
consistent. It should remain an HTTP adapter: business rules belong in
services, not in the controller.

### `auth.dto.request`

#### `RegisterRequest`

Input data for account registration. It defines the registration payload
and its bean-validation constraints.

#### `LoginRequest`

Carries the email and password submitted for login. It applies
validation such as non-blank email, email format, and field-size limits.

#### `RefreshRequest`

Internal request DTO used by the service layer to pass the refresh-token
value into refresh and logout use cases. The controller constructs it
from the cookie; clients do not need to send the refresh token in the
JSON body when using the cookie-based endpoints.

### `auth.dto.response`

#### `RegisterResponse`

Public result of successful registration. It represents the registration
outcome without exposing the stored password hash.

#### `AuthResponse`

Public authentication result. It contains the user identity and token
metadata/access token returned to the client. The refresh token itself
is not included in this response body; it is transported as a cookie.

### `auth.domain`

#### `RefreshSession`

JPA entity mapped to the `refresh_sessions` table. It represents one
refresh-token generation for one user.

Important fields:

-   `id` --- unique identifier for this refresh-session row.
-   `userId` --- owner of the session.
-   `familyId` --- groups successive rotated sessions belonging to the
    same login/session family.
-   `tokenHash` --- SHA-256 hash of the raw refresh token; unique in the
    table.
-   `createdAt` / `expiresAt` --- session creation and expiry instants.
-   `revokedAt` --- set when this generation is revoked.
-   `replacedById` --- points to the next session generation after
    rotation.

The family and replacement relationship support refresh-token rotation
and detection of reuse of an already-rotated token.

### `auth.repository`

#### `RefreshSessionRepository`

Spring Data repository for `RefreshSession`. It provides persistence and
the specialized lookup/update operations needed by the refresh flow. The
refresh lookup uses a database lock so concurrent refresh attempts
cannot both rotate the same active session as if they were independent
requests.

### `auth.service`

#### `AuthService`

The application-level coordinator for authentication use cases.

-   `register` --- normalizes/validates the registration flow, encodes
    the password, persists a new user, and returns `RegisterResponse`.
-   `login` --- authenticates the supplied credentials through Spring
    Security, obtains the authenticated `WorkzenoUserDetails`, issues an
    access JWT, generates a refresh token, persists the hashed refresh
    session, and returns an `AuthenticationResult`.
-   `refresh` --- hashes the supplied refresh token, finds and validates
    its session, checks the associated account, rotates the refresh
    session, and issues a new access JWT.
-   `logout` --- revokes the refresh session associated with the
    supplied token.

This is the main orchestration point. It delegates token mechanics to
security services and database work to repositories.

#### `RefreshCookieService`

Creates and clears the `refreshToken` browser cookie. It centralizes
cookie attributes such as HttpOnly, Secure, SameSite, path, and max-age
so the controller does not hand-build cookie strings.

#### `RefreshSessionRevocationService`

Contains the operation for revoking all sessions in a refresh-token
family. It is used for family-wide revocation when refresh-token reuse
is detected. Its transaction behavior is important because a security
revocation should be committed even when the surrounding refresh
operation fails.

### `auth.service.result`

#### `AuthenticationResult`

Internal record that carries two values from `AuthService` to
`AuthController`:

1.  `AuthResponse` --- safe response data for the client.
2.  Raw refresh token --- used by the controller to create the HttpOnly
    cookie.

This separation allows the raw refresh token to travel through the
server-side call chain without placing it in the JSON response.

### `auth.validation`

#### `AuthOriginValidator`

Checks the HTTP `Origin` header against the configured allowed frontend
origin for browser-sensitive authentication operations. The controller
invokes it for refresh and logout, where the browser automatically
attaches the refresh cookie.

### `auth.exception`

#### `DuplicateEmailException`

Authentication-domain exception used when registration attempts to
create an account with an email that is already registered. It
specializes the shared `ApiException` response mechanism.

## 4. `security` package

### `security.config`

#### `SecurityConfig`

Defines the application's Spring Security filter-chain behavior: which
routes are public, which require authentication/roles, and how OAuth2
Resource Server JWT authentication is enabled. It also configures the
JWT authentication converter used to translate token claims into Spring
Security authorities.

This class does not issue tokens. It configures how incoming requests
are secured.

#### `JwtConfig`

Creates the JWT cryptographic infrastructure, notably the `JwtEncoder`
used to sign access tokens and the `JwtDecoder` used to verify incoming
tokens. The encoder and decoder are configured to use the application's
signing key and expected JWT settings.

The encoder and decoder have opposite jobs:

-   `JwtEncoder`: server-side token creation.
-   `JwtDecoder`: validation/decoding of a token sent back by a client.

#### `PasswordConfig`

Exposes the `PasswordEncoder` bean used to hash passwords during
registration and to verify submitted passwords during login. Passwords
are not stored in plaintext.

### `security.principal`

#### `WorkzenoUserDetails`

Adapts the application's `User` entity to Spring Security's
`UserDetails` interface. It exposes the account's username/email,
password hash, authorities, user ID, and account-state flags.

`WorkzenoUserDetailsService` returns this adapter during
username/password authentication. This is part of the login credential
flow; it is not the mechanism used to load a user on every bearer-token
request.

### `security.properties`

#### `JwtProperties`

Binds JWT-related application configuration, such as issuer, signing
secret/key material, and access-token time-to-live. It also validates
that the configured access-token lifetime is positive.

#### `RefreshTokenProperties`

Binds refresh-token configuration, including the refresh-session
lifetime (`ttl`).

### `security.service`

#### `AccessTokenService`

Creates signed access JWTs. It builds the JWT header and claims
(including issuer, subject/user ID, role, issued-at, and expiry) and
delegates signing to `JwtEncoder`.

#### `RefreshTokenService`

Owns refresh-token mechanics:

-   Generates cryptographically secure random refresh-token values.
-   Hashes a raw refresh token using SHA-256.
-   Encodes the digest as hexadecimal for storage in
    `refresh_sessions.token_hash`.

It does not persist sessions itself; `AuthService` and
`RefreshSessionRepository` coordinate persistence.

#### `CurrentUserProvider`

Reads the authenticated principal from `SecurityContextHolder`, verifies
that it is a JWT authentication, and parses the JWT subject as the
current user's UUID. It gives application services a reusable way to
obtain the authenticated user ID without each service manually parsing
the JWT.

### `user` package

### `user.domain`

#### `User`

JPA entity mapped to the `users` table. It stores the user's UUID,
normalized email, password hash, role, status, and timestamps. The
ordinary constructor creates a `USER` account; `createAdmin` creates an
`ADMIN` account. Lifecycle callbacks maintain creation/update
timestamps.

#### `UserRole`

Role enum currently containing `USER` and `ADMIN`. The role is used to
determine authorities for authenticated operations.

#### `UserStatus`

Account-status enum containing `ACTIVE`, `DISABLED`, and `LOCKED`. The
user-details adapter uses account status when Spring Security performs
credential authentication.

### `user.repository`

#### `UserRepository`

Spring Data repository for `User`. It supports normal CRUD plus lookups
by email and existence checks used by registration and bootstrap logic.

### `user.service`

#### `WorkzenoUserDetailsService`

Implements Spring Security's `UserDetailsService`. It normalizes the
submitted email, loads the matching account through `UserRepository`,
and wraps it in `WorkzenoUserDetails`. Spring Security's
username/password authentication provider uses this service during
login.

### `user.controller`

#### `CurrentUserController`

Exposes `GET /api/v1/users/me`. It reads the authenticated JWT
principal, extracts the subject UUID, loads the corresponding `User`,
maps it to `CurrentUserResponse`, and wraps it in the standard API
response.

### `user.dto`

#### `CurrentUserResponse`

Public DTO for the current-user endpoint. Its `from(User)` factory maps
the entity to response fields such as ID, email, role, status, and
creation time. It does not expose the password hash.

## 5. `bootstrap` package

These classes support initial administrative setup rather than normal
user-facing authentication.

#### `BootstrapAdminProperties`

Typed configuration for the initial-admin bootstrap feature, including
whether it is enabled and the configured admin credentials/settings.

#### `AdminBootstrapRunner`

Implements Spring Boot's `ApplicationRunner`. It checks the bootstrap
configuration at application startup and delegates initial-admin
creation to `AdminBootstrapService` when enabled.

#### `AdminBootstrapService`

Interface defining the initial-admin creation use case. The interface
keeps the runner independent from the implementation.

#### `AdminBootstrapServiceImpl`

Implements initial-admin creation. It checks whether an administrator
already exists, encodes the supplied password, and creates the initial
admin account through `UserRepository`.

#### `AdminPasswordResetRunner`

A startup runner for the configured administrative password-reset
operation. Because it is startup-triggered, its enablement and repeat
behavior must be controlled carefully in each environment.

## 6. How the classes interact

### A. Registration

``` mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Controller as AuthController
    participant Service as AuthService
    participant Encoder as PasswordEncoder
    participant Users as UserRepository
    participant DB as users table

    Client->>Controller: POST /api/v1/auth/register<br/>RegisterRequest
    Controller->>Service: register(request)
    Service->>Users: Check email / persist user
    Service->>Encoder: encode(raw password)
    Encoder-->>Service: password hash
    Service->>Users: save(User)
    Users->>DB: INSERT user
    DB-->>Users: saved row
    Users-->>Service: User
    Service-->>Controller: RegisterResponse
    Controller-->>Client: 201 Created + ApiResponse
```

Registration creates the user account. It does not need to return a
refresh token or an access token in the current flow.

### B. Login: password authentication and token issuance

``` mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Controller as AuthController
    participant Auth as AuthService
    participant Manager as AuthenticationManager
    participant Provider as DaoAuthenticationProvider
    participant UserDetailsSvc as WorkzenoUserDetailsService
    participant Users as UserRepository
    participant Passwords as PasswordEncoder
    participant Access as AccessTokenService
    participant Encoder as JwtEncoder
    participant Refresh as RefreshTokenService
    participant Sessions as RefreshSessionRepository
    participant Cookie as RefreshCookieService

    Client->>Controller: POST /api/v1/auth/login<br/>LoginRequest
    Controller->>Auth: login(request)
    Auth->>Manager: authenticate(email, password)
    Manager->>Provider: authenticate(...)
    Provider->>UserDetailsSvc: loadUserByUsername(email)
    UserDetailsSvc->>Users: findByEmail(normalized email)
    Users-->>UserDetailsSvc: User
    UserDetailsSvc-->>Provider: WorkzenoUserDetails
    Provider->>Passwords: verify submitted password
    Passwords-->>Provider: match / reject
    Provider-->>Manager: authenticated Authentication
    Manager-->>Auth: Authentication
    Auth->>Access: issueAccessToken(userId, role)
    Access->>Encoder: encode JWT claims
    Encoder-->>Access: signed JWT
    Access-->>Auth: access JWT
    Auth->>Refresh: generateToken()
    Refresh-->>Auth: raw refresh token
    Auth->>Refresh: hashToken(raw token)
    Refresh-->>Auth: SHA-256 hash
    Auth->>Sessions: save RefreshSession(hash, family, expiry)
    Sessions-->>Auth: saved session
    Auth-->>Controller: AuthenticationResult(response, raw refresh token)
    Controller->>Cookie: create(raw refresh token)
    Cookie-->>Controller: HttpOnly Set-Cookie value
    Controller-->>Client: access JWT in response + refresh cookie
```

The key distinction is that the password is checked during login, while
the access JWT is created after successful authentication. The raw
refresh token is returned internally to the controller only so it can be
placed in a cookie.

### C. A protected API request: JWT validation

``` mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Filter as Spring Security Filter Chain
    participant Bearer as BearerTokenAuthenticationFilter
    participant Provider as JwtAuthenticationProvider
    participant Decoder as JwtDecoder
    participant Converter as JwtAuthenticationConverter
    participant Context as SecurityContextHolder
    participant Endpoint as Protected Controller

    Client->>Filter: Request + Authorization: Bearer JWT
    Filter->>Bearer: Process bearer token
    Bearer->>Provider: Authenticate token
    Provider->>Decoder: Decode and validate JWT
    Note over Decoder: Verify signature and configured claims<br/>such as issuer and timestamps
    Decoder-->>Provider: Validated Jwt
    Provider->>Converter: Convert Jwt to Authentication
    Note over Converter: Map role claim to GrantedAuthority
    Converter-->>Provider: JwtAuthenticationToken
    Provider-->>Bearer: Authenticated principal
    Bearer->>Context: Store Authentication for request
    Filter->>Endpoint: Continue authorized request
    Endpoint->>Context: Read current authentication
    Endpoint-->>Client: Protected response
```

The `spring-boot-starter-oauth2-resource-server` dependency supplies the
Resource Server machinery that extracts bearer tokens and coordinates
authentication. Workzeno's `JwtConfig` supplies the decoder, while
`SecurityConfig` enables the Resource Server behavior and configures
claim-to-authority conversion.

The normal protected-request path validates the JWT; it does not repeat
the email/password login process.

### D. Refresh: rotate the refresh session and issue a new JWT

``` mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Controller as AuthController
    participant Origin as AuthOriginValidator
    participant Auth as AuthService
    participant Refresh as RefreshTokenService
    participant Sessions as RefreshSessionRepository
    participant Users as UserRepository
    participant Revocation as RefreshSessionRevocationService
    participant Access as AccessTokenService
    participant Cookie as RefreshCookieService

    Client->>Controller: POST /api/v1/auth/refresh<br/>refreshToken cookie
    Controller->>Origin: validate(request Origin)
    Controller->>Auth: refresh(RefreshRequest)
    Auth->>Refresh: hashToken(raw token)
    Refresh-->>Auth: token hash
    Auth->>Sessions: Find session by hash (locked)
    Sessions-->>Auth: RefreshSession
    Note over Auth: Check existence, revoked state,<br/>expiry and session ownership
    Auth->>Users: Load/check account
    Users-->>Auth: User
    Auth->>Refresh: generate replacement token
    Refresh-->>Auth: new raw refresh token
    Auth->>Refresh: hashToken(new token)
    Refresh-->>Auth: new hash
    Auth->>Sessions: Save replacement session in same family
    Auth->>Sessions: Revoke old session + set replacedById
    opt Reuse of a previously rotated token detected
        Auth->>Revocation: revokeFamily(familyId, time)
        Revocation->>Sessions: Revoke family sessions
    end
    Auth->>Access: issueAccessToken(current user ID and role)
    Access-->>Auth: new signed JWT
    Auth-->>Controller: AuthenticationResult
    Controller->>Cookie: create(new raw refresh token)
    Cookie-->>Controller: replacement Set-Cookie
    Controller-->>Client: new access JWT + replacement refresh cookie
```

The refresh token is looked up by its hash, not by storing the raw
credential in the database. The repository lock helps serialize
competing refresh operations for the same session. A successful rotation
creates a new session generation and revokes the previous one.

### E. Logout

``` mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Controller as AuthController
    participant Origin as AuthOriginValidator
    participant Auth as AuthService
    participant Refresh as RefreshTokenService
    participant Sessions as RefreshSessionRepository
    participant Cookie as RefreshCookieService

    Client->>Controller: POST /api/v1/auth/logout<br/>refreshToken cookie
    Controller->>Origin: validate(request Origin)
    opt Refresh cookie is present
        Controller->>Auth: logout(RefreshRequest)
        Auth->>Refresh: hashToken(raw token)
        Refresh-->>Auth: hash
        Auth->>Sessions: Find and revoke matching session
    end
    Controller->>Cookie: clear()
    Cookie-->>Controller: expired/cleared cookie
    Controller-->>Client: success + Set-Cookie clearing cookie
```

Logout revokes the refresh session and asks the browser to clear its
cookie. Clearing the cookie alone would not revoke the server-side
session. An already-issued access JWT is a separate credential and may
remain usable until it expires unless an additional access-token
revocation/account-status mechanism is implemented.

### F. Current-user profile

``` mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Security as Spring Security
    participant Controller as CurrentUserController
    participant Users as UserRepository
    participant DB as users table

    Client->>Security: GET /api/v1/users/me + Bearer JWT
    Security->>Security: Validate JWT and establish Authentication
    Security->>Controller: Authorized request
    Controller->>Controller: Read JWT subject and parse UUID
    Controller->>Users: findById(userId)
    Users->>DB: SELECT user
    DB-->>Users: User row
    Users-->>Controller: User
    Controller->>Controller: Map User to CurrentUserResponse
    Controller-->>Client: ApiResponse<CurrentUserResponse>
```

`CurrentUserProvider` is a reusable security service for obtaining the
current user ID from the `SecurityContextHolder`. In the reviewed source
snapshot, `CurrentUserController` currently extracts the subject
directly from its `Authentication` argument rather than using
`CurrentUserProvider`.

### G. Initial administrator bootstrap

``` mermaid
sequenceDiagram
    autonumber
    participant Boot as Spring Boot startup
    participant Runner as AdminBootstrapRunner
    participant Props as BootstrapAdminProperties
    participant Service as AdminBootstrapService
    participant Impl as AdminBootstrapServiceImpl
    participant Encoder as PasswordEncoder
    participant Users as UserRepository

    Boot->>Runner: run(ApplicationArguments)
    Runner->>Props: Read bootstrap settings
    alt Bootstrap enabled
        Runner->>Service: createInitialAdmin(email, password)
        Service->>Impl: implementation
        Impl->>Users: Check whether admin exists
        Impl->>Encoder: Encode configured password
        Encoder-->>Impl: password hash
        Impl->>Users: Save admin User
    else Bootstrap disabled
        Runner-->>Boot: No bootstrap action
    end
```

`AdminPasswordResetRunner` is a separate startup runner for the
configured admin-password-reset operation. Treat startup-triggered
credential changes as operationally sensitive and verify its
configuration before enabling it.

## 7. Important interaction boundaries

These boundaries help explain why the module is split into packages:

-   **Controller → service:** controllers translate HTTP input/output;
    services coordinate use cases.
-   **Service → repository:** services decide what must happen;
    repositories perform persistence operations.
-   **Service → token service:** `AuthService` coordinates
    authentication/session lifecycle; token services implement token
    generation and hashing.
-   **Spring Security → user-details service:** username/password
    authentication loads the account through
    `WorkzenoUserDetailsService`.
-   **Spring Security Resource Server → JWT decoder:** bearer-token
    authentication validates incoming JWTs independently from password
    login.
-   **JWT converter → authorities:** the role claim is translated into
    Spring Security authorities used by route authorization.
-   **Entity → DTO:** JPA entities represent persistence; response DTOs
    define what leaves the API.

## 8. Configuration and persistence touchpoints

The module depends on configuration and database objects outside its
Java package tree:

-   JWT properties configure issuer, signing key, and access-token
    lifetime.
-   Refresh-token properties configure refresh-session lifetime.
-   Cookie settings configure HttpOnly, Secure, SameSite, path, and
    lifetime behavior.
-   Security configuration defines public/protected routes and JWT
    authentication.
-   The `users` table stores account identity, password hash, role,
    status, and timestamps.
-   The `refresh_sessions` table stores hashed refresh credentials and
    rotation/revocation relationships.

Check the active `application.yml` profile and Flyway migrations for the
exact deployed values and schema. Do not put real secrets in this
README.




## 9. Quick mental model

When reading this module, follow these paths:

-   **Creating an account:**
    `AuthController → AuthService → PasswordEncoder + UserRepository`.
-   **Logging in:**
    `AuthController → AuthService → AuthenticationManager → WorkzenoUserDetailsService → UserRepository → AccessTokenService/JwtEncoder + RefreshTokenService/RefreshSessionRepository → RefreshCookieService`.
-   **Calling a protected API:**
    `SecurityConfig → Resource Server bearer filter → JwtDecoder → JWT authentication converter → SecurityContextHolder → controller/service`.
-   **Refreshing:**
    `AuthController → AuthOriginValidator → AuthService → RefreshTokenService + RefreshSessionRepository (+ UserRepository) → AccessTokenService → RefreshCookieService`.
-   **Getting the current user:**
    `CurrentUserController → UserRepository → CurrentUserResponse`.
-   **Bootstrapping admin:**
    `AdminBootstrapRunner → AdminBootstrapService → AdminBootstrapServiceImpl → PasswordEncoder + UserRepository`.

That is the central architecture: **`auth` coordinates authentication
use cases, `security` supplies the security mechanisms, `user` owns
account data, and `bootstrap` handles controlled initial
administration.**
