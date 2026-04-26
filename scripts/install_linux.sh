#!/bin/bash

#DESTDIR=/
SHAREDIR=/usr/share
BINDIR=/usr/bin
VERSION=0.1

MYSHAREDIR=${SHAREDIR}/md2gem

if [ -f pom.xml ]; then

  mkdir -p $DESTDIR/$MYSHAREDIR 
  mkdir -p $DESTDIR/$BINDIR 
  cp binaries/md2gem-$VERSION.jar $DESTDIR/$MYSHAREDIR 
  cat << EOF > $DESTDIR/$BINDIR/md2gem
  #!/bin/bash
  exec java -jar $MYSHAREDIR/md2gem-$VERSION.jar "\$@"
EOF

chmod 755 $DESTDIR/$BINDIR/md2gem

else

  echo Run this from the source directory, e.g., 
  echo \"sudo ./samples/install_linux.sh\"

fi

