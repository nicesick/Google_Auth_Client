# Google_Auth_Client
This Project is for making Google_Auth_Client for practicing Auth2.0



## Environment

1. Spring-Boot 2.4.1
2. Google OAuth
3. Gson(com.google.code.gson)
4. Java-jwt(com.auth0)



## [Github_Auth_Client](https://github.com/nicesick/Github_Auth_Client)

* Github_Auth_Client repository is similar with This repository
* The link show you how to execute, OAuth Process, Issue, References



## Difference with Github_Auth_Client

1. OpenID Connection

   * Until requesting Access Token from google, process is same with github
   * But Google's responsed token additionally has **ID_TOKEN** information
   * ID_TOKEN is type of **JWT(Json Web Token)** and payload has user's information
   * You can request addtional information for setting scope in application.properties

   ```properties
   config.google.client.id = {#Your client id}
   config.google.client.secret = #{Your client secret}
   
   # You can set scope According to OpenID Connection's rule
   config.google.client.scope = openid%20profile%20email%20address%20phone
   config.google.client.redirect_uri= http://localhost:8080/oauth
   
   config.google.authorize_url= https://accounts.google.com/o/oauth2/v2/auth
   config.google.token_url= https://www.googleapis.com/oauth2/v4/token
   config.google.api_url_base= https://www.googleapis.com/oauth2/v3
   ```
   



2. JWT(Json Web Token)

   * To decode ID_TOKEN, i use java-jwt package

   ```java
   /**
    * In Spring-Boot Server's OAuthController#getJwtDecode
    */
   
   ...
   
   import com.auth0.jwt.JWT;
   import com.auth0.jwt.interfaces.DecodedJWT;
   
   ...
       
   DecodedJWT          decodedToken    = JWT.decode(id_token);
   Map<String, String> resultToken     = new HashMap<>();
   
   for (String tokenKey : decodedToken.getClaims().keySet()) {
       String tokenValue = decodedToken.getClaim(tokenKey).asString();
   
       logger.info("getJwtDecode : " + tokenKey + " = " + tokenValue);
       resultToken.put(tokenKey, tokenValue);
   }
   
   return resultToken;
   
   ...
   ```



## Google OAuth Process

1. Request Authorize
   * response_type
   * client_id
   * redirect_uri
   * scope
   * state
2. Request Access Token
   * grant_type
   * client_id
   * client_secret
   * redirect_uri
   * code
3. Decode ID_TOKEN
   * Header
   * Payload
     * Registered Claims
     * Public Claims
     * Private Claims(Google's user information is here!)
   * Signature
4. (Optional) Request API
   * Authorization
   * Request URL



## References

* [oauth.com](https://www.oauth.com/)
* [VELOPERT.LOG](https://velopert.com/2389)

* [OpenID Connect Basic Client Implementer's Guide](https://openid.net/specs/openid-connect-basic-1_0.html)