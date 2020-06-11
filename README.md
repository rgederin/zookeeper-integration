# Table of Content

- [Apache Zookeeper](#apache-zookeeper)
    * [Zookeeper ZNodes](#zooKeeper-znodes)
    * [Implementation choices](#implementation-choices)
    * [Install Zookeeper on Mac OS](#install-zookeeper-on-mac-os)
    * [Zookeeper operations](#zookeeper-operations)
    * [Znode Types and their Use Cases](#znode-types-and-their-use-cases)

# Apache Zookeeper

Apache Zookeeper is a software project of the Apache Software Foundation. It is essentially a service for distributed systems offering a hierarchical key-value store, which is used to provide a distributed configuration service, synchronization service, and naming registry for large distributed systems.

It is a library that enables coordination in distributed systems. Below are some of the distributed systems coordination problem that zookeeper solves:

* **Configuration management** — managing application configuration that can be shared across servers in a cluster. The idea is to maintain any configuration in a centralized place so that all servers will see any change in configuration files/data.
* **Leader election** — electing a leader in a multi-node cluster. You might need a leader to maintain a single point for an update request or distributing tasks from leader to worker nodes.
* **Locks in distributed systems** — distributed locks enables different systems to operate on a shared resource in a mutually exclusive way. Think of an example where you want to write to a shared file or any shared data. Before updating the shared resource, each server will acquire a lock and release it after the update.
* **Manage cluster membership** — maintain and detect if any server leaves or joins a cluster and store other complex information of a cluster.

## Zookeeper ZNodes

Zookeeper solves these problems using its magical tree structure file system called znodes, somewhat similar to the Unix file system. These znodes are analogous to folders and files in a Unix file system with some additional capabilities. 

Zookeeper provides primitive operations to manipulate these znodes, through which we will solve our distributed system problems. 

![struct](https://github.com/rgederin/zookeeper-integration/blob/master/img/struct.jpg)


### Key Znode features

* Znodes can store data and have children Znode at same time
* It can store information like the current version of data changes in Znode, transaction Id of the latest transaction performed on the Znode.
* Each znode can have its access control list(ACL), like the permissions in Unix file systems. Zookeeper supports: create, read, write, delete, admin(set/edit permissions) permissions.
* Znodes ACL supports username/password-based authentication on individual znodes too.
* Clients can set a watch on these Znodes and get notified if any changes occur in these znodes.
* These change/events could be a change in znodes data, change in any of znodes children, new child Znode creation or if any child Znode is deleted under the znode on which watch is set.

## Install Zookeeper on Mac OS

Using terminal:

```
brew install zookeeper
```

## Zookeeper operations

![ops](https://github.com/rgederin/zookeeper-integration/blob/master/img/ops.png)

Start ZK server

```
ODL1610003:~ rgederin$ zkServer start
/usr/bin/java
ZooKeeper JMX enabled by default
Using config: /usr/local/etc/zookeeper/zoo.cfg
Starting zookeeper ... STARTED
ODL1610003:~ rgederin$ 
```

Start ZK CLI

```
ODL1610003:~ rgederin$ zkCli
/usr/bin/java
Connecting to localhost:2181
Welcome to ZooKeeper!
JLine support is enabled

WATCHER::

WatchedEvent state:SyncConnected type:None path:null
[zk: localhost:2181(CONNECTED) 0] 
```

reate() — creating a znode “/test_znode”

```
[zk: localhost:2181(CONNECTED) 10] create /test_znode
Created /test_znode
```

getData() or get — get on “/test_znode”

```
[zk: localhost:2181(CONNECTED) 13] get -s /test_znode
null
cZxid = 0x101
ctime = Thu Jun 11 11:31:35 EEST 2020
mZxid = 0x101
mtime = Thu Jun 11 11:31:35 EEST 2020
pZxid = 0x101
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 0
numChildren = 0
```

Creating child znodes under “/test_znode” and displaying all children using “ls” operation a.k.a getChildren()

```
[zk: localhost:2181(CONNECTED) 15] create /test_znode/child_1 "first child"
Created /test_znode/child_1
[zk: localhost:2181(CONNECTED) 16] create /test_znode/child_2 "second child"
Created /test_znode/child_2
[zk: localhost:2181(CONNECTED) 18] ls /test_znode
[child_1, child_2]
```

Deleting a znode

```
[zk: localhost:2181(CONNECTED) 19] delete /test_znode/child_1
[zk: localhost:2181(CONNECTED) 21] ls /test_znode
[child_2]
```

## Znode Types and their Use Cases

* **Persistent Znodes** - as the name says, once created these Znodes will be there forever in the Zookeeper. To remove these Znodes, you need to delete them manually(use delete operation).
As we learn this type of Znode never dies/deleted automatically, we can store any config information or any data that needs to be persistent. All servers can consume data from this Znode.
Note: If no flag is passed, by default persistent znode is created.

* **Ephemeral ZNodes** - These znodes are automatically deleted by the Zookeeper, once the client that created it, ends the session with zookeeper.
Zookeeper clients keep sending the ping request to keep the session alive. If Zookeeper does not see any ping request from the client for a period of configured session timeout, Zookeeper considers the client as dead and deletes the client session and the Znode created by the client.
You might have already guessed the use case of these znodes. Let’s say you want to maintain a list of active servers in a cluster. So, you create a parent Znode “/live_servers”. Under it, you keep creating child Znode for every new server in the cluster. At any point, if a server crashes/dies, child Znode belonging to the respective server will be deleted. Other servers will get a notification of this deletion if they are watching the znode “/live_servers”.
