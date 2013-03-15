#!/bin/sh

set -x

cd `dirname $0`/basic

if [ -z "${MVN}" ]
then
  MVN=mvn
fi

SED1="s,\(.*Installing\) /[a-zA-Z/\.-]*\(/unix/.*\),\1 ~\2,g"
SED2="s,\(.*to\) /[a-z]*/[a-z]*\(.*\),\1 ~\2,g"
SED3="s,\(.*resourceDirectory\) /[a-zA-Z/\.-]*\(/unix/.*\),\1 ~\2,g"

if [ -x "`which pkgchk 2>/dev/null`" ]
then
  rm -rf target
  "${MVN}" -Dencoding=UTF-8 -f pom-pkg.xml clean install |
    sed -e "${SED1}" -e "${SED2}" -e "${SED3}" \
    > mvn-pkg.txt 2>&1

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
  "${MVN}" -Dencoding=UTF-8 -f pom-rpm.xml clean install |
    sed -e "${SED1}" -e "${SED2}" -e "${SED3}" \
    > mvn-rpm.txt 2>&1

  cmd="rpm -q -v -l -p target/basic-*.rpm"
  echo "$ $cmd" > rpm-qvlp.txt
  $cmd >> rpm-qvlp.txt

  cmd="rpm -q -l -p target/basic-*.rpm"
  echo "$ $cmd" > rpm-qlp.txt
  $cmd >> rpm-qlp.txt
fi

if [ -x "`which dpkg-deb 2>/dev/null`" ]
then
  rm -rf target
  "${MVN}" -Dencoding=UTF-8 -f pom-deb.xml clean install |
    sed -e "${SED1}" -e "${SED2}" -e "${SED3}" \
    > mvn-deb.txt 2>&1

  cmd="dpkg-deb -c target/*.deb"
  echo "$ $cmd" > dpkg-deb-c.txt
  $cmd >> dpkg-deb-c.txt

  cmd="dpkg-deb -f target/*.deb"
  echo "$ $cmd" > dpkg-deb-f.txt
  $cmd >> dpkg-deb-f.txt
fi
exit
rm -rf target
"${MVN}" -Dencoding=UTF-8 -f pom-zip.xml clean install |
    sed -e "${SED1}" -e "${SED2}" -e "${SED3}" \
    > mvn-zip.txt 2>&1

cmd="unzip -l target/basic-*.zip"
echo "$ $cmd" > unzip-l.txt
$cmd >> unzip-l.txt
