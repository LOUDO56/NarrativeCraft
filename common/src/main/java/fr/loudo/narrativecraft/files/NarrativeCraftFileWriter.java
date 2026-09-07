/*
 * NarrativeCraft - Create narrative games inside Minecraft. No coding, no game engine, only text and logic.
 * Copyright (c) 2025 LOUDO and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package fr.loudo.narrativecraft.files;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class NarrativeCraftFileWriter {

    private static final String TEMPORARY_SUFFIX = ".tmp";

    @FunctionalInterface
    public interface WriterContent {
        void write(Writer writer) throws IOException;
    }

    @FunctionalInterface
    public interface StreamContent {
        void write(DataOutputStream stream) throws IOException;
    }

    private NarrativeCraftFileWriter() {}

    public static boolean isTemporary(File file) {
        return file.getName().endsWith(TEMPORARY_SUFFIX);
    }

    public static void writeString(File target, String content) throws IOException {
        write(target, writer -> writer.write(content));
    }

    public static void write(File target, WriterContent content) throws IOException {
        Path temporary = temporaryPathFor(target);
        try {
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                content.write(writer);
            }
            commit(temporary, target.toPath());
        } catch (IOException e) {
            Files.deleteIfExists(temporary);
            throw e;
        }
    }

    public static void writeStream(File target, StreamContent content) throws IOException {
        Path temporary = temporaryPathFor(target);
        try {
            try (DataOutputStream stream =
                    new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(temporary)))) {
                content.write(stream);
            }
            commit(temporary, target.toPath());
        } catch (IOException e) {
            Files.deleteIfExists(temporary);
            throw e;
        }
    }

    private static Path temporaryPathFor(File target) throws IOException {
        Path path = target.toPath().toAbsolutePath();
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        return path.resolveSibling(path.getFileName() + TEMPORARY_SUFFIX);
    }

    private static void commit(Path temporary, Path target) throws IOException {
        try {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
