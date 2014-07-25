package org.esupportail.catapp.web.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.esupportail.catapp.domain.service.ICatAppServ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.portlet.bind.annotation.ActionMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.portlet.*;
import java.io.IOException;

import static org.esupportail.catapp.web.controllers.CatalogueController.ResourceUrlW.wrap;

@Controller("catalogueController")
@RequestMapping("VIEW")
public class CatalogueController {

	private ICatAppServ catalogueService;

    @Autowired
	private CatalogueController(ICatAppServ catalogueService) {
		this.catalogueService = catalogueService;
	}

    public static CatalogueController catalogueController(
			ICatAppServ catalogueService) {
		return new CatalogueController(catalogueService);
	}

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(CatalogueController.class);

    /**
     * Add ressourceURL and actionURL to spring MVC model
     * @param request
     *            RenderRequest the request
     * @param response
     *            RenderResponse the response
     * @return completed spring MVC model
     */
    @RenderMapping
    public String goHome(RenderRequest request, RenderResponse response, ModelMap model) throws IOException, InterruptedException {
        if (request.getWindowState().equals(WindowState.MINIMIZED)) {
            return "mini";
        }
        bindInitialModel(request);
        model.addAttribute("resourceURL",
                wrap(response.createResourceURL())
                        .withResId("@@id@@")
                        .withResParam("p1", "__p1__")
                        .withResParam("p2", "__p2__")
                        .withResParam("p3", "__p3__")
                        .withResParam("p4", "__p4__")
                        .resourceUrl);

        final PortletURL actionURL = response.createActionURL();
        actionURL.setParameter("action", "@@id@@");
        actionURL.setParameter("p1", "__p1__");
        actionURL.setParameter("p2", "__p2__");
        actionURL.setParameter("p3", "__p3__");
        model.addAttribute("actionURL", actionURL);

        final String fragUrl1 = request.getScheme() + "://" + request.getServerName() +":"+ request.getServerPort();
        final String fragUrl2 = response.createRenderURL().toString();
        model.addAttribute("containerURL", fragUrl1.toLowerCase()+fragUrl2.toLowerCase());

        final PortletPreferences prefs = request.getPreferences();
        model.addAttribute("withFavs", prefs.getValue("withFavs", null).trim());

        return "catalogue";
    }

    /**
     * Init domains tree and applications list in service
     * @param request
     */
    protected void bindInitialModel(final RenderRequest request) throws IOException, InterruptedException {
        PortletPreferences prefs = request.getPreferences();
        String userId = request.getRemoteUser();
        String wsUrl = prefs.getValue("wsUrl", null).trim();
        String idDomain = prefs.getValue("idDomain", null).trim();
        catalogueService.initData(wsUrl, idDomain, userId);
    }

    /**
     * Add preferred application to spring MVC model
     * @param request RenderRequest the request
     * @return completed spring MVC model
     */
    @ResourceMapping(value = "favapps")
	public View getApplications(ResourceRequest request) throws IOException, InterruptedException {
        Boolean find = false;
        PortletPreferences prefs = request.getPreferences();
        String[] favoris = prefs.getValues("favoris", new String[0]);
        JsonNode appsUser = catalogueService.getApplications();
        ArrayNode allowedFav = new ObjectMapper().createArrayNode();
        ArrayNode disallowedFavs = new ObjectMapper().createArrayNode();
        MappingJackson2JsonView view = new MappingJackson2JsonView();
        for (String favori : favoris) {
            find = false;
            for (JsonNode jApp : appsUser) {
                if(favori.equals(jApp.get("code").asText())) {
                    allowedFav.add(jApp);
                    find = true;
                    break;
                }
            }
            if(!find) {
                JsonNode disallowedFav = catalogueService.getApplication(favori);
                if(disallowedFav != null) {
                    ((ObjectNode)disallowedFav).put("url", "");
                    disallowedFavs.add(disallowedFav);
                }
            }
        }
        view.addStaticAttribute("apps", allowedFav);
        view.addStaticAttribute("noAccessApps", disallowedFavs);
        return view;
	}

    /**
     * Add domains tree to spring MVC model
     * @param request RenderRequest the request
     * @return completed spring MVC model
     */
    @ResourceMapping(value = "doms")
    public View getDomains(ResourceRequest request) throws IOException, InterruptedException {
        MappingJackson2JsonView view = new MappingJackson2JsonView();
        JsonNode jdoms = catalogueService.getDomainsTree();
        view.addStaticAttribute("doms", jdoms);
        return view;
    }

    /**
     * Add all allowed applications to spring MVC model
     * @param request RenderRequest the request
     * @return completed spring MVC model
     */
    @ResourceMapping(value = "allowedapps")
    public View getPerms(ResourceRequest request) throws IOException, InterruptedException {
        MappingJackson2JsonView view = new MappingJackson2JsonView();
        JsonNode japps = catalogueService.getApplications();
        view.addStaticAttribute("allowedapps", japps);
        return view;
    }


    /**
     * Update portlet preferences
     * Receive the key, action parameters from UI.
     * Because the action value is updateFavorite, updateFavorite will be called.
     * @param request
     * @param response
     * @throws Exception
     */
    @ActionMapping(params="action=updateFavorite")
    public void updateFavorite(@RequestParam(value = "p1") String favCodes,
                               ActionRequest request, ActionResponse response) throws Exception {
        PortletPreferences prefs = request.getPreferences();
        String[] favPrefs = favCodes.split(",");
        try {
            prefs.setValues("favoris", favPrefs);
            prefs.store();
        } catch (ReadOnlyException e) {
            LOG.debug("La propriété 'favoris' n'est pas modifiable");
        }
    }


    public static final class ResourceUrlW {

        private final ResourceURL resourceUrl;

        ResourceUrlW withResId(String resId) {
            resourceUrl.setResourceID(resId);
            return this;
        }

        ResourceUrlW withResParam(String key, String value) {
            resourceUrl.setParameter(key, value);
            return this;
        }

        private ResourceUrlW(ResourceURL resourceUrl) {
            this.resourceUrl = resourceUrl;
        }

        public static ResourceUrlW wrap(ResourceURL resourceUrl) {
            return new ResourceUrlW(resourceUrl);
        }

        public ResourceURL getResourceUrl() {
            return this.resourceUrl;
        }
    }



}
