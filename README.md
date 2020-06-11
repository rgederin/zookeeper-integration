# Table of Content

- [Apache Zookeeper Overview](#apache-zookeeper-overview)
    * [Zookeeper ZNodes](#zooKeeper-znodes)
    * [Implementation choices](#implementation-choices)
    * [Install Zookeeper on Mac OS](#install-zookeeper-on-mac-os)
    * [Zookeeper operations](#zookeeper-operations)
    * [Znode Types and their Use Cases](#znode-types-and-their-use-cases)
- [Apache Zookeeper Recipes](#apache-zookeeper-recipes)
    * [Leader election](#leader-election)

# Apache Zookeeper Overview

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

There are four types of ZNodes in Zookeeper:

* Persistent Znodes
* Ephemeral Znodes
* Ephemeral Sequential Znodes
* Persistent Sequential Znodes


### Persistent Znodes 

As the name says, **once created these Znodes will be there forever in the Zookeeper.** To remove these Znodes, you need to delete them manually(use delete operation).

As we learn this type of Znode never dies/deleted automatically, we can store any config information or any data that needs to be persistent. All servers can consume data from this Znode.


### Ephemeral Znodes

These znodes are **automatically deleted by the Zookeeper, once the client that created it, ends the session with zookeeper.**

Zookeeper clients keep sending the ping request to keep the session alive. If Zookeeper does not see any ping request from the client for a period of configured session timeout, Zookeeper considers the client as dead and deletes the client session and the Znode created by the client. 

You might have already guessed the use case of these znodes. Let’s say you want to maintain a **list of active servers in a cluster.** So, you create a parent Znode **“/live_servers”.** Under it, you keep creating child Znode for every new server in the cluster. At any point, if a server crashes/dies, child Znode belonging to the respective server will be deleted. Other servers will get a notification of this deletion if they are watching the znode “/live_servers”.

### Ephemeral Sequential Znodes

It is the same as ephemeral Znode, the only difference is **Zookeeper attaches a sequential number as a suffix, and if any new sibling Znode of the same type is created, it will be assigned a number higher than previous one.**

Let’s say, we want to create two ephemeral sequential Znodes “child_nodeA” and “child_nodeB” inside “test_znode” parent Znode. It will attach sequence number “0000000000” and “0000000001” as the suffix.

```
[zk: localhost:2181(CONNECTED) 25] create -e -s /test_znode/child_node_a
Created /test_znode/child_node_a0000000002
[zk: localhost:2181(CONNECTED) 26] create -e -s /test_znode/child_node_b
Created /test_znode/child_node_b0000000003
[zk: localhost:2181(CONNECTED) 27] ls /test_znode
[child_node_a0000000002, child_node_b0000000003]
```

This type of znode could be used in the leader election algorithm.

Say I have a parent node “/election”, and for any new node that joins the cluster, I add an ephemeral sequential Znode to this “/election” node. We can consider a server as the leader if any server that created the znode has the least sequential number attached to it.

So, even if a leader goes down, zookeeper will delete corresponding Znode created by the leader server and notify the client applications, then that client fetches the new lowermost sequence node and considers that as a new leader. We will talk in detail about the leader election in the later section.

### Persistent Sequential Znodes

This is a persistent node with a sequence number attached to its name as a suffix. This type is rarely used. Same principle as for Ephemeral Sequential Znodes


# Apache Zookeeper Recipes

Here I collected some of the common Zookeeper Recipes.

## Leader election

We will discuss three algorithms for the leader election.

### Approach 1: Using ephemeral single znode /leader

1. A client(any server belonging to the cluster) creates a **persistent znode /election** in Zookeeper.

2. All clients add **a watch to /election** znode and listen to any children znode deletion or addition under /election znode.

3. Now **each server joining the cluster** will try to create an **ephemeral znode /leader** under node /election with data as hostname, ex: node1.domain.com 
Since **multiple servers in the cluster will try to create znode with the same name(/leader), only one will succeed**, and that server will be considered as a leader.

4. Once all servers in the cluster completes above step, they will call **getChildren(“/election”)** and get the data(hostname) associated with child znode “/leader”, which will give the leader’s hostname.

5. At any point, if the leader server goes down, Zookeeper will kill the session for that server after the specified session timeout. In the process, it will delete the node /leader as it was created by leader server and is an ephemeral node and then Zookeeper will notify all the servers that have set the watch on /election znode, as one of the children has been deleted.

6. Once all server gets notified that the leader is dead or leader’s znode(/leader) is deleted, they will retry creating “/leader” znode and again only one server will succeed, making it a new leader.

7. Once the /leader node is created with the hostname as the data part of the znode, zookeeper will again notify all servers (as we have set the watch in step 2).

8. All servers will call getChildren() on “/election” and update the new leader in their memory.

The problem with the above approach is, each time /leader node is deleted, Zookeeper will send the notification to all servers and all servers will try to write to zookeeper to become a new leader at the same time creating a **herd effect.**

If we have a large number of servers, this approach would not be the right idea.


### Approach 2: Using Ephemeral Sequential Znodes

1. A client(any server belonging to the cluster) creates a persistent znode **/election.**

2. **All clients add a watch to /election znode** and listen to any children znode deletion or addition under /election znode.

3. Now each server joining the cluster will try to create an **ephemeral sequential znode /leader-sequential number under node /election** with data as hostname, ex: node1.domain.com
Let’s say three servers in a cluster created znodes under /election, then the znode names would be: /election/leader-00000001, /election/leader-00000002, /election/leader-00000003.
**Znode with least sequence number will be automatically considered as the leader.**

4. Once all server completes the creation of znode under /election, they will perform getChildren(“/election”) and get the data(hostname) associated with least sequenced child node “/election/leader-00000001”, which will give the leader hostname.

5. At any point, if the current leader server goes down, Zookeeper will kill the session for that server after the specified session timeout. In the process, it will delete the node “/election/leader-00000001” as it was created by the leader server and is an ephemeral node and then Zookeeper will send a notification to all the server that was watching znode /election.

6. Once all server gets the leader’s znode-delete notification, they again fetch all children under /election znode and get the data associated with the child znode that has the least sequence number(/election/leader-00000002) and store that as the new leader in its own memory.

In this approach, we saw, if an existing leader dies, the servers are not sending an extra write request to the zookeeper to become the leader, leading to reduce network traffic.

But, even with this approach, we will face some degree of herd effect we talked about in the previous approach. When the leader server dies, notification is sent to all servers in the cluster, creating a herd effect.

But, this is a design call that you need to take. Use approach 1 or 2, if you need all servers in your cluster to store the current leader’s hostname for its purpose.

If you do not want to store current leader information in each server/follower and only the leader needs to know if he is the current leader to do leader specific tasks. You can further simplify the leader election process, which we will discuss in approach 3.