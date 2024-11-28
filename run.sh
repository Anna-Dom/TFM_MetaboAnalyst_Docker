#!/bin/bash
# start.sh

# Step 1: Run the R script to start Rserve in daemon mode
echo "Starting Rserve..."
Rscript /metab4script.R

# Step 2: Start Payara Micro
echo "Starting Payara Micro..."
java -jar /opt/payara/payara-micro.jar --deploymentDir /opt/payara/deployments
