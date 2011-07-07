#!/bin/sh

set -x

cd `dirname $0`/basic

if [ -z "${MVN}" ]
then
  MVN=mvn
fi

if [ -x "`which pkgchk 2>/dev/null`" ]
then
  rm -rf target
  "${MVN}" -f pom-pkg.xml clean install > mvn-pkg.txt 2>&1

  cmd="pkgchk -l -d target/basic-*.pkg all"
  echo "$ $cmd" > pkgchk.txt
  $cmd | \
    sed "s,\(.*> from\) <.*\(/target/.*\),\1\n <..\2,g" \
    >> pkgchk.txt

  cmd="pkginfo -l -d target/basic-*.pkg"
  echo "$ $cmd" > pkginfo.txt
  $cmd >> pkginfo.txt
fi

if [ -x "`which rpmbuild 2>/dev/null`" ]
then
  rm -rf target
  "${MVN}" -f pom-rpm.xml clean install > mvn-rpm.txt 2>&1

  cmd="rpm -q -v -l -p target/basic-*.rpm"
  echo "$ $cmd" > rpm-qvlp.txt
  $cmd >> rpm-qvlp.txt

  cmd="rpm -q -l -p target/basic-*.rpm"
  echo "$ $cmd" > rpm-qlp.txt
  $cmd >> rpm-qlp.txt
fi

rm -rf target
"${MVN}" -f pom-zip.xml clean install > mvn-zip.txt 2>&1

cmd="unzip -l target/basic-*.zip"
echo "$ $cmd" > unzip-l.txt
$cmd >> unzip-l.txt
