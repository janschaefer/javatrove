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
package org.kordamp.javatrove.example01.controller;

import org.kordamp.javatrove.example01.model.AppModel;
import org.kordamp.javatrove.example01.service.Github;
import org.kordamp.javatrove.example01.util.ApplicationEventBus;
import org.kordamp.javatrove.example01.util.ThrowableEvent;

import javax.inject.Inject;

import static org.kordamp.javatrove.example01.model.State.READY;
import static org.kordamp.javatrove.example01.model.State.RUNNING;

/**
 * @author Andres Almiray
 */
public class AppController {
    @Inject private AppModel model;
    @Inject private Github github;
    @Inject private ApplicationEventBus eventBus;

    public void loadRepositories() {
        model.setState(RUNNING);
        github.repositories(model.getOrganization())
            .done(model.getRepositories()::addAll)
            .fail(throwable -> eventBus.publishAsync(new ThrowableEvent(throwable)))
            .always((state, resolved, rejected) -> model.setState(READY));
    }
}
