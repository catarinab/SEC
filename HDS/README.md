# Highly Dependable Systems Ledger - Stage 2

### Requirements

- Maven 3.9.0
- Java 13
- JUnit5
- gnome-terminal (for script)

## System Membership services.txt Setup
The system membership is static for the entire system lifetime, including a predefined leader process.
The services.txt file describes the following regarding the system membership (can be edited as long as it respects the file architecture):
- Maximum number of byzantine members the system supports 3f+1 (first line of services.txt)
- The services that are a part of the system, line by line, described as ```hostname port```
- The first service described in the .txt file is the leader (second line of services.txt)
- Make sure a new line at the end of the file is added to the services.txt file

Default services.txt file:

```agsl
1
localhost 1234
localhost 1235
localhost 1236
localhost 1237

```

## Base Setup

First time running the program, in the directory ```$ <path to project>/HDS/```
```shell
$ mvn install
```

---

## To Run Service and Client

In the directory ```$ <path to project>/HDS/```
```shell
$ ./runService.sh -NByzantine <Number of byzantine members> -NService <Number of members in the system> -NCLient <number of clients> -System <path to services.txt file>
```

- The script warns when the pretended number of byzantine members is not possible within the scope of the system membership described in the services.txt
- For each process, apart from the leader, the shell waits for the input of the user to declare the behaviour of each member - B for byzantine and C for correct. (Note that it is expected from the user to declare the same amount of byzantine members as it stated in the -NByzantine parameter)
- For each client, the shell waits for the input of the user to declare the hostname and the port sequentially of each member - String for hostname and Integer for port.
- In case the shell prompts an error upon running the script try ```chmod +x runService.sh``` and re-run the script as previously stated.
- This script runs the desired amount of Services, and also runs the Client module.

### Suggested Parameters for Run

- `<NByzantine>`: 1
- `<NService>`: 4
- `<NClient >`: 2
- `<System>`: <absolute path of user>/HDS/services.txt
- `<Byzantine behaviour>`: B for first prompt of input (server 1235)
- `<Hostname of Client 1>`: localhost
- `<Port of Client 1>`: 4321
- `<Hostname of Client 2>`: localhost
- `<Port of Client 2>`: 4322

```shell
$ ./runService.sh -NByzantine 1 -NService 4 -NClient 2 -System /home/user/HDS/services.txt 
$ "For localhost 1235 enter C for correct behaviour or B for byzantine behaviour:" B
$ "Enter hostname for client 1:" localhost
$ "Enter port for client 1:" 4321
$ "Enter hostname for client 2:" localhost 
$ "Enter port for client 2:" 4322
```

## Clean

- After each run we suggest to do in the directory ```$ <path to project>/HDS/```
```shell
$ mvn clean
```

## To Run Tests (aprox time: 5min 30sec )

### To Run Tests For The First Time

In the directory ```$ <path to project>/HDS/```
```shell
$ mvn clean install compile
```

### To Run Tests

In the directory ```$ <path to project>/HDS/```
```shell
$ mvn clean test
```

- Test 1 fourServersNoByzantine(): tests four correct members and two clients who create accounts, transfer from one to another and check their balance both through weak read and strong read. The test validate that the blockchain in the system is correct.
- Test 2 strongBalanceByzantine(): tests three correct members, one byzantine member and a client who requests a correct strong read of their balance.
- Test 3 readBalanceNotInBlockchain(): sanity test that checks that when the operations were not yet commited to the blockchain, upon requesting a check_balance the user is informed about it.
- Test 4 weakBalanceByzantine(): tests three correct members, one byzantine and a client that first requests the weak read to the byzantine and he is informed to try again because the operation he requested might have been replied by a byzantine and then asks for a weak read to a correct server and the reply is valid and the valid signatures are indeed 3 (3 correct servers).
- Test 5 weakReadCorrect(): tests if when a client asks for a weak read twice while doing transfers in the meanwhile, the first reply is always correct even if outdated and the second reply is also always correct even if outdated but always more recent or equal to the first one.
- If by any chance tests are failing because they require more time to run the algorithm adjust the values inside Thread.sleep(<value>) in each test of the ServiceTest (under the directory /SEC/HDS/Service/src/test).