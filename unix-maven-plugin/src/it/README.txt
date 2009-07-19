== jetty ==

 o Shows how to depend on a ZIP file.
 o Shows how to create a package in multiple formats.
 o Uses default settings for the file and directory objects.
 o Shows format-specific configuration elements (<deb/> and <rpm/>).
 o Shows how to deliver empty directories.

== test-{deb,pkg,rpm}-1 ==

 o Maven project with packaging={deb,pkg,rpm}. The project delivers the Hudson web application as its primary artifact.
 o Packages the Hudson WAR file as the "hudson" user.
 o Shows how to depend on a WAR file.

== test-{deb,pkg,rpm}-2 ==

 o Maven project with packaging={deb,pkg,rpm}. The project delivers the Hudson slave JAR file as its primary artifact.
 o Extracts the Hudson slave.jar and the licenses. The files are installed with "hudson" as owner.
 o Shows how to depend on a WAR file.

== test-{deb,pkg,rpm}-3 ==

 o Maven project with packaging={deb,pkg,rpm}. The project delivers two packages of the same format. The default
   is similar to test-{..}-1 and the client is similar to test-{..}-2.
 o Shows how to depend on a WAR file.
