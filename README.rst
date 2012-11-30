************************************
Akiban SQL Parser
************************************

Overview
========

The Akiban SQL Parser is a complete, production-quality Java parser for the SQL
language. It defines the SQL grammar as implemented by Akiban, but can be used
independently. It is derived from the Apache Derby parser.

Building the Akiban SQL Parser From Source
==========================================

Use Maven (http://maven.apache.org) to build the Akiban SQL Parser.

To build::

  mvn install

The resulting jar files are in the ``target`` directory. To build the Javadoc::

  mvn javadoc:javadoc

The resulting Javadoc HTML files are in ``target/site/apidocs``.

Install Akiban SQL Parser from Binaries
=======================================

Pre-built jars can be downloaded directly from
https://launchpad.net/akiban-sql-parser/+download

Unpack the distribution kit into a convenient directory using the
appropriate utility (e.g. unzip or tar).

Review the ``LICENSE.txt`` file located in the root of the installation
directory. The Akiban SQL Parser is licensed under the Eclipse Public
License or a free-use community license, see our
`licensing options <http://www.akiban.com/akiban-licensing-options>`_
for more details. By installing, copying or otherwise using the Software
contained in the distribution kit, you agree to be bound by the terms of the
license agreement. If you do not agree to these terms, remove and destroy all
copies of the software in your possession immediately.

Working with the Akiban SQL Parser
==================================

Add the jar file (e.g. ``akiban-sql-parser-1.0.12.jar``), found in the root
directory of the distribution kit, to your project's classpath. For example,
copy it to ``jre/lib/ext`` in your Java Runtime Environment, or add it to
your classpath environment variable..

More Information
================

For more information, join the Aiban mailing list on google groups
(https://groups.google.com/a/akiban.com/d/forum/akiban-user) or hop on the
#akiban channel on irc.freenode.net
