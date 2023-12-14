#!/bin/bash -ex

mvn -q clean
mvn -q compile
mvn -q exec:exec -Dexec.mainClass=kim.charlesb.rcouModule/kim.charlesb.rcou.AppDriver
