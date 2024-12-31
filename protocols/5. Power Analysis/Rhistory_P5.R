# PID of current job: 634
mSet<-InitDataObjects("conc", "power", FALSE)
mSet<-Read.TextData(mSet, "Replacing_with_your_file_path", "rowu", "disc");
mSet<-SanityCheckData(mSet)
mSet<-ReplaceMin(mSet);
mSet<-PreparePrenormData(mSet)
mSet<-Normalization(mSet, "SumNorm", "LogNorm", "NULL", ratio=FALSE, ratioNum=20)
mSet<-PlotNormSummary(mSet, "norm_0_", "png", 72, width=NA)
mSet<-PlotSampleNormSummary(mSet, "snorm_0_", "png", 72, width=NA)
mSet<-InitPowerAnal(mSet, "NA")
mSet<-PlotPowerStat(mSet, "power_stat_0_", "png", 72, width=NA)
mSet<-GetGroupNames(mSet, "")
mSet<-PerformPowerProfiling(mSet, 0.1, 200)
mSet<-PlotPowerProfile(mSet, 0.1, 200, "power_profile_0_", "png", 72, width=NA)
mSet<-PerformPowerProfiling(mSet, 0.1, 800)
mSet<-PlotPowerProfile(mSet, 0.1, 800, "power_profile_1_", "png", 72, width=NA)
mSet<-SaveTransformedData(mSet)