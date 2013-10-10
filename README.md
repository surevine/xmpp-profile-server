xmpp-profile-server
===================

Deliver different vCard4s to users based on their existance in a user's roster group (or lack of existance).

Please see the [Wiki](https://github.com/surevine/xmpp-profile-server/wiki) for more information.

## Build status

[![Build Status](https://travis-ci.org/surevine/xmpp-profile-server.png?branch=master)](https://travis-ci.org/surevine/xmpp-profile-server)

## Discuss

We have a MUC (Multi User Chat) room open to discuss this work. Please feel free to join us at 
[profile-server@chat.surevine.com](xmpp:profile-server@chat.surevine.com).

## Database install

See the database directory for SQL import files.

## Build and run

* `git clone https://github.com/surevine/xmpp-profile-server`
* `cd xmpp-profile-server`
* `mvn package`
* Edit configuration files as required
* Install database
* `java -jar target/profileserver-%VERSION%-jar-with-dependencies.jar
