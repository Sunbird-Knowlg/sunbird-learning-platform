EkStep Transaction Event Handler

1. Build it:

        mvn clean package

2. Copy target/transaction-event-handler-1.1.jar to the plugins/ directory of your Neo4j server.

3. Start your Neo4j Server

4. Run these queries, And check the Logs should be updated:

        CREATE (A:User {name:"Azhar"}) RETURN A;

5. You should see:

		Updated Logs.



