/**
 * 
 */
package de.saumya.mojo.ruby.script;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.DuplicateRealmException;
import org.codehaus.classworlds.NoSuchRealmException;

import de.saumya.mojo.ruby.Logger;
import de.saumya.mojo.ruby.ScriptUtils;

public class ScriptFactory {

    public static List<String> NO_CLASSPATH = Collections.emptyList();

    final Arguments            switches     = new Arguments();
    final Arguments            javaArgs     = new Arguments();
    final Arguments            jvmArgs      = new Arguments();
    final Map<String, String>  env          = new HashMap<String, String>();
    final Logger               logger;
    final ClassRealm           classRealm;
    final File                 jrubyJar;
    final List<String>         classpathElements;
    final boolean              fork;

    final Launcher             launcher;

    public ScriptFactory(final Logger logger, final ClassRealm classRealm,
            final File jrubyJar, final List<String> classpathElements,
            final boolean fork) throws ScriptException, IOException {
        this.logger = logger;
        this.jrubyJar = jrubyJar;
        this.classpathElements = classpathElements == null
                ? NO_CLASSPATH
                : Collections.unmodifiableList(classpathElements);
        this.fork = fork;
        if (classRealm != null) {
            ClassRealm jruby;
            try {
                jruby = classRealm.getWorld().getRealm("jruby");
            }
            catch (final NoSuchRealmException e) {
                try {
                    jruby = classRealm.getWorld().newRealm("jruby");
                    jruby.addConstituent(jrubyJar.toURI().toURL());
                }
                catch (final DuplicateRealmException ee) {
                    throw new ScriptException("could not setup classrealm for jruby",
                            ee);
                }
            }
            this.classRealm = jruby;
        }
        else {
            this.classRealm = null;
        }

        if (fork) {
            this.launcher = new AntLauncher(logger, this);
        }
        else {

            this.launcher = new EmbeddedLauncher(logger, this);
        }
    }

    public Script newScriptFromSearchPath(final String scriptName)
            throws IOException {
        return new Script(this, scriptName, true);
    }

    public Script newScriptFromJRubyJar(final String scriptName)
            throws MalformedURLException {
        return new Script(this, new URL("jar:file:"
                + this.jrubyJar.getAbsolutePath()
                + "!/META-INF/jruby.home/bin/" + scriptName));
    }

    public Script newScriptFromResource(final String scriptName)
            throws IOException {
        URL url = this.classRealm != null ? this.classRealm.getClassLoader()
                .getResource(scriptName) : null;
        if (url == null) {
            url = ScriptUtils.getScriptFromResource(scriptName);
        }
        if (url.getProtocol().equals("file")) {
            return new Script(this, url.getPath(), false);
        }
        else {
            return new Script(this, url);
        }
    }

    public Script newArguments() {
        return new Script(this);
    }

    public Script newScript(final String script) throws IOException {
        return new Script(this, script);
    }

    public Script newScript(final File file) {
        return new Script(this, file);
    }

    public void addJavaOption(final String name) {
        this.javaArgs.add(name);
    }

    public void addJavaOption(final String name, final String value) {
        this.javaArgs.add(name, value);
    }

    public void addJavaProperty(final String name, final String value) {
        this.javaArgs.add("-D" + name + "=" + value);
    }

    public void addJavaArgs(final String args) {
        this.javaArgs.parseAndAdd(args);
    }

    public void addJvmArgs(final String args) {
        this.jvmArgs.parseAndAdd(args);
    }
    
    
    public void addSwitch(final String name) {
        this.switches.add(name);
    }

    public void addSwitch(final String name, final String value) {
        this.switches.add(name, value);
    }

    public void addSwitches(final String switches) {
        this.switches.parseAndAdd(switches);
    }

    public void addEnv(final String name, final File value) {
        if (value != null) {
            this.env.put(name, value.getAbsolutePath());
        }
        else {
            this.env.put(name, null);
        }
    }

    public void addEnv(final String name, final String value) {
        this.env.put(name, value);
    }

    public void addEnvs(final String environmentVars) {
        for (final String var : environmentVars.split("\\s+")) {
            final int index = var.indexOf("=");
            if (index > -1) {
                this.env.put(var.substring(0, index), var.substring(index + 1));
            }
        }
    }

    @Override
    public String toString() {
        // TODO
        final StringBuilder buf = new StringBuilder(getClass().getName());
        // for (final String arg : this.switches) {
        // buf.append(arg).append(" ");
        // }
        return buf.toString().trim();
    }

}