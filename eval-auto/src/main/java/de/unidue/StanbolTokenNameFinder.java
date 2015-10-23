package de.unidue;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.iterator.Filter;
import de.unidue.proxyapi.util.EnhancementEngine;
import de.unidue.proxyapi.util.EntitiesExtractorAnswerConverter;
import de.unidue.proxyapi.util.impl.StanbolAnswerConverter;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.util.Span;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StanbolTokenNameFinder implements TokenNameFinder {
    private final String baseUrl;
    private final EntitiesExtractorAnswerConverter stanbolAnswerConverter = new StanbolAnswerConverter();
    private CloseableHttpClient httpClient;
    private final EnhancementEngine engine;

    public StanbolTokenNameFinder(final String serverAddress, final EnhancementEngine engine) {
        this.engine = engine;
        this.baseUrl = serverAddress.concat("/enhancer/chain/");
        init();
    }

    private void init() {
        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);
        final List<Header> defaultRequestHeaders = getDefaultStanbolRequestHeaders();
        this.httpClient = HttpClients.custom().setConnectionManager(connectionManager).setDefaultHeaders(defaultRequestHeaders).build();
    }

    private List<Header> getDefaultStanbolRequestHeaders() {
        final List<Header> defaultRequestHeaders = new ArrayList<>();
        defaultRequestHeaders.add(new BasicHeader("Accept", "application/rdf+xml"));
        defaultRequestHeaders.add(new BasicHeader("Content-type", "text/plain"));
        return defaultRequestHeaders;
    }

    @Override
    public Span[] find(final String[] strings) {
        final String sentence = StringUtils.join(strings, " ");
        List<Span> foundEntities = null;
        final HttpContext httpContext = HttpClientContext.create();
        final HttpPost enhancementRequest = generatePostRequest(sentence, engine);
        try (CloseableHttpResponse stanbolResponse = this.httpClient.execute(enhancementRequest, httpContext)) {
            HttpEntity responseBody = stanbolResponse.getEntity();
            foundEntities = convertExtractorAnswer(responseBody.getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Span> converted = new ArrayList<>();
        foundEntities.stream().forEach(span -> {
            int start = 0;
            for (int i = 0; i <= span.getStart(); i++) {
                if (i >= sentence.length() || sentence.substring(i, i+1).equals(" ")) {
                    start++;
                }
            }
            int end = 0;
            for (int i = 0; i <= span.getEnd(); i++) {
                if (i >= sentence.length() || sentence.substring(i, i+1).equals(" ")) {
                    end++;
                }
            }

            converted.add(new Span(start, end, span.getType()));
        });
        return converted.toArray(new Span[foundEntities.size()]);
    }

    private HttpPost generatePostRequest(final String snippet, final EnhancementEngine engine) {
        final HttpPost enhancementRequest = new HttpPost(this.baseUrl.concat(engine.toString()));
        try {
            enhancementRequest.setEntity(new StringEntity(snippet));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return enhancementRequest;
    }

    private List<Span> convertExtractorAnswer(final InputStream content) {
        final Model rawResponseModel = ModelFactory.createDefaultModel();
        final Set<Span> foundEntities = new HashSet<>();

        rawResponseModel.read(content, null);
        rawResponseModel.listResourcesWithProperty(ResourceFactory.createProperty("http://fise.iks-project.eu/ontology/", "start")).
                filterKeep(new Filter<Resource>() {
                    @Override
                    public boolean accept(final Resource o) {
                        return o.getProperty(ResourceFactory.createProperty("http://purl.org/dc/terms/", "creator")).getString().equals("org.apache.stanbol.enhancer.engines.entitylinking.engine.EntityLinkingEngine");
                    }
                }).
                mapWith(entity -> new Span(entity.getProperty(ResourceFactory.createProperty("http://fise.iks-project.eu/ontology/", "start")).getInt(),
                        entity.getProperty(ResourceFactory.createProperty("http://fise.iks-project.eu/ontology/", "end")).getInt(),
                        null)).forEachRemaining(foundEntities::add);
        return new ArrayList<>(foundEntities);
    }

    @Override
    public void clearAdaptiveData() {

    }
}
