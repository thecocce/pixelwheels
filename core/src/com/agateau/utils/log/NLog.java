/*
 * Copyright 2017 Aurélien Gâteau <mail@agateau.com>
 *
 * This file is part of Tiny Wheels.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.agateau.utils.log;

import java.util.Vector;

import com.badlogic.gdx.Application;

public class NLog {
    private static Vector<Printer> sPrinters = new Vector<Printer>();
    private static int sStackDepth = -1;

    public interface Printer {
        public void print(int level, String tag, String message);
    }

    /**
     * Implementation of Printer which logs to System.err
     *
     * @author aurelien
     */
    public static class DefaultPrinter implements Printer {
        @Override
        public void print(int level, String tag, String message) {
            String levelString;
            if (level == Application.LOG_DEBUG) {
                levelString = "D";
            } else if (level == Application.LOG_INFO) {
                levelString = "I";
            } else { // LOG_ERROR
                levelString = "E";
            }
            System.err.printf("%s %s %s\n", levelString, tag, message);
        }
    }

    public static void d(Object obj, Object...args) {
        print(Application.LOG_DEBUG, obj, args);
    }

    public static void i(Object obj, Object...args) {
        print(Application.LOG_INFO, obj, args);
    }

    public static void e(Object obj, Object...args) {
        print(Application.LOG_ERROR, obj, args);
    }

    public static void addPrinter(Printer printer) {
        sPrinters.add(printer);
    }

    private synchronized static void print(int level, Object obj, Object...args) {
        if (sStackDepth < 0) {
            initStackDepth();
        }
        final String tag = getCallerMethod();
        String message;
        if (obj == null) {
            message = "(null)";
        } else {
            String format = obj.toString();
            message = args.length > 0 ? String.format(format, args) : format;
        }
        if (sPrinters.isEmpty()) {
            sPrinters.add(new DefaultPrinter());
        }
        for (Printer printer: sPrinters) {
            printer.print(level, tag, message);
        }
    }

    private static void initStackDepth() {
        final StackTraceElement lst[] = Thread.currentThread().getStackTrace();
        for (int i = 0, n = lst.length; i < n; ++i) {
            if (lst[i].getMethodName().equals("initStackDepth")) {
                sStackDepth = i;
                return;
            }
        }
    }

    private static String getCallerMethod() {
        final StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[sStackDepth + 3];
        final String fullClassName = stackTraceElement.getClassName();
        final String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        final String method = stackTraceElement.getMethodName();
        return className + "." + method;
    }

}
