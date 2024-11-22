we are going to try to reproduce and improve the experiece for the pipeline proposed by the authors in MetaboAnalyst 3.0 article


There are several protocols in the article. Start with num 2: Identification of significant variables

1. Statistical Analysis -> StatUploadView

    InitDataObjects("conc", "stat", FALSE)

2. Here it goes directly to data check, always performed



A la primera linia hi ha la info sobre quin tipus analisis es tracta, llavors si inciem l'objct i pillem el anal.type saben a quina pagina anar despres pel 
data upload. 

1. Fer que despres de fer el upload del .R et redirigeixi a la pagina d'analisis que toca
2. A la pagina d'analysis que puguis fer el upload de les dades
3. Quan arrivis a la pagina d'analysis que es subratllin les pagines pertinents


mSet<-InitDataObjects("conc", "roc", FALSE) -> BioMarkerAnalysis
mSet<-InitDataObjects("conc", "msetqea", FALSE) -> Enrichment Analysis
mSet<-InitDataObjects("conc", "pathqea", FALSE) -> Pathway Analysis