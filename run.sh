#!/usr/bin/env bash
Rscript /metab4script.R > /dev/null 2>&1


# Then start the Payara server
java -jar /opt/payara/payara-micro.jar --deploymentDir /opt/payara/deployments