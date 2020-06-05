# How to Run
1. make sure that you have `java`, `maven`, `tomcat` and any mysql server (e.g. `XAMPP`)
2. run your mysql server
3. you may need to set the server time zone using: `SET GLOBAL time_zone = '+2:00';`
4. write the following commands in the terminal in the directory of `search` server
   1. `mvn compile`
   2. `mvn package`
5. deploy the `search.war` file (can be found in `target` folder) on `tomcat`
6. run `WebCrawler` and give it the number of threads as a command argument
7. run `Indexer`
8. run `PageRank`
   