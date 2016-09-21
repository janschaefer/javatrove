package org.kordamp.javatrove.example01;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tngtech.jgiven.annotation.Quoted;
import com.tngtech.jgiven.annotation.ScenarioState;
import org.kordamp.javatrove.example01.model.Repository;

import javax.inject.Inject;

import java.util.Collection;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.kordamp.javatrove.example01.TestHelper.createSampleRepositories;
import static org.kordamp.javatrove.example01.TestHelper.repositoriesAsJSON;

public class GivenGitHub {
    @ScenarioState
    private ObjectMapper objectMapper;

    @ScenarioState
    private Collection<Repository> repositories;

    public void organization_$_exists(@Quoted String organization) {
        organization_$_has_$_repositories(organization, 3);
    }

    public void organization_$_has_$_repositories(@Quoted String organization, int numberOfRepositories) {
        repositories = createSampleRepositories(numberOfRepositories);
        stubFor(get(urlEqualTo("/orgs/" + organization + "/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/json")
                        .withBody(repositoriesAsJSON(repositories, objectMapper))));
    }

    public void organization_$_does_not_exist(@Quoted String organization) {
        stubFor(get(urlEqualTo("/orgs/" + organization + "/repos"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withStatusMessage("Internal Error")));
    }

}
