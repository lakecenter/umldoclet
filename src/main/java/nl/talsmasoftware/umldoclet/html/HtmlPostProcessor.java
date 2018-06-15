/*
 * Copyright 2016-2018 Talsma ICT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.talsmasoftware.umldoclet.html;

import nl.talsmasoftware.umldoclet.configuration.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

/**
 * Postprocesses the HTML output from the standard doclet to add UML diagrams.
 *
 * @author Sjoerd Talsma
 */
public class HtmlPostProcessor {
    private final Configuration config;

    public HtmlPostProcessor(Configuration config) {
        this.config = requireNonNull(config, "Configuration is <null>.");
    }

    public boolean postProcessHtml() throws IOException {
        final Path dir = getDestinationPath();
        final Collection<Diagram> diagrams = new DiagramCollector(config).collect(dir);

        return Files.walk(dir)
                .filter(HtmlFile::matches)
                .map(path -> new HtmlFile(config, path))
                .map(htmlFile -> htmlFile.process(diagrams))
                .reduce((a, b) -> a & b).orElse(true);
    }

    /**
     * @return The configured destination path.
     */
    private Path getDestinationPath() {
        final File destinationDir = new File(config.destinationDirectory());
        if (!destinationDir.isDirectory() || !destinationDir.canRead()) {
            throw new IllegalStateException("Cannot read from configured destination directory \"" + destinationDir + "\"!");
        }
        return destinationDir.toPath();
    }

}
