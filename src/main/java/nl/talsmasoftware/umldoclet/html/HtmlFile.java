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

import net.sourceforge.plantuml.FileUtils;
import nl.talsmasoftware.umldoclet.configuration.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static nl.talsmasoftware.umldoclet.logging.Message.DEBUG_POSTPROCESSING_FILE;
import static nl.talsmasoftware.umldoclet.logging.Message.DEBUG_SKIPPING_FILE;

/**
 * Abstraction for a single HTML file generated by the Standard doclet.
 *
 * @author Sjoerd Talsma
 */
final class HtmlFile {

    final Configuration config;
    final Path path;

    HtmlFile(Configuration config, Path path) {
        this.config = requireNonNull(config, "Configuration is <null>.");
        this.path = requireNonNull(path, "HTML file is <null>.").normalize();
    }

    static boolean isHtmlFile(Path path) {
        return Optional.ofNullable(path).map(Path::toFile)
                .filter(File::isFile).filter(File::canRead)
                .map(File::getName).filter(name -> name.endsWith(".html"))
                .isPresent();
    }

    boolean process(Collection<UmlDiagram> diagrams) {
        return diagrams.stream()
                .map(diagram -> diagram.createPostprocessor(this))
                .filter(Optional::isPresent).map(Optional::get).findFirst()
                .map(this::process)
                .orElseGet(this::skip);
    }

    private boolean skip() {
        config.logger().debug(DEBUG_SKIPPING_FILE, path);
        return false;
    }

    private boolean process(Postprocessor postprocessor) {
        try {
            config.logger().debug(DEBUG_POSTPROCESSING_FILE, path);
            return postprocessor.call();
        } catch (IOException ioe) {
            throw new IllegalStateException("I/O exception postprocessing " + path, ioe);
        }
    }

    public List<String> readLines() throws IOException {
        return Files.readAllLines(path, config.htmlCharset());
    }

    public void replaceBy(File tempFile) throws IOException {
        File original = path.toFile();
        if (!original.delete()) throw new IllegalStateException("Cannot delete " + original);
        if (tempFile.renameTo(original)) {
            // TODO debug: System.out.println("Debug: " + original + " renamed from " + tempFile);
        } else {
            FileUtils.copyToFile(tempFile, original);
            if (!tempFile.delete()) {
                throw new IllegalStateException("Cannot delete " + tempFile + " after postprocessing!");
            }
            // TODO: debug: System.out.println("Debug: " + original + " copied from " + tempFile);
        }
    }
}
