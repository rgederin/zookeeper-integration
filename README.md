# Table of Content

- [Apache Zookeeper Overview](#apache-zookeeper-overview)
    * [Zookeeper ZNodes](#zooKeeper-znodes)
    * [Install Zookeeper on Mac OS](#install-zookeeper-on-mac-os)
    * [Zookeeper operations](#zookeeper-operations)
    * [Znode Types and their Use Cases](#znode-types-and-their-use-cases)
- [Apache Zookeeper Recipes](#apache-zookeeper-recipes)
    * [Leader election](#leader-election)
    * [Distributed Locks](#distributed-locks)
    * [Cluster management](#cluster-management)
    * [Typical distributed application structure](#typical-distributed-application-structure)

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


### Approach 3: Using Ephemeral Sequential Znode but notify only one server in the event of a leader going down.

1. Create a persistent znode /election.

2. Now each server joining the cluster will try to create an ephemeral sequential znode /leader-sequential number under node /election with data as hostname, ex: node1.domain.com
Let’s say three servers in a cluster created znodes under /election, then the znode names would be:
/election/leader-00000001,
/election/leader-00000002,
/election/leader-00000003,
Znode with least sequence number will be automatically considered as a leader.

3. Here we will **not set the watch on whole /election znode for any children change(add/delete child znode)**, instead, **each server in the cluster will set watch on child znode with one less sequence.**
The idea is if a leader goes down only the next candidate who would become a leader should get the notification.
So, in our example:
* The server that created the znode /election/leader-00000001 will have no watch set.
* The server that created the znode /election/leader-00000002 will watch for deletion of znode /election/leader-00000001
* The server that created the znode /election/leader-00000003 will watch for deletion of znode /election/leader-00000002

4. Then, if the current leader goes down, zookeeper will delete the node /election/leader-00000001 and send the notification to only the next leader i.e. the server that created node /election/leader-00000002

That’s all on leader election logic. These are simple algorithms. There could be a situation when you want only those servers to take part in a leader election which has the latest data if you are creating a distributed database.

In that case, you might want to create one more node that keeps this information, and in the event of the leader going down, only those servers that have the latest data can take part in an election.

## Distributed Locks

Suppose we have “n” servers trying to update a shared resource simultaneously, say a shared file. If we do not write these files in a mutually exclusive way, it may lead to data inconsistencies in the shared file.
We will manipulate operations on znode to implement a distributed lock, so that, different servers can acquire this lock and perform a task.
The algorithm for managing distributed locks is the same as the leader election with a slight change.

1. Instead of the /election parent node, we will use /lock as the parent node.

2. The rest of the steps will remain the same as in the leader election algorithm. Any server which is considered a leader is analogous to server acquiring the lock.
3. The only difference is, once the server acquires the lock, the server will perform its task and then call the delete operation on the child znode it has created so that the next server can acquire lock upon delete notification from zookeeper and perform the task.


## Cluster management

In Zookeeper it is pretty simple to maintain group membership info using persistent and ephemeral znodes. I will talk about a simple case where you want to maintain information about all servers in a cluster and what servers are currently alive.

We will use a persistent znode to keep track of all the servers that join the cluster and zookeeper’s ability to delete an ephemeral znodes upon client session termination will come handy in maintaining the list of active/live servers.

1. Create a parent persistent znode **/all_nodes**, this znode will be used to store any server that connects to the cluster.

2. Create a parent persistent znode **/live_nodes**, this znode will be used to store only the live nodes in the cluster and will store ephemeral child znodes. If any server crashes or goes down, respective child ephemeral znode will be deleted.

3. Any server connecting to the cluster will create a new **persistent znode** under **/all_nodes** say /node1.domain.com. Let’s say another two node joins the cluster. Then the znode structure will look like:
* /all_nodes/node1.domain.com
* /all_nodes/node2.domain.com
* /all_nodes/node3.domain.com
You can store any information specific to the node in znode’s data

4. Any server connecting to the cluster will create a new **ephemeral znode** under **/live_nodes** say /node1.domain.com. Let’s say another two-node joins the cluster. Then the znode structure will look like:
* /live_nodes/node1.domain.com
* /live_nodes/node2.domain.com
* /live_nodes/node3.domain.com

5. Add a watch for any change in **children of /all_nodes**. If any server is added or deleted to/from the cluster, all server in the cluster needs to be notified.

6. Add a watch for any change in **children of /live_nodes**. This way all servers will be notified if any server in the cluster goes down or comes alive.

## Typical distributed application structure

With that let’s look at, how a zookeeper Znode structure looks like for a typical distributes application:

![typical](https://github.com/rgederin/zookeeper-integration/blob/master/img/typical.png)
