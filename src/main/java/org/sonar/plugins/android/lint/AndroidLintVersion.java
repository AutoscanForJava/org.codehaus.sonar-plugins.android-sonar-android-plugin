/*
 * Sonar Android Plugin
 * Copyright (C) 2013 Jerome Van Der Linden, Stephane Nicolas, Florian Roncari, Thomas Bores and SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.android.lint;

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public enum AndroidLintVersion {
  INSTANCE;

  private static final String PROPERTIES_PATH = "/org/sonar/plugins/android/lint/android-plugin.properties";
  private String version;

  public static String getVersion() {
    return INSTANCE.version;
  }

  private AndroidLintVersion() {
    InputStream input = getClass().getResourceAsStream(PROPERTIES_PATH);
    try {
      Properties properties = new Properties();
      properties.load(input);
      this.version = properties.getProperty("lint.version");

    } catch (IOException e) {
      LoggerFactory.getLogger(getClass()).warn("Can not load the Android Lint version from the file " + PROPERTIES_PATH);
      this.version = "";

    } finally {
      IOUtils.closeQuietly(input);
    }
  }
}
