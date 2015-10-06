#!/usr/bin/Rscript


cat('Begin executing R script on:',format(Sys.Date(), "%B %d, %Y"), '\n')
require(jsonlite)
require(rmarkdown)


# set thse Env for rmarkdown to run the script from command line instead of RStudio
# exact values don't matter
Sys.setenv(RSTUDIO_PANDOC="/usr/bin/pandoc")
Sys.setenv(HOME="/something_not_important")

outputFile=Sys.getenv("outputFile")
cat(outputFile)
source_dir=Sys.getenv("sourceDir")
cat('** x **')
cat(source_dir)

cat('** Y **\n')
jobConfig=Sys.getenv("jobConfig")
print(jobConfig)
jobConfig = fromJSON(jobConfig)
#save(jobConfig, file = "/Users/soma/Documents/ekStep/Scripts/ASER_AWS/ASER_ConfigObject.RData")
cat('\n')
#initial.options <- commandArgs(trailingOnly = FALSE)
#file.arg.name <- "--file="
#script.name <- sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)])
#source_dir <- dirname(script.name)


#outputFile="/Users/soma/Documents/ekStep/Scripts/ASER_AWS/test_try.html"
#source_dir=getwd()

cat(" Environment Variables:")
cat(" Output File - ",outputFile,'\n')
cat(" Source Dir - ",source_dir,'\n') 

cat(' Reading Json Sream\n')

readJsonLineStreams <- function()
{
  tryCatch(
    {
      stream_in(file("stdin"))
      #stream_in(file("sample.txt"))
    },
    error = function(err){
      cat(' Could not read streaming data\n')
      message(err,'\n')
      cat(' Terminating R script on:',format(Sys.Date(), "%B %d, %Y"), '\n')
      quit(save="no",status=1,runLast=FALSE)
    }
  )
}
df <- readJsonLineStreams()
cat(' Done reading Json Sream\n')
cat(' Read',length(df$uid),' events \n')

output_file=basename(outputFile)
output_dir=dirname(outputFile)
#save(df, file = "/Users/soma/Documents/ekStep/Scripts/ASER_AWS/ASER_JsonObject.RData")


renderHTML <- function()
{
  tryCatch(
    {
      rmarkdown::render(paste(source_dir,"/ASER_GenerateReport.Rmd",sep=''), 
                  output_file = output_file, output_dir = output_dir)
    },
    error=function(err){
      cat(' Could not render HTML output\n')
      message(err,'\n')
      cat(' Terminating R script on:',format(Sys.Date(), "%B %d, %Y"), '\n')
      quit(save="no",status=2,runLast=FALSE)
    }
    )
}
cat(' Invoking markdown\n')
renderHTML()
cat(' Terminating R script on:',format(Sys.Date(), "%B %d, %Y"), '\n')
