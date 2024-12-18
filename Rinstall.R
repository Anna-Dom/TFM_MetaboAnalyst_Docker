metanr_packages <- function(){

  metr_pkgs <- c("impute", "pcaMethods", "globaltest", "GlobalAncova", "Rgraphviz", "preprocessCore", "genefilter", "sva", "limma", "KEGGgraph", "siggenes","BiocParallel", "MSnbase", "multtest","RBGL","edgeR","fgsea","devtools","crmn","httr","qs")
  
  list_installed <- installed.packages()
  
  new_pkgs <- subset(metr_pkgs, !(metr_pkgs %in% list_installed[, "Package"]))
  
  if(length(new_pkgs)!=0){
    
    if (!requireNamespace("BiocManager", quietly = TRUE))
        install.packages("BiocManager")
    BiocManager::install(new_pkgs)
    print(c(new_pkgs, " packages added..."))
  }
  
  if((length(new_pkgs)<1)){
    print("No new packages added...")
  }
}

metanr_packages()

install.packages(c(
  "plotly", "Cairo", "plotly",
  "ggplot2", "randomForest", 
  "ellipse", "rjson", 
  "RJSONIO", "RColorBrewer",
  "xtable", "fitdistrplus", 
  "som", "ROCR", "gplots", 
  "e1071", "caTools", "igraph",
  "pls", "pheatmap", "lattice", 
  "rmarkdown", "knitr", "data.table", 
  "pROC", "Rcpp", "caret", 
  "scatterplot3d", "lars", "tidyverse", 
  "Hmisc", "reshape", "plyr", "car"
  )
)