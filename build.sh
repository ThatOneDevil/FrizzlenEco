#!/bin/bash
echo "Building FrizzlenEco..."
mvn clean package
if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi
echo "Build successful!"
echo "JAR file is in the target directory." 