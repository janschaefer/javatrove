/*
 * Copyright 2016 Andres Almiray
 *
 * This file is part of Java Trove Examples
 *
 * Java Trove Examples is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Java Trove Examples is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Java Trove Examples. If not, see <http://www.gnu.org/licenses/>.
 */
package org.kordamp.javatrove.example01.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.Quoted;
import com.tngtech.jgiven.annotation.ScenarioState;
import com.tngtech.jgiven.junit.ScenarioTest;
import org.jdeferred.Promise;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kordamp.javatrove.example01.AppModule;
import org.kordamp.javatrove.example01.GivenGitHub;
import org.kordamp.javatrove.example01.model.Repository;
import org.kordamp.javatrove.example01.service.Github;
import org.kordamp.javatrove.example01.service.GithubAPI;

import javax.inject.Inject;
import java.util.Collection;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.google.inject.name.Names.named;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.kordamp.javatrove.example01.TestHelper.createSampleRepositories;

/**
 * @author Andres Almiray
 */
@RunWith(JukitoRunner.class)
@UseModules({GithubImplTest.AppTestModule.class})
public class GithubImplTest extends ScenarioTest<GivenGitHub, GithubImplTest.Steps, GithubImplTest.Steps> {
    private static final String ORGANIZATION = "foo";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8080);

    @ScenarioState
    @Inject private ObjectMapper objectMapper;

    @ScenarioState
    @Inject private Github github;

    @Test
    public void happyPath() throws Exception {
        given().organization_$_exists(ORGANIZATION);
        when().getting_repositories_of(ORGANIZATION);
        then().all_repositories_are_returned()
            .and().a_GET_request_was_sent_to("/orgs/" + ORGANIZATION + "/repos");
    }

    @Test
    public void failurePath() {
        given().organization_$_does_not_exist(ORGANIZATION);
        when().getting_repositories_of(ORGANIZATION);
        then().an_error_with_message_$_is_returned("Internal Error")
             .and().a_GET_request_was_sent_to("/orgs/" + ORGANIZATION + "/repos");
    }

    public static class Steps extends Stage<Steps> {
        @ExpectedScenarioState
        private Github github;

        @ExpectedScenarioState
        private Collection<Repository> repositories;

        private Promise<Collection<Repository>, Throwable, Void> promise;

        public void getting_repositories_of(@Quoted String organization) {
            promise = github.repositories(ORGANIZATION);
        }

        public Steps all_repositories_are_returned() {
            await().timeout(2, SECONDS).until(promise::state, equalTo(Promise.State.RESOLVED));
            promise.done(result -> assertThat(result, equalTo(repositories)));
            return this;
        }

        public void a_GET_request_was_sent_to(String url) {
            verify(getRequestedFor(urlEqualTo(url)));
        }

        public Steps an_error_with_message_$_is_returned(String s) {
            await().timeout(2, SECONDS).until(promise::state, equalTo(Promise.State.REJECTED));
            promise.fail(throwable -> assertThat(throwable.getMessage(), equalTo("Internal Error")));
            return this;
        }
    }

    public static class AppTestModule extends AppModule {
        @Override
        protected void bindGithubApiUrl() {
            bindConstant()
                .annotatedWith(named(GithubAPI.GITHUB_API_URL_KEY))
                .to("http://localhost:8080");
        }
    }
}
