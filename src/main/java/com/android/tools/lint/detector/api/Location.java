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
package com.android.tools.lint.detector.api;


import java.io.File;

/**
 * Location information for a warning
 * <p/>
 * <b>NOTE: This is not a public or final API; if you rely on this be prepared
 * to adjust your code for the next tools release.</b>
 */
public class Location {
    private final File mFile;
    private final Position mStart;
    private final Position mEnd;
    private String mMessage;
    private Location mSecondary;
    private Object mClientData;

    /**
     * (Private constructor, use one of the factory methods
     * {@link com.android.tools.lint.detector.api.Location#create(java.io.File)},
     * {@link com.android.tools.lint.detector.api.Location#create(java.io.File, Position, Position)}, or
     * {@link com.android.tools.lint.detector.api.Location#create(java.io.File, String, int, int)}.
     * <p/>
     * Constructs a new location range for the given file, from start to end. If
     * the length of the range is not known, end may be null.
     *
     * @param file  the associated file (but see the documentation for
     *              {@link #getFile()} for more information on what the file
     *              represents)
     * @param start the starting position, or null
     * @param end   the ending position, or null
     */
    protected Location(File file, Position start, Position end) {
        super();
        this.mFile = file;
        this.mStart = start;
        this.mEnd = end;
    }

    /**
     * Returns the file containing the warning. Note that the file *itself* may
     * not yet contain the error. When editing a file in the IDE for example,
     * the tool could generate warnings in the background even before the
     * document is saved. However, the file is used as a identifying token for
     * the document being edited, and the IDE integration can map this back to
     * error locations in the editor source code.
     *
     * @return the file handle for the location
     */
    public File getFile() {
        return mFile;
    }

    /**
     * The start position of the range
     *
     * @return the start position of the range, or null
     */
    public Position getStart() {
        return mStart;
    }

    /**
     * The end position of the range
     *
     * @return the start position of the range, may be null for an empty range
     */
    public Position getEnd() {
        return mEnd;
    }

    /**
     * Returns a secondary location associated with this location (if
     * applicable), or null.
     *
     * @return a secondary location or null
     */
    public Location getSecondary() {
        return mSecondary;
    }

    /**
     * Sets a secondary location for this location.
     *
     * @param secondary a secondary location associated with this location
     */
    public void setSecondary(Location secondary) {
        this.mSecondary = secondary;
    }

    /**
     * Sets a custom message for this location. This is typically used for
     * secondary locations, to describe the significance of this alternate
     * location. For example, for a duplicate id warning, the primary location
     * might say "This is a duplicate id", pointing to the second occurrence of
     * id declaration, and then the secondary location could point to the
     * original declaration with the custom message "Originally defined here".
     *
     * @param message the message to apply to this location
     */
    public void setMessage(String message) {
        mMessage = message;
    }

    /**
     * Returns the custom message for this location, if any. This is typically
     * used for secondary locations, to describe the significance of this
     * alternate location. For example, for a duplicate id warning, the primary
     * location might say "This is a duplicate id", pointing to the second
     * occurrence of id declaration, and then the secondary location could point
     * to the original declaration with the custom message
     * "Originally defined here".
     *
     * @return the custom message for this location, or null
     */
    public String getMessage() {
        return mMessage;
    }

    /**
     * Sets the client data associated with this location. This is an optional
     * field which can be used by the creator of the {@link com.android.tools.lint.detector.api.Location} to store
     * temporary state associated with the location.
     *
     * @param clientData the data to store with this location
     */
    public void setClientData(Object clientData) {
        mClientData = clientData;
    }

    /**
     * Returns the client data associated with this location - an optional field
     * which can be used by the creator of the {@link com.android.tools.lint.detector.api.Location} to store
     * temporary state associated with the location.
     *
     * @return the data associated with this location
     */
    public Object getClientData() {
        return mClientData;
    }

    @Override
    public String toString() {
        return "Location [file=" + mFile + ", start=" + mStart + ", end=" + mEnd + ", message="
                + mMessage + "]";
    }

    /**
     * Creates a new location for the given file
     *
     * @param file the file to create a location for
     * @return a new location
     */
    public static Location create(File file) {
        return new Location(file, null /*start*/, null /*end*/);
    }

    /**
     * Creates a new location for the given file and starting and ending
     * positions.
     *
     * @param file  the file containing the positions
     * @param start the starting position
     * @param end   the ending position
     * @return a new location
     */
    public static Location create(
            File file,
            Position start,
            Position end) {
        return new Location(file, start, end);
    }

    /**
     * Creates a new location for the given file, with the given contents, for
     * the given offset range.
     *
     * @param file        the file containing the location
     * @param contents    the current contents of the file
     * @param startOffset the starting offset
     * @param endOffset   the ending offset
     * @return a new location
     */
    public static Location create(
            File file,
            String contents,
            int startOffset,
            int endOffset) {
        if (startOffset < 0 || endOffset < startOffset) {
            throw new IllegalArgumentException("Invalid offsets");
        }

        if (contents == null) {
            return new Location(file,
                    new DefaultPosition(-1, -1, startOffset),
                    new DefaultPosition(-1, -1, endOffset));
        }

        int size = contents.length();
        endOffset = Math.min(endOffset, size);
        startOffset = Math.min(startOffset, endOffset);
        Position start = null;
        int line = 0;
        int lineOffset = 0;
        for (int offset = 0; offset <= size; offset++) {
            if (offset == startOffset) {
                start = new DefaultPosition(line, offset - lineOffset, offset);
            }
            if (offset == endOffset) {
                Position end = new DefaultPosition(line, offset - lineOffset, offset);
                return new Location(file, start, end);
            }
            char c = contents.charAt(offset);
            if (c == '\n') {
                lineOffset = offset;
                line++;
            }
        }
        return Location.create(file);
    }

    /**
     * Creates a new location for the given file, with the given contents, for
     * the given line number.
     *
     * @param file     the file containing the location
     * @param contents the current contents of the file
     * @param line     the line number (0-based) for the position
     * @return a new location
     */

    public static Location create(File file, String contents, int line) {
        return create(file, contents, line, null, null);
    }

    /**
     * Creates a new location for the given file, with the given contents, for
     * the given line number.
     *
     * @param file         the file containing the location
     * @param contents     the current contents of the file
     * @param line         the line number (0-based) for the position
     * @param patternStart an optional pattern to search for from the line
     *                     match; if found, adjust the column and offsets to begin at the
     *                     pattern start
     * @param patternEnd   an optional pattern to search for behind the start
     *                     pattern; if found, adjust the end offset to match the end of
     *                     the pattern
     * @return a new location
     */

    public static Location create(File file, String contents, int line,
                                  String patternStart, String patternEnd) {
        int currentLine = 0;
        int offset = 0;
        while (currentLine < line) {
            offset = contents.indexOf('\n', offset);
            if (offset == -1) {
                return Location.create(file);
            }
            currentLine++;
            offset++;
        }

        if (line == currentLine) {
            if (patternStart != null) {
                int index = contents.indexOf(patternStart, offset);
                if (index == -1) {
                    // Allow some flexibility: peek at previous couple of lines
                    // as well (for example, bytecode line numbers are sometimes
                    // a few lines off from their location in a source file
                    // since they are attached to executable lines of code)
                    int lineStart = offset;
                    for (int i = 0; i < 4; i++) {
                        int prevLineStart = contents.lastIndexOf('\n', lineStart - 1);
                        if (prevLineStart == -1) {
                            break;
                        }
                        index = contents.indexOf(patternStart, prevLineStart);
                        if (index != -1 || prevLineStart == 0) {
                            break;
                        }
                        lineStart = prevLineStart;
                    }
                }

                if (index != -1) {
                    int lineStart = contents.lastIndexOf('\n', index);
                    if (lineStart == -1) {
                        lineStart = 0;
                    } else {
                        lineStart++; // was pointing to the previous line's CR, not line start
                    }
                    int column = index - lineStart;
                    if (patternEnd != null) {
                        int end = contents.indexOf(patternEnd, offset + patternStart.length());
                        if (end != -1) {
                            return new Location(file, new DefaultPosition(line, column, index),
                                    new DefaultPosition(line, -1, end + patternEnd.length()));
                        }
                    }
                    return new Location(file, new DefaultPosition(line, column, index),
                            new DefaultPosition(line, column, index + patternStart.length()));
                }
            }

            Position position = new DefaultPosition(line, -1, offset);
            return new Location(file, position, position);
        }

        return Location.create(file);
    }

    /**
     * Reverses the secondary location list initiated by the given location
     *
     * @param location the first location in the list
     * @return the first location in the reversed list
     */
    public static Location reverse(Location location) {
        Location next = location.getSecondary();
        location.setSecondary(null);
        while (next != null) {
            Location nextNext = next.getSecondary();
            next.setSecondary(location);
            location = next;
            next = nextNext;
        }

        return location;
    }

    /**
     * A {@link com.android.tools.lint.detector.api.Location.Handle} is a reference to a location. The point of a location
     * handle is to be able to create them cheaply, and then resolve them into
     * actual locations later (if needed). This makes it possible to for example
     * delay looking up line numbers, for locations that are offset based.
     */
    public static interface Handle {
        /**
         * Compute a full location for the given handle
         *
         * @return create a location for this handle
         */

        Location resolve();

        /**
         * Sets the client data associated with this location. This is an optional
         * field which can be used by the creator of the {@link com.android.tools.lint.detector.api.Location} to store
         * temporary state associated with the location.
         *
         * @param clientData the data to store with this location
         */
        public void setClientData(Object clientData);

        /**
         * Returns the client data associated with this location - an optional field
         * which can be used by the creator of the {@link com.android.tools.lint.detector.api.Location} to store
         * temporary state associated with the location.
         *
         * @return the data associated with this location
         */

        public Object getClientData();
    }


}
