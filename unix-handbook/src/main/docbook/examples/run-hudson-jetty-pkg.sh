#!/bin/sh

set -x

cd hudson-jetty-pkg

mvn -Dmaven.unix.debug=true clean install > mvn.txt
cmd="pkgchk -l -d target/hudson-jetty-pkg-*.pkg all"
echo "$ $cmd" > pkgchk.txt
$cmd | \
  sed "s,\(.*> from <\).*\(/target/.*\),\1..\2,g" \
  >> pkgchk.txt

cmd="pkginfo -l -d target/hudson-jetty-pkg-*.pkg"
echo "$ $cmd" > pkginfo.txt
$cmd >> pkginfo.txt
