
 Given we have some file messages to store in S3 and ElaticSearch
   When a message is present
   Then turn the message into an entprise message
   And store the message into S3
   And store the message into Elastic Search

