package com.bakkcover.user;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.amazonaws.services.cognitoidp.model.*;
import com.bakkcover.user.dtos.*;
import com.bakkcover.user.entities.User;
import com.bakkcover.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.bakkcover.user.exceptions.CustomException;

@RestController
@RequestMapping(path = "/api/users")
// @CrossOrigin(origins = {"http://localhost:4200", "http://bakkcover.s3-website-ap-southeast-1.amazonaws.com"})
@ResponseBody
public class UserController {

    private AWSCognitoIdentityProvider cognitoClient;
    private UserService userService;

    @Value(value = "${aws.cognito.userPoolId}")
    private String userPoolId;
    @Value(value = "${aws.cognito.clientId}")
    private String clientId;

    @Value(value = "${aws.cognito.clientSecret}")
    private String clientSecret;

    @Autowired
    public UserController(AWSCognitoIdentityProvider cognitoClient, UserService userService) {
        this.cognitoClient = cognitoClient;
        this.userService = userService;
    }

    @PostMapping(path = "/sign-up")
    public ResponseEntity<UserSignUpResponse> signUp(
            @RequestBody UserSignUpRequest userSignUpRequest) {

        UserSignUpResponse userSignUpResponse = new UserSignUpResponse();

        try {

            AttributeType emailAttr =
                    new AttributeType().withName("email").withValue(userSignUpRequest.getEmail());

            SignUpRequest signUpRequest = new SignUpRequest()
                    .withUserAttributes(emailAttr)
                    .withUsername(userSignUpRequest.getUsername())
                    .withClientId(clientId)
                    .withPassword(userSignUpRequest.getPassword())
                    .withSecretHash(calculateSecretHash(clientId, clientSecret, userSignUpRequest.getUsername()));

            SignUpResult signUpResult = cognitoClient.signUp(signUpRequest);

            System.out.println("User " + signUpResult.getUserSub()
                    + " is created. Is user confirmed: " + signUpResult.isUserConfirmed());

        } catch (AWSCognitoIdentityProviderException e) {
            System.out.println(e.getErrorMessage());
            userSignUpResponse.setErrorMessage(e.getErrorMessage());

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(userSignUpResponse);
        } catch (Exception e) {
            userSignUpResponse.setErrorMessage(e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(userSignUpResponse);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userSignUpResponse);
    }

    @PostMapping(path = "/sign-in")
    public ResponseEntity<UserSignInResponse> signIn(
            @RequestBody UserSignInRequest userSignInRequest) {

        UserSignInResponse userSignInResponse = new UserSignInResponse();

        final Map<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", userSignInRequest.getUsername());
        authParams.put("PASSWORD", userSignInRequest.getPassword());
        authParams.put("SECRET_HASH", calculateSecretHash(clientId, clientSecret, userSignInRequest.getUsername()));

        final AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest();
        authRequest.withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .withClientId(clientId)
                .withUserPoolId(userPoolId)
                .withAuthParameters(authParams);

        try {
            AdminInitiateAuthResult result = cognitoClient.adminInitiateAuth(authRequest);

            AuthenticationResultType authenticationResult = null;

            if (result.getChallengeName() != null && !result.getChallengeName().isEmpty()) {

                System.out.println("Challenge Name is " + result.getChallengeName());

                if (result.getChallengeName().contentEquals("NEW_PASSWORD_REQUIRED")) {
                    if (userSignInRequest.getPassword() == null) {
                        throw new CustomException(
                                "User must change password " + result.getChallengeName());

                    } else {

                        final Map<String, String> challengeResponses = new HashMap<>();
                        challengeResponses.put("USERNAME", userSignInRequest.getUsername());
                        challengeResponses.put("PASSWORD", userSignInRequest.getPassword());
                        // add new password
                        challengeResponses.put("NEW_PASSWORD", userSignInRequest.getNewPassword());

                        final AdminRespondToAuthChallengeRequest request =
                                new AdminRespondToAuthChallengeRequest()
                                        .withChallengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                                        .withChallengeResponses(challengeResponses)
                                        .withClientId(clientId).withUserPoolId(userPoolId)
                                        .withSession(result.getSession());

                        AdminRespondToAuthChallengeResult resultChallenge =
                                cognitoClient.adminRespondToAuthChallenge(request);
                        authenticationResult = resultChallenge.getAuthenticationResult();

                        userSignInResponse.setAccessToken(authenticationResult.getAccessToken());
                        userSignInResponse.setIdToken(authenticationResult.getIdToken());
                        userSignInResponse.setRefreshToken(authenticationResult.getRefreshToken());
                        userSignInResponse.setExpiresIn(authenticationResult.getExpiresIn());
                        userSignInResponse.setTokenType(authenticationResult.getTokenType());
                    }

                } else {
                    throw new CustomException(
                            "User has other challenge " + result.getChallengeName());
                }
            } else {

                System.out.println("User successfully authenticated!");
                authenticationResult = result.getAuthenticationResult();

                userSignInResponse.setAccessToken(authenticationResult.getAccessToken());
                userSignInResponse.setIdToken(authenticationResult.getIdToken());
                userSignInResponse.setRefreshToken(authenticationResult.getRefreshToken());
                userSignInResponse.setExpiresIn(authenticationResult.getExpiresIn());
                userSignInResponse.setTokenType(authenticationResult.getTokenType());
            }

        } catch (InvalidParameterException e) {
            throw new CustomException(e.getErrorMessage());
        } catch (AWSCognitoIdentityProviderException e) {
            userSignInResponse.setErrorMessage(e.getErrorMessage());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(userSignInResponse);

        } catch (Exception e) {
            // throw new CustomException(e.getMessage());

            userSignInResponse.setErrorMessage(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(userSignInResponse);
        }

//        cognitoClient.shutdown();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userSignInResponse);
    }

    @GetMapping(path = "/detail")
    public ResponseEntity<UserDetailResponse> getUserDetail(Authentication authentication) {
        UserDetailResponse userDetail = new UserDetailResponse();

        // userDetail.setFirstName("Test");
        // userDetail.setLastName("Buddy");
        // userDetail.setEmail("testbuddy@tutotialsbuddy.com");

        // String sub = this.userService.getCognitoSub(authentication);
        // System.out.println(sub);

        try {
            User user = this.userService.getUser(authentication);
            userDetail.setUser(user);
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userDetail);
    }

    private static String calculateSecretHash(String userPoolClientId, String userPoolClientSecret, String userName) {
        final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

        SecretKeySpec signingKey = new SecretKeySpec(
                userPoolClientSecret.getBytes(StandardCharsets.UTF_8),
                HMAC_SHA256_ALGORITHM);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            mac.update(userName.getBytes(StandardCharsets.UTF_8));
            byte[] rawHmac = mac.doFinal(userPoolClientId.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error while calculating ");
        }
    }
}
