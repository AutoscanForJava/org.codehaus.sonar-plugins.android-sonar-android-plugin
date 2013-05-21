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

import com.google.common.io.Closeables;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ServerComponent;
import org.sonar.api.rules.Rule;
import org.sonar.api.utils.SonarException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Rule text file (retrieve with <i>lint --show</i>) parser
 *
 * @author Stephane Nicolas
 * @author Jerome Van Der Linden
 */
public final class AndroidLintRuleParser implements ServerComponent {

    private static final Logger logger = LoggerFactory.getLogger(AndroidLintRuleParser.class);

    public List<Rule> parse(File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(FileUtils.openInputStream(file), CharEncoding.UTF_8));
            return parse(reader);

        } catch (IOException e) {
            throw new SonarException("Fail to load the file: " + file, e);

        } finally {
            Closeables.closeQuietly(reader);
        }
    }

    /**
     * Warning : the input stream is closed in this method
     */
    public List<Rule> parse(InputStream input) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(input, CharEncoding.UTF_8));
            return parse(reader);

        } catch (IOException e) {
            throw new SonarException("Fail to load the xml stream", e);

        } finally {
            Closeables.closeQuietly(reader);
        }
    }

    public List<Rule> parse(BufferedReader reader) {

        try {
            List<Rule> rules = new ArrayList<Rule>();

            List<String> listLines = IOUtils.readLines(reader);

            String previousLine = null;
            Rule rule = null;
            boolean inSummary = false;
            boolean previousWasCategory = false;
            for (String line : listLines) {
                if (line.matches("\\=.*")) {
                    previousWasCategory = false;
                } else if (line.matches("[\\-]{4,}.*")) {
                    logger.debug("Rule found : " + previousLine);

                    // remove the rule name from the description of the previous rule
                    if (rule != null) {
                        int index = rule.getDescription().lastIndexOf(previousLine);
                        if (index > 0) {
                            rule.setDescription(rule.getDescription().substring(0, index));
                        }
                    }

                    rule = Rule.create();
                    rules.add(rule);
                    rule.setName(previousLine);
                    rule.setKey(previousLine);
                    previousWasCategory = false;
                } else if (line.matches("Summary:.*")) {
                    inSummary = true;
                    rule.setDescription(line.substring(line.indexOf(':') + 1));
                } else if (line.matches("Priority:.*")) {
                    inSummary = false;
                    previousWasCategory = false;
                } else if (line.matches("Category:.*")) {
                    inSummary = false;
                    previousWasCategory = true;
                } else if (line.matches("Severity:.*")) {
                    inSummary = false;
                    previousWasCategory = false;
                    String severity = line.substring("Severity: ".length());
                    rule.setSeverity(AndroidLintUtils.getSonarSeverityFromLintSeverity(severity));
                } else {
                    if (inSummary || previousWasCategory) {
                        if (line.contains("http://")) {
                            int indexOfLink = line.indexOf("http://");
                            String link = line.substring(indexOfLink);
                            link = "<a href=\"" + link + "\" target=\"_blank\">" + link + "</a>";
                            line = link;
                        }
                        rule.setDescription(rule.getDescription() + "<br>" + line);
                    }
                }
                previousLine = line;
            }
            return rules;

        } catch (IOException e) {
            throw new SonarException("Rules file is not valid", e);
        }
    }
}
