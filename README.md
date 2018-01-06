# Java Instant Message Server and Client

This is a java version chat service that create by Netbeans.

## Prerequisite

1. Java 8
2. [Netbeans](https://netbeans.org/downloads/)
3. sqlite

## Usage

## Chat Server

start `com.niu.server.IMServer`


## Chat Client

start `com.niu.client.Application`

## Sqlite Database

### Users

```sql
create table users(name primary key, password text, status text);
```
