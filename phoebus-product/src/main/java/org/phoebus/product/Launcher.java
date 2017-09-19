package org.phoebus.product;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.phoebus.ui.application.ApplicationServer;
import org.phoebus.ui.application.PhoebusApplication;

import javafx.application.Application;

@SuppressWarnings("nls")
public class Launcher
{
    public static final Logger logger = Logger.getLogger(Launcher.class.getName());

    public static void main(final String[] original_args) throws Exception
    {
        LogManager.getLogManager().readConfiguration(Launcher.class.getResourceAsStream("/logging.properties"));

        logger.info("Phoebus (PID " + ProcessHandle.current().pid() + ")");

        // Handle arguments, potentially not even starting the UI
        final List<String> args = new ArrayList<>(List.of(original_args));
        final Iterator<String> iter = args.iterator();
        int port = -1;
        try
        {
            while (iter.hasNext())
            {
                final String cmd = iter.next();
                if (cmd.startsWith("-h"))
                {
                    help();
                    return;
                }

                if (cmd.equals("-settings"))
                {
                    if (! iter.hasNext())
                        throw new Exception("Missing -settings file name");
                    iter.remove();
                    final String filename = iter.next();
                    iter.remove();
                    Preferences.importPreferences(new FileInputStream(filename));
                }
                else if (cmd.equals("-export_settings"))
                {
                    if (! iter.hasNext())
                        throw new Exception("Missing -export_settings file name");
                    iter.remove();
                    final String filename = iter.next();
                    iter.remove();
                    System.out.println("Exporting settings to " + filename);
                    Preferences.userRoot().node("org/phoebus").exportSubtree(new FileOutputStream(filename));
                    return;
                }
                else if (cmd.equals("-server"))
                {
                    if (! iter.hasNext())
                        throw new Exception("Missing -server port");
                    iter.remove();
                    port = Integer.parseInt(iter.next());
                    iter.remove();
                }
            }
        }
        catch (Exception ex)
        {
            help();
            System.out.println();
            ex.printStackTrace();
            return;
        }

        // Check for an existing instance
        // If found, pass remaining arguments to it,
        // instead of starting a new application
        if (port > 0)
        {
            final ApplicationServer server = ApplicationServer.create(port);
            if (! server.isServer())
            {
                server.sendArguments(args);
                return;
            }
        }

        // Remaining args passed on
        Application.launch(PhoebusApplication.class, args.toArray(new String[args.size()]));
    }

    private static void help()
    {
        System.out.println(" _______           _______  _______  ______            _______ ");
        System.out.println("(  ____ )|\\     /|(  ___  )(  ____ \\(  ___ \\ |\\     /|(  ____ \\");
        System.out.println("| (    )|| )   ( || (   ) || (    \\/| (   ) )| )   ( || (    \\/");
        System.out.println("| (____)|| (___) || |   | || (__    | (__/ / | |   | || (_____ ");
        System.out.println("|  _____)|  ___  || |   | ||  __)   |  __ (  | |   | |(_____  )");
        System.out.println("| (      | (   ) || |   | || (      | (  \\ \\ | |   | |      ) |");
        System.out.println("| )      | )   ( || (___) || (____/\\| )___) )| (___) |/\\____) |");
        System.out.println("|/       |/     \\|(_______)(_______/|/ \\___/ (_______)\\_______)");
        System.out.println();
        System.out.println("Command-line arguments:");
        System.out.println();
        System.out.println("-help                                   -  This text");
        System.out.println("-settings settings.xml                  -  Import settings from file");
        System.out.println("-export_settings settings.xml           -  Export settings to file");
        System.out.println("-app  \"probe:?pvs?pv_name1,pv_name2\"  -  launch an application with input arguments");
        System.out.println("-resource  /tmp/example.plt             -  open a application configuration file with the default application");
        System.out.println("-server port                            -  Create instance server on given TCP port");
        System.out.println();
        System.out.println("Remaining arguments are names of resources to open in associated application.");
        System.out.println();
        System.out.println("In 'server' mode, first instance opens UI.");
        System.out.println("Additional calls to open resources are then forwarded to the initial instance.");
    }
}
