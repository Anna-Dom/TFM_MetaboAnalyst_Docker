# PID of current job: 426
mSet<-InitDataObjects("conc", "roc", FALSE)
mSet<-Read.TextData(mSet, "Replacing_with_your_file_path", "rowu", "disc");
mSet<-SanityCheckData(mSet)
mSet<-ReplaceMin(mSet);
mSet<-PreparePrenormData(mSet)
mSet<-Normalization(mSet, "NULL", "LogNorm", "AutoNorm", ratio=TRUE, ratioNum=20)
mSet<-PlotNormSummary(mSet, "norm_0_", "png", 72, width=NA)
mSet<-PlotSampleNormSummary(mSet, "snorm_0_", "png", 72, width=NA)
mSet<-SetAnalysisMode(mSet, "univ")
mSet<-PrepareROCData(mSet)
mSet<-CalculateFeatureRanking(mSet)
mSet<-Perform.UnivROC(mSet, "Glycerol/Trimethylamine", 0, "png", 72, F, T, "closest.topleft", F, "sp", 0.2)
mSet<-PlotRocUnivBoxPlot(mSet, "Glycerol/Trimethylamine", 0, "png", 72, T, FALSE)
mSet<-PrepareROCDetails(mSet, "Glycerol/Trimethylamine")
mSet<-SaveTransformedData(mSet)