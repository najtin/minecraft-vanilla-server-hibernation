# minecraft-server-hibernation
forked from [gekigek99](https://github.com/gekigek99/minecraft-vanilla-server-hibernation) but has been completly rewritten in java since

This Java-Project lets you run your minecraft server on demand with almost no overhead! If no players are online for a prolonged period of time the server will be shutdown. When a player connects to your server, the server will be started. 

## First steps:
1. You can either build the project yourself or [download the precompiled jar](https://github.com/najtin/minecraft-server-hibernation/releases/download/v.0.0.1/minecraft-hibernation-0.0.2-jar-with-dependencies.jar).
2. Put the jar in your minecraft-server folder.
3. On the first start it will generate a config.txt. Run the hibernate-jar. 
4. Customize it to your needs. 

Now you can run you mincraft server on demand by running the hibernate-jar just like you would your `server.jar`. 

Notice, that it also creates a new folder `hibernate-logs`. Inside you find the logs of the hibernation-jar.

## How is this different from the project of [gekigek99](https://github.com/gekigek99/minecraft-vanilla-server-hibernation)?
- no overhead for a proxy, since there is no proxy
- no need for any other software, only java
- nice logging
- monitoring via http
