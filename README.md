# Voter Queue Project

Deployed on <link>https://voter-queue.herokuapp.com/</link>.

Or run `heroku open -r prod` in your terminal.

## The Stack
* ~~SparkJava~~ Javalin - web framework
* postgres - database 
* Lombok - library for getters/setters
* Jackson  - JSON parsing library 
* ZXing - library for QR codes
* SLF4J - logging framework

## Useful commands 
If any of the Maven dependencies change, run 
* `mvn assembly:single` to build the JAR file
* `mvn heroku:deploy`

Usually, just running this would update the changes to Heroku.
* `git push heroku master` to push code from master branch to heroku remote branch


To start a local Postgres server and set up the database,  
* `pg_ctl -D /usr/local/var/postgres start`
* enter the `psql` shell with an existing database 
* copy and paste `setup.sql` then `schema.sql` from the `src/db` directory
