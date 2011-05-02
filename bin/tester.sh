#!/bin/bash
BASEDIR=`dirname $0`/..
java -cp ${BASEDIR}/target/akiban-sql-parser-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.akiban.sql.test.Tester "$@"

