package edu.georgetown.library.oauth;

import java.io.FileNotFoundException;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;

import org.apache.commons.codec.binary.Base64;

class OAuthConn {
	OAuthClient oAuthClient;
	String authPath;
	String apiRoot;
	String accessToken;
	String resumptionToken;
	String secret;
	String key;
	
	OAuthConn(String apiRoot, String authPath, String key, String secret) throws OAuthSystemException, OAuthProblemException {
		this.key = key;
		this.secret = secret;
		this.apiRoot = apiRoot;
		this.authPath = authPath;
		oAuthClient = new OAuthClient(new URLConnectionClient());
		
		initConn();
	}
	
	public void initConn() throws OAuthSystemException, OAuthProblemException {		
		System.out.println("Initializing connection...");
		OAuthClientRequest request = OAuthClientRequest
				.tokenLocation(apiRoot + authPath)
				.setGrantType(GrantType.CLIENT_CREDENTIALS)
				.buildBodyMessage();
		request.addHeader(OAuth.HeaderType.AUTHORIZATION,
				createBasicAuthHeader(key, secret));

		OAuthJSONAccessTokenResponse oAuthResponse = oAuthClient
				.accessToken(request);
		
		accessToken = oAuthResponse.getAccessToken();
		resumptionToken = oAuthResponse.getRefreshToken();
	}
	
	public void refreshConn() throws OAuthSystemException, OAuthProblemException {		
		System.out.println("Resumption " + resumptionToken + " not implemented... requesting new token");
		initConn();
	}

	OAuthResourceResponse tryRequest(String apiFuncUrl) throws OAuthSystemException, OAuthProblemException {
		OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(apiRoot + apiFuncUrl).buildQueryMessage();
		bearerClientRequest.addHeader(OAuth.HeaderType.AUTHORIZATION, "Bearer "	+ accessToken);
		return oAuthClient.resource(bearerClientRequest,	OAuth.HttpMethod.GET, OAuthResourceResponse.class);
	}
	
	boolean isResponseValid(OAuthResourceResponse response) {
		if (response.getResponseCode() == 200) return true;
		return false;
	}
	
	String getApiResult(String apiFuncUrl) throws OAuthSystemException, OAuthProblemException, IIIExtractException {
		OAuthResourceResponse response;
		
		try {
			response = tryRequest(apiFuncUrl);
			if (isResponseValid(response)) {
				if (GUExtractSierraBibs.isDebug()) System.out.println(response.getBody());
				return response.getBody();
			}
			
			refreshConn();
			response = tryRequest(apiFuncUrl);
			if (isResponseValid(response)) {
				if (GUExtractSierraBibs.isDebug()) System.out.println(response.getBody());
				return response.getBody();
			}
			
			initConn();
			response = tryRequest(apiFuncUrl);
			if (isResponseValid(response)) {			
				if (GUExtractSierraBibs.isDebug()) System.out.println(response.getBody());
				return response.getBody();
			}
		} catch (OAuthSystemException e) {
			if (e.getCause() instanceof FileNotFoundException) return "{entries:[]}";
		}

		throw new IIIExtractException("Cannot access "+apiFuncUrl);
	}

	// Sample code from OAuthRequestTest
	private static String createBasicAuthHeader(String clientId,
			String clientSecret) {
		clientSecret = OAuthUtils.isEmpty(clientSecret) ? "" : clientSecret;
		clientId = OAuthUtils.isEmpty(clientId) ? "" : clientId;
		final String authString = clientId + ":" + clientSecret;
		return "Basic " + Base64.encodeBase64String(authString.getBytes());
	}
}

