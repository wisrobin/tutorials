package org.baeldung.httpclient.rare;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class HttpClientUnshortenLiveTest {

    private CloseableHttpClient client;

    // fixtures

    @Before
    public final void before() {
        client = HttpClientBuilder.create().disableRedirectHandling().build();
    }

    // tests

    @Test
    public final void givenShortenedOnce_whenUrlIsUnshortened_thenCorrectResult() throws IOException {
        final String expectedResult = "http://www.baeldung.com/rest-versioning";
        final String actualResult = expandSingleLevel("http://bit.ly/13jEoS1");
        assertThat(actualResult, equalTo(expectedResult));
    }

    @Test
    public final void givenShortenedMultiple_whenUrlIsUnshortened_thenCorrectResult() throws IOException {
        final String expectedResult = "http://www.baeldung.com/rest-versioning";
        final String actualResult = expand("http://t.co/e4rDDbnzmk");
        assertThat(actualResult, equalTo(expectedResult));
    }

    // API

    final String expand(final String urlArg) throws IOException {
        String originalUrl = urlArg;
        String newUrl = expandSingleLevel(originalUrl);
        while (!originalUrl.equals(newUrl)) {
            originalUrl = newUrl;
            newUrl = expandSingleLevel(originalUrl);
        }

        return newUrl;
    }

    final String expandSafe(final String urlArg) throws IOException {
        String originalUrl = urlArg;
        String newUrl = expandSingleLevelSafe(originalUrl).getRight();
        final List<String> alreadyVisited = Lists.newArrayList(originalUrl, newUrl);
        while (!originalUrl.equals(newUrl)) {
            originalUrl = newUrl;
            final Pair<Integer, String> statusAndUrl = expandSingleLevelSafe(originalUrl);
            newUrl = statusAndUrl.getRight();
            final boolean isRedirect = statusAndUrl.getLeft() == 301 || statusAndUrl.getLeft() == 302;
            if (isRedirect && alreadyVisited.contains(newUrl)) {
                throw new IllegalStateException("Likely a redirect loop");
            }
            alreadyVisited.add(newUrl);
        }

        return newUrl;
    }

    final Pair<Integer, String> expandSingleLevelSafe(final String url) throws IOException {
        HttpGet request = null;
        HttpEntity httpEntity = null;
        InputStream entityContentStream = null;

        try {
            request = new HttpGet(url);
            final HttpResponse httpResponse = client.execute(request);

            httpEntity = httpResponse.getEntity();
            entityContentStream = httpEntity.getContent();

            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != 301 && statusCode != 302) {
                return new ImmutablePair<Integer, String>(statusCode, url);
            }
            final Header[] headers = httpResponse.getHeaders(HttpHeaders.LOCATION);
            Preconditions.checkState(headers.length == 1);
            final String newUrl = headers[0].getValue();

            return new ImmutablePair<Integer, String>(statusCode, newUrl);
        } catch (final IllegalArgumentException uriEx) {
            return new ImmutablePair<Integer, String>(500, url);
        } finally {
            if (request != null) {
                request.releaseConnection();
            }
            if (entityContentStream != null) {
                entityContentStream.close();
            }
            if (httpEntity != null) {
                EntityUtils.consume(httpEntity);
            }
        }
    }

    final String expandSingleLevel(final String url) throws IOException {
        HttpGet request = null;
        HttpEntity httpEntity = null;
        InputStream entityContentStream = null;

        try {
            request = new HttpGet(url);
            final HttpResponse httpResponse = client.execute(request);

            httpEntity = httpResponse.getEntity();
            entityContentStream = httpEntity.getContent();

            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != 301 && statusCode != 302) {
                return url;
            }
            final Header[] headers = httpResponse.getHeaders(HttpHeaders.LOCATION);
            Preconditions.checkState(headers.length == 1);
            final String newUrl = headers[0].getValue();

            return newUrl;
        } catch (final IllegalArgumentException uriEx) {
            return url;
        } finally {
            if (request != null) {
                request.releaseConnection();
            }
            if (entityContentStream != null) {
                entityContentStream.close();
            }
            if (httpEntity != null) {
                EntityUtils.consume(httpEntity);
            }
        }
    }

}
