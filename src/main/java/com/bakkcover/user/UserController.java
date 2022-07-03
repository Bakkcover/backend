package com.bakkcover.user;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.bakkcover.user.dtos.*;
import com.bakkcover.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AWSCognitoIdentityProviderException;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserResult;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AdminRespondToAuthChallengeRequest;
import com.amazonaws.services.cognitoidp.model.AdminRespondToAuthChallengeResult;
import com.amazonaws.services.cognitoidp.model.AdminSetUserPasswordRequest;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.amazonaws.services.cognitoidp.model.ChallengeNameType;
import com.amazonaws.services.cognitoidp.model.DeliveryMediumType;
import com.amazonaws.services.cognitoidp.model.InvalidParameterException;
import com.amazonaws.services.cognitoidp.model.MessageActionType;
import com.bakkcover.user.exceptions.CustomException;

@RestController
@RequestMapping(path = "/api/users")
@CrossOrigin(origins = "${local.angular.uri}")
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
            AttributeType emailVerifiedAttr =
                    new AttributeType().withName("email_verified").withValue("true");

            AdminCreateUserRequest userRequest = new AdminCreateUserRequest()
                    .withUserPoolId(userPoolId).withUsername(userSignUpRequest.getUsername())
                    .withTemporaryPassword(userSignUpRequest.getPassword())
                    .withUserAttributes(emailAttr, emailVerifiedAttr)
                    .withMessageAction(MessageActionType.SUPPRESS)
                    .withDesiredDeliveryMediums(DeliveryMediumType.EMAIL);

            AdminCreateUserResult createUserResult = cognitoClient.adminCreateUser(userRequest);

            System.out.println("User " + createUserResult.getUser().getUsername()
                    + " is created. Status: " + createUserResult.getUser().getUserStatus());

            // Disable force change password during first login
            AdminSetUserPasswordRequest adminSetUserPasswordRequest =
                    new AdminSetUserPasswordRequest().withUsername(userSignUpRequest.getUsername())
                            .withUserPoolId(userPoolId)
                            .withPassword(userSignUpRequest.getPassword()).withPermanent(true);

            cognitoClient.adminSetUserPassword(adminSetUserPasswordRequest);

        } catch (AWSCognitoIdentityProviderException e) {
            System.out.println(e.getErrorMessage());

            userSignUpResponse.setErrorMessage(e.getErrorMessage());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(userSignUpResponse);
        } catch (Exception e) {
            System.out.println("Setting user password");

            userSignUpResponse.setErrorMessage("Setting user password");
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

                System.out.println("User has no challenge");
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
    public @ResponseBody UserDetailResponse getUserDetail(Authentication authentication) {
        UserDetailResponse userDetail = new UserDetailResponse();
        userDetail.setFirstName("Test");
        userDetail.setLastName("Buddy");
        userDetail.setEmail("testbuddy@tutotialsbuddy.com");

        System.out.println(this.userService.getCognitoSub(authentication));

        return userDetail;
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
