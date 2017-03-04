/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.osgi.dev.e4.plugin.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * A Temporary {@link File} holder that will delete the temporary file if the holder is closed.
 */
public class DeleteOnCloseTempFileWrapper implements Closeable {

  private File tempFile;

  /**
   * Constructor.
   *
   * @param prefix
   *          See {@link File#createTempFile(String, String)} prefix parameter.
   * @param suffix
   *          See {@link File#createTempFile(String, String)} suffix parameter.
   */
  public DeleteOnCloseTempFileWrapper(final String prefix, final String suffix) {
    try {
      tempFile = File.createTempFile(prefix, suffix);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Deletes the temporary file.
   */
  @Override
  public void close() {
    tempFile.delete();
  }

  public File getTempFile() {
    return tempFile;
  }
}
