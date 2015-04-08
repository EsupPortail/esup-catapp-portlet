package org.esupportail.catapp.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;


public class CatAppServImpl implements ICatAppServ {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private String wsDomainPath;
    private String wsAppPath;
    private String wsUserPath;
    private WebTarget webTarget;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static List<String> codesApp;
    private static ArrayNode allJApps = MAPPER.createArrayNode();
    private static JsonNode jDomTree = MAPPER.createArrayNode();

    private CatAppServImpl(String wsDomainPath, String wsUserPath, String wsAppPath) {
        this.wsDomainPath = wsDomainPath;
        this.wsUserPath = wsUserPath;
        this.wsAppPath = wsAppPath;
    }

    public static CatAppServImpl CatAppService(String wsDomainPath, String wsUserPath, String wsAppPath)
            throws MalformedURLException {
        return new CatAppServImpl(wsDomainPath, wsUserPath, wsAppPath);
    }

    /**
     * Create the WebTarget.
     *
     * @return WebResource
     * @throws MalformedURLException
     */
    public WebTarget createWebTarget(final String wsCatAppServiceURL) throws MalformedURLException {
        ClientConfig config = new ClientConfig().connectorProvider(new ApacheConnectorProvider());
        Client client = ClientBuilder.newClient(config);
        this.webTarget = client.target(wsCatAppServiceURL);
        return this.webTarget;
    }

    @Override
    public JsonNode getApplications() {
        return this.allJApps;
    }

    @Override
    public List<String> getCodesApplications() {
        return this.codesApp;
    }

    @Override
    public void initData(String wsUrl, String domId, String user) throws IOException, InterruptedException {
        WebTarget domainsTree = createWebTarget(wsUrl)
                .path(wsDomainPath)
                .path(domId)
                .path("/tree")
                .queryParam(wsUserPath, user);
        Response response = domainsTree.request().get();
        if(response.getStatus() == 200) {
            allJApps.removeAll();
            codesApp = new ArrayList<String>();
            String domainTree = response.readEntity(String.class);
            JsonNode nodeTree = MAPPER.readTree(domainTree);
            jDomTree = completeTree(nodeTree);
        }
    }

    private ObjectNode completeTree(JsonNode nodeTree) throws IOException, InterruptedException {
        ObjectNode jDomTree = (ObjectNode) nodeTree;
        JsonNode jDom;
        JsonNode doms;
        JsonNode apps = nodeTree.path("domain").path("applications");
        if(apps.isArray()) {
            ((ObjectNode)nodeTree.path("domain")).put("applications", appsForDom(apps));
        }
        for (JsonNode jsubDom : jDomTree.path("subDomains")) {
            completeTree(jsubDom);
        }
        return jDomTree;
    }

    private JsonNode appsForDom(JsonNode apps) throws IOException, InterruptedException {
        String appString;
        JsonNode jApp;
        ArrayNode jApps = MAPPER.createArrayNode();
        for (JsonNode sApp : apps) {
            appString = sApp.asText();
            jApp = getApplication(appString);
            if(jApp != null) {
                if(!codesApp.contains(jApp.get("code").asText())) {
                    codesApp.add(jApp.get("code").asText());
                    allJApps.add(jApp);
                }
                jApps.add(jApp);
            }
        }
        return jApps;
    }

    @Override
    public JsonNode getApplication(String code) throws InterruptedException, IOException {
        WebTarget application = this.webTarget
                .path(wsAppPath)
                .path(code);
        Response response = application.request().get();
        if(response.getStatus() == 200) {
            String sApp = response.readEntity(String.class);
            JsonNode jApp = MAPPER.readTree(sApp);
            return jApp;
        }
        return null;
    }

    @Override
    public JsonNode getDomainsTree() {
        return this.jDomTree;
    }

}

