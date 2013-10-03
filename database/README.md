PostgreSQL schema
=================

Installation instructions
-------------------------

When installing the server, you must first execute `0001.sql`, then all the
upgrade files in order, i.e. first `0002.sql`, then `0003.sql`, etc.:

    psql -U <username> -d <db> < 0001.sql
    psql -U <username> -d <db> < 0002.sql
    ...etc...


Upgrade instructions
--------------------

If you need to upgrade the schema version after upgrading the server software,
you'll need to be a little more careful.

First, stop the server and **back up your DB**. The simplest way to do this is
to run `pg_dump -c -U <username> <db> > backup.sql`.

Then, read the version notes below: they will tell you what you need to take
care of.

Once done you can apply the files needed for your upgrade: if your DB schema is
currently version 3 and you need version 5, you will apply `0004.sql` and
`0005.sql` but not `0003.sql` and below.