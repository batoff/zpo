#!/usr/bin/env zsh

# Navigate to the script's directory
cd "${0:a:h}" || exit 1

# Run the project using maven
mvn spring-boot:run
