In order to run migrations run command:
    migrate -path ./migrations -database postgres://<user>:<password>@<host>:<port>/<database> up 
    migrate -path ./migrations -database postgres://postgres:root@localhost:5432/contractor_tracking

To give postgres database uuid capabilities, must run command:
    psql > CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

Quick command to copy db as postgres user:
    pg_dump production-db | psql test-db

To generate models from database, run command(while in directory /server):
    $ xo pgsql://postgres:root@localhost/contractor_tracking -o models --template-path model-templates

To generate code coverage file type:
    $ go test -coverprofile=<file_name> <path/to/package>
    ex: $ go test -coverprofile=/tmp/code-cover bitbucket.org/TravisS25/contractor-tracking/contractor-tracking/contractor-server/api

To view code coverge file in html type:
    $ go tool cover -html=<file_name>