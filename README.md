## MetaboAnalyst - UOC TFM

This repository contains the source code for running the MetaboAnalyst web application version 5.0.0 locally. The original code was obtained from the ZIP file provided [here](https://www.metaboanalyst.ca/docs/About.xhtml) for the WebApp. The primary repositories from the original authors are:

- [https://github.com/xia-lab/MetaboAnalystR](MetaboAnalystR)
- [MetaboAnalyst Docker](https://github.com/xia-lab/MetaboAnalyst_Docker)

The MetaboAnalyst application is licensed under the GNU General Public License (GPL) as published by the Free Software Foundation, either version 2 of the License or (at your option) any later version.

The original work was developed by *Xia Lab @ McGill*. This repository includes minor modifications to some functionalities of the original application, implemented as part of a Final Master's Project. The key updates are:

1. Updated Dockerfile: Upgraded to use a newer version of Java and JDK.
2. Support for Version 5.0.0: Modified the Dockerfile to enable compatibility with the 5.0.0 release of the MetaboAnalyst web application.
3. Enhanced Configuration Management: Added functionality to upload an R script as a configuration file to re-run or restore a previously conducted analysis.

### Run the Application

There are two options to run the application.

1. Build the docker image and then run it. 

```sh
# Build the image
docker build -t metaboanalyst .
# Run it
docker run --rm -p 8080:8080 --name metabo-app -d metaboanalyst
# To check the logs
docker logs metabo-app -f
```

2. Download the image and run it directly

```sh
# TODO
```

Once the docker is running, MetaboAnalyst Webapp can be access at http://localhost:8080/MetaboAnalyst/. Notice that after running the image it might take a couple of seconds before the web application is available in this address.



