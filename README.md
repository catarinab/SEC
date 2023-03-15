# Highly Dependable Systems Ledger - Stage 1

### Requirements

- Maven 3.9.0
- Java 13

## System Membership services.txt Setup
The system membership is static for the entire system lifetime, including a predefined leader process.
The services.txt file describes the following regarding the system membership:
- Maximum number of byzantine members the system supports (first line of services.txt)
- The services that are a part of the system, line by line, described as ```hostname port```
- The first service described in the .txt file is the leader (second line of services.txt)
- Make sure a new line at the end of the file is added to the services.txt file
- Make sure its path is ```$ <path to project>/SEC/HDS/services.txt```

Default services.txt file:

```agsl
1
localhost 1234
localhost 1235
localhost 1236
localhost 1237

```

## Base Setup

In the directory ```$ <path to project>/SEC/HDS/```
```shell
$ mvn clean install compile
```

---

## To Run Service and Client 

In the directory ```$ <path to project>/SEC/HDS/```
```shell
$ cd Service
$ ./runService.sh -NByzantine <Number of byzantine members> -NService <Number of members in the system> -System <path to services.txt file>
```

- The script warns when the pretended number of byzantine members is not possible within the scope of the system membership described in the services.txt
- For each process, apart from the leader, the shell waits for the input of the user to declare the behaviour of each member - B for byzantine and C for correct. (Note that it is expected from the user to declare the same amount of byzantine members as it stated in the -NByzantine parameter)
- In case the shell prompts an error upon running the script try ```chmod +x runService.sh``` and re-run the script as previously stated.

### Suggested Parameters

- `<NByzantine>`: 1
- `<NService>`: 4
- `<System>`: ../services.txt

## Clean

- After each run we suggest to do in the directory ```$ <path to project>/SEC/HDS/```
```shell
$ mvn clean
```

## To Run Tests

### To Run *Type of Tests*