package com.ibm.microservices.refapp.zuul.filters.pre;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Enumeration;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import com.ibm.microservices.refapp.zuul.SecurityConfig;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;

public class JWTValidationPreFilter extends ZuulFilter {
	private static Logger log = LoggerFactory.getLogger(JWTValidationPreFilter.class);
	
	// Base64 URL Encoded shared secret
	//private static String b64uenc_secret = 
	//		"VOKJrRJ4UuKbioNd6nIHXCpYkHhxw6-0Im-AupSk9ATvUwF8wwWzLWKQZOMbke-Swo79eca4_QgCZNY0iYAKLgDfWf4lfZ9MmiAkJMVfEWt9pp9r7ycQT8WhkMtPVNULlvOI4RbhBq1dxQkY4A6-h_lVIFbxGF6uHo8KpmwM1jAWxRvKYYu0VbTYOBjQWuakS7dIq11_6maRoAEaLNWjigMKlQSeCP6kVKnpoEDxy1Rqw9sV4ttJjFDrqZRcwwIvNhqVc-eq1Ed-Uzev-HQVMTCDuHs8m0wPRNQYHP6M0fJNRae6tkhvxKEFwZKbco7om6F3VPE-xRyOT_HkpAU9HA";
	//private static String alg = "HS256";
	/*
	 * 
	 *  Added this to API Connect:
{
  "alg": "HS256",
  "kty": "oct",
  "use": "sig",
  "k": "VOKJrRJ4UuKbioNd6nIHXCpYkHhxw6-0Im-AupSk9ATvUwF8wwWzLWKQZOMbke-Swo79eca4_QgCZNY0iYAKLgDfWf4lfZ9MmiAkJMVfEWt9pp9r7ycQT8WhkMtPVNULlvOI4RbhBq1dxQkY4A6-h_lVIFbxGF6uHo8KpmwM1jAWxRvKYYu0VbTYOBjQWuakS7dIq11_6maRoAEaLNWjigMKlQSeCP6kVKnpoEDxy1Rqw9sV4ttJjFDrqZRcwwIvNhqVc-eq1Ed-Uzev-HQVMTCDuHs8m0wPRNQYHP6M0fJNRae6tkhvxKEFwZKbco7om6F3VPE-xRyOT_HkpAU9HA"
}
	 */

	private final byte[] secret;
	


	public JWTValidationPreFilter(String sharedSecret) {
		// Base64 URL decode the secret for verification
		final Base64 base64 = new Base64(true);
		secret = base64.decode(sharedSecret);
	}
	
	private void sendResponse(int responseCode, String responseBody) {
		final RequestContext ctx = RequestContext.getCurrentContext();
		ctx.setResponseBody(responseBody);
		ctx.setResponseStatusCode(responseCode);
		
		
		try {
			ctx.getResponse().sendError(responseCode, responseBody);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public boolean shouldFilter() {
		return false;
	}

	@Override
	public Object run() {
		// unpack header to determine whether it's a trusted entity (?)
		
		final RequestContext ctx = RequestContext.getCurrentContext();
		final HttpServletRequest request = ctx.getRequest();
		final String getHeader = request.getHeader("Authorization");
		
		final Enumeration<String> headers = request.getHeaderNames();
		
		while (headers.hasMoreElements()) {
			final String headerName = headers.nextElement();
			final String header = request.getHeader(headerName);
			
			log.info(headerName+ "=" +header);
		}
		
		log.trace(String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()));
		
		if (getHeader == null || getHeader.equals("null")) {
			log.debug("Missing authentication header!");
			
			sendResponse(HttpServletResponse.SC_UNAUTHORIZED, "Missing authentication header!");
			return null;
		}

		// the header should be like "Bearer askjdhgkjasdfhkdaslhgkadsjhkd"
		if (!getHeader.toLowerCase().startsWith("bearer")) {
			log.debug("Invalid authentication header!");
			
			sendResponse(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authentication header!");
			return null;
		}
		
		// split the string after the bearer and validate it
		final String[] arr = getHeader.split("\\s+");
		log.info("Header array length is: " + arr.length);
		
		if (arr.length < 2) {
			log.debug("Invalid authentication header!");
			sendResponse(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authentication header!");
			return null;
		}
		
		final String jwt = arr[1];
		log.trace("Encoded token is: " + jwt);
		try {
			final SignedJWT signedJWT = SignedJWT.parse(jwt);
			final JWSVerifier verifier = new MACVerifier(secret);

			log.info("Issuer:" + signedJWT.getJWTClaimsSet().getIssuer());
			log.info("Issue time:" + signedJWT.getJWTClaimsSet().getIssueTime());
			log.info("Expiration time:" + signedJWT.getJWTClaimsSet().getExpirationTime());
			log.info("Not Before time:" + signedJWT.getJWTClaimsSet().getNotBeforeTime());
			log.info("Audience:" + signedJWT.getJWTClaimsSet().getAudience());
	
			
			// verify the claims
			if (!signedJWT.verify(verifier)) {
				log.debug("Unable to verify JWT");
				sendResponse(HttpServletResponse.SC_UNAUTHORIZED, "Unable to verify JWT token");
				return null;
			}
			
			log.info("JWT token is valid!");
			
			// issuer must be "apic"
			if (signedJWT.getJWTClaimsSet().getIssuer() == null ||
				!signedJWT.getJWTClaimsSet().getIssuer().equals("apic")) {
				log.debug("Invalid issuer!");
				sendResponse(HttpServletResponse.SC_UNAUTHORIZED, "Unable to verify JWT token");
				return null;
			}
			
			if (signedJWT.getJWTClaimsSet().getExpirationTime() == null ||
				signedJWT.getJWTClaimsSet().getExpirationTime().before(Calendar.getInstance().getTime())) {
				log.debug("JWT Token expired!");
				sendResponse(HttpServletResponse.SC_UNAUTHORIZED, "JWT token expired");
				return null;
			}
			
			if (signedJWT.getJWTClaimsSet().getNotBeforeTime() != null &&
				signedJWT.getJWTClaimsSet().getNotBeforeTime().after(Calendar.getInstance().getTime())) {
				log.debug("JWT Token not valid!");
				sendResponse(HttpServletResponse.SC_UNAUTHORIZED, "JWT token invalid");
				return null;
			}
		} catch (ParseException e) {
			log.error("Parse exception: " + e.getMessage());
			sendResponse(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
		} catch (JOSEException e) {
			log.error("JOSEException: " + e.getMessage());
			sendResponse(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
		}
		
		return null;
	}

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return 1;
	}

}
