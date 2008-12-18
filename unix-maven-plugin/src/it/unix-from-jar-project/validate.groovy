String userHome = System.getProperty("user.home")
File jar = new File(userHome, ".m2/repository/bar/project-uber-1/1.1-2/project-uber-1-1.1-2.jar")
File deb = new File(userHome, ".m2/repository/bar/project-uber-1/1.1-2/project-uber-1-1.1-2.deb")
File rpm = new File(userHome, ".m2/repository/bar/project-uber-1/1.1-2/project-uber-1-1.1-2.rpm")
File pkg = new File(userHome, ".m2/repository/bar/project-uber-1/1.1-2/project-uber-1-1.1-2.pkg")

return jar.canRead() && deb.canRead() && pkg.canRead() && rpm.canRead()
