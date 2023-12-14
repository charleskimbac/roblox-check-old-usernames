#!/bin/bash -ex

mvn -q clean
mvn -q compile
mvn -q exec:exec -Dexec.mainClass=kim.charlesb.projects.crou/kim.charlesb.crou.AppDriver
