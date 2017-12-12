## scala-chat-room

This is a simple chatroom using Play, Akka and Websockets with the Scala API.

Front-end: HTML / JS + JQuery + Bootstrap
Back-end: Scala / Akka

### Prerequisites

You will need [JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) and [sbt](http://www.scala-sbt.org/) installed.

### Running

```
sbt run
```

server will be running on localhost:9000

Instructions:

1. *Chat*: type on chat box and hit ENTER or click on "OK" button
2. subscribe to a topic: select available topics on chat box and hit ENTER or click on "OK" button (optionally type a msg to enter or start chatroom with a msg)
3. A user can see the last 10 messages of a topic that he will specify through the subscribed chatboxes
4. A user can subscribe to a topic and receive in real-time new messages added to this topic
5. Every message posted by a user is scored using a simulated external third-party API (simulating different delays for different msgs)
6. A user can see a ranking of top 10 users with the highest score on the right panel inside each subscribed topic chatbox
7. unsubscribe from topic: click on "subscribe" checkbox (un-check option) so user will unwatch the topic and chatbox will be removed from page
8. When new topics are created a TopicList update is sent to all users and the available select options with topics is updated so all users can see topic list updates
9. If WebSocket connection is closed for any reason all the chatboxes are removed from page and user will unwatch all topics.
10. If WebSocket is closed user can restart it simply by "chatting" (in this case topic MAIN will be used) or creating a new topic
11. User starts by default listening on topic "MAIN"
12. To test multiple with users open multiple browser windows and point to localhost:9000 start chatting


### Testing

```
sbt test
```

***

## Folder structure

#### build.sbt

sbt build file

#### app

where all the src code is

#### test

where all the test src code is

#### conf

configuration files

#### public

all public static assets such as JS and others

