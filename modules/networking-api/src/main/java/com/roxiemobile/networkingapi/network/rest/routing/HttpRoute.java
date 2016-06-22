package com.roxiemobile.networkingapi.network.rest.routing;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.roxiemobile.androidcommons.util.CollectionUtils;
import com.roxiemobile.androidcommons.util.LogUtils;
import com.roxiemobile.networkingapi.network.http.util.LinkedMultiValueMap;
import com.roxiemobile.networkingapi.network.http.util.MultiValueMap;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.roxiemobile.androidcommons.util.AssertUtils.assertNotNull;

public final class HttpRoute
{
// MARK: - Construction

    private HttpRoute(@NonNull URI uri) {
        assertNotNull(uri, "uri == null");

        // Init instance variables
        mUri = uri;
    }

// MARK: - Methods

    public static HttpRoute buildRoute(URI baseUri) {
        return buildRoute(baseUri, null);
    }

    public static HttpRoute buildRoute(URI baseUri, String path) {
        return buildRoute(baseUri, path, null);
    }

    public static HttpRoute buildRoute(URI baseUri, String path, MultiValueMap<String, String> params) {
        String uriString = null;

        // Build new URI
        if (baseUri != null) {
            uriString = baseUri.toString();

            // Append path to URI
            if (path != null) {
                uriString += path.trim();
            }

            // Append query params to URI
            if (params != null && params.size() > 0) {
                uriString += "?" + buildQueryString(params, "UTF-8");
            }
        }

        // Build new HTTP route
        HttpRoute route = null;
        try {
            if (uriString != null) {
                route = new HttpRoute(new URI(uriString));
            }
        }
        catch (URISyntaxException e) {
            LogUtils.e(TAG, e);
        }

        // Validate result
        if (route == null) {
            throw new IllegalStateException("Could not create HTTP route for path ‘" + path + "’.");
        }

        // Done
        return route;
    }

    public URI toURI() {
        return mUri;
    }

    public String toString() {
        return mUri.toString();
    }

// MARK: - Private Methods

    private static String buildQueryString(MultiValueMap<String, String> params, String charsetName) {
        List<String> components = new LinkedList<>();

        try {
            // Build query string components
            for (String key : params.keySet()) {
                components.addAll(buildQueryStringComponents(key, params.get(key), charsetName));
            }
        }
        catch (UnsupportedEncodingException e) {
            LogUtils.e(TAG, e);

            // Re-throw internal error
            throw new IllegalStateException("Could not build query string.", e);
        }

        // Done
        return TextUtils.join("&", components);
    }

    @SuppressWarnings("UnusedAssignment")
    private static List<String> buildQueryStringComponents(String key, List<String> values, String charsetName)
            throws UnsupportedEncodingException {

        if (key == null || CollectionUtils.isEmpty(values) || charsetName == null) {
            throw new IllegalArgumentException();
        }

        List<String> components = new LinkedList<>();
        String encodedValue = null;

        if (values.size() > 1) {
            for (String value : values) {
                encodedValue = URLEncoder.encode(key, charsetName) + "[]=" + URLEncoder.encode(value, charsetName);
                components.add(encodedValue);
            }
        }
        else {
            encodedValue = URLEncoder.encode(key, charsetName) + "=" + URLEncoder.encode(values.get(0), charsetName);
            components.add(encodedValue);
        }

        // Done
        return components;
    }

// MARK: - Inner Types

    public static class QueryParams extends LinkedMultiValueMap<String, String>
    {
        public QueryParams() {
            super();
        }

        public QueryParams(int initialCapacity) {
            super(initialCapacity);
        }

        public QueryParams(MultiValueMap<String, String> otherMap) {
            super(otherMap == null ? Collections.emptyMap() : otherMap);
        }
    }

// MARK: - Constants

    private static String TAG = HttpRoute.class.getSimpleName();

// MARK: - Variables

    private URI mUri;

}