/*
 * This file is part of MyPet
 *
 * Copyright (C) 2011-2014 Keyle
 * MyPet is licensed under the GNU Lesser General Public License.
 *
 * MyPet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyPet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet.util.logger;

import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class DebugLogger {
    private static final Logger debugLogger = Logger.getLogger("MyPet");
    private static boolean isEnabled = false;

    public static boolean setup() {
        if (debugLogger.getHandlers().length > 0) {
            for (Handler h : debugLogger.getHandlers()) {
                if (h.toString().equals("MyPet-Debug-Logger-FileHandler")) {
                    isEnabled = true;
                    return true;
                }
            }
        }
        try {
            String path = DebugLogger.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            path = path.replace("/", File.separator);
            path = path.replaceAll(String.format("\\%s[^\\%s]*\\.jar", File.separator, File.separator), "");
            File pluginDirFile = new File(path);
            FileHandler fileHandler = new FileHandler(pluginDirFile.getAbsolutePath() + File.separator + "MyPet" + File.separator + "logs" + File.separator + "MyPet.log", true) {
                @Override
                public String toString() {
                    return "MyPet-Debug-Logger-FileHandler";
                }
            };
            debugLogger.setUseParentHandlers(false);
            fileHandler.setFormatter(new LogFormat());
            debugLogger.addHandler(fileHandler);
            isEnabled = true;
            return true;
        } catch (IOException e) {
            isEnabled = false;
            e.printStackTrace();
            return false;
        } catch (URISyntaxException e) {
            isEnabled = false;
            e.printStackTrace();
            return false;
        }
    }

    public static void info(String text) {
        if (isEnabled) {
            debugLogger.info("[MyPet] " + ChatColor.stripColor(text));
        }
    }

    public static void warning(String text) {
        if (isEnabled) {
            debugLogger.warning("[MyPet] " + ChatColor.stripColor(text));
        }
    }

    public static void severe(String text) {
        if (isEnabled) {
            debugLogger.severe("[MyPet] " + ChatColor.stripColor(text));
        }
    }


    public static void info(String text, String source) {
        if (isEnabled) {
            debugLogger.info("[" + source + "] " + ChatColor.stripColor(text));
        }
    }

    public static void warning(String text, String source) {
        if (isEnabled) {
            debugLogger.warning("[" + source + "] " + ChatColor.stripColor(text));
        }
    }

    public static void severe(String text, String source) {
        if (isEnabled) {
            debugLogger.severe("[" + source + "] " + ChatColor.stripColor(text));
        }
    }

    public static void printThrowable(Throwable cause) {
        if (isEnabled) {
            debugLogger.severe("=====================================================================================================================================");
            debugLogger.severe(cause.toString());

            StackTraceElement[] trace = cause.getStackTrace();
            for (StackTraceElement aTrace : trace) {
                debugLogger.severe("\tat " + aTrace);
            }
            while ((cause = cause.getCause()) != null) {
                debugLogger.severe("Caused by" + cause.toString());

                trace = cause.getStackTrace();
                for (StackTraceElement aTrace : trace) {
                    debugLogger.severe("\tat " + aTrace);
                }
            }
            debugLogger.severe("=====================================================================================================================================");
        }
    }

    public static void printStackTrace(StackTraceElement[] stacktrace) {
        if (isEnabled) {
            debugLogger.severe("=====================================================================================================================================");
            for (StackTraceElement aTrace : stacktrace) {
                debugLogger.severe("\tat " + aTrace);
            }
            debugLogger.severe("=====================================================================================================================================");
        }
    }
}