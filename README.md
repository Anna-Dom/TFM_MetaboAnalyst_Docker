## MetaboAnalyst - UOC TFM

This repository contains the code of the MetaboAnalyst webapp version 5.0.0 to run locally.

The project was developed as a Final Master's Project and aims to improve some funcionalities of the original application. 

1. Allows the upload of the R script as a configuration file to re-run or restore an already run analysis
2. Improves PDF report capabilities
3. Improves the usability of Dockerfile and upgrades the JDK version to be able to run v 5.0.0 of the application

### Run the Application

There are two options to run the application.

1. Build the docker image and then run it

```sh
# Build the image
docker build -t metaboanalyst .
# Run it
docker run -p 8080:8080 --name metabo-app -d metaboanalyst
```

2. Download the image and run it directly

```sh
# TODO
```

Once the docker is running, MetaboAnalyst Webapp can be access at http://localhost:8080/MetaboAnalyst/. 

