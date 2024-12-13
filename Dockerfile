# Start from an ubuntu Image
FROM ubuntu:18.04

# 3 Stages to construct this image
    # Install dependences
    # Install RPackages
    # Set up Paraya
    # Build war file
    # Deploy and run war file


#################################
### SET UP BASE CONFIGURATION ###
#################################

# Install and set up project dependencies (netcdf library for XCMS, imagemagick and 
# graphviz libraries for RGraphviz), then purge apt-get lists.
# Install base packages and setup java environment (move from Java 8 to Java 11)
RUN apt-get update && \
    apt-get install -y software-properties-common \
    apt-transport-https \
    wget \
    locales \
    maven \
    && add-apt-repository --enable-source --yes "ppa:marutter/rrutter3.5" \
    && add-apt-repository --enable-source --yes "ppa:marutter/c2d4u3.5" 

# Ensure UTF-8 is set
RUN echo "en_US.UTF-8 UTF-8" >> /etc/locale.gen \
    && locale-gen en_US.utf8 \
    && /usr/sbin/update-locale LANG=en_US.UTF-8

# set up env
ENV LC_ALL=en_US.UTF-8 \
    LANG=en_US.UTF-8 \
    DEBIAN_FRONTEND=noninteractive 

RUN apt-get update && \
    apt-get install -y software-properties-common sudo apt-transport-https apt-utils openjdk-11-jdk 

# Install dependencies for R and other libraries
RUN apt-get update && \
    apt-get install -y \
    graphviz \
    imagemagick \
    libcairo2-dev \
    libnetcdf-dev \
    netcdf-bin \
    libssl-dev \
    libxt-dev \
    libxml2-dev \
    libcurl4-openssl-dev \
    gfortran \
    texlive-full \
    texlive-latex-extra \
    littler \
    r-base \
    r-base-dev \
    r-recommended && \
    ln -s /usr/lib/R/site-library/littler/examples/install.r /usr/local/bin/install.r && \
    ln -s /usr/lib/R/site-library/littler/examples/install2.r /usr/local/bin/install2.r && \
    install.r docopt && \
    rm -rf /tmp/downloaded_packages/ /tmp/*.rds && \
    rm -rf /var/lib/apt/lists/*


################
### SET UP R ###
################

RUN apt-get update && \
    apt-get install -y \
    r-cran-biocmanager

# Need updated R serve not from CRAN
RUN R -e 'install.packages("Rserve",,"http://rforge.net/",type="source")'

# Need to install XML from specific repo
RUN R -e 'install.packages("XML", repos = "http://www.omegahat.net/R")'

COPY Rinstall.R /tmp/Rinstall.R

# Run the R script to install the packages
RUN Rscript /tmp/Rinstall.R

# # Install all R packages from Bioconductor 
# RUN R -e 'BiocManager::install(c("impute", "pcaMethods", "siggenes", "globaltest", "GlobalAncova", "Rgraphviz", "KEGGgraph", "preprocessCore", "genefilter", "SSPA", "sva", "limma", "mzID", "xcms"))'

############################
### ADD FILES FOR SERVER ###
############################

ADD rserve.conf /etc/rserve.conf
ADD metab4script.R /metab4script.R
ADD run.sh /run.sh
RUN chmod +x /run.sh


#####################
### SET UP PAYARA ###
#####################

ENV PAYARA_PATH="/opt/payara" \
    PAYARA_PKG=https://nexus.payara.fish/repository/payara-community/fish/payara/extras/payara-micro/5.194/payara-micro-5.194.jar \
    PAYARA_VERSION=181 \
    PKG_FILE_NAME=payara-micro.jar \
    AUTODEPLOY_DIR=$PAYARA_PATH/deployments \
    PAYARA_MICRO_JAR=$PAYARA_PATH/$PKG_FILE_NAME \
    DEPLOY_DIR=$PAYARA_PATH/deployments 

# Install and configure Payara Micro
RUN mkdir -p $PAYARA_PATH/deployments && \
    useradd -d $PAYARA_PATH payara && echo payara:payara | chpasswd && \
    chown -R payara:payara /opt && \
    wget --quiet -O $PAYARA_PATH/$PKG_FILE_NAME $PAYARA_PKG


###########################
### BUILD METABOANALYST ###
###########################

# Copy pom.xml and source code to the container
COPY ./MetaboAnalyst/pom.xml .
COPY ./MetaboAnalyst/src ./src

# Run Maven to build the WAR file
RUN mvn clean package -DskipTests

# # Expose Payara's ports
EXPOSE 4848 8009 8080 8181 6311

# Copy the WAR file from the build stage into the Payara Micro deployments directory
RUN mv /target/MetaboAnalyst-5.0.war $DEPLOY_DIR/MetaboAnalyst.war && \
    # Remove unused files
    rm -rf ./src ./target

# # Set the entrypoint to start Payara Micro
ENTRYPOINT ["/run.sh"]

