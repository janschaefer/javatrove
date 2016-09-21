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
package org.kordamp.javatrove.example01;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.tngtech.jgiven.CurrentStep;
import com.tngtech.jgiven.annotation.Quoted;
import com.tngtech.jgiven.annotation.ScenarioState;
import com.tngtech.jgiven.attachment.Attachment;
import com.tngtech.jgiven.attachment.MediaType;
import com.tngtech.jgiven.junit.ScenarioTest;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Window;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.kordamp.javatrove.example01.service.GithubAPI;
import org.kordamp.javatrove.example01.view.AppView;
import org.testfx.framework.junit.ApplicationRule;
import org.testfx.service.support.WaitUntilSupport;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static com.google.inject.name.Names.named;
import static org.testfx.api.FxAssert.assertContext;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isEnabled;
import static org.testfx.matcher.control.LabeledMatchers.hasText;
import static org.testfx.matcher.control.ListViewMatchers.hasItems;

/**
 * @author Andres Almiray
 */
@RunWith(JukitoRunner.class)
@UseModules({FunctionalTest.AppTestModule.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FunctionalTest extends ScenarioTest<GivenGitHub, FunctionalTest.Steps, FunctionalTest.Steps> {
    private static final String ORGANIZATION = "foo";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8080);

    @Inject
    @ScenarioState
    private ObjectMapper objectMapper;

    @Inject
    private AppView view;

    @Rule
    @ScenarioState
    public ApplicationRule testfx = new ApplicationRule(stage -> {
        stage.setScene(view.createScene());
        stage.sizeToScene();
        stage.setResizable(false);
        stage.show();
    });

    @Test
    public void _01_happy_path() throws Exception {
        given().organization_$_has_$_repositories(ORGANIZATION, 3);
        when().loading(ORGANIZATION);
        then().$_repositories_will_be_loaded(3);
    }

    @Test
    public void _02_failure_path() {
        given().organization_$_does_not_exist(ORGANIZATION);
        when().loading(ORGANIZATION);
        then().an_error_is_shown();
    }

    public static class Steps {
        @ScenarioState
        public ApplicationRule testfx;

        @ScenarioState
        CurrentStep currentStep;

        public void loading(@Quoted String organization) {
            testfx.clickOn("#organization")
                    .eraseText(ORGANIZATION.length())
                    .write(ORGANIZATION);
            testfx.clickOn("#loadButton");
        }

        public void $_repositories_will_be_loaded(int numberOfRepositories) {
            Button loadButton = testfx.lookup("#loadButton").query();
            new WaitUntilSupport().waitUntil(loadButton, isEnabled(), 2);

            verifyThat("#total", hasText("" + numberOfRepositories));
            verifyThat("#repositories", hasItems(numberOfRepositories));

            takeScreenshot(testfx.targetWindow());
        }

        public void an_error_is_shown()  {
            Window w = testfx.window("Error");
            new WaitUntilSupport().waitUntil(w, WindowMatchers.isShowing(), 5);
            // workaround to ensure that the error window is completely rendered, should be improved
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            takeScreenshot(w);
        }

        private void takeScreenshot(Window w) {
            try {
                Rectangle2D region = new Rectangle2D(w.getX(), w.getY(), w.getWidth(), w.getHeight());
                Image image = assertContext().getCaptureSupport().captureRegion(region);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", os);
                os.flush();
                InputStream is = new ByteArrayInputStream(os.toByteArray());
                currentStep.addAttachment(Attachment.fromBinaryInputStream(is, MediaType.PNG).showDirectly());
            } catch (IOException o) {
                throw new RuntimeException(o);
            }
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
