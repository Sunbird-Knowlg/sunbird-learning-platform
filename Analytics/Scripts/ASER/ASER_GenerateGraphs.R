# clear working space
suppressPackageStartupMessages(require(reshape2, quietly=TRUE))
suppressPackageStartupMessages(require(ggplot2, quietly=TRUE))
suppressPackageStartupMessages(require(plyr, quietly=TRUE))
suppressPackageStartupMessages(require(dplyr, quietly=TRUE))
suppressPackageStartupMessages(require(ComplexHeatmap, quietly=TRUE))
suppressPackageStartupMessages(require(RColorBrewer, quietly=TRUE))
suppressPackageStartupMessages(require(circlize, quietly=TRUE))
suppressPackageStartupMessages(require(colorspace, quietly=TRUE))
suppressPackageStartupMessages(require(GetoptLong, quietly=TRUE))
suppressPackageStartupMessages(require(scales, quietly=TRUE))

#setwd('/Users/soma/Documents/ekStep/Scripts/ASER_AWS')
thresh = 5; # atleast spend 5s

#############
# ASER question info INLINE
#############

# obtain by using
# dput(read.csv(ASER.qinfo))
item.df <- structure(list(QuestionText = structure(c(2L, 1L, 3L, 4L, 5L, 
                                          7L, 6L, 8L, 9L, 11L, 10L, 12L, 13L, 14L, 16L, 15L, 17L, 18L, 
                                          20L, 19L, 21L, 22L, 23L, 25L, 24L, 26L, 27L, 29L, 28L, 30L, 31L, 
                                          32L, 34L, 33L, 35L, 36L), .Label = c("Sample 1 division", "Sample 1 double digit recognition", 
                                                                               "Sample 1 letters", "Sample 1 Paragraph 1", "Sample 1 Paragraph 2", 
                                                                               "Sample 1 single digit recognition", "Sample 1 Story ", "Sample 1 subtraction ", 
                                                                               "Sample 1 words", "Sample 2 division", "Sample 2 double digit recognition", 
                                                                               "Sample 2 letters", "Sample 2 Paragraph 1", "Sample 2 Paragraph 2", 
                                                                               "Sample 2 single digit recognition", "Sample 2 Story ", "Sample 2 subtraction ", 
                                                                               "Sample 2 words", "Sample 3 division", "Sample 3 double digit recognition", 
                                                                               "Sample 3 letters", "Sample 3 Paragraph 1", "Sample 3 Paragraph 2", 
                                                                               "Sample 3 single digit recognition", "Sample 3 Story ", "Sample 3 subtraction ", 
                                                                               "Sample 3 words", "Sample 4 division", "Sample 4 double digit recognition", 
                                                                               "Sample 4 letters", "Sample 4 Paragraph 1", "Sample 4 Paragraph 2", 
                                                                               "Sample 4 single digit recognition", "Sample 4 Story ", "Sample 4 subtraction ", 
                                                                               "Sample 4 words"), class = "factor"), Comment = c(NA, NA, NA, 
                                                                                                                                 NA, NA, NA, NA, NA, NA, NA, NA, NA, NA, NA, NA, NA, NA, NA, NA, 
                                                                                                                                 NA, NA, NA, NA, NA, NA, NA, NA, NA, NA, NA, NA, NA, NA, NA, NA, 
                                                                                                                                 NA), subj = structure(c(2L, 2L, 1L, 1L, 1L, 1L, 2L, 2L, 1L, 2L, 
                                                                                                                                                         2L, 1L, 1L, 1L, 1L, 2L, 2L, 1L, 2L, 2L, 1L, 1L, 1L, 1L, 2L, 2L, 
                                                                                                                                                         1L, 2L, 2L, 1L, 1L, 1L, 1L, 2L, 2L, 1L), .Label = c("Literacy", 
                                                                                                                                                                                                             "Numeracy"), class = "factor"), MC = structure(c(5L, 4L, 1L, 
                                                                                                                                                                                                                                                              2L, 2L, 2L, 5L, 6L, 3L, 5L, 4L, 1L, 2L, 2L, 2L, 5L, 6L, 3L, 5L, 
                                                                                                                                                                                                                                                              4L, 1L, 2L, 2L, 2L, 5L, 6L, 3L, 5L, 4L, 1L, 2L, 2L, 2L, 5L, 6L, 
                                                                                                                                                                                                                                                              3L), .Label = c("LO5", "LO7", "LO8", "M125", "M53", "M92"), class = "factor"), 
               NewMc = structure(c(1L, 1L, 2L, 3L, 3L, 3L, 1L, 1L, 4L, 1L, 
                                   1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 
                                   1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L), .Label = c("", 
                                                                                           "LO27", "LO50", "LO7"), class = "factor"), qid = structure(1:36, .Label = c("q_1_dd", 
                                                                                                                                                                       "q_1_div", "q_1_l", "q_1_p_1", "q_1_p_2", "q_1_s", "q_1_sd", 
                                                                                                                                                                       "q_1_sub", "q_1_w", "q_2_dd", "q_2_div", "q_2_l", "q_2_p_1", 
                                                                                                                                                                       "q_2_p_2", "q_2_s", "q_2_sd", "q_2_sub", "q_2_w", "q_3_dd", 
                                                                                                                                                                       "q_3_div", "q_3_l", "q_3_p_1", "q_3_p_2", "q_3_s", "q_3_sd", 
                                                                                                                                                                       "q_3_sub", "q_3_w", "q_4_dd", "q_4_div", "q_4_l", "q_4_p_1", 
                                                                                                                                                                       "q_4_p_2", "q_4_s", "q_4_sd", "q_4_sub", "q_4_w"), class = "factor"), 
               qtype = structure(c(5L, 2L, 3L, 4L, 4L, 6L, 1L, 7L, 8L, 1L, 
                                   2L, 3L, 4L, 4L, 6L, 5L, 7L, 8L, 1L, 2L, 3L, 4L, 4L, 6L, 5L, 
                                   7L, 8L, 1L, 2L, 3L, 4L, 4L, 6L, 5L, 7L, 8L), .Label = c("DD", 
                                                                                           "DIV", "LETTERS", "PARA", "SD", "STORY", "SUB", "WORDS"), class = "factor"), 
               mmc = structure(c(1L, 1L, 1L, 1L, 1L, 1L, 1L, 2L, 1L, 1L, 
                                 1L, 1L, 1L, 1L, 1L, 1L, 2L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 
                                 2L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 2L, 1L), .Label = c("", 
                                                                                         "M73| M74| M92"), class = "factor"), maxscore = c(1L, 1L, 
                                                                                                                                           1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 
                                                                                                                                           1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 
                                                                                                                                           1L, 1L, 1L, 1L), exres = c(NA, NA, NA, NA, NA, NA, NA, 89L, 
                                                                                                                                                                      NA, NA, NA, NA, NA, NA, NA, NA, 93L, NA, NA, NA, NA, NA, 
                                                                                                                                                                      NA, NA, NA, 93L, NA, NA, NA, NA, NA, NA, NA, NA, 93L, NA), 
               qtech = structure(c(1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 
                                   1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 
                                   1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L), .Label = "Procedural", class = "factor")), .Names = c("QuestionText", 
                                                                                                                                      "Comment", "subj", "MC", "NewMc", "qid", "qtype", "mmc", "maxscore", 
                                                                                                                                      "exres", "qtech"), class = "data.frame", row.names = c(NA, -36L
                                                                                                                                      ))

############

# Produce # of assessments by District
#load('ASER_JsonObject.RData')
# loads json object
# prepare assess data frame
# no of questions attempted by each student
nq <- sapply(df$questions,nrow)

df.assess <- dplyr::bind_rows(df$questions)
df.assess$uid <- rep(df$uid,times=nq)
# order age
tmp <- df$age;
tmp <- ordered(tmp, levels = sort(unique(tmp)))
df$age <- tmp



#item.df <- read.csv('ASERqinfo_V1.0.csv')
# was replaced by inline ASER qinfo
df.assess.full <- left_join(df.assess,item.df,by="qid")

tmp <- df.assess.full$qtype
tmp <- ordered(tmp, levels = c("SD","DD","SUB","DIV","LETTERS","WORDS","PARA","STORY"))
df.assess.full$qtype <- tmp



# App Data Used to produce this report
cat('** *** **\n');
cat('** Data Source **\n');
cat('** *** **\n');

cat('App/Game/Screener ID: ',jobConfig$filter$contentId, '\n');
cat('Played/Administed between: ',jobConfig$query$dateFilter$from, ' and ',jobConfig$query$dateFilter$to,'\n');
cat('\n\n\n');


# demographics
cat('** *** ** \n');
cat('** Student Demographics ** \n');
cat('** *** ** \n\n');
cat('Number of Students:',length(df$uid),'\n');

cat('** Age Distribution (counts) ** \n');
p<-ggplot(df, aes(x=factor(age))) + geom_bar(stat='bin',position='dodge')+
  labs(x='Age',y='# of Assessments',title='')
print(p)
cat('** Age Distribution (percentage) ** \n');
p<-ggplot(df, aes(x=factor(age))) + 
  geom_bar(stat='bin',aes(y = (..count..)/sum(..count..)),position='dodge')+
  scale_y_continuous(labels = percent_format())+
  labs(x='Age',y='% of Students',title='')
print(p)
cat('** Gender Distribution (counts) ** \n');
p<-ggplot(df, aes(x=factor(gender))) + geom_bar(stat='bin',position='dodge')+
  labs(x='Gender',y='# of Assessments',title='')
print(p)
cat('** Gender Distribution (percentage) ** \n');
p<-ggplot(df, aes(x=factor(gender))) + 
  geom_bar(stat='bin',aes(y = (..count..)/sum(..count..)),position='dodge')+
  scale_y_continuous(labels = percent_format())+
  labs(x='Gender',y='% of Students',title='')
print(p)
cat('** Gender x Age Distribution (counts) ** \n');
p<-ggplot(df, aes(x=factor(age),fill=factor(gender))) + geom_bar(stat='bin',position='dodge')+
  labs(x='Age',y='# of Assessments',title='',fill='Gender')
print(p)
cat('** Gender x Age Distribution (percentage) ** \n');
p<-ggplot(df, aes(x=factor(age),fill=factor(gender))) + 
  geom_bar(stat='bin',aes(y = (..count..)/sum(..count..)),position='dodge')+
  scale_y_continuous(labels = percent_format())+
  labs(x='Age',fill='Gender',y='% of Students',title='')
print(p)


cat('** *** ** \n');
cat('** Student Assessment Events ** \n');
cat('** *** ** \n\n');


cat("Histogram of # of questions attempted by Student's\n")
p<-ggplot(df,aes(x=nq)) + geom_histogram(aes(y=..density..),fill='darkgrey')+geom_density()+
  scale_x_discrete(breaks=nq,labels=nq)+
  labs(x='# of Questions Answered in ASER',y='Density',title='')
print(p)


cat("Distribution of Total Time Spent in ASER\n")
# remove later
# df$timeSpent <- rnorm(nrow(df))

p<-ggplot(df$game,aes(x=timeSpent)) + geom_histogram(aes(y=..density..),fill='darkgrey')+geom_density()+
  labs(x='Time Spent(s) in ASER',y='Density',title='')
print(p)


cat("% Correct Responses By Question Level\n")
p<-ggplot(df.assess.full, aes(x=qtype,fill=factor(score))) + 
  geom_bar(stat='bin',aes(y = (..count..)/sum(..count..)),position='fill')+
  scale_y_continuous(labels = percent_format())+
  labs(x='Question Level',fill='Score',y='% of Correct Responses',title='')
print(p)

cat("Time Spent(in seconds) By Question Level\n")

# remove this later
#df.assess.full$timeSpent <- rnorm(nrow(df.assess.full))

p<-ggplot(df.assess.full, aes(x=factor(qtype),y=timeSpent)) + 
  geom_boxplot(outlier.shape = 3)+geom_point(position = position_jitter(width = 0.1),col='darkgray')+
  labs(x='Question Level',y='Time(s)',title='')
print(p)


cat('** *** ** \n');
cat('** Student Level Set Events ** \n');
cat('** *** ** \n\n');

# Distribution of Math grades (by age)
# remove later
#tmpNum <- sample(LETTERS[1:5],nrow(df),replace=T)
#tmpLit <- sample(letters[1:5],nrow(df),replace=T)

tmpDF <- data.frame(age=df$age,math=df$game$level$MATH,lit=df$game$level$READING)


cat('** Age x ASER Math Level Distribution (count) ** \n');
p<-ggplot(tmpDF, aes(x=factor(age),fill=factor(math))) + 
  geom_bar(stat='bin',position='dodge')+
  labs(x='Age',fill='ASER Math Level',y='# of Students',title='')
print(p)
cat('** Age x ASER Math Level Distribution (percentage) ** \n');
p<-ggplot(tmpDF, aes(x=factor(age),fill=factor(math))) + 
  geom_bar(stat='bin',aes(y = (..count..)/sum(..count..)),position='fill')+
  scale_y_continuous(labels = percent_format())+
  labs(x='Age',fill='ASER Math Level',y='% of Students',title='')
print(p)

cat('** Age x ASER Lit Level Distribution (count) ** \n');
p<-ggplot(tmpDF, aes(x=factor(age),fill=factor(lit))) + 
  geom_bar(stat='bin',position='dodge')+
  labs(x='Age',fill='ASER Lit Level',y='# of Students',title='')
print(p)
cat('** Age x ASER Lit Level Distribution (percentage) ** \n');
p<-ggplot(tmpDF, aes(x=factor(age),fill=factor(lit))) + 
  geom_bar(stat='bin',aes(y = (..count..)/sum(..count..)),position='fill')+
  scale_y_continuous(labels = percent_format())+
  labs(x='Age',fill='ASER Lit Level',y='% of Students',title='')
print(p)

cat('** Corr between Math and Lit by age ** \n');
p<-ggplot(tmpDF, aes(x=factor(math),y=factor(lit),colour=factor(age))) + 
  geom_point(position = "jitter",aes(color = factor(age)),size=3)+
  labs(x='Math Level',y='Lit Level',title='',col='Age')+
  theme(legend.position="right",axis.text.x = element_text(angle = 45, hjust = 1))
print(p)


# Error Diagnosis
# Loop thru Num and for each question
# TBD

# TSP per question by Assigned Level
