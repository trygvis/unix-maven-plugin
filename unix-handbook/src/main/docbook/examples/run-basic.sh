#!/bin/sh

set -x

cd `dirname $0`/basic-pkg

rm -rf target

if [ -x `which pkgchk 2>/dev/null` ]
then
  mvn -f pom-pkg.xml clean install > mvn-pkg.txt

  cmd="pkgchk -l -d target/basic-*.pkg all"
  echo "$ $cmd" > pkgchk.txt
  $cmd | \
    sed "s,\(.*> from\) <.*\(/target/.*\),\1\n <..\2,g" \
    >> pkgchk.txt

  cmd="pkginfo -l -d target/basic-*.pkg"
  echo "$ $cmd" > pkginfo.txt
  $cmd >> pkginfo.txt
fi
exit

if [ -x `which rpmbuild 2>/dev/null` ]
then
  mvn -f pom-rpm.xml clean install > mvn-rpm.txt

  cmd="rpm -q -v -l -p target/basic-*.rpm"
  echo "$ $cmd" > rpm-qvlp.txt
  $cmd >> rpm-qvlp.txt

  cmd="rpm -q -l -p target/basic-*.rpm"
  echo "$ $cmd" > rpm-qlp.txt
  $cmd >> rpm-qlp.txt
fi
