### Application to test incorrect password is leading DB connection to max out because of jasync-mysql driver

To run the application, simply update the DB information in application.yml file and right click on SpringKotlinR2dbcSampleApplication.kt and press Run button. 

Another way would be by running following command on root of the folder `mvn spring-boot:run`

Standard properties to override
```yaml
r2dbc.pool:
    initialSize: 3
    maxSize: 5
    maxLife: 10
    maxCreateConnectionTime: 5
    maxIdleTime: 10

r2dbc:
    host: mysql-db.cpthxklw6jlh.us-east-1.rds.amazonaws.com
    port: 3306
    db: sushil_demo
    username: admin
    password: root

```

Connect to the mysql db using CLI, and keep running `show status like '%onn%';`

In case of incorrect password entered in this spring boot application. You will notice that max connection is reaching db max limit even when we override pool max size to something way smaller. 

Also, results are different on mac & windows for this incorrect password scenario. on Windows we are seeing AccessDenied Exception which is leading to connection closure, but when we run the same code on Mac, we are getting 

`com.github.jasync.sql.db.exceptions.UnsupportedAuthenticationMethodException: Unknown authentication method -> 'caching_sha2_password'`

