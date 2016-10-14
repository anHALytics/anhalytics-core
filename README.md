# AnHALytics Core

[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

AnHALytics is a project aiming at creating an analytic platform for the [HAL research archive](https://hal.archives-ouvertes.fr) or other scientific Open Access repositories, exploring various analytic aspects such as search/discovery, activity and collaboration statistics, trend/technology maps, knowledge graph and data visualization. The project is supported by an [ADT Inria](http://www.inria.fr/en/research/research-teams/technological-development-at-inria) grant and good will :). 

This module share the data ingestion chain and the core back-end functionalities of the system. 

## License

This code is distributed under [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0). 

## Warning

AnHALytics is a work at early stage and a work in progress. It is evolving rapidly and is certainly not production ready! 

So far, only HAL is supported.

## People

- Achraf Azhar
- [Patrice Lopez](https://github.com/kermitt2) 

If you are interested in contributing to the project, please contact <patrice.lopez@inria.fr>. 
### Prerequisites:
###### 1. Java/Maven

[JAVA](https://java.com/en/download/manual_java7.jsp) & [Maven](https://maven.apache.org)

###### 2. GROBID (GeneRation Of BIbliographic Data)

[Grobid](https://github.com/kermitt2/grobid) is used as entry point for document digestion, which means extracting metadata and structured full text in ([TEI](http://www.tei-c.org/Guidelines/)). [Grobid](https://github.com/kermitt2/grobid) is a machine learning library for extracting bibliographical information and structured full texts from technical and scientific documents, in particular from PDF. It is distributed under Apache 2 license.

Clone the current project from github (as of March 2016, GROBID version 0.4.1-SNAPSHOT):

	git clone https://github.com/kermitt2/grobid.git

AnHALytics uses GROBID as a service so that we can use distribution and multithreaded process easily. See the [GROBID documentation](http://grobid.readthedocs.org) on how to start the RESTful service.

###### 3. (N)ERD - Entity Recognition and Disambiguisation

Our (N)ERD service annotates the text by recognizing and disambiguating terms. Entities are currently identified against Wikipedia/FreeBase. In this project, the (N)ERD is using the free disambiguation service, and the recognition and disambiguation is not constrained by a preliminary Named Entity Recognition. 

At the present date, only the NER part of the NERD is available in open source (see [grobid-ner](https://github.com/kermitt2/grobid-ner). The full NERD repo will be made publicly available on GitHub soon under Apache 2 license. 

###### 4. Keyterm extraction and disambiguation 

This key term extraction service is based on the [keyphrase extraction tool](http://www.aclweb.org/anthology/S10-1055) developed and ranked first at [SemEval-2010, task 5 - Automatic Keyphrase Extraction from Scientific Articles](http://www.aclweb.org/anthology/S10-1004). _Term_ in this context has to be understood as a complex technical term, e.g. a phrase having a specialized meaning given a technical or scientific field. In addition to key term extraction, the weighted vector of terms is disambiguated by the above (N)ERD service, resulting in a weighted list of Wikipedia topics.  

The full NERD repo will be made publicly available on GitHub soon under Apache 2 license. 


###### 5. ElasticSearch

[Elasticsearch](https://github.com/elastic/elasticsearch) is a distributed RESTful search engine. Specify (and adapt if you want more shards and replicas) the following in the ElasticSearch config file (elasticsearch.yml):

    cluster.name: traces # or something else
    index.number_of_shards: 1
    index.number_of_replicas: 0
    cluster.routing.allocation.disk.threshold_enabled: false
    cluster.routing.allocation.disk.watermark.low: 95
    cluster.routing.allocation.disk.watermark.high: 99
    http.jsonp.enable: true

AnHALytics supports currently (April 2016) a version __1.7__ of ElasticSearch, we plan to upgrade to a version 2.* in the next weeks. 

###### 6. MongoDB

A running instance of [MongoDB](https://www.mongodb.org) is required for document persistence and document provision.

###### 7. Mysql

A running instance of [Mysql](https://www.mysql.fr/) is required for building the knowledge base (relational database).

###### 8. Web server

A web application server, such as Tomcat, JBoss or Jetty, is necessary to deploy the complementary front demos which are packaged in a war file.

### Project design

anHALytics-core performs the document ingestion, from external harvesting of documents to indexing. It has (so far) six components corresponding to six sub-projects:

0. __common__ contains methods and resources shared by several other components. 
1. __harvest__ performs the document harvesting (PDF and metadata) and the transformations into common TEI representations. 
2. __annotate__ realises document enrichment, more precisely it disambiguates and annotates entities and key-concepts into the TEI structures.
3. __kb__ build and update the Knowledge Base (KB) of anHALytics.
4. __index__ performs indexing in ElasticSearch for the final TEI, the annotations and the KB.
5. __test__ is dedicated to integration tests.

### Build with maven

After cloning the repository, compile and build using maven:

    cd anHALytics-core
    mvn clean install
After the compilation you'll find the jar produced for each module under ``MODULE/target``.

You need to customize the configuration :

    ./anhalytics.sh --configure

Then to import the relational database schema use :

    ./anhalytics.sh --prepare

Make sure all the required service are up..
### Components:

#### 1. Harvesting

> cd anhalytics-harvest

Currently only OAI-PMH is used as harvesting protocol and only HAL archive is supported 

An executable jar file is produced under the directory ``anhalytics-harvest/target``.

The following command displays the help:

> java -jar target/anhalytics-harvest-```<current version>```.one-jar.jar -h

For a large harvesting task, use -Xmx2048m to set the JVM memory to avoid OutOfMemoryException.

###### HarvestAll / HarvestDaily

To start harvesting all the documents of HAL based on [OAI-PMH](http://www.openarchives.org/pmh) v2, use:
> java -Xmx2048m -jar target/anhalytics-harvest-```<current version>```.one-jar.jar -exe harvestAll

Harvesting is done through a reverse chronological order, here is a sample of the OAI-PMH request:
http://api.archives-ouvertes.fr/oai/hal/?verb=ListRecords&metadataPrefix=xml-tei&from=2015-01-14&until=2015-01-14

To perform an harvesting on a daily basis, use:

> java -Xmx2048m -jar target/anhalytics-harvest-```<current version>```.one-jar.jar -exe harvestDaily

For instance, the process can be configured on a cron table.

###### Grobid processing

Once the document are downloaded, the TEI extrating threads will run automatically. You can also run the process manually with
> java -Xmx2048m -jar target/anhalytics-harvest-```<current version>```.one-jar.jar -exe processGrobid

###### TEI building

The working TEI is generated following this struture

```xml
    <teiCorpus>
        <teiHeader>
            <!-- Consolidated harvested metadata, from HAL for example, with entity 
				(author, affiliation, etc.) disambiguation -->
        </teiHeader>
        <TEI>
            <!-- GROBID automatically extracted data -->
        </TEI>
    </teiCorpus>
```

At least the harvested Metadata TEI is necessary to produce the final TEI(the PDF is not always available :( ), it's done using :

> java -Xmx2048m -jar target/anhalytics-harvest-```<current version>```.one-jar.jar -exe generateTei

###### Document storage and provision

We use MongoDD GridFS component for document file support. Each type of files are stored in a different collection. hal tei => hal-tei-collection , binaries => binaries-collection,..., 

<!-- documentation of the collections here !! -->

#### 2. Annotation
Once the working TEI collection is set, we could start as a first step to extract named entities and compute keyterms from the downloaded documents, that's the purpose of the anhalytics-annotate sub-project : 
> cd anhalytics-annotate

The documents are enriched with semantic annotations. This is realized with the NERD service.

An executable jar file is produced under the directory ``anhalytics-annotate/target``.

The following command displays the help:
> java -Xmx2048m -jar target/anhalytics-annotate-```<current version>```.one-jar.jar -h

For launching the full annotation of all the documents using all the available annotators: 

> java -Xmx2048m -jar target/anhalytics-annotate-```<current version>```.one-jar.jar -multiThread -exe annotateAll

(-multiThread option is recommended, it takes time)

###### Annotation of the HAL collection with the (N)ERD service

The annotation of the sub-project ``anhalytics-annotate/``:

> java -Xmx2048m -jar target/anhalytics-annotate-```<current version>```.one-jar.jar -multiThread -exe annotateAllNerd

(-multiThread option is recommended. this activates parallel processing )

###### Annotation of the HAL collection with the KeyTerm extraction and disambiguation service

The annotation on the HAL collection can be launch with the command in the main directory of the sub-project ``anhalytics-annotate/``:

> java -Xmx2048m -jar target/anhalytics-annotate-```<current version>```.one-jar.jar -multiThread -exe annotateAllKeyTerm

(-multiThread option is recommended, it takes time)

###### Storage of annotations

Annotations are persistently stored in a MongoDB collection and available for indexing in ElasticSearch. 

#### 3. KB
The knowledge base is built using mysql, all the metadata at our disposal is extracted and saved in a relational database following this data model https://github.com/anHALytics/documentation/blob/master/model/anhalyticsDB.png

> cd anhalytics-kb

To see all available options:

> java -Xmx2048m -jar target/anhalytics-kb-```<current version>```.one-jar.jar -h

To feed the database from the TEI use :
> java -Xmx2048m -jar target/anhalytics-kb-```<current version>```.one-jar.jar -exe initKnowledgeBase

Then another database is needed for the bibliographic references extracted with GROBID, to build it use :
> java -Xmx2048m -jar target/anhalytics-kb-```<current version>```.one-jar.jar -exe initCitationKnowledgeBase
#### 4. Indexing
> cd anhalytics-index
###### Build all the indexes 

For building all the indexes required by the different freontend applications using all the existing loaded documents, use the following command:

>java -Xmx2048m -jar target/anhalytics-index-```<current version>```.one-jar.jar -exe indexAll

For indexing only the data corresponding on a daily basis:

>java -Xmx2048m -jar target/anhalytics-index-```<current version>```.one-jar.jar -exe indexDaily

The following commands make possible to index separately only certain type of data. 

###### Indexing TEI

First, the working TEI documents have to be indexed, ``anhalytics-index/`` is a module made to index data: 

>java -Xmx2048m -jar target/anhalytics-index-```<current version>```.one-jar.jar -exe indexTEI


###### Indexing annotations

For indexing the annotations, in the main directory of the sub-project ``anhalytics-index/``:

>java -Xmx2048m -jar target/anhalytics-index-```<current version>```.one-jar.jar -exe indexAnnotations

##### Indexing the Knowledge Base

For indexing the content of the Knowlkedge Base, in the main directory of the sub-project ``anhalytics-index/``:

>java -Xmx2048m -jar target/anhalytics-index-```<current version>```.one-jar.jar -exe indexKB


#### 5. Test
> cd anhalytics-test
This subproject is dedicated to integration and end-to-end tests - in contrast to unit tests which come with each specific sub-project.

Work in progress...


