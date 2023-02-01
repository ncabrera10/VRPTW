#### 0. Libraries ####
# This function verifies which packages are missing. If a package is missing, is automatically installed.

  foo <- function(x){
    for(i in x){
      #  Returns true if it was possible to load the package
      if(!require(i,character.only = TRUE)){
        #  If the package was not loaded, then we proceed to the installation
        install.packages(i,dependencies = TRUE)
        #  Load the package after the installation
        require( i , character.only = TRUE )
      }
    }
  }

#  Load pakages:

  foo(c("readr","openxlsx","sqldf","stringr","fields"))

#### 1. Parameters ####
  
  # Path to the results files:
  
  instancesPath = "/Users/nicolas.cabrera-malik/Documents/Work/CodingProjects/VRPTW/VRPTW/data/Solomon/"
  printPath = "/Users/nicolas.cabrera-malik/Documents/Work/CodingProjects/VRPTW/VRPTW/data/Solomon_F/"
  
#### 2. Reads and re-writes the files ####
  
  files = list.files(instancesPath)
  
  for(i in files){
    
    df <- read.table(paste0(instancesPath,i), quote="\"", comment.char="")
    write.table(df,paste0(printPath,i),sep=",",row.names = FALSE,col.names=FALSE)
  }
  
