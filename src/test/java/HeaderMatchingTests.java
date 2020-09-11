import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class HeaderMatchingTests {

    private WireMockServer wireMock;

    @BeforeEach
    public void startWireMock() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
    }

    @AfterEach
    public void stopWireMock() {
        wireMock.stop();
    }

    // This test passes, but it should fail because the If-Modified-Since header is not provided.
    @Test
    public void testMatchHeader_PathWithExtension() throws URISyntaxException, IOException, InterruptedException {
        wireMock.stubFor(
                get(urlPathEqualTo("/test.txt"))
                        .withHeader("If-Modified-Since", equalTo("Mon, 07 Sep 2020 20:55:12 GMT"))
                        .willReturn(aResponse()
                                .withBody("hello, world\n")));
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(new URI(wireMock.url("test.txt"))).build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        assertThat(response.statusCode(), equalTo(200));
        assertThat(response.body(), Matchers.equalTo("hello, world\n"));
    }

    // This test should fail and does.
    @Test
    public void testMatchHeader_PathWithoutExtension() throws URISyntaxException, IOException, InterruptedException {
        wireMock.stubFor(
                get(urlPathEqualTo("/test"))
                        .withHeader("If-Modified-Since", equalTo("Mon, 07 Sep 2020 20:55:12 GMT"))
                        .willReturn(aResponse()
                                .withBody("hello, world\n")));
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(new URI(wireMock.url("test"))).build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        assertThat(response.statusCode(), equalTo(200));
        assertThat(response.body(), Matchers.equalTo("hello, world\n"));
    }

}
