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
import com.google.common.collect.Lists;
import org.kordamp.javatrove.example01.model.Repository;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author Andres Almiray
 */
public class TestHelper {
    public static Collection<Repository> createSampleRepositories() {
        return createSampleRepositories(3);
    }

    public static Collection<Repository> createSampleRepositories(int numberOfRepositories) {
        List<Repository> result = Lists.newArrayList();
        for (int i = 1; i <= numberOfRepositories; i++) {
            result.add(Repository.builder().name("repo"+i).fullName("foo/repo"+1).build());
        }
        return result;
    }

    public static String repositoriesAsJSON(Collection<Repository> repositories, ObjectMapper objectMapper) {
        StringWriter writer = new StringWriter();
        try {
            objectMapper.writeValue(writer, repositories);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return writer.toString();
    }
}
