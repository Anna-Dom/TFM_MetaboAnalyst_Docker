# PID of current job: 520
mSet<-InitDataObjects("nmrpeak", "stat", FALSE)
UnzipUploadedFile("nmr_peaks.zip", "upload");
mSet<-Read.PeakList(mSet, "upload");
mSet<-SanityCheckData(mSet)
mSet<-GroupPeakList(mSet, 0.03,10);
SetPeakList.GroupValues(mSet)
mSet<-SanityCheckData(mSet)
mSet<-ReplaceMin(mSet);
mSet<-SanityCheckData(mSet)
mSet<-FilterVariable(mSet, "iqr", 5, "F", 25, T)
mSet<-PreparePrenormData(mSet)
mSet<-Normalization(mSet, "SumNorm", "NULL", "AutoNorm", ratio=FALSE, ratioNum=20)
mSet<-PlotNormSummary(mSet, "norm_0_", "png", 72, width=NA)
mSet<-PlotSampleNormSummary(mSet, "snorm_0_", "png", 72, width=NA)
mSet<-PCA.Anal(mSet)
mSet<-PlotPCAPairSummary(mSet, "pca_pair_0_", "png", 72, width=NA, 5)
mSet<-PlotPCAScree(mSet, "pca_scree_0_", "png", 72, width=NA, 5)
mSet<-PlotPCA2DScore(mSet, "pca_score2d_0_", "png", 72, width=NA, 1,2,0.95,0,0)
mSet<-PlotPCALoading(mSet, "pca_loading_0_", "png", 72, width=NA, 1,2);
mSet<-PlotPCABiplot(mSet, "pca_biplot_0_", "png", 72, width=NA, 1,2)
mSet<-PlotPCA3DScore(mSet, "pca_score3d_0_", "json", 1,2,3)
mSet<-PlotPCA3DLoading(mSet, "pca_loading3d_0_", "json", 1,2,3)
mSet<-PlotPCA2DScore(mSet, "pca_score2d_1_", "png", 72, width=NA, 1,2,0.95,1,0)
mSet<-GetGroupNames(mSet, "")
mSet<-UpdateData(mSet, T, c(""),c("C004"),c("Healthy","Renal"))
mSet<-PreparePrenormData(mSet)
mSet<-Normalization(mSet, "SumNorm", "NULL", "AutoNorm", ratio=FALSE, ratioNum=20)
mSet<-PlotNormSummary(mSet, "norm_1_", "png", 72, width=NA)
mSet<-PlotSampleNormSummary(mSet, "snorm_1_", "png", 72, width=NA)
mSet<-PCA.Anal(mSet)
mSet<-PlotPCAPairSummary(mSet, "pca_pair_0_", "png", 72, width=NA, 5)
mSet<-PlotPCAScree(mSet, "pca_scree_0_", "png", 72, width=NA, 5)
mSet<-PlotPCA2DScore(mSet, "pca_score2d_1_", "png", 72, width=NA, 1,2,0.95,0,0)
mSet<-PlotPCALoading(mSet, "pca_loading_0_", "png", 72, width=NA, 1,2);
mSet<-PlotPCABiplot(mSet, "pca_biplot_0_", "png", 72, width=NA, 1,2)
mSet<-PlotPCA3DScore(mSet, "pca_score3d_0_", "json", 1,2,3)
mSet<-PlotPCA3DLoading(mSet, "pca_loading3d_0_", "json", 1,2,3)
mSet<-PlotPCA2DScore(mSet, "pca_score2d_2_", "png", 72, width=NA, 1,2,0.95,1,0)
mSet<-RSVM.Anal(mSet, 10)
mSet<-PlotRSVM.Classification(mSet, "svm_cls_0_", "png", 72, width=NA)
mSet<-PlotRSVM.Cmpd(mSet, "svm_imp_0_", "png", 72, width=NA)
mSet<-RSVM.Anal(mSet, "carlo", tsize = 0.6, CVnum=700)
mSet<-PlotRSVM.Classification(mSet, "svm_cls_1_", "png", 72, width=NA)
mSet<-PlotRSVM.Cmpd(mSet, "svm_imp_1_", "png", 72, width=NA)
mSet<-SaveTransformedData(mSet)