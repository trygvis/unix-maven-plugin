import org.codehaus.plexus.util.cli.CommandLineUtils
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer
import org.codehaus.plexus.util.cli.Commandline

boolean success = true

boolean assertEntries(String path, List pathEntries)
{
  boolean s = true
  File root = new File((File) basedir, path)
  path = root.absolutePath

  List<File> actualEntries = findEntries(root)

  pathEntries.each {p ->
    entry = new File((File) root, (String) p)
    if ( actualEntries.remove(entry) )
    {
      println "Found entry: ${entry.getAbsolutePath().substring(path.length())}"
    }
    else
    {
      s = false
      println "Missing entry: ${entry.getAbsolutePath().substring(path.length())}"
    }
  }

  if ( actualEntries.size() > 0 )
  {
    s = false
    println "Extra files in root:"

    actualEntries.each {entry ->
      println "Extra entry: ${entry.getAbsolutePath().substring(path.length())}"
    }
  }

  return s
}

List<File> findEntries(File basedir)
{
  List<File> actualEntries = new ArrayList<File>()

  println "Scanning for files in ${basedir}"
  findEntries0(basedir, actualEntries);

  return actualEntries
}

void findEntries0(File basedir, List<File> entries)
{
  basedir.listFiles().each {file ->
    entries.add(file)
    if ( file.isDirectory() )
    {
      findEntries0(file, entries);
    }
  }
}

void dumpDeb(File deb)
{
  Commandline cl = new Commandline();
  cl.setExecutable("dpkg")
  cl.createArgument().value = "-c"
  cl.createArgument().value = deb.absolutePath

  StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer()
  StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer()
  println "Executing"
  println cl.toString()
  System.out.flush()

  int i = CommandLineUtils.executeCommandLine(cl, stdout, stderr)

  String output = stdout.output
  println "Output:"
  println output

  if ( i != 0 )
  {
    throw new RuntimeException("${cl.executable} returned a non-0 return value: ${i}")
  }
}

success &= assertEntries("target/unix/root-dpkg", [
        "DEBIAN",
        "DEBIAN/control",
        "usr",
        "usr/share",
        "usr/share/hudson",
        "usr/share/hudson/lib",
        "usr/share/hudson/lib/slave.jar",
        "usr/share/hudson/license",
        "usr/share/hudson/license/atom-license.txt",
        "usr/share/hudson/license/dc-license.txt",
])

dumpDeb(new File(System.getProperty("user.home"), ".m2/repository/bar/project-dpkg-2/1.1-2/project-dpkg-2-1.1-2.deb"))

return success
