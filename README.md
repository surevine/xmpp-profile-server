xmpp-profile-server
===================

Deliver different vCard4s to users based on their existance in a user's roster group (or lack of existance).

## Build status

Note this points to the main buddycloud repository for the java server.

[![Build Status](https://travis-ci.org/surevine/xmpp-profile-server.png?branch=master)](https://travis-ci.org/surevine/xmpp-profile-server)

## Database install

See the database directory for SQL import files.

## Build and run

* `git clone https://github.com/surevine/xmpp-profile-server`
* `cd xmpp-profile-server`
* `mvn package`
* Edit configuration files as required
* Install database
* `java -jar target/profileserver-<VERSION>-jar-with-dependencies.jar
