# ogpDownloader

Scalable download utility service for geospatial data.

Uses ActiveMQ, Spring Boot, GeoTools, 7-zip.

A download request sent to the controller gets converted into a JMS message and put into a queue (embedded by default, but an external
queue can be referenced in application.properties). A JMS listener picks up the message and performs the download, transferring data from
a GeoTools DataStore to a shapefile. A request to an OGP solr instance retrieves XML metadata for inclusion. 7-zip is used to create an archive.
At this point, a reply message is sent with the path to the zip archive.

Eventually, multiple instances of the downloader will be able to listen to the same queue, providing a highly available service.

GDAL bindings are included, but only currently used for testing. A command line switch like this:

`-Djava.library.path=/usr/local/Cellar/gdal/1.11.3_1/lib`

is required to reference the native GDAL library.


