# PROTOCOL 1: IDENTIFICATION OF SIGNIFICANT VARIABLES

This protocol will show the user how to perform classical univariate statistical methods to identify significant variables. It will follow three different approaches to identify these variables: (a) identifying variables that are significantly different among different conditions; (b) identifying variables that show particular patterns of change under different conditions; (c) identifying variables that are significantly associated with other known biomarkers or features of interests.


1. Move to the Module Selection page by clicking “Click here to start”
2. Click on “Statistical Analysis [one factor]”
3. In the data upload page, click "+ Choose" in the "A plain text file (.txt or .csv)" and select the "cow_diet.csv" located in the `./data` folder in this repository. Click "Submit".
4. The Data Integrity Check page indicates that the data passes all the data integrity checks and no mission values have been detected. Click "Proceed" to go to the Normalization step.

NORMALIZATION

5. Select "Normalization by a pooled sample from group (group PQN)" option for sample normalization. Make sure the group "0" is selected from teh pull-down menu when clicking in "Specify" and click "Submit". Leave the data transformation set to "none" and select "Auto scaling" for data scaling. Finally click "Normalize" at the bottom of the page.
5. Click in view results to see the graphical summary of the normalization, which shows that the data looks reasonable "bell-shaped" after the normalization procedure. Click "Proceed" to go to the "Data Analysis Exploration" page, where you will be able to choose what analysis do you want to perform.

STATISTICAL ANALYSIS

6. Click on “One-way Analysis of Variance (ANOVA)”. The ANOVA results based on the default parameters are shown. Click on a dot to see a box plot showing its concentration values within each group
7. There are two small icons that appear near the top right corner of the ANOVA graph. Click on the icon that looks like a table. This allows one to see the Feature Details View. Leave the details of post-hoc analysis as they are. The first column shows the feature names. Click on "View", in any row, for instance 3-PP, to view box-plot summaries before and after normalization.
8. Another very useful function for the identification of significantly different variables is the Significance Analysis of Microarray (SAM) approach, originally designed for microarray data analysis (Tusher et al., 2001). To use SAM, click the SAM hyperlink on the left side menu or navigation tree (about midway down the menu). SFor instance, if we increase the delta value to 1.0 and press the Submit button, the FDR (on the y axis) will be close to zero (left plot) while the Delta (on the x axis) is set as 1.0.