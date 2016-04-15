/**
 * Created by Praveen on 3/3/2016.
 */

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class ModuleLoader {

    /**
     * Parameters of the method to add an URL to the System classes.
     */
    private static final Class<?>[] parameters = new Class[]{URL.class};

    /**
     * Adds a file to the classpath
     * @param jarFile the file to be added
     * @throws IOException
     */
    public static void addFile(File jarFile) throws IOException {
        loadModule(jarFile.toURI().toURL());
    }

    /**
     * Adds the content pointed by the URL to the classpath.
     * @param jarUrl the URL pointing to the content to be added
     * @throws IOException
     */
    public static void loadModule(URL jarUrl) throws IOException {

        URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Class<?> sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL",parameters);
            method.setAccessible(true);
            method.invoke(sysloader,new Object[]{ jarUrl });
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }

    }
}
