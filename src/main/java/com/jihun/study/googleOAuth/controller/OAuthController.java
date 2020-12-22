package com.jihun.study.googleOAuth.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jihun.study.googleOAuth.utils.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/oauth")
public class OAuthController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private String CLIENT_ID        = "";
    private String CLIENT_SECRET    = "";
    private String SCOPE            = "";
    private String REDIRECT_URI     = "";

    private String AUTHORIZE_URL    = "";
    private String TOKEN_URL        = "";
    private String API_URL_BASE     = "";

    @Autowired
    OAuthController(Environment environment) {
        CLIENT_ID       = environment.getProperty("config.google.client.id");
        CLIENT_SECRET   = environment.getProperty("config.google.client.secret");
        SCOPE           = environment.getProperty("config.google.client.scope");
        REDIRECT_URI    = environment.getProperty("config.google.client.redirect_uri");

        AUTHORIZE_URL   = environment.getProperty("config.google.authorize_url");
        TOKEN_URL       = environment.getProperty("config.google.token_url");
        API_URL_BASE    = environment.getProperty("config.google.api_url_base");
    }

    @RequestMapping("")
    public String getOAuth(HttpSession session, String code, String state) {
        String template = "/templates/google_OAuth2.0_page.html";

        logger.info("getOAuth : code  = " + code);
        logger.info("getOAuth : state = " + state);

        if (state != null && session.getAttribute("state") != null) {
            String sessionState = (String) session.getAttribute("state");
            logger.info("getOAuth : sessionState = " + sessionState);

            if (sessionState.equals(state)) {
                session.setAttribute("code", code);
            } else {
                session.invalidate();
            }
        } else {
            session.invalidate();
        }

        return template;
    }

    @RequestMapping("/authorize")
    @ResponseBody
    public String getGithubAuthorize(HttpSession session) {
        Map<String, String> params  = new HashMap<>();
        String              state   = String.valueOf(new SecureRandom().nextInt(1000000000));

        params.put("response_type"  , "code");
        params.put("client_id"      , CLIENT_ID);
        params.put("redirect_uri"   , REDIRECT_URI);
        params.put("scope"          , SCOPE);
        params.put("state"          , state);

        session.setAttribute("state", state);
        session.setMaxInactiveInterval(10000);

        return AUTHORIZE_URL + "?" + StreamUtils.getRequestGetParams(params);
    }

    @RequestMapping("/session")
    @ResponseBody
    public Map<String, String> getSession(HttpSession session) {
        Map<String, String> result      = new HashMap<>();
        Enumeration<String> sessionKeys = session.getAttributeNames();

        /*
         * Session Information
         *
         * code
         * state
         *
         * access_token
         * expires_in
         * scope
         * token_type
         * id_token
         */
        while(sessionKeys.hasMoreElements()) {
            String sessionKey = sessionKeys.nextElement();
            result.put(sessionKey, (String) session.getAttribute(sessionKey));
        }

//        result.put("code"           , (String) session.getAttribute("code"));
//        result.put("state"          , (String) session.getAttribute("state"));
//        result.put("access_token"   , (String) session.getAttribute("access_token"));
//        result.put("token_type"     , (String) session.getAttribute("token_type"));
//        result.put("scope"          , (String) session.getAttribute("scope"));

        return result;
    }

    @RequestMapping("/accessToken")
    @ResponseBody
    public String getGithubAccessToken(HttpSession session) {
        String code     = (String) session.getAttribute("code");
        String state    = (String) session.getAttribute("state");

        logger.info("getGithubAccessToken : code  = " + code);
        logger.info("getGithubAccessToken : state = " + state);

        if (code != null && state != null) {
            Map<String, String> headers = new HashMap<>();
            Map<String, String> params  = new HashMap<>();

            headers.put("Accept"        , "application/vnd.github.v3+json, application/json");

            params.put("grant_type"     , "authorization_code");
            params.put("client_id"      , CLIENT_ID);
            params.put("client_secret"  , CLIENT_SECRET);
            params.put("redirect_uri"   , REDIRECT_URI);
            params.put("code"           , code);

            String result = StreamUtils.getStream(TOKEN_URL, StreamUtils.METHOD_POST, headers, params);
            logger.info("getGithubAccessToken : result     = " + result);

            if (!"".equals(result)) {
                JsonElement resultJson      = JsonParser.parseString(result);

                for (String jsonKey : resultJson.getAsJsonObject().keySet()) {
                    String jsonValue = resultJson.getAsJsonObject().get(jsonKey).getAsString();

                    if ("expires_in".equals(jsonKey)) {
                        session.setMaxInactiveInterval(Integer.valueOf(jsonValue));
                    }

                    session.setAttribute(jsonKey, jsonValue);
                }
            }
        }

        return null;
    }

    @RequestMapping("/api")
    @ResponseBody
    public String getGithubApi(HttpSession session) {
        String code         = (String) session.getAttribute("code");
        String state        = (String) session.getAttribute("state");
        String access_token = (String) session.getAttribute("access_token");
        String token_type   = (String) session.getAttribute("token_type");
        String scope        = (String) session.getAttribute("scope");

        logger.info("getGithubApi : code         = " + code);
        logger.info("getGithubApi : state        = " + state);
        logger.info("getGithubApi : access_token = " + access_token);
        logger.info("getGithubApi : token_type   = " + token_type);
        logger.info("getGithubApi : scope        = " + scope);

        if (code != null && state != null && access_token != null && token_type != null && scope != null) {
            Map<String, String> headers = new HashMap<>();
            Map<String, String> params  = new HashMap<>();

            headers.put("Accept"        , "application/vnd.github.v3+json, application/json");
            headers.put("Authorization" , token_type + " " + access_token);

            params.put("sort"           , "created");
            params.put("direction"      , "desc");

            String result = StreamUtils.getStream(API_URL_BASE + "/user/repos", StreamUtils.METHOD_GET, headers, params);
            return result;
        } else {
            session.invalidate();
        }

        return null;
    }

    @RequestMapping("jwtDecode")
    @ResponseBody
    public Map<String, String> getJwtDecode(HttpSession session) {
        String code         = (String) session.getAttribute("code");
        String state        = (String) session.getAttribute("state");
        String access_token = (String) session.getAttribute("access_token");
        String expires_in   = (String) session.getAttribute("expires_in");
        String scope        = (String) session.getAttribute("scope");
        String token_type   = (String) session.getAttribute("token_type");
        String id_token     = (String) session.getAttribute("id_token");

        logger.info("getJwtDecode : code         = " + code);
        logger.info("getJwtDecode : state        = " + state);
        logger.info("getJwtDecode : access_token = " + access_token);
        logger.info("getJwtDecode : expires_in   = " + expires_in);
        logger.info("getJwtDecode : scope        = " + scope);
        logger.info("getJwtDecode : token_type   = " + token_type);
        logger.info("getJwtDecode : id_token     = " + id_token);

        if (    code != null && state       != null && access_token != null && expires_in != null
            && scope != null && token_type  != null && id_token     != null
        ) {
            DecodedJWT          decodedToken    = JWT.decode(id_token);
            Map<String, String> resultToken     = new HashMap<>();

            for (String tokenKey : decodedToken.getClaims().keySet()) {
                String tokenValue = decodedToken.getClaim(tokenKey).asString();

                logger.info("getJwtDecode : " + tokenKey + " = " + tokenValue);
                resultToken.put(tokenKey, tokenValue);
            }

            return resultToken;
        } else {
            session.invalidate();
        }

        return null;
    }
}
