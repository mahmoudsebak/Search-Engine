# Search-Engine
Crawler-based search engine that contains the main modules of a search engine (crawler, indexer, and ranker)  
with features like image search, voice recognition and personalized search
written in java

## Database Design
The database is created using MySQL with [IndexerDbAdapter.java](search/src/main/java/IndexerDbAdapter.java)  
The database of the search engine server has the following tables

| table name    |
| ------------- |
| tb1_urls      |
| tb2_words     |
| tb3_links     |
| tb4_images    |
| tb5_queries   |
| tb6_user_urls |

The first table is used to store information about each URL, and has the following schema

| column           | type                           |
| ---------------- | ------------------------------ |
| _id              | int primary key auto_increment |
| url              | varchar(256)                   |
| content          | meduimtext                     |
| title            | varchar(512)                   |
| carwled_at       | datetime                       |
| indexed          | tinyint                        |
| page_rank        | double                         |
| date_score       | double                         |
| geographic_score | double                         |

The second table is used to store the keywords in each URL with word score  
The score of the word is the (sum (each HTML-tag score * number of occurrences of this tag) ) / number of total words  
The table has the following schema

| column | type                           |
| ------ | ------------------------------ |
| _id    | int primary key auto_increment |
| word   | varchar(100)                   |
| url    | varchar(256) foreign key       |
| score  | double                         |

The third table is used to store links between URLs to use it in [PageRank.java](search/src/main/java/PageRank.java)  
It has the following schema

| column  | type                           |
| ------- | ------------------------------ |
| _id     | int primary key auto_increment |
| src_url | varchar(256) foreign key       |
| dst_url | varchar(256) foreign key       |

The fourth table is used to store image links in each URL, and has the following schema

| column | type                           |
| ------ | ------------------------------ |
| _id    | int primary key auto_increment |
| url    | varchar(256) foreign key       |
| image  | varchar(512) foreign key       |

The fifth table is used to store users' queries that is used to make suggestions when a user enter some text in search bar

| column | type                           |
| ------ | ------------------------------ |
| _id    | int primary key auto_increment |
| query  | text unique                    |

The sixth table is used to store the URLs that the user has clicked on and its frequency,  
to favor its base-link in Personalized search feature (assuming one user)

| column | type                           |
| ------ | ------------------------------ |
| _id    | int primary key auto_increment |
| url    | varchar(256) unique            |
| freq   | int                            |

## System Design
### Web Crawler
It has some URLs in its seed, it does the following steps
- add the URLs from the seed in the queue with other un-crawled URLs fetched from the database
- fetch URL from the queue
- parse its content and get other URLs in it
- add the source and destination URLs in `tb1_urls`
- add the destination URL to the queue
- add the link between source and destination URLs in `tb3_links`

### Indexer
It does the following steps
- fetch un-indexed URL from the database
- parse the document and put its content in `tb1_urls` to be used in phrase search, and store other URL-related information
- remove stopping words form the document, apply stemming on them and calculate their score, then store them in `tb2_words`
- store image links in `tb4_images`

### Query Processor
It does the following steps
- check if the query has double quotes that surrounds it, if so, it does phrase search, otherwise it does normal search
- the words in the query are split, the stopping words are removed, and stemming is applied
- the database is queried to get the results and rank the URLs according to sum(score * IDF) where score is the word score calculated in the ranker, and IDF is the inverse document frequency
- The content of the returned URLs is searched for any word that match the original query, and the content is clipped to start with that word, limited by 200 character
- The result of the 10 returned URLs is sorted by clicking frequency of base-URL
