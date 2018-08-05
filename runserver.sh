#!/bin/sh
POLSKAGRA_VERSION="1.02.1"

LOCALCLASSPATH=.:data/script/:data/conf/:polskagra-server-$POLSKAGRA_VERSION.jar:marauroa.jar:mysql-connector.jar:log4j.jar:commons-lang.jar:h2.jar

java -Xmx400m -cp "${LOCALCLASSPATH}" games.stendhal.server.StendhalServer -c server.ini -l

