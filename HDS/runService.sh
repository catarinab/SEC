#!/bin/bash

mvn clean install compile -DskipTests || exit 1

# Get command line arguments
while [[ $# -gt 0 ]]
do
key="$1"

case $key in
    -NByzantine)
    NByzantine="$2"
    shift
    shift
    ;;
    -NService)
    NService="$2"
    shift
    shift
    ;;
    -NClient)
    NClient="$2"
    shift
    shift
    ;;
    -System)
    System="$2"
    shift
    shift
    ;;
    *)
    echo "Invalid argument: $1"
    exit 1
    ;;
esac
done

# Check if NByzantine is greater than the first line of the file
maxByzantineServer=$(head -n 1 "$System")
if (( $NByzantine > $maxByzantineServer )); then
    echo "NByzantine must be less than or equal to the first line of the file."
    exit 1
fi

# Loop over each line of the file and run mvn exec:java with user input and file line
counter=0
counterByzantine=NByzantine
while IFS= read -r line && (( counter <= NService))
do
    counter=$((counter+1))
    if (( counter == 1 )); then
        continue # Skip first line
    elif (( counter == 2 )) || ((counterByzantine == 0)); then
        # Skip prompt for input for second line
        input="C"
    else
        # Prompt user for input
        read -p "For $line enter C for correct behaviour or B for byzantine behaviour: " input < /dev/tty

         if [ "$input" = "B" ]; then
           counterByzantine=$((counterByzantine-1))
        fi

        if [[ $input != "C" && $input != "B" ]]; then
            echo "Invalid input. Please enter either C or B."
            exit 1
        fi
    fi

    # Run program with input and file line arguments
    gnome-terminal --tab --title="Service $line" -- /bin/bash -c "cd Service/ && mvn exec:java -Dbehaviour=\"$input\" -Dserver=\"$line\" -Dpath=\"$System\"; exec /bin/bash"

done < "$System"

# Create new terminal and run client program
counterClient=0
while (( counterClient < NClient ))
do
    counterClient=$((counterClient+1))

    # Prompt user for input
    read -p "Enter hostname for client $counterClient: " hostname < /dev/tty
    read -p "Enter port for client $counterClient: " port < /dev/tty

    # Run program with input arguments
    gnome-terminal --tab --title="Client $hostname $port" -- /bin/bash -c "cd Client/ && mvn exec:java -Dhostname=\"$hostname\" -Dport=\"$port\" -Dpath=\"$System\"; exec /bin/bash"
done
