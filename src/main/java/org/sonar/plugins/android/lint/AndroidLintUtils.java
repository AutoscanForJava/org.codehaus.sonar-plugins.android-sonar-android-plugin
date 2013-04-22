/*
 * Sonar Android Plugin
 * Copyright (C) 2013 Jerome Van Der Linden, Stephane Nicolas and SonarSource
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

import com.android.tools.lint.detector.api.Severity;
import org.sonar.api.rules.RulePriority;

/**
 *
 * @author Jerome Van Der Linden
 */
public class AndroidLintUtils {

    /**
     * private constructor for utils class
     */
    private AndroidLintUtils() {

    }

    /**
     * Convert Android Lint {@link Severity} to Sonar {@link RulePriority}
     * @param severityLint Android Lint Severity
     * @return Sonar Severity
     */
    public static RulePriority getSonarSeverityFromLintSeverity(Severity severityLint) {
        RulePriority severity;
        switch (severityLint) {
            case FATAL:
                severity = RulePriority.BLOCKER;
                break;
            case ERROR:
                severity = RulePriority.CRITICAL;
                break;
            case WARNING:
                severity = RulePriority.MAJOR;
                break;
            case INFORMATIONAL:
                severity = RulePriority.INFO;
                break;
            default:
                severity = null;
        }
        return severity;
    }
}
