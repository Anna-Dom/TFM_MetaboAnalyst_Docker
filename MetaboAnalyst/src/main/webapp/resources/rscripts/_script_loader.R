# This script should be called by server to load actual scripts
# based on the modules user selected
.on.public.web <- TRUE;

general_files <- c("general_data_utils","general_misc_utils","general_lib_utils","generic_c_utils","depends_refs");
general_stat_files <- c("general_norm_utils","general_proc_utils");
general_anot_files <- "general_anot_utils";
stats_files <- c("stats_chemometrics","stats_classification","stats_clustering","stats_correlations","stats_sigfeatures","stats_univariates", "stats_peaks");
enrich_files <- c("enrich_graphics","enrich_mset","enrich_name_match","enrich_stats");
pathway_files <- c("enrich_mset","enrich_stats","enrich_name_match","enrich_path_graphics","enrich_path_stats", "gene_fun_utils")
integmex_files <- c("enrich_integ","enrich_stats","enrich_name_match", "enrich_path_graphics", "enrich_kegg")
biomarker_files <- c("biomarker_utils");
power_files <- c("util_sspa", "power_utils");
multifac_files <-c("multifac_metadata", "multifac_asca_heatmap2","multifac_pca_anova2", "multifac_mb","multifac_covariate", "stats_classification", "stats_chemometrics","stats_univariates","stats_correlations" );
mummichog_files <- c("peaks_to_function", "enrich_kegg");
metaanal_files <- c("meta_methods", "meta_data_utils");
network_files <- c("networks_enrich", "networks_view", "enrich_kegg", "enrich_integ", "enrich_name_match", "gene_fun_utils");
spectra_files <- c("spectra_processing", "stats_chemometrics", "util_scatter3d")
metapath_files <- c("meta_pathway", "peaks_to_function", "meta_methods", "meta_data_utils")
other_files <- c("batch_effect_utils", "others_lipomics", "enrich_name_match", "duplicates_estimates");

if(file.exists("/data/glassfish/projects/MetaboNew_marker")){
  compilePath = "/home/glassfish/payara5/glassfish/domains/domain1/applications/MetaboAnalyst/resources/rscripts/MetaboAnalystR/R/"
  loadPath = paste0(compilePath,"/")
  workingPath <- "/home/glassfish/payara5/glassfish/domains/domain1/applications/MetaboAnalyst/resources/rscripts/MetaboAnalystR/src/"
}else{
  loadPath <- "../../rscripts/MetaboAnalystR/R/"
  compilePath <- "../rscripts/MetaboAnalystR/R"
  workingPath <- "../rscripts/MetaboAnalystR/src/"
}

LoadRscripts <- function(module.nm = "stat"){
  file.sources <- "";
  if(module.nm == "stat"){
    file.sources <- c(general_files, general_stat_files, stats_files);
  }else if(module.nm == "mf"){
    file.sources <- c(general_files, general_stat_files, multifac_files);
  }else if(module.nm == "pathinteg"){
    file.sources <- c(general_files, general_anot_files, integmex_files, mummichog_files, general_stat_files, network_files);
  }else if(substring(module.nm, 1,4) == "path"){
    file.sources <- c(general_files, general_stat_files, general_anot_files,  pathway_files);
  }else if(substring(module.nm, 1,4) == "mset"){
    file.sources <- c(general_files, general_stat_files, general_anot_files, enrich_files);
  }else if(module.nm == "roc"){
    file.sources <- c(general_files, general_stat_files, biomarker_files);
  }else if(module.nm == "power"){
    file.sources <- c(general_files, general_stat_files, power_files);
  }else if(module.nm == "utils"){
    file.sources <- c(general_files, general_anot_files, other_files);
  }else if(module.nm == "network"){
    file.sources <- c(general_files, general_anot_files, network_files, general_stat_files);
  }else if(module.nm == "mass_all"){
    file.sources <- c(general_files, mummichog_files);
  }else if(module.nm == "mass_table" || module.nm == "mummichog"){
    file.sources <- c(general_files, mummichog_files, general_stat_files, "stats_univariates");
  }else if(module.nm == "metadata"){
    file.sources <- c(general_files, general_stat_files, metaanal_files);
  }else if(module.nm == "raw"){
    file.sources <- c(general_files, spectra_files);
  }else if(module.nm == "metapaths"){
    file.sources <- c(general_files, metapath_files, mummichog_files, general_stat_files, general_anot_files, "stats_univariates");
  }else if(module.nm == "covid"){
    file.sources <- c(general_files, "covid_utils");
  }else{
    print(paste("Unknown module code: ", module.nm));
    return(0);
  }
  print(paste("Loading scripts for: ", module.nm));
  file.sources <- paste(loadPath, file.sources, ".Rc", sep="");
  library(compiler);
  sapply(file.sources,loadcmp,.GlobalEnv);
  return(1);
}

LoadReporter <- function(module.nm = "stat"){
  general_files <-"sweave_reporter";
  if(module.nm == "stat"){
    file.sources <- c(general_files, "sweave_report_stats", "stats_plot3d");
  }else if(module.nm == "mf"){
    file.sources <- c(general_files, "sweave_report_time");
  }else if(module.nm == "pathinteg"){
    file.sources <- c(general_files, "sweave_report_integmex");
  }else if(substring(module.nm, 1,4) == "path"){
    file.sources <- c(general_files, "sweave_report_pathway");
  }else if(module.nm == "roc" | module.nm == "power"){
    file.sources <- c(general_files, "sweave_report_biomarker", "sweave_report_power");
  }else if(module.nm == "utils"){
    file.sources <- c(general_files, other_files);
  }else if(substring(module.nm, 1,4) == "mset"){
    file.sources <- c(general_files, "sweave_report_enrichment");
  }else if(module.nm == "network"){
    file.sources <- c(general_files, "sweave_report_network");
  }else if(module.nm == "mummichog"){
    file.sources <- c(general_files, "sweave_report_mummichog");
  }else if(module.nm == "metapaths"){
    file.sources <- c(general_files, "sweave_report_metamummi");
  }else if(module.nm == "metadata"){
    file.sources <- c(general_files, "sweave_report_meta_analysis");
  }else if(module.nm == "raw"){
    file.sources <- c(general_files, "sweave_report_raw_spectra");
  }else{
    print(paste("Unknown module code: ", module.nm));
  }
  
  file.sources <- paste(loadPath, file.sources, ".Rc", sep="");
  
  sapply(file.sources,loadcmp,.GlobalEnv);
}

# init path for anal.type == "raw"
CompileScripts <- function(){
  library(compiler);
  library(parallel);
  
  files <- list.files(compilePath, full.names=TRUE, pattern=".R$");
  ncore <- ifelse(ceiling(detectCores()/3*2)>10, 6, ceiling(detectCores()/3*2));
  cat("compiling R codes with",ncore, "CPU Cores in parallel ...\n");
  cl <- makeCluster(getOption("cl.cores", ncore), outfile = "")
  clusterExport(cl, c("files"), envir = environment())
  res <- parSapply(cl, 
                   files, 
                   function(f){
                     compiler::cmpfile(f, paste(f, "c", sep=""), options=list(suppressAll=TRUE));
                   })
  stopCluster(cl)
  #  for(f in files){
  #    cmpfile(f, paste(f, "c", sep=""), options=list(suppressAll=TRUE));
  #  }
  
  # compile other code (including C/fortran/C++)
  print("compiling other codes .... ");

  setwd(workingPath);
  cxxflgs <- capture.output(Rcpp:::CxxFlags())
  sink(file = "Makevars")
  cat("BATCHOBJECTS=fortran/decorana.o c/Internal_utils_batch.o\n")
  cat("UTILSOBJECTS=c/util.o c/fastmatch.o c/nncgc.o cpp/melt.o\n")
  cat("XCMSOBJECTS=c/mzROI.o c/xcms_binners.o\n")
  cat("INITOBJECT=Exports.o\n")
  cat("OBJECTS= $(BATCHOBJECTS) $(UTILSOBJECTS) $(XCMSOBJECTS) $(INITOBJECT)\n")
  cat("\n")
  cat("all: clean $(SHLIB)\n")
  cat("\n")
  cat("clean:\n")
  cat("\trm -f $(OBJECTS)\n")
  cat("\n")
  cat("CXX_STD = CXX11\n")
  cat("\n")
  cat("PKG_CXXFLAGS = $(SHLIB_OPENMP_CXXFLAGS) ")
  cat(cxxflgs, "\n")
  cat("PKG_LIBS = $(SHLIB_OPENMP_CXXFLAGS) $(LAPACK_LIBS) $(BLAS_LIBS) $(FLIBS)\n")
  sink()

  my.cmd <- paste("R CMD SHLIB", "decorana.f Internal_utils_batch.c util.c nncgc.c -o MetaboAnalyst.so", sep=" ");
  system(my.cmd);
  return(1);
}