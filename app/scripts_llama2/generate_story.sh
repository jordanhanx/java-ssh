#!/bin/bash

# Check if the number of arguments is correct
if [ $# -ne 1 ]; then
    echo "Usage: $0 <prompt>"
    exit 1
fi

# Set the OUTPUT_DIR environment variable to the desired location
export OUTPUT_DIR="."

# Run the Python script with the provided prompt
python Llama2.py "$1"

echo "The JSON output is saved in: $OUTPUT_DIR/story.json"
