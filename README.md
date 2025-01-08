## MetaboAnalyst - UOC TFM

This repository contains the source code for running the MetaboAnalyst web application version 5.0.0 locally using Docker. The original code was obtained from the ZIP file provided [here](https://www.metaboanalyst.ca/docs/About.xhtml) for the WebApp. The primary repositories from the original authors are:

- [https://github.com/xia-lab/MetaboAnalystR](MetaboAnalystR)
- [MetaboAnalyst Docker](https://github.com/xia-lab/MetaboAnalyst_Docker)

The original work was developed by *Xia Lab @ McGill*. This repository includes several modifications to some functionalities of the original application, implemented as part of a Final Master's Project. The key updates are:

1. Updated Dockerfile to support MetaboAnalyst 5.0: 

    - Upgraded to use a newer version of Java and JDK.
    - Upgraded R version and all the necessary dependencies.
    - Added `run.sh` to start the R server and Paraya server directly.
    - Added the WAR file build steps

2. New module to re-use R History obtained from previous MetaboAnalyst analysis: 

    - The new module can be accessed through the module selection page.
    - The re-use only works for analysis made with the modules: Statistical Analysis [one factor], Biomarker Analysis and Power Analysis
    - In the folder `/protocols` there are 5 protocols that can be followed to test and learn about how the new module works

3. Added Monte Carlo Cross Validation in the R-SVM analysis

    - The parameter "carlo" can be now passed as a CVType in the RSVM.Anal function
    - The change has also been implemented in the UI where users can now choose this type of CV in the R-SVM analysis

### LICENSE

This program is free software for all users, including both academic and commercial users. You can access it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

### MetaboAnalystR Package

The MetaboAnalystR package for this version of the web application has also been edited and can be found [here](https://github.com/Anna-Dom/TFM-MetaboAnalystR).

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
docker pull annadom/tfm-metaboanalyst5.0:latest
docker run --rm -p 8080:8080 --name metabo-app -d annadom/tfm-metaboanalyst5.0:latest
```

Once the docker is running, MetaboAnalyst Webapp can be access at http://localhost:8080/MetaboAnalyst/. Notice that after running the image it might take a couple of seconds before the web application is available in this address.



